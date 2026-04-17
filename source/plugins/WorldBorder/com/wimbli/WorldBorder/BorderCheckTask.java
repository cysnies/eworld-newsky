package com.wimbli.WorldBorder;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class BorderCheckTask implements Runnable {
   private static Set handlingPlayers = Collections.synchronizedSet(new LinkedHashSet());

   public void run() {
      if (Config.KnockBack() != (double)0.0F) {
         Player[] players = Bukkit.getServer().getOnlinePlayers();

         for(int i = 0; i < players.length; ++i) {
            checkPlayer(players[i], (Location)null, false, true);
         }

      }
   }

   public static Location checkPlayer(Player player, Location targetLoc, boolean returnLocationOnly, boolean notify) {
      if (player != null && player.isOnline()) {
         Location loc = targetLoc == null ? player.getLocation().clone() : targetLoc;
         if (loc == null) {
            return null;
         } else {
            World world = loc.getWorld();
            if (world == null) {
               return null;
            } else {
               BorderData border = Config.Border(world.getName());
               if (border == null) {
                  return null;
               } else if (border.insideBorder(loc.getX(), loc.getZ(), Config.ShapeRound())) {
                  return null;
               } else if (!Config.isPlayerBypassing(player.getName()) && !handlingPlayers.contains(player.getName().toLowerCase())) {
                  handlingPlayers.add(player.getName().toLowerCase());
                  Location newLoc = newLocation(player, loc, border, notify);
                  boolean handlingVehicle = false;
                  if (player.isInsideVehicle()) {
                     Entity ride = player.getVehicle();
                     player.leaveVehicle();
                     if (ride != null) {
                        double vertOffset = ride instanceof LivingEntity ? (double)0.0F : ride.getLocation().getY() - loc.getY();
                        Location rideLoc = newLoc.clone();
                        rideLoc.setY(newLoc.getY() + vertOffset);
                        if (Config.Debug()) {
                           Config.LogWarn("Player was riding a \"" + ride.toString() + "\".");
                        }

                        if (ride instanceof Boat) {
                           ride.remove();
                           ride = world.spawnEntity(rideLoc, EntityType.BOAT);
                        } else {
                           ride.setVelocity(new Vector(0, 0, 0));
                           ride.teleport(rideLoc);
                        }

                        if (Config.RemountTicks() > 0) {
                           setPassengerDelayed(ride, player, player.getName(), (long)Config.RemountTicks());
                           handlingVehicle = true;
                        }
                     }
                  }

                  Config.showWhooshEffect(loc);
                  if (!returnLocationOnly) {
                     player.teleport(newLoc);
                  }

                  if (!handlingVehicle) {
                     handlingPlayers.remove(player.getName().toLowerCase());
                  }

                  return returnLocationOnly ? newLoc : null;
               } else {
                  return null;
               }
            }
         }
      } else {
         return null;
      }
   }

   public static Location checkPlayer(Player player, Location targetLoc, boolean returnLocationOnly) {
      return checkPlayer(player, targetLoc, returnLocationOnly, true);
   }

   private static Location newLocation(Player player, Location loc, BorderData border, boolean notify) {
      if (Config.Debug()) {
         Config.LogWarn((notify ? "Border crossing" : "Check was run") + " in \"" + loc.getWorld().getName() + "\". Border " + border.toString());
         Config.LogWarn("Player position X: " + Config.coord.format(loc.getX()) + " Y: " + Config.coord.format(loc.getY()) + " Z: " + Config.coord.format(loc.getZ()));
      }

      Location newLoc = border.correctedPosition(loc, Config.ShapeRound(), player.isFlying());
      if (newLoc == null) {
         if (Config.Debug()) {
            Config.LogWarn("Target new location unviable, using spawn.");
         }

         newLoc = player.getWorld().getSpawnLocation();
      }

      if (Config.Debug()) {
         Config.LogWarn("New position in world \"" + newLoc.getWorld().getName() + "\" at X: " + Config.coord.format(newLoc.getX()) + " Y: " + Config.coord.format(newLoc.getY()) + " Z: " + Config.coord.format(newLoc.getZ()));
      }

      if (notify) {
         player.sendMessage(ChatColor.RED + Config.Message());
      }

      return newLoc;
   }

   private static void setPassengerDelayed(final Entity vehicle, final Player player, final String playerName, long delay) {
      Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(WorldBorder.plugin, new Runnable() {
         public void run() {
            BorderCheckTask.handlingPlayers.remove(playerName.toLowerCase());
            if (vehicle != null && player != null) {
               vehicle.setPassenger(player);
            }
         }
      }, delay);
   }
}
