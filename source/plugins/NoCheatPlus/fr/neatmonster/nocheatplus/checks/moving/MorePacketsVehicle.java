package fr.neatmonster.nocheatplus.checks.moving;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class MorePacketsVehicle extends Check {
   private static final int packetsPerTimeframe = 22;

   public MorePacketsVehicle() {
      super(CheckType.MOVING_MOREPACKETSVEHICLE);
   }

   public Location check(Player player, Location from, Location to, MovingData data, MovingConfig cc) {
      long time = System.currentTimeMillis();
      Location newTo = null;
      if (!data.hasMorePacketsVehicleSetBack()) {
         data.setMorePacketsVehicleSetBack(from);
         if (data.morePacketsVehicleTaskId != -1) {
            Bukkit.getScheduler().cancelTask(data.morePacketsVehicleTaskId);
         }
      }

      --data.morePacketsVehicleBuffer;
      if (data.morePacketsVehicleTaskId != -1) {
         return data.getMorePacketsVehicleSetBack();
      } else {
         if (data.morePacketsVehicleBuffer < 0) {
            data.morePacketsVehiclePackets = -data.morePacketsVehicleBuffer;
            data.morePacketsVehicleVL = (double)(-data.morePacketsVehicleBuffer);
            if (this.executeActions(player, data.morePacketsVehicleVL, (double)(-data.morePacketsVehicleBuffer), cc.morePacketsVehicleActions)) {
               newTo = data.getMorePacketsVehicleSetBack();
            }
         }

         if (data.morePacketsVehicleLastTime + 1000L < time) {
            double seconds = (double)(time - data.morePacketsVehicleLastTime) / (double)1000.0F;
            data.morePacketsVehicleBuffer = (int)((double)data.morePacketsVehicleBuffer + (double)22.0F * seconds);
            if (seconds > (double)2.0F) {
               if (data.morePacketsVehicleBuffer > 100) {
                  data.morePacketsVehicleBuffer = 100;
               }
            } else if (data.morePacketsVehicleBuffer > 50) {
               data.morePacketsVehicleBuffer = 50;
            }

            data.morePacketsVehicleLastTime = time;
            if (newTo == null) {
               data.setMorePacketsVehicleSetBack(from);
            }
         } else if (data.morePacketsVehicleLastTime > time) {
            data.morePacketsVehicleLastTime = time;
         }

         return newTo == null ? null : new Location(player.getWorld(), newTo.getX(), newTo.getY(), newTo.getZ(), to.getYaw(), to.getPitch());
      }
   }

   protected Map getParameterMap(ViolationData violationData) {
      Map<ParameterName, String> parameters = super.getParameterMap(violationData);
      parameters.put(ParameterName.PACKETS, String.valueOf(MovingData.getData(violationData.player).morePacketsVehiclePackets));
      return parameters;
   }
}
