package com.earth2me.essentials;

import com.earth2me.essentials.api.IJails;
import com.earth2me.essentials.storage.AsyncStorageObjectHolder;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.PluginManager;

public class Jails extends AsyncStorageObjectHolder implements IJails {
   private static final transient Logger LOGGER = Bukkit.getLogger();
   private static transient boolean enabled = false;

   public Jails(IEssentials ess) {
      super(ess, com.earth2me.essentials.settings.Jails.class);
      this.reloadConfig();
   }

   private void registerListeners() {
      enabled = true;
      PluginManager pluginManager = this.ess.getServer().getPluginManager();
      JailListener blockListener = new JailListener();
      pluginManager.registerEvents(blockListener, this.ess);
      if (this.ess.getSettings().isDebug()) {
         LOGGER.log(Level.INFO, "Registering Jail listener");
      }

   }

   public File getStorageFile() {
      return new File(this.ess.getDataFolder(), "jail.yml");
   }

   public void finishRead() {
      this.checkRegister();
   }

   public void finishWrite() {
      this.checkRegister();
   }

   public void resetListener() {
      enabled = false;
      this.checkRegister();
   }

   private void checkRegister() {
      if (!enabled && this.getCount() > 0) {
         this.registerListeners();
      }

   }

   public Location getJail(String jailName) throws Exception {
      this.acquireReadLock();

      Location var3;
      try {
         if (((com.earth2me.essentials.settings.Jails)this.getData()).getJails() == null || jailName == null || !((com.earth2me.essentials.settings.Jails)this.getData()).getJails().containsKey(jailName.toLowerCase(Locale.ENGLISH))) {
            throw new Exception(I18n._("jailNotExist"));
         }

         Location loc = (Location)((com.earth2me.essentials.settings.Jails)this.getData()).getJails().get(jailName.toLowerCase(Locale.ENGLISH));
         if (loc == null || loc.getWorld() == null) {
            throw new Exception(I18n._("jailNotExist"));
         }

         var3 = loc;
      } finally {
         this.unlock();
      }

      return var3;
   }

   public Collection getList() throws Exception {
      this.acquireReadLock();

      Collection var1;
      try {
         if (((com.earth2me.essentials.settings.Jails)this.getData()).getJails() != null) {
            var1 = new ArrayList(((com.earth2me.essentials.settings.Jails)this.getData()).getJails().keySet());
            return var1;
         }

         var1 = Collections.emptyList();
      } finally {
         this.unlock();
      }

      return var1;
   }

   public void removeJail(String jail) throws Exception {
      this.acquireWriteLock();

      try {
         if (((com.earth2me.essentials.settings.Jails)this.getData()).getJails() != null) {
            ((com.earth2me.essentials.settings.Jails)this.getData()).getJails().remove(jail.toLowerCase(Locale.ENGLISH));
            return;
         }
      } finally {
         this.unlock();
      }

   }

   public void sendToJail(IUser user, String jail) throws Exception {
      this.acquireReadLock();

      try {
         if (user.isOnline()) {
            Location loc = this.getJail(jail);
            user.getTeleport().now(loc, false, TeleportCause.COMMAND);
         }

         user.setJail(jail);
      } finally {
         this.unlock();
      }

   }

   public void setJail(String jailName, Location loc) throws Exception {
      this.acquireWriteLock();

      try {
         if (((com.earth2me.essentials.settings.Jails)this.getData()).getJails() == null) {
            ((com.earth2me.essentials.settings.Jails)this.getData()).setJails(new HashMap());
         }

         ((com.earth2me.essentials.settings.Jails)this.getData()).getJails().put(jailName.toLowerCase(Locale.ENGLISH), loc);
      } finally {
         this.unlock();
      }

   }

   public int getCount() {
      try {
         return this.getList().size();
      } catch (Exception var2) {
         return 0;
      }
   }

   private class JailListener implements Listener {
      private JailListener() {
      }

      @EventHandler(
         priority = EventPriority.LOW,
         ignoreCancelled = true
      )
      public void onBlockBreak(BlockBreakEvent event) {
         User user = Jails.this.ess.getUser(event.getPlayer());
         if (user.isJailed()) {
            event.setCancelled(true);
         }

      }

      @EventHandler(
         priority = EventPriority.LOW,
         ignoreCancelled = true
      )
      public void onBlockPlace(BlockPlaceEvent event) {
         User user = Jails.this.ess.getUser(event.getPlayer());
         if (user.isJailed()) {
            event.setCancelled(true);
         }

      }

      @EventHandler(
         priority = EventPriority.LOW,
         ignoreCancelled = true
      )
      public void onBlockDamage(BlockDamageEvent event) {
         User user = Jails.this.ess.getUser(event.getPlayer());
         if (user.isJailed()) {
            event.setCancelled(true);
         }

      }

      @EventHandler(
         priority = EventPriority.LOW,
         ignoreCancelled = true
      )
      public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
         if (event.getCause() == DamageCause.ENTITY_ATTACK && event.getEntity().getType() == EntityType.PLAYER) {
            Entity damager = event.getDamager();
            if (damager.getType() == EntityType.PLAYER) {
               User user = Jails.this.ess.getUser(damager);
               if (user != null && user.isJailed()) {
                  event.setCancelled(true);
               }
            }

         }
      }

      @EventHandler(
         priority = EventPriority.LOW,
         ignoreCancelled = true
      )
      public void onPlayerInteract(PlayerInteractEvent event) {
         User user = Jails.this.ess.getUser(event.getPlayer());
         if (user.isJailed()) {
            event.setCancelled(true);
         }

      }

      @EventHandler(
         priority = EventPriority.HIGHEST
      )
      public void onPlayerRespawn(PlayerRespawnEvent event) {
         User user = Jails.this.ess.getUser(event.getPlayer());
         if (user.isJailed() && user.getJail() != null && !user.getJail().isEmpty()) {
            try {
               event.setRespawnLocation(Jails.this.getJail(user.getJail()));
            } catch (Exception ex) {
               if (Jails.this.ess.getSettings().isDebug()) {
                  Jails.LOGGER.log(Level.INFO, I18n._("returnPlayerToJailError", user.getName(), ex.getLocalizedMessage()), ex);
               } else {
                  Jails.LOGGER.log(Level.INFO, I18n._("returnPlayerToJailError", user.getName(), ex.getLocalizedMessage()));
               }
            }

         }
      }

      @EventHandler(
         priority = EventPriority.HIGH
      )
      public void onPlayerTeleport(PlayerTeleportEvent event) {
         User user = Jails.this.ess.getUser(event.getPlayer());
         if (user.isJailed() && user.getJail() != null && !user.getJail().isEmpty()) {
            try {
               event.setTo(Jails.this.getJail(user.getJail()));
            } catch (Exception ex) {
               if (Jails.this.ess.getSettings().isDebug()) {
                  Jails.LOGGER.log(Level.INFO, I18n._("returnPlayerToJailError", user.getName(), ex.getLocalizedMessage()), ex);
               } else {
                  Jails.LOGGER.log(Level.INFO, I18n._("returnPlayerToJailError", user.getName(), ex.getLocalizedMessage()));
               }
            }

            user.sendMessage(I18n._("jailMessage"));
         }
      }

      @EventHandler(
         priority = EventPriority.HIGHEST
      )
      public void onPlayerJoin(PlayerJoinEvent event) {
         User user = Jails.this.ess.getUser(event.getPlayer());
         long currentTime = System.currentTimeMillis();
         user.checkJailTimeout(currentTime);
         if (user.isJailed() && user.getJail() != null && !user.getJail().isEmpty()) {
            try {
               Jails.this.sendToJail(user, user.getJail());
            } catch (Exception ex) {
               if (Jails.this.ess.getSettings().isDebug()) {
                  Jails.LOGGER.log(Level.INFO, I18n._("returnPlayerToJailError", user.getName(), ex.getLocalizedMessage()), ex);
               } else {
                  Jails.LOGGER.log(Level.INFO, I18n._("returnPlayerToJailError", user.getName(), ex.getLocalizedMessage()));
               }
            }

            user.sendMessage(I18n._("jailMessage"));
         }
      }
   }
}
