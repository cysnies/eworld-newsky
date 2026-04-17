package fr.neatmonster.nocheatplus.checks.moving;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class MorePackets extends Check {
   private static final int packetsPerTimeframe = 22;

   public MorePackets() {
      super(CheckType.MOVING_MOREPACKETS);
   }

   public Location check(Player player, PlayerLocation from, PlayerLocation to, MovingData data, MovingConfig cc) {
      long time = System.currentTimeMillis();
      Location newTo = null;
      if (!data.hasMorePacketsSetBack()) {
         if (data.hasSetBack()) {
            data.setMorePacketsSetBack(data.getSetBack(to));
         } else {
            data.setMorePacketsSetBack(from);
         }
      }

      --data.morePacketsBuffer;
      if (data.morePacketsBuffer < 0) {
         data.morePacketsPackets = -data.morePacketsBuffer;
         data.morePacketsVL = (double)(-data.morePacketsBuffer);
         if (this.executeActions(player, data.morePacketsVL, (double)(-data.morePacketsBuffer), MovingConfig.getConfig(player).morePacketsActions)) {
            newTo = data.getMorePacketsSetBack();
            data.setTeleported(newTo);
         }
      }

      if (data.morePacketsLastTime + 1000L < time) {
         double seconds = (double)(time - data.morePacketsLastTime) / (double)1000.0F;
         data.morePacketsBuffer = (int)((double)data.morePacketsBuffer + (double)22.0F * seconds);
         if (seconds > (double)2.0F) {
            if (data.morePacketsBuffer > 100) {
               data.morePacketsBuffer = 100;
            }
         } else if (data.morePacketsBuffer > 50) {
            data.morePacketsBuffer = 50;
         }

         data.morePacketsLastTime = time;
         if (newTo == null) {
            data.setMorePacketsSetBack(from);
         }
      } else if (data.morePacketsLastTime > time) {
         data.morePacketsLastTime = time;
      }

      return newTo == null ? null : new Location(player.getWorld(), newTo.getX(), newTo.getY(), newTo.getZ(), to.getYaw(), to.getPitch());
   }

   protected Map getParameterMap(ViolationData violationData) {
      Map<ParameterName, String> parameters = super.getParameterMap(violationData);
      parameters.put(ParameterName.PACKETS, String.valueOf(MovingData.getData(violationData.player).morePacketsPackets));
      return parameters;
   }
}
