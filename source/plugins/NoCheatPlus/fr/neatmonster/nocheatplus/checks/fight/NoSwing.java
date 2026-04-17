package fr.neatmonster.nocheatplus.checks.fight;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import org.bukkit.entity.Player;

public class NoSwing extends Check {
   public NoSwing() {
      super(CheckType.FIGHT_NOSWING);
   }

   public boolean check(Player player) {
      FightData data = FightData.getData(player);
      boolean cancel = false;
      if (data.noSwingArmSwung) {
         data.noSwingArmSwung = false;
         data.noSwingVL *= 0.9;
      } else {
         ++data.noSwingVL;
         cancel = this.executeActions(player, data.noSwingVL, (double)1.0F, FightConfig.getConfig(player).noSwingActions);
      }

      return cancel;
   }
}
