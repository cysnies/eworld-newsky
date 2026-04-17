package fr.neatmonster.nocheatplus.utilities;

import fr.neatmonster.nocheatplus.checks.blockbreak.BlockBreakData;
import fr.neatmonster.nocheatplus.checks.combined.CombinedData;
import fr.neatmonster.nocheatplus.checks.fight.FightData;
import fr.neatmonster.nocheatplus.checks.inventory.InventoryData;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class CheckUtils {
   public static void kickIllegalMove(Player player) {
      player.kickPlayer("Illegal move.");
      LogUtil.logWarning("[NCP] Disconnect " + player.getName() + " due to illegal move!");
   }

   public static final long guessKeepAliveTime(Player player, long now, long maxAge) {
      int tick = TickTask.getTick();
      long ref = Long.MIN_VALUE;
      FightData fData = FightData.getData(player);
      ref = Math.max(ref, fData.speedBuckets.lastAccess());
      ref = Math.max(ref, now - 50L * (long)(tick - fData.lastAttackTick));
      ref = Math.max(ref, fData.regainHealthTime);
      ref = Math.max(ref, CombinedData.getData(player).lastMoveTime);
      InventoryData iData = InventoryData.getData(player);
      ref = Math.max(ref, iData.lastClickTime);
      ref = Math.max(ref, iData.instantEatInteract);
      BlockBreakData bbData = BlockBreakData.getData(player);
      ref = Math.max(ref, bbData.frequencyBuckets.lastAccess());
      ref = Math.max(ref, bbData.fastBreakfirstDamage);
      return ref <= now && ref >= now - maxAge ? ref : Long.MIN_VALUE;
   }

   public static Player getFirstPlayerPassenger(Entity entity) {
      for(Entity passenger = entity.getPassenger(); passenger != null; passenger = passenger.getPassenger()) {
         if (passenger instanceof Player) {
            return (Player)passenger;
         }
      }

      return null;
   }

   public static Entity getLastNonPlayerVehicle(Entity entity) {
      Entity vehicle;
      for(vehicle = entity.getVehicle(); vehicle != null; vehicle = vehicle.getVehicle()) {
         if (vehicle instanceof Player) {
            return null;
         }
      }

      return vehicle;
   }
}
