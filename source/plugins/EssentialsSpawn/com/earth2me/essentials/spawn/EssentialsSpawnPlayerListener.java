package com.earth2me.essentials.spawn;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.Kit;
import com.earth2me.essentials.OfflinePlayer;
import com.earth2me.essentials.User;
import com.earth2me.essentials.textreader.IText;
import com.earth2me.essentials.textreader.KeywordReplacer;
import com.earth2me.essentials.textreader.SimpleTextPager;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class EssentialsSpawnPlayerListener implements Listener {
   private final transient IEssentials ess;
   private final transient SpawnStorage spawns;
   private static final Logger LOGGER = Bukkit.getLogger();

   public EssentialsSpawnPlayerListener(IEssentials ess, SpawnStorage spawns) {
      this.ess = ess;
      this.spawns = spawns;
   }

   public void onPlayerRespawn(PlayerRespawnEvent event) {
      User user = this.ess.getUser(event.getPlayer());
      if (!user.isJailed() || user.getJail() == null || user.getJail().isEmpty()) {
         if (this.ess.getSettings().getRespawnAtHome()) {
            Location bed = user.getBedSpawnLocation();
            Location home;
            if (bed != null) {
               home = bed;
            } else {
               home = user.getHome(user.getLocation());
            }

            if (home != null) {
               event.setRespawnLocation(home);
               return;
            }
         }

         Location spawn = this.spawns.getSpawn(user.getGroup());
         if (spawn != null) {
            event.setRespawnLocation(spawn);
         }

      }
   }

   public void onPlayerJoin(final PlayerJoinEvent event) {
      this.ess.runTaskAsynchronously(new Runnable() {
         public void run() {
            EssentialsSpawnPlayerListener.this.delayedJoin(event.getPlayer());
         }
      });
   }

   public void delayedJoin(Player player) {
      if (player.hasPlayedBefore()) {
         LOGGER.log(Level.FINE, "Old player join");
      } else {
         final User user = this.ess.getUser(player);
         if (!"none".equalsIgnoreCase(this.ess.getSettings().getNewbieSpawn())) {
            this.ess.scheduleSyncDelayedTask(new NewPlayerTeleport(user), 1L);
         }

         this.ess.scheduleSyncDelayedTask(new Runnable() {
            public void run() {
               if (user.isOnline()) {
                  if (EssentialsSpawnPlayerListener.this.ess.getSettings().getAnnounceNewPlayers()) {
                     IText output = new KeywordReplacer(EssentialsSpawnPlayerListener.this.ess.getSettings().getAnnounceNewPlayerFormat(), user, EssentialsSpawnPlayerListener.this.ess);
                     SimpleTextPager pager = new SimpleTextPager(output);

                     for(String line : pager.getLines()) {
                        EssentialsSpawnPlayerListener.this.ess.broadcastMessage(user, line);
                     }
                  }

                  String kitName = EssentialsSpawnPlayerListener.this.ess.getSettings().getNewPlayerKit();
                  if (!kitName.isEmpty()) {
                     try {
                        Map<String, Object> kit = EssentialsSpawnPlayerListener.this.ess.getSettings().getKit(kitName.toLowerCase(Locale.ENGLISH));
                        List<String> items = Kit.getItems(user, kit);
                        Kit.expandItems(EssentialsSpawnPlayerListener.this.ess, user, items);
                     } catch (Exception ex) {
                        EssentialsSpawnPlayerListener.LOGGER.log(Level.WARNING, ex.getMessage());
                     }
                  }

                  EssentialsSpawnPlayerListener.LOGGER.log(Level.FINE, "New player join");
               }
            }
         });
      }
   }

   private class NewPlayerTeleport implements Runnable {
      private final transient User user;

      public NewPlayerTeleport(User user) {
         this.user = user;
      }

      public void run() {
         if (!(this.user.getBase() instanceof OfflinePlayer)) {
            try {
               Location spawn = EssentialsSpawnPlayerListener.this.spawns.getSpawn(EssentialsSpawnPlayerListener.this.ess.getSettings().getNewbieSpawn());
               if (spawn != null) {
                  this.user.getTeleport().now(spawn, false, TeleportCause.PLUGIN);
               }
            } catch (Exception ex) {
               Bukkit.getLogger().log(Level.WARNING, I18n._("teleportNewPlayerError", new Object[0]), ex);
            }

         }
      }
   }
}
