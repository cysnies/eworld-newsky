package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class TeleportUtil {
   public static void teleport(Entity vehicle, Player player, Location location, boolean debug) {
      Entity passenger = vehicle.getPassenger();
      boolean playerIsPassenger = player.equals(passenger);
      boolean vehicleTeleported;
      if (playerIsPassenger && !vehicle.isDead()) {
         vehicle.eject();
         vehicleTeleported = vehicle.teleport(location, TeleportCause.PLUGIN);
      } else if (passenger == null && !vehicle.isDead()) {
         vehicleTeleported = vehicle.teleport(location, TeleportCause.PLUGIN);
      } else {
         vehicleTeleported = false;
      }

      boolean playerTeleported = player.teleport(location);
      if (playerIsPassenger && playerTeleported && vehicleTeleported && player.getLocation().distance(vehicle.getLocation()) < (double)1.0F) {
         vehicle.setPassenger(player);
      }

      if (debug) {
         System.out.println(player.getName() + " vehicle set back: " + location);
      }

   }
}
