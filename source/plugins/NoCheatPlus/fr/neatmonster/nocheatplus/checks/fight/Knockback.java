package fr.neatmonster.nocheatplus.checks.fight;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

public class Knockback extends Check {
   public Knockback() {
      super(CheckType.FIGHT_KNOCKBACK);
   }

   public boolean check(Player player) {
      FightConfig cc = FightConfig.getConfig(player);
      FightData data = FightData.getData(player);
      boolean cancel = false;
      long time = System.currentTimeMillis();
      if (!player.getItemInHand().containsEnchantment(Enchantment.KNOCKBACK) && !player.getItemInHand().containsEnchantment(Enchantment.ARROW_KNOCKBACK)) {
         long usedTime = time - data.knockbackSprintTime;
         long effectiveTime = (long)((float)usedTime * (cc.lag ? TickTask.getLag(usedTime) : 1.0F));
         if (data.knockbackSprintTime > 0L && effectiveTime < cc.knockbackInterval) {
            double difference = (double)(cc.knockbackInterval - time + data.knockbackSprintTime);
            data.knockbackVL += difference;
            cancel = this.executeActions(player, data.knockbackVL, difference, cc.knockbackActions);
         }

         return cancel;
      } else {
         return false;
      }
   }
}
