package fr.neatmonster.nocheatplus.checks.chat;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.combined.CombinedData;
import fr.neatmonster.nocheatplus.utilities.ColorUtil;
import org.bukkit.entity.Player;

public class Relog extends Check {
   public Relog() {
      super(CheckType.CHAT_RELOG);
   }

   public boolean unsafeLoginCheck(Player player, ChatConfig cc, ChatData data) {
      boolean cancel = false;
      long now = System.currentTimeMillis();
      CombinedData cData = CombinedData.getData(player);
      if (now - cData.lastLogoutTime < cc.relogTimeout) {
         if (now - data.relogWarningTime > cc.relogWarningTimeout) {
            data.relogWarnings = 0;
         }

         if (data.relogWarnings < cc.relogWarningNumber) {
            player.sendMessage(ColorUtil.replaceColors(cc.relogWarningMessage));
            data.relogWarningTime = now;
            ++data.relogWarnings;
         } else {
            ++data.relogVL;
            cancel = this.executeActions(player, data.relogVL, (double)1.0F, cc.relogActions, true);
         }
      }

      return cancel;
   }
}
