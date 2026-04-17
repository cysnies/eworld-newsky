package fr.neatmonster.nocheatplus.checks.blockplace;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import org.bukkit.entity.Player;

public class Speed extends Check {
   public Speed() {
      super(CheckType.BLOCKPLACE_SPEED);
   }

   public boolean check(Player player) {
      BlockPlaceConfig cc = BlockPlaceConfig.getConfig(player);
      BlockPlaceData data = BlockPlaceData.getData(player);
      boolean cancel = false;
      if (data.speedLastTime != 0L && System.currentTimeMillis() - data.speedLastTime < cc.speedInterval) {
         if (data.speedLastRefused) {
            double difference = (double)(cc.speedInterval - System.currentTimeMillis() + data.speedLastTime);
            data.speedVL += difference;
            cancel = this.executeActions(player, data.speedVL, difference, cc.speedActions);
         }

         data.speedLastRefused = true;
      } else {
         data.speedVL *= 0.9;
         data.speedLastRefused = false;
      }

      data.speedLastTime = System.currentTimeMillis();
      return cancel;
   }
}
