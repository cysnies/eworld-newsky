package fr.neatmonster.nocheatplus.checks.chat;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.ColorUtil;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import org.bukkit.entity.Player;

public class Commands extends Check {
   public Commands() {
      super(CheckType.CHAT_COMMANDS);
   }

   public boolean check(Player player, String message, Captcha captcha) {
      long now = System.currentTimeMillis();
      int tick = TickTask.getTick();
      ChatConfig cc = ChatConfig.getConfig(player);
      ChatData data = ChatData.getData(player);
      boolean captchaEnabled = captcha.isEnabled(player);
      if (captchaEnabled) {
         synchronized(data) {
            if (captcha.shouldCheckCaptcha(cc, data)) {
               captcha.checkCaptcha(player, message, cc, data, true);
               return true;
            }
         }
      }

      float weight = 1.0F;
      data.commandsWeights.add(now, 1.0F);
      if ((long)tick < data.commandsShortTermTick) {
         data.commandsShortTermTick = (long)tick;
         data.commandsShortTermWeight = (double)1.0F;
      } else if ((long)tick - data.commandsShortTermTick < (long)cc.commandsShortTermTicks) {
         if (cc.lag && !(TickTask.getLag(50L * ((long)tick - data.commandsShortTermTick), true) < 1.3F)) {
            data.commandsShortTermTick = (long)tick;
            data.commandsShortTermWeight = (double)1.0F;
         } else {
            ++data.commandsShortTermWeight;
         }
      } else {
         data.commandsShortTermTick = (long)tick;
         data.commandsShortTermWeight = (double)1.0F;
      }

      float nw = data.commandsWeights.score(1.0F);
      double violation = Math.max((double)nw - cc.commandsLevel, data.commandsShortTermWeight - cc.commandsShortTermLevel);
      if (violation > (double)0.0F) {
         data.commandsVL += violation;
         if (captchaEnabled) {
            synchronized(data) {
               captcha.sendNewCaptcha(player, cc, data);
               return true;
            }
         }

         if (this.executeActions(player, data.commandsVL, violation, cc.commandsActions)) {
            return true;
         }
      } else if (!cc.chatWarningCheck || now - data.chatWarningTime <= cc.chatWarningTimeout || !((double)(100.0F * nw) / cc.commandsLevel > (double)cc.chatWarningLevel) && !((double)100.0F * data.commandsShortTermWeight / cc.commandsShortTermLevel > (double)cc.chatWarningLevel)) {
         data.commandsVL *= 0.99;
      } else {
         player.sendMessage(ColorUtil.replaceColors(cc.chatWarningMessage));
         data.chatWarningTime = now;
      }

      return false;
   }
}
