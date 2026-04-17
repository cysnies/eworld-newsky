package fr.neatmonster.nocheatplus.checks.inventory;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class InstantBow extends Check {
   private static final float maxTime = 800.0F;

   public InstantBow() {
      super(CheckType.INVENTORY_INSTANTBOW);
   }

   public boolean check(Player player, float force, long now) {
      InventoryData data = InventoryData.getData(player);
      InventoryConfig cc = InventoryConfig.getConfig(player);
      boolean cancel = false;
      long expectedPullDuration = (long)(800.0F - 800.0F * (1.0F - force) * (1.0F - force)) - cc.instantBowDelay;
      long pullDuration = now - (cc.instantBowStrict ? data.instantBowInteract : data.instantBowShoot);
      if ((!cc.instantBowStrict || data.instantBowInteract > 0L) && pullDuration >= expectedPullDuration) {
         data.instantBowVL *= 0.9;
      } else if (data.instantBowInteract <= now) {
         long correctedPullduration = cc.lag ? (long)(TickTask.getLag(expectedPullDuration, true) * (float)pullDuration) : pullDuration;
         if (correctedPullduration < expectedPullDuration) {
            double difference = (double)(expectedPullDuration - pullDuration) / (double)100.0F;
            data.instantBowVL += difference;
            cancel = this.executeActions(player, data.instantBowVL, difference, cc.instantBowActions);
         }
      }

      if (cc.debug && player.hasPermission("nocheatplus.admin.debug")) {
         player.sendMessage(ChatColor.YELLOW + "NCP: " + ChatColor.GRAY + "Bow shot - force: " + force + ", " + (!cc.instantBowStrict && pullDuration >= 2L * expectedPullDuration ? "" : "pull time: " + pullDuration) + "(" + expectedPullDuration + ")");
      }

      data.instantBowInteract = 0L;
      data.instantBowShoot = now;
      return cancel;
   }
}
