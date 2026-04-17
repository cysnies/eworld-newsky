package fr.neatmonster.nocheatplus.checks.fight;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import org.bukkit.entity.Player;

public class SelfHit extends Check {
   public SelfHit() {
      super(CheckType.FIGHT_SELFHIT);
   }

   public boolean check(Player damager, Player damaged, FightData data, FightConfig cc) {
      if (!damager.getName().equals(damaged.getName())) {
         return false;
      } else {
         boolean cancel = false;
         data.selfHitVL.add(System.currentTimeMillis(), 1.0F);
         cancel = this.executeActions(damager, (double)data.selfHitVL.score(0.99F), (double)1.0F, cc.selfHitActions);
         return cancel;
      }
   }
}
