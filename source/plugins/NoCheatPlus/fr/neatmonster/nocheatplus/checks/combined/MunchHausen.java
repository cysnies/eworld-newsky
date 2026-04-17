package fr.neatmonster.nocheatplus.checks.combined;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;

public class MunchHausen extends Check {
   public MunchHausen() {
      super(CheckType.COMBINED_MUNCHHAUSEN);
   }

   public boolean checkFish(Player player, Entity caught, PlayerFishEvent.State state) {
      if (caught != null && caught instanceof Player) {
         Player caughtPlayer = (Player)caught;
         CombinedData data = CombinedData.getData(player);
         if (player.equals(caughtPlayer)) {
            ++data.munchHausenVL;
            if (this.executeActions(player, data.munchHausenVL, (double)1.0F, CombinedConfig.getConfig(player).munchHausenActions)) {
               return true;
            }
         } else {
            data.munchHausenVL *= 0.96;
         }

         return false;
      } else {
         return false;
      }
   }
}
