package fr.neatmonster.nocheatplus.checks.combined;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import org.bukkit.entity.Player;

public class BedLeave extends Check {
   public BedLeave() {
      super(CheckType.COMBINED_BEDLEAVE);
   }

   public boolean checkBed(Player player) {
      CombinedData data = CombinedData.getData(player);
      boolean cancel = false;
      if (!data.wasInBed) {
         ++data.bedLeaveVL;
         if (this.executeActions(player, data.bedLeaveVL, (double)1.0F, CombinedConfig.getConfig(player).bedLeaveActions)) {
            cancel = true;
         }
      } else {
         data.wasInBed = false;
      }

      return cancel;
   }
}
