package com.earth2me.essentials;

import com.earth2me.essentials.textreader.IText;
import com.earth2me.essentials.textreader.KeywordReplacer;
import com.earth2me.essentials.textreader.TextInput;
import com.earth2me.essentials.textreader.TextPager;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class EssentialsPlayerListener implements Listener {
   private static final Logger LOGGER = Logger.getLogger("Minecraft");
   private final transient IEssentials ess;
   private static final int AIR;
   private static final int BED;

   public EssentialsPlayerListener(IEssentials parent) {
      this.ess = parent;
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPlayerRespawn(PlayerRespawnEvent event) {
      User user = this.ess.getUser(event.getPlayer());
      this.updateCompass(user);
      user.setDisplayNick();
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerChat(AsyncPlayerChatEvent event) {
      User user = this.ess.getUser(event.getPlayer());
      if (user.isMuted()) {
         event.setCancelled(true);
         user.sendMessage(I18n._("playerMuted"));
         LOGGER.info(I18n._("mutedUserSpeaks", user.getName()));
      }

      Iterator<Player> it = event.getRecipients().iterator();

      while(it.hasNext()) {
         User u = this.ess.getUser(it.next());
         if (u.isIgnoredPlayer(user)) {
            it.remove();
         }
      }

      user.updateActivity(true);
      user.setDisplayNick();
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onPlayerMove(PlayerMoveEvent event) {
      if (!this.ess.getSettings().cancelAfkOnMove() && !this.ess.getSettings().getFreezeAfkPlayers()) {
         event.getHandlers().unregister(this);
         if (this.ess.getSettings().isDebug()) {
            LOGGER.log(Level.INFO, "Unregistering move listener");
         }

      } else if (event.getFrom().getBlockX() != event.getTo().getBlockX() || event.getFrom().getBlockZ() != event.getTo().getBlockZ() || event.getFrom().getBlockY() != event.getTo().getBlockY()) {
         User user = this.ess.getUser(event.getPlayer());
         if (user.isAfk() && this.ess.getSettings().getFreezeAfkPlayers()) {
            Location from = event.getFrom();
            Location origTo = event.getTo();
            Location to = origTo.clone();
            if (this.ess.getSettings().cancelAfkOnMove() && origTo.getY() >= (double)(from.getBlockY() + 1)) {
               user.updateActivity(true);
            } else {
               to.setX(from.getX());
               to.setY(from.getY());
               to.setZ(from.getZ());

               try {
                  event.setTo(Util.getSafeDestination(to));
               } catch (Exception var7) {
                  event.setTo(to);
               }

            }
         } else {
            Location afk = user.getAfkPosition();
            if (afk == null || !event.getTo().getWorld().equals(afk.getWorld()) || afk.distanceSquared(event.getTo()) > (double)9.0F) {
               user.updateActivity(true);
            }

         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerQuit(PlayerQuitEvent event) {
      User user = this.ess.getUser(event.getPlayer());
      if (this.ess.getSettings().removeGodOnDisconnect() && user.isGodModeEnabled()) {
         user.setGodModeEnabled(false);
      }

      if (user.isVanished()) {
         user.toggleVanished();
      }

      user.setLogoutLocation();
      if (user.isRecipeSee()) {
         user.getPlayer().getOpenInventory().getTopInventory().clear();
      }

      user.updateActivity(false);
      user.dispose();
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerJoin(final PlayerJoinEvent event) {
      this.ess.runTaskAsynchronously(new Runnable() {
         public void run() {
            EssentialsPlayerListener.this.delayedJoin(event.getPlayer());
         }
      });
   }

   public void delayedJoin(Player player) {
      if (player.isOnline()) {
         this.ess.getBackup().onPlayerJoin();
         final User user = this.ess.getUser(player);
         if (user.isNPC()) {
            user.setNPC(false);
         }

         final long currentTime = System.currentTimeMillis();
         user.checkMuteTimeout(currentTime);
         user.updateActivity(false);
         this.ess.scheduleSyncDelayedTask(new Runnable() {
            public void run() {
               if (user.isOnline()) {
                  user.setLastLogin(currentTime);
                  user.setDisplayNick();
                  EssentialsPlayerListener.this.updateCompass(user);
                  if (!EssentialsPlayerListener.this.ess.getVanishedPlayers().isEmpty() && !user.isAuthorized("essentials.vanish.see")) {
                     for(String p : EssentialsPlayerListener.this.ess.getVanishedPlayers()) {
                        Player toVanish = EssentialsPlayerListener.this.ess.getServer().getPlayerExact(p);
                        if (toVanish != null && toVanish.isOnline()) {
                           user.hidePlayer(toVanish);
                        }
                     }
                  }

                  if (user.isAuthorized("essentials.sleepingignored")) {
                     user.setSleepingIgnored(true);
                  }

                  if (!EssentialsPlayerListener.this.ess.getSettings().isCommandDisabled("motd") && user.isAuthorized("essentials.motd")) {
                     try {
                        IText input = new TextInput(user, "motd", true, EssentialsPlayerListener.this.ess);
                        IText output = new KeywordReplacer(input, user, EssentialsPlayerListener.this.ess);
                        TextPager pager = new TextPager(output, true);
                        pager.showPage("1", (String)null, "motd", user);
                     } catch (IOException ex) {
                        if (EssentialsPlayerListener.this.ess.getSettings().isDebug()) {
                           EssentialsPlayerListener.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                        } else {
                           EssentialsPlayerListener.LOGGER.log(Level.WARNING, ex.getMessage());
                        }
                     }
                  }

                  if (!EssentialsPlayerListener.this.ess.getSettings().isCommandDisabled("mail") && user.isAuthorized("essentials.mail")) {
                     List<String> mail = user.getMails();
                     if (mail.isEmpty()) {
                        String msg = I18n._("noNewMail");
                        if (!msg.isEmpty()) {
                           user.sendMessage(msg);
                        }
                     } else {
                        user.sendMessage(I18n._("youHaveNewMail", mail.size()));
                     }
                  }

                  if (user.isAuthorized("essentials.fly.safelogin")) {
                     World world = user.getLocation().getWorld();
                     int x = user.getLocation().getBlockX();
                     int y = user.getLocation().getBlockY();

                     for(int z = user.getLocation().getBlockZ(); Util.isBlockUnsafe(world, x, y, z) && y > -1; --y) {
                     }

                     if (user.getLocation().getBlockY() - y > 1 || y < 0) {
                        user.setAllowFlight(true);
                        user.setFlying(true);
                        user.sendMessage(I18n._("flyMode", I18n._("enabled"), user.getDisplayName()));
                     }
                  }

                  user.setFlySpeed(0.1F);
                  user.setWalkSpeed(0.2F);
               }
            }
         });
      }
   }

   private void updateCompass(User user) {
      Location loc = user.getHome(user.getLocation());
      if (loc == null) {
         loc = user.getBedSpawnLocation();
      }

      if (loc != null) {
         user.setCompassTarget(loc);
      }

   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onPlayerLogin(PlayerLoginEvent event) {
      switch (event.getResult()) {
         case KICK_FULL:
         case KICK_BANNED:
            User user = this.ess.getUser(event.getPlayer());
            if (event.getResult() == Result.KICK_BANNED || user.isBanned()) {
               boolean banExpired = user.checkBanTimeout(System.currentTimeMillis());
               if (!banExpired) {
                  String banReason = user.getBanReason();
                  if (banReason == null || banReason.isEmpty() || banReason.equalsIgnoreCase("ban")) {
                     banReason = I18n._("defaultBanReason");
                  }

                  if (user.getBanTimeout() > 0L) {
                     banReason = banReason + "\n\nExpires in " + Util.formatDateDiff(user.getBanTimeout());
                  }

                  event.disallow(Result.KICK_BANNED, banReason);
                  return;
               }
            }

            if (event.getResult() == Result.KICK_FULL && !user.isAuthorized("essentials.joinfullserver")) {
               event.disallow(Result.KICK_FULL, I18n._("serverFull"));
               return;
            } else {
               event.allow();
               return;
            }
         default:
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onPlayerTeleport(PlayerTeleportEvent event) {
      boolean backListener = this.ess.getSettings().registerBackInListener();
      boolean teleportInvulnerability = this.ess.getSettings().isTeleportInvulnerability();
      if (backListener || teleportInvulnerability) {
         User user = this.ess.getUser(event.getPlayer());
         if (backListener && (event.getCause() == TeleportCause.PLUGIN || event.getCause() == TeleportCause.COMMAND)) {
            user.setLastLocation();
         }

         if (teleportInvulnerability) {
            user.enableInvulnerabilityAfterTeleport();
         }
      }

   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onPlayerEggThrow(PlayerEggThrowEvent event) {
      User user = this.ess.getUser(event.getPlayer());
      ItemStack stack = new ItemStack(Material.EGG, 1);
      if (user.hasUnlimited(stack)) {
         user.getInventory().addItem(new ItemStack[]{stack});
         user.updateInventory();
      }

   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
      final User user = this.ess.getUser(event.getPlayer());
      if (user.hasUnlimited(new ItemStack(event.getBucket()))) {
         event.getItemStack().setType(event.getBucket());
         this.ess.scheduleSyncDelayedTask(new Runnable() {
            public void run() {
               user.updateInventory();
            }
         });
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
      Player player = event.getPlayer();
      String cmd = event.getMessage().toLowerCase(Locale.ENGLISH).split(" ")[0].replace("/", "").toLowerCase(Locale.ENGLISH);
      if (this.ess.getSettings().getSocialSpyCommands().contains(cmd)) {
         for(Player onlinePlayer : this.ess.getServer().getOnlinePlayers()) {
            User spyer = this.ess.getUser(onlinePlayer);
            if (spyer.isSocialSpyEnabled() && !player.equals(onlinePlayer)) {
               onlinePlayer.sendMessage(player.getDisplayName() + " : " + event.getMessage());
            }
         }
      } else if (!cmd.equalsIgnoreCase("afk")) {
         User user = this.ess.getUser(player);
         user.updateActivity(true);
      }

   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPlayerChangedWorldFlyReset(PlayerChangedWorldEvent event) {
      User user = this.ess.getUser(event.getPlayer());
      if (user.getGameMode() != GameMode.CREATIVE && !user.isAuthorized("essentials.fly")) {
         user.setAllowFlight(false);
      }

      user.setFlySpeed(0.1F);
      user.setWalkSpeed(0.2F);
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
      User user = this.ess.getUser(event.getPlayer());
      String newWorld = event.getPlayer().getLocation().getWorld().getName();
      user.setDisplayNick();
      this.updateCompass(user);
      if (this.ess.getSettings().getNoGodWorlds().contains(newWorld) && user.isGodModeEnabledRaw()) {
         user.sendMessage(I18n._("noGodWorldWarning"));
      }

      if (!user.getWorld().getName().equals(newWorld)) {
         user.sendMessage(I18n._("currentWorld", newWorld));
      }

      if (user.isVanished()) {
         user.setVanished(user.isAuthorized("essentials.vanish"));
      }

   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPlayerInteract(PlayerInteractEvent event) {
      switch (event.getAction()) {
         case RIGHT_CLICK_BLOCK:
            if (!event.isCancelled() && event.getClickedBlock().getTypeId() == BED && this.ess.getSettings().getUpdateBedAtDaytime()) {
               User player = this.ess.getUser(event.getPlayer());
               if (player.isAuthorized("essentials.sethome.bed")) {
                  player.setBedSpawnLocation(event.getClickedBlock().getLocation());
                  player.sendMessage(I18n._("bedSet", player.getLocation().getWorld().getName(), player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()));
               }
            }
            break;
         case LEFT_CLICK_AIR:
            if (event.getPlayer().isFlying()) {
               User user = this.ess.getUser(event.getPlayer());
               if (user.isFlyClickJump()) {
                  this.useFlyClickJump(user);
                  return;
               }
            }
         case LEFT_CLICK_BLOCK:
            if (event.getItem() != null && event.getItem().getTypeId() != AIR) {
               User user = this.ess.getUser(event.getPlayer());
               user.updateActivity(true);
               if (user.hasPowerTools() && user.arePowerToolsEnabled() && this.usePowertools(user, event.getItem().getTypeId())) {
                  event.setCancelled(true);
               }
            }
      }

   }

   private void useFlyClickJump(final User user) {
      try {
         final Location otarget = Util.getTarget(user);
         this.ess.scheduleSyncDelayedTask(new Runnable() {
            public void run() {
               Location loc = user.getLocation();
               loc.setX(otarget.getX());
               loc.setZ(otarget.getZ());

               while(Util.isBlockDamaging(loc.getWorld(), loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ())) {
                  loc.setY(loc.getY() + (double)1.0F);
               }

               user.getBase().teleport(loc, TeleportCause.PLUGIN);
            }
         });
      } catch (Exception ex) {
         if (this.ess.getSettings().isDebug()) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
         }
      }

   }

   private boolean usePowertools(final User user, int id) {
      List<String> commandList = user.getPowertool(id);
      if (commandList != null && !commandList.isEmpty()) {
         boolean used = false;

         for(final String command : commandList) {
            if (!command.contains("{player}")) {
               if (command.startsWith("c:")) {
                  used = true;
                  user.chat(command.substring(2));
               } else {
                  used = true;
                  this.ess.scheduleSyncDelayedTask(new Runnable() {
                     public void run() {
                        user.getServer().dispatchCommand(user.getBase(), command);
                        EssentialsPlayerListener.LOGGER.log(Level.INFO, String.format("[PT] %s issued server command: /%s", user.getName(), command));
                     }
                  });
               }
            }
         }

         return used;
      } else {
         return false;
      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onPlayerPickupItem(PlayerPickupItemEvent event) {
      if (this.ess.getSettings().getDisableItemPickupWhileAfk() && this.ess.getUser(event.getPlayer()).isAfk()) {
         event.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onInventoryClickEvent(InventoryClickEvent event) {
      Inventory top = event.getView().getTopInventory();
      InventoryType type = top.getType();
      if (type == InventoryType.PLAYER) {
         User user = this.ess.getUser(event.getWhoClicked());
         InventoryHolder invHolder = top.getHolder();
         if (invHolder != null && invHolder instanceof HumanEntity) {
            User invOwner = this.ess.getUser((HumanEntity)invHolder);
            if (user.isInvSee() && (!user.isAuthorized("essentials.invsee.modify") || invOwner.isAuthorized("essentials.invsee.preventmodify") || !invOwner.isOnline())) {
               event.setCancelled(true);
               user.updateInventory();
            }
         }
      } else if (type == InventoryType.ENDER_CHEST) {
         User user = this.ess.getUser(event.getWhoClicked());
         if (user.isEnderSee() && !user.isAuthorized("essentials.enderchest.modify")) {
            event.setCancelled(true);
         }
      } else if (type == InventoryType.WORKBENCH) {
         User user = this.ess.getUser(event.getWhoClicked());
         if (user.isRecipeSee()) {
            event.setCancelled(true);
         }
      } else if (type == InventoryType.CHEST && top.getSize() == 9) {
         User user = this.ess.getUser(event.getWhoClicked());
         InventoryHolder invHolder = top.getHolder();
         if (invHolder != null && invHolder instanceof HumanEntity && user.isInvSee()) {
            event.setCancelled(true);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onInventoryCloseEvent(InventoryCloseEvent event) {
      Inventory top = event.getView().getTopInventory();
      InventoryType type = top.getType();
      if (type == InventoryType.PLAYER) {
         User user = this.ess.getUser(event.getPlayer());
         user.setInvSee(false);
      } else if (type == InventoryType.ENDER_CHEST) {
         User user = this.ess.getUser(event.getPlayer());
         user.setEnderSee(false);
      } else if (type == InventoryType.WORKBENCH) {
         User user = this.ess.getUser(event.getPlayer());
         if (user.isRecipeSee()) {
            user.setRecipeSee(false);
            event.getView().getTopInventory().clear();
         }
      } else if (type == InventoryType.CHEST && top.getSize() == 9) {
         InventoryHolder invHolder = top.getHolder();
         if (invHolder != null && invHolder instanceof HumanEntity) {
            User user = this.ess.getUser(event.getPlayer());
            user.setInvSee(false);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onPlayerFishEvent(PlayerFishEvent event) {
      User user = this.ess.getUser(event.getPlayer());
      user.updateActivity(true);
   }

   static {
      AIR = Material.AIR.getId();
      BED = Material.BED_BLOCK.getId();
   }
}
