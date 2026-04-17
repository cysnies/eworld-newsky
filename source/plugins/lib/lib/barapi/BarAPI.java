package lib.barapi;

import java.util.HashMap;
import lib.Lib;
import lib.barapi.nms.FakeDragon;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class BarAPI implements Listener {
   private static HashMap players = new HashMap();
   private static HashMap timers = new HashMap();
   private static Lib lib;

   public BarAPI(Lib lib) {
      BarAPI.lib = lib;
      Bukkit.getServer().getPluginManager().registerEvents(this, lib);
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void PlayerLoggout(PlayerQuitEvent event) {
      this.quit(event.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerKick(PlayerKickEvent event) {
      this.quit(event.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerTeleport(PlayerTeleportEvent event) {
      this.handleTeleport(event.getPlayer(), event.getTo().clone());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerTeleport(PlayerRespawnEvent event) {
      this.handleTeleport(event.getPlayer(), event.getRespawnLocation().clone());
   }

   private void handleTeleport(final Player player, final Location loc) {
      if (hasBar(player)) {
         Bukkit.getScheduler().runTaskLater(lib, new Runnable() {
            public void run() {
               FakeDragon oldDragon = BarAPI.getDragon(player, "");
               float health = oldDragon.health;
               String message = oldDragon.name;
               Util.sendPacket(player, BarAPI.getDragon(player, "").getDestroyPacket());
               BarAPI.players.remove(player.getName());
               FakeDragon dragon = BarAPI.addDragon(player, loc, message);
               dragon.health = health;
               BarAPI.sendDragon(dragon, player);
            }
         }, 2L);
      }
   }

   private void quit(Player player) {
      removeBar(player);
   }

   public static void setMessage(Player player, String message) {
      FakeDragon dragon = getDragon(player, message);
      dragon.name = cleanMessage(message);
      dragon.health = 200.0F;
      cancelTimer(player);
      sendDragon(dragon, player);
   }

   public static void setMessage(Player player, String message, float percent) {
      FakeDragon dragon = getDragon(player, message);
      dragon.name = cleanMessage(message);
      dragon.health = percent / 100.0F * 200.0F;
      cancelTimer(player);
      sendDragon(dragon, player);
   }

   public static void setMessage(final Player player, String message, int seconds) {
      FakeDragon dragon = getDragon(player, message);
      dragon.name = cleanMessage(message);
      dragon.health = 200.0F;
      final int dragonHealthMinus = 200 / seconds;
      cancelTimer(player);
      timers.put(player.getName(), Bukkit.getScheduler().runTaskTimer(lib, new BukkitRunnable() {
         public void run() {
            FakeDragon drag = BarAPI.getDragon(player, "");
            drag.health -= (float)dragonHealthMinus;
            if (drag.health <= 0.0F) {
               BarAPI.removeBar(player);
               BarAPI.cancelTimer(player);
            } else {
               BarAPI.sendDragon(drag, player);
            }

         }
      }, 20L, 20L).getTaskId());
      sendDragon(dragon, player);
   }

   public static boolean hasBar(Player player) {
      return players.get(player.getName()) != null;
   }

   public static void removeBar(Player player) {
      if (hasBar(player)) {
         Util.sendPacket(player, getDragon(player, "").getDestroyPacket());
         players.remove(player.getName());
         cancelTimer(player);
      }
   }

   public static void setHealth(Player player, float percent) {
      if (hasBar(player)) {
         FakeDragon dragon = getDragon(player, "");
         dragon.health = percent / 100.0F * 200.0F;
         cancelTimer(player);
         sendDragon(dragon, player);
      }
   }

   public static float getHealth(Player player) {
      return !hasBar(player) ? -1.0F : getDragon(player, "").health;
   }

   public static String getMessage(Player player) {
      return !hasBar(player) ? "" : getDragon(player, "").name;
   }

   private static String cleanMessage(String message) {
      if (message.length() > 64) {
         message = message.substring(0, 63);
      }

      return message;
   }

   private static void cancelTimer(Player player) {
      Integer timerID = (Integer)timers.remove(player.getName());
      if (timerID != null) {
         Bukkit.getScheduler().cancelTask(timerID);
      }

   }

   private static void sendDragon(FakeDragon dragon, Player player) {
      Util.sendPacket(player, dragon.getMetaPacket(dragon.getWatcher()));
      Util.sendPacket(player, dragon.getTeleportPacket(player.getLocation().add((double)0.0F, (double)-200.0F, (double)0.0F)));
   }

   private static FakeDragon getDragon(Player player, String message) {
      return hasBar(player) ? (FakeDragon)players.get(player.getName()) : addDragon(player, cleanMessage(message));
   }

   private static FakeDragon addDragon(Player player, String message) {
      FakeDragon dragon = Util.newDragon(message, player.getLocation().add((double)0.0F, (double)-200.0F, (double)0.0F));
      Util.sendPacket(player, dragon.getSpawnPacket());
      players.put(player.getName(), dragon);
      return dragon;
   }

   private static FakeDragon addDragon(Player player, Location loc, String message) {
      FakeDragon dragon = Util.newDragon(message, loc.add((double)0.0F, (double)-200.0F, (double)0.0F));
      Util.sendPacket(player, dragon.getSpawnPacket());
      players.put(player.getName(), dragon);
      return dragon;
   }
}
