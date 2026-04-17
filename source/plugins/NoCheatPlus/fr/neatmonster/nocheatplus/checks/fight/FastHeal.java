package fr.neatmonster.nocheatplus.checks.fight;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import org.bukkit.entity.Player;

public class FastHeal extends Check {
   public FastHeal() {
      super(CheckType.FIGHT_FASTHEAL);
   }

   public boolean check(Player player) {
      long time = System.currentTimeMillis();
      FightConfig cc = FightConfig.getConfig(player);
      FightData data = FightData.getData(player);
      boolean cancel = false;
      if (time >= data.fastHealRefTime && time - data.fastHealRefTime < cc.fastHealInterval) {
         double correctedDiff = ((double)time - (double)data.fastHealRefTime) * (double)TickTask.getLag(cc.fastHealInterval);
         if (correctedDiff < (double)cc.fastHealInterval) {
            data.fastHealBuffer = (long)((double)data.fastHealBuffer - ((double)cc.fastHealInterval - correctedDiff));
            if (data.fastHealBuffer <= 0L) {
               double violation = ((double)cc.fastHealInterval - correctedDiff) / (double)1000.0F;
               data.fastHealVL += violation;
               if (this.executeActions(player, data.fastHealVL, violation, cc.fastHealActions)) {
                  cancel = true;
               }
            }
         }
      } else {
         data.fastHealVL *= 0.96;
         data.fastHealBuffer = Math.min(cc.fastHealBuffer, data.fastHealBuffer + 50L);
      }

      if (cc.debug && player.hasPermission("nocheatplus.admin.debug")) {
         player.sendMessage("Regain health(SATIATED): " + (time - data.fastHealRefTime) + " ms " + "(buffer=" + data.fastHealBuffer + ")" + " , cancel=" + cancel);
      }

      data.fastHealRefTime = time;
      return cancel;
   }
}
