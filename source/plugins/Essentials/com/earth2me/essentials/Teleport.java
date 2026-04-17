package com.earth2me.essentials;

import com.earth2me.essentials.api.ITeleport;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Teleport implements Runnable, ITeleport {
   private static final double MOVE_CONSTANT = 0.3;
   private IUser user;
   private IUser teleportUser;
   private int teleTimer = -1;
   private long started;
   private long tpdelay;
   private int health;
   private long initX;
   private long initY;
   private long initZ;
   private Target teleportTarget;
   private boolean respawn;
   private Trade chargeFor;
   private final IEssentials ess;
   private static final Logger logger = Logger.getLogger("Minecraft");
   private PlayerTeleportEvent.TeleportCause cause;

   private void initTimer(long delay, Target target, Trade chargeFor, PlayerTeleportEvent.TeleportCause cause) {
      this.initTimer(delay, this.user, target, chargeFor, cause, false);
   }

   private void initTimer(long delay, IUser teleportUser, Target target, Trade chargeFor, PlayerTeleportEvent.TeleportCause cause, boolean respawn) {
      this.started = System.currentTimeMillis();
      this.tpdelay = delay;
      this.health = teleportUser.getHealth();
      this.initX = Math.round(teleportUser.getLocation().getX() * 0.3);
      this.initY = Math.round(teleportUser.getLocation().getY() * 0.3);
      this.initZ = Math.round(teleportUser.getLocation().getZ() * 0.3);
      this.teleportUser = teleportUser;
      this.teleportTarget = target;
      this.chargeFor = chargeFor;
      this.cause = cause;
      this.respawn = respawn;
   }

   public void run() {
      if (this.user != null && this.user.isOnline() && this.user.getLocation() != null) {
         if (this.teleportUser != null && this.teleportUser.isOnline() && this.teleportUser.getLocation() != null) {
            if (this.user.isAuthorized("essentials.teleport.timer.move") || Math.round(this.teleportUser.getLocation().getX() * 0.3) == this.initX && Math.round(this.teleportUser.getLocation().getY() * 0.3) == this.initY && Math.round(this.teleportUser.getLocation().getZ() * 0.3) == this.initZ && this.teleportUser.getHealth() >= this.health) {
               this.health = this.teleportUser.getHealth();
               long now = System.currentTimeMillis();
               if (now > this.started + this.tpdelay) {
                  try {
                     this.cooldown(false);
                     this.teleportUser.sendMessage(I18n._("teleportationCommencing"));

                     try {
                        if (this.respawn) {
                           this.teleportUser.getTeleport().respawn(this.cause);
                        } else {
                           this.teleportUser.getTeleport().now(this.teleportTarget, this.cause);
                        }

                        this.cancel(false);
                        if (this.chargeFor != null) {
                           this.chargeFor.charge(this.user);
                        }
                     } catch (Throwable ex) {
                        this.ess.showError(this.user.getBase(), ex, "teleport");
                     }
                  } catch (Exception ex) {
                     this.user.sendMessage(I18n._("cooldownWithMessage", ex.getMessage()));
                     if (this.user != this.teleportUser) {
                        this.teleportUser.sendMessage(I18n._("cooldownWithMessage", ex.getMessage()));
                     }
                  }
               }

            } else {
               this.cancel(true);
            }
         } else {
            this.cancel(false);
         }
      } else {
         this.cancel(false);
      }
   }

   public Teleport(IUser user, IEssentials ess) {
      this.user = user;
      this.ess = ess;
   }

   public void cooldown(boolean check) throws Exception {
      Calendar time = new GregorianCalendar();
      if (this.user.getLastTeleportTimestamp() > 0L) {
         double cooldown = this.ess.getSettings().getTeleportCooldown();
         Calendar earliestTime = new GregorianCalendar();
         earliestTime.add(13, -((int)cooldown));
         earliestTime.add(14, -((int)(cooldown * (double)1000.0F % (double)1000.0F)));
         long earliestLong = earliestTime.getTimeInMillis();
         Long lastTime = this.user.getLastTeleportTimestamp();
         if (lastTime > time.getTimeInMillis()) {
            this.user.setLastTeleportTimestamp(time.getTimeInMillis());
            return;
         }

         if (lastTime > earliestLong && !this.user.isAuthorized("essentials.teleport.cooldown.bypass")) {
            time.setTimeInMillis(lastTime);
            time.add(13, (int)cooldown);
            time.add(14, (int)(cooldown * (double)1000.0F % (double)1000.0F));
            throw new Exception(I18n._("timeBeforeTeleport", Util.formatDateDiff(time.getTimeInMillis())));
         }
      }

      if (!check) {
         this.user.setLastTeleportTimestamp(time.getTimeInMillis());
      }

   }

   public void cancel(boolean notifyUser) {
      if (this.teleTimer != -1) {
         try {
            this.ess.getServer().getScheduler().cancelTask(this.teleTimer);
            if (notifyUser) {
               this.user.sendMessage(I18n._("pendingTeleportCancelled"));
               if (this.teleportUser != this.user) {
                  this.teleportUser.sendMessage(I18n._("pendingTeleportCancelled"));
               }
            }
         } finally {
            this.teleTimer = -1;
         }

      }
   }

   public void now(Location loc, boolean cooldown, PlayerTeleportEvent.TeleportCause cause) throws Exception {
      if (cooldown) {
         this.cooldown(false);
      }

      this.now(new Target(loc), cause);
   }

   public void now(Player entity, boolean cooldown, PlayerTeleportEvent.TeleportCause cause) throws Exception {
      if (cooldown) {
         this.cooldown(false);
      }

      this.now(new Target(entity), cause);
   }

   private void now(Target target, PlayerTeleportEvent.TeleportCause cause) throws Exception {
      this.cancel(false);
      this.user.setLastLocation();
      this.user.getBase().teleport(Util.getSafeDestination(target.getLocation()), cause);
   }

   /** @deprecated */
   @Deprecated
   public void teleport(Location loc, Trade chargeFor) throws Exception {
      this.teleport(loc, chargeFor, TeleportCause.PLUGIN);
   }

   public void teleport(Location loc, Trade chargeFor, PlayerTeleportEvent.TeleportCause cause) throws Exception {
      this.teleport(new Target(loc), chargeFor, cause);
   }

   public void teleport(Player entity, Trade chargeFor, PlayerTeleportEvent.TeleportCause cause) throws Exception {
      this.teleport(new Target(entity), chargeFor, cause);
   }

   private void teleport(Target target, Trade chargeFor, PlayerTeleportEvent.TeleportCause cause) throws Exception {
      double delay = this.ess.getSettings().getTeleportDelay();
      if (chargeFor != null) {
         chargeFor.isAffordableFor(this.user);
      }

      this.cooldown(true);
      if (!(delay <= (double)0.0F) && !this.user.isAuthorized("essentials.teleport.timer.bypass")) {
         this.cancel(false);
         this.warnUser(this.user, delay);
         this.initTimer((long)(delay * (double)1000.0F), target, chargeFor, cause);
         this.teleTimer = this.ess.scheduleSyncRepeatingTask(this, 10L, 10L);
      } else {
         this.cooldown(false);
         this.now(target, cause);
         if (chargeFor != null) {
            chargeFor.charge(this.user);
         }

      }
   }

   public void teleportToMe(User otherUser, Trade chargeFor, PlayerTeleportEvent.TeleportCause cause) throws Exception {
      Target target = new Target(this.user);
      double delay = this.ess.getSettings().getTeleportDelay();
      if (chargeFor != null) {
         chargeFor.isAffordableFor(this.user);
      }

      this.cooldown(true);
      if (!(delay <= (double)0.0F) && !this.user.isAuthorized("essentials.teleport.timer.bypass")) {
         this.cancel(false);
         this.warnUser(otherUser, delay);
         this.initTimer((long)(delay * (double)1000.0F), otherUser, target, chargeFor, cause, false);
         this.teleTimer = this.ess.scheduleSyncRepeatingTask(this, 10L, 10L);
      } else {
         this.cooldown(false);
         otherUser.getTeleport().now(target, cause);
         if (chargeFor != null) {
            chargeFor.charge(this.user);
         }

      }
   }

   private void warnUser(IUser user, double delay) {
      Calendar c = new GregorianCalendar();
      c.add(13, (int)delay);
      c.add(14, (int)(delay * (double)1000.0F % (double)1000.0F));
      user.sendMessage(I18n._("dontMoveMessage", Util.formatDateDiff(c.getTimeInMillis())));
   }

   public void respawn(Trade chargeFor, PlayerTeleportEvent.TeleportCause cause) throws Exception {
      double delay = this.ess.getSettings().getTeleportDelay();
      if (chargeFor != null) {
         chargeFor.isAffordableFor(this.user);
      }

      this.cooldown(true);
      if (!(delay <= (double)0.0F) && !this.user.isAuthorized("essentials.teleport.timer.bypass")) {
         this.cancel(false);
         this.warnUser(this.user, delay);
         this.initTimer((long)(delay * (double)1000.0F), this.user, (Target)null, chargeFor, cause, true);
         this.teleTimer = this.ess.scheduleSyncRepeatingTask(this, 10L, 10L);
      } else {
         this.cooldown(false);
         this.respawn(cause);
         if (chargeFor != null) {
            chargeFor.charge(this.user);
         }

      }
   }

   public void respawn(PlayerTeleportEvent.TeleportCause cause) throws Exception {
      Player player = this.user.getBase();
      Location bed = player.getBedSpawnLocation();
      if (bed != null) {
         this.now(new Target(bed), cause);
      } else {
         if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().info("Could not find bed spawn, forcing respawn event.");
         }

         PlayerRespawnEvent pre = new PlayerRespawnEvent(player, player.getWorld().getSpawnLocation(), false);
         this.ess.getServer().getPluginManager().callEvent(pre);
         this.now(new Target(pre.getRespawnLocation()), cause);
      }

   }

   public void warp(String warp, Trade chargeFor, PlayerTeleportEvent.TeleportCause cause) throws Exception {
      Location loc = this.ess.getWarps().getWarp(warp);
      this.user.sendMessage(I18n._("warpingTo", warp));
      this.teleport(new Target(loc), chargeFor, cause);
   }

   public void back(Trade chargeFor) throws Exception {
      this.teleport(new Target(this.user.getLastLocation()), chargeFor, TeleportCause.COMMAND);
   }

   public void back() throws Exception {
      this.now(new Target(this.user.getLastLocation()), TeleportCause.COMMAND);
   }

   public void home(Location loc, Trade chargeFor) throws Exception {
      this.teleport(new Target(loc), chargeFor, TeleportCause.COMMAND);
   }

   private class Target {
      private final Location location;
      private final String name;

      Target(Location location) {
         this.location = location;
         this.name = null;
      }

      Target(Player entity) {
         this.name = entity.getName();
         this.location = null;
      }

      public Location getLocation() {
         return this.name != null ? Teleport.this.ess.getServer().getPlayerExact(this.name).getLocation() : this.location;
      }
   }
}
