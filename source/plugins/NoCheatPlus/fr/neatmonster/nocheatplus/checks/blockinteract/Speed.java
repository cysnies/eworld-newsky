package fr.neatmonster.nocheatplus.checks.blockinteract;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import org.bukkit.entity.Player;

public class Speed extends Check {
   public Speed() {
      super(CheckType.BLOCKINTERACT_SPEED);
   }

   public boolean check(Player player, BlockInteractData data, BlockInteractConfig cc) {
      long time = System.currentTimeMillis();
      if (time < data.speedTime || time > data.speedTime + cc.speedInterval) {
         data.speedTime = time;
         data.speedCount = 0;
      }

      ++data.speedCount;
      boolean cancel = false;
      if (data.speedCount > cc.speedLimit) {
         int correctedCount = (int)((double)data.speedCount / (double)TickTask.getLag(time - data.speedTime));
         if (correctedCount > cc.speedLimit) {
            ++data.speedVL;
            if (this.executeActions(player, data.speedVL, (double)1.0F, cc.speedActions)) {
               cancel = true;
            }
         }
      } else {
         data.speedVL *= 0.99;
      }

      if (cc.debug && player.hasPermission("nocheatplus.admin.debug")) {
         player.sendMessage("Interact speed: " + data.speedCount);
      }

      return cancel;
   }
}
