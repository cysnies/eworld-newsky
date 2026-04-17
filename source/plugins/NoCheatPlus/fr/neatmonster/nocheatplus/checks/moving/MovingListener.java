package fr.neatmonster.nocheatplus.checks.moving;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.CheckListener;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.combined.BedLeave;
import fr.neatmonster.nocheatplus.checks.combined.Combined;
import fr.neatmonster.nocheatplus.checks.combined.CombinedConfig;
import fr.neatmonster.nocheatplus.checks.combined.CombinedData;
import fr.neatmonster.nocheatplus.compat.BridgeHealth;
import fr.neatmonster.nocheatplus.components.IData;
import fr.neatmonster.nocheatplus.components.IHaveCheckType;
import fr.neatmonster.nocheatplus.components.INeedConfig;
import fr.neatmonster.nocheatplus.components.INotifyReload;
import fr.neatmonster.nocheatplus.components.IRemoveData;
import fr.neatmonster.nocheatplus.components.JoinLeaveListener;
import fr.neatmonster.nocheatplus.components.TickListener;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import fr.neatmonster.nocheatplus.logging.DebugUtil;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.utilities.BlockCache;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.TeleportUtil;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;
import fr.neatmonster.nocheatplus.utilities.build.BuildParameters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class MovingListener extends CheckListener implements TickListener, IRemoveData, IHaveCheckType, INotifyReload, INeedConfig, JoinLeaveListener {
   private final Plugin plugin = Bukkit.getPluginManager().getPlugin("NoCheatPlus");
   public final NoFall noFall = (NoFall)this.addCheck(new NoFall());
   private final CreativeFly creativeFly = (CreativeFly)this.addCheck(new CreativeFly());
   private final MorePackets morePackets = (MorePackets)this.addCheck(new MorePackets());
   private final MorePacketsVehicle morePacketsVehicle = (MorePacketsVehicle)this.addCheck(new MorePacketsVehicle());
   private final SurvivalFly survivalFly = (SurvivalFly)this.addCheck(new SurvivalFly());
   private final Passable passable = (Passable)this.addCheck(new Passable());
   private final BedLeave bedLeave = (BedLeave)this.addCheck(new BedLeave());
   private final List parkedInfo = new ArrayList(10);
   private final Map processingEvents = new HashMap();
   private final Set hoverTicks = new LinkedHashSet(30);
   private int hoverTicksStep = 5;
   private final Set normalVehicles = new HashSet();

   public static final boolean shouldCheckSurvivalFly(Player player, MovingData data, MovingConfig cc) {
      if (player.hasPermission("nocheatplus.checks.moving.creativefly")) {
         return false;
      } else if (cc.survivalFlyCheck && !NCPExemptionManager.isExempted(player, CheckType.MOVING_SURVIVALFLY) && !player.hasPermission("nocheatplus.checks.moving.survivalfly")) {
         return (cc.ignoreCreative || player.getGameMode() != GameMode.CREATIVE) && (cc.ignoreAllowFlight || !player.getAllowFlight());
      } else {
         return false;
      }
   }

   public static void handleIllegalMove(PlayerMoveEvent event, Player player, MovingData data) {
      boolean restored = false;
      PlayerLocation pLoc = new PlayerLocation(NCPAPIProvider.getNoCheatPlusAPI().getMCAccess(), (BlockCache)null);
      Location loc = player.getLocation();
      if (!restored && data.hasSetBack()) {
         Location setBack = data.getSetBack(loc);
         pLoc.set(setBack, player);
         if (!pLoc.isIllegal()) {
            event.setFrom(setBack);
            event.setTo(setBack);
            restored = true;
         } else {
            data.resetSetBack();
         }
      }

      if (!restored) {
         pLoc.set(loc, player);
         if (!pLoc.isIllegal()) {
            event.setFrom(loc);
            event.setTo(loc);
            restored = true;
         }
      }

      pLoc.cleanup();
      if (!restored && MovingConfig.getConfig(player).tempKickIllegal) {
         NCPAPIProvider.getNoCheatPlusAPI().denyLogin(player.getName(), 86400000L);
         LogUtil.logSevere("[NCP] could not restore location for " + player.getName() + " deny login for 24 hours");
      }

      CheckUtils.kickIllegalMove(player);
   }

   public MovingListener() {
      super(CheckType.MOVING);
   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.MONITOR
   )
   public void onBlockPlace(BlockPlaceEvent event) {
      Player player = event.getPlayer();
      if (!player.isInsideVehicle()) {
         Block block = event.getBlock();
         if (block != null) {
            int blockY = block.getY();
            Material mat = block.getType();
            MovingData data = MovingData.getData(player);
            if (this.creativeFly.isEnabled(player) || this.survivalFly.isEnabled(player)) {
               if (data.hasSetBack() && !((double)blockY + (double)1.0F < data.getSetBackY())) {
                  Location loc = player.getLocation();
                  if (Math.abs(loc.getX() - (double)0.5F - (double)block.getX()) <= (double)1.0F && Math.abs(loc.getZ() - (double)0.5F - (double)block.getZ()) <= (double)1.0F && loc.getY() - (double)blockY > (double)0.0F && loc.getY() - (double)blockY < (double)2.0F && (this.canJumpOffTop(mat.getId()) || BlockProperties.isLiquid(mat.getId()))) {
                     data.setSetBackY((double)blockY + (double)1.0F);
                     data.sfJumpPhase = 0;
                  }

               }
            }
         }
      }
   }

   private final boolean canJumpOffTop(int id) {
      return BlockProperties.isGround(id) || BlockProperties.isSolid(id);
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerBedEnter(PlayerBedEnterEvent event) {
      CombinedData.getData(event.getPlayer()).wasInBed = true;
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
      Player player = event.getPlayer();
      if (this.bedLeave.isEnabled(player) && this.bedLeave.checkBed(player)) {
         Location loc = player.getLocation();
         MovingData data = MovingData.getData(player);
         MovingConfig cc = MovingConfig.getConfig(player);
         Location target = null;
         boolean sfCheck = shouldCheckSurvivalFly(player, data, cc);
         if (sfCheck) {
            target = data.getSetBack(loc);
         }

         if (target == null) {
            target = loc;
         }

         if (target != null) {
            if (sfCheck && cc.sfFallDamage && this.noFall.isEnabled(player)) {
               double y = loc.getY();
               if (data.hasSetBack()) {
                  y = Math.min(y, data.getSetBackY());
               }

               this.noFall.checkDamage(player, data, y);
            }

            data.setTeleported(target);
            player.teleport(target, TeleportCause.PLUGIN);
         }
      } else {
         CombinedData.getData(player).wasInBed = false;
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
      Player player = event.getPlayer();
      MovingData data = MovingData.getData(player);
      data.clearFlyData();
      data.clearMorePacketsData();
      data.setSetBack(player.getLocation());
   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.MONITOR
   )
   public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
      if (event.getPlayer().getGameMode() == GameMode.CREATIVE || event.getNewGameMode() == GameMode.CREATIVE) {
         MovingData data = MovingData.getData(event.getPlayer());
         data.clearFlyData();
         data.clearMorePacketsData();
      }

   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.LOWEST
   )
   public void onPlayerMove(PlayerMoveEvent event) {
      Player player = event.getPlayer();
      String playerName = player.getName();
      this.processingEvents.put(playerName, event);
      MovingData data = MovingData.getData(player);
      if (player.isInsideVehicle()) {
         Entity vehicle = CheckUtils.getLastNonPlayerVehicle(player);
         data.wasInVehicle = true;
         data.sfHoverTicks = -1;
         data.removeAllVelocity();
         data.sfLowJump = false;
         if (vehicle != null && !this.normalVehicles.contains(vehicle.getType())) {
            this.onVehicleMove(vehicle, event.getFrom(), event.getFrom(), true);
         }

      } else if (player.isDead()) {
         data.sfHoverTicks = -1;
      } else if (player.isSleeping()) {
         data.sfHoverTicks = -1;
      } else {
         Location from = event.getFrom();
         Location to = event.getTo();
         if (from.getWorld().equals(to.getWorld())) {
            MoveInfo moveInfo;
            if (this.parkedInfo.isEmpty()) {
               moveInfo = new MoveInfo(this.mcAccess);
            } else {
               moveInfo = (MoveInfo)this.parkedInfo.remove(this.parkedInfo.size() - 1);
            }

            MovingConfig cc = MovingConfig.getConfig(player);
            moveInfo.set(player, from, to, cc.yOnGround);
            data.noFallAssumeGround = false;
            data.resetTeleported();
            if (cc.debug) {
               DebugUtil.outputMoveDebug(player, moveInfo.from, moveInfo.to, Math.max(cc.noFallyOnGround, cc.yOnGround), this.mcAccess);
            }

            if (!moveInfo.from.isIllegal() && !moveInfo.to.isIllegal()) {
               long time = System.currentTimeMillis();
               if (player.isSprinting() && player.getFoodLevel() > 5) {
                  data.timeSprinting = time;
               } else if (time < data.timeSprinting) {
                  data.timeSprinting = 0L;
               }

               PlayerLocation pFrom = moveInfo.from;
               PlayerLocation pTo = moveInfo.to;
               if (data.wasInVehicle) {
                  if (cc.debug) {
                     LogUtil.logWarning("[NoCheatPlus] VehicleExitEvent missing for: " + player.getName());
                  }

                  this.onPlayerVehicleLeave(player);
                  data.noFallSkipAirCheck = true;
                  data.sfLowJump = false;
                  data.clearNoFallData();
               }

               double jumpAmplifier = this.survivalFly.getJumpAmplifier(player);
               if (jumpAmplifier > data.jumpAmplifier) {
                  data.jumpAmplifier = jumpAmplifier;
               }

               data.removeInvalidVelocity(TickTask.getTick() - cc.velocityActivationTicks);
               data.velocityTick();
               if (data.verticalVelocity <= 0.09) {
                  ++data.verticalVelocityUsed;
                  --data.verticalVelocityCounter;
               } else if ((double)data.verticalVelocityCounter > (double)0.0F) {
                  ++data.verticalVelocityUsed;
                  data.verticalFreedom += data.verticalVelocity;
                  data.verticalVelocity = Math.max((double)0.0F, data.verticalVelocity - 0.09);
               } else if (data.verticalFreedom > 0.001) {
                  if (data.verticalVelocityUsed == 1 && data.verticalVelocity > (double)1.0F) {
                     data.verticalVelocityUsed = 0;
                     data.verticalVelocity = (double)0.0F;
                     data.verticalFreedom = (double)0.0F;
                  } else {
                     ++data.verticalVelocityUsed;
                     data.verticalFreedom *= 0.93;
                  }
               }

               Location loc = !cc.noFallCheck && !cc.passableCheck ? null : player.getLocation();
               Location newTo = null;
               boolean mightSkipNoFall = false;
               if (newTo == null && cc.passableCheck && !NCPExemptionManager.isExempted(player, CheckType.MOVING_PASSABLE) && !player.hasPermission("nocheatplus.checks.moving.passable")) {
                  newTo = this.passable.check(player, loc, pFrom, pTo, data, cc);
                  if (newTo != null) {
                     mightSkipNoFall = true;
                  }
               }

               boolean checkCf;
               boolean checkSf;
               if (player.hasPermission("nocheatplus.checks.moving.creativefly")) {
                  checkSf = false;
                  checkCf = false;
               } else if ((cc.ignoreCreative || player.getGameMode() != GameMode.CREATIVE) && (cc.ignoreAllowFlight || !player.getAllowFlight()) && cc.survivalFlyCheck && !NCPExemptionManager.isExempted(player, CheckType.MOVING_SURVIVALFLY) && !player.hasPermission("nocheatplus.checks.moving.survivalfly")) {
                  checkCf = false;
                  checkSf = true;
               } else if (cc.creativeFlyCheck && !NCPExemptionManager.isExempted(player, CheckType.MOVING_CREATIVEFLY)) {
                  checkCf = true;
                  checkSf = false;
               } else {
                  checkSf = false;
                  checkCf = false;
               }

               if (checkSf) {
                  double maxYNoFall = Math.max(cc.noFallyOnGround, cc.yOnGround);
                  pFrom.collectBlockFlags(maxYNoFall);
                  if (pFrom.isSamePos(pTo)) {
                     pTo.prepare(pFrom);
                  } else {
                     pTo.collectBlockFlags(maxYNoFall);
                  }

                  if (newTo == null) {
                     newTo = this.survivalFly.check(player, pFrom, pTo, data, cc, time);
                  }

                  boolean checkNf = cc.noFallCheck && !NCPExemptionManager.isExempted(player, CheckType.MOVING_NOFALL) && !player.hasPermission("nocheatplus.checks.moving.nofall");
                  if (newTo == null) {
                     if (cc.sfHoverCheck && !data.toWasReset && !pTo.isOnGround()) {
                        this.hoverTicks.add(playerName);
                        data.sfHoverTicks = 0;
                     } else {
                        data.sfHoverTicks = -1;
                     }

                     if (checkNf) {
                        this.noFall.check(player, loc, pFrom, pTo, data, cc);
                     }
                  } else if (checkNf && cc.sfFallDamage) {
                     if (mightSkipNoFall && !pFrom.isOnGround() && !pFrom.isResetCond()) {
                        mightSkipNoFall = false;
                     }

                     if (!mightSkipNoFall) {
                        this.noFall.checkDamage(player, data, Math.min(Math.min(from.getY(), to.getY()), loc.getY()));
                     }
                  }
               } else if (checkCf) {
                  newTo = this.creativeFly.check(player, pFrom, pTo, data, cc, time);
                  data.sfHoverTicks = -1;
                  data.sfLowJump = false;
               } else {
                  data.clearFlyData();
               }

               if (newTo == null && cc.morePacketsCheck && !NCPExemptionManager.isExempted(player, CheckType.MOVING_MOREPACKETS) && !player.hasPermission("nocheatplus.checks.moving.morepackets")) {
                  newTo = this.morePackets.check(player, pFrom, pTo, data, cc);
               } else {
                  data.clearMorePacketsData();
               }

               if (newTo != null) {
                  data.clearAccounting();
                  data.sfJumpPhase = 0;
                  data.sfLastYDist = Double.MAX_VALUE;
                  data.toWasReset = false;
                  data.fromWasReset = false;
                  event.setTo(newTo);
                  data.setTeleported(newTo);
                  if (cc.debug) {
                     System.out.println(player.getName() + " set back to: " + newTo.getWorld() + StringUtil.fdec3.format(newTo.getX()) + ", " + StringUtil.fdec3.format(newTo.getY()) + ", " + StringUtil.fdec3.format(newTo.getZ()));
                  }
               }

               data.fromX = from.getX();
               data.fromY = from.getY();
               data.fromZ = from.getZ();
               data.toX = to.getX();
               data.toY = to.getY();
               data.toZ = to.getZ();
               moveInfo.cleanup();
               this.parkedInfo.add(moveInfo);
            } else {
               handleIllegalMove(event, player, data);
               moveInfo.cleanup();
               this.parkedInfo.add(moveInfo);
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = false
   )
   public final void onPlayerMoveMonitor(PlayerMoveEvent event) {
      long now = System.currentTimeMillis();
      Player player = event.getPlayer();
      if (this.processingEvents.remove(player.getName()) != null) {
         if (!player.isDead() && !player.isSleeping()) {
            CombinedData data = CombinedData.getData(player);
            data.lastMoveTime = now;
            Location from = event.getFrom();
            String fromWorldName = from.getWorld().getName();
            if (!event.isCancelled()) {
               Location to = event.getTo();
               String toWorldName = to.getWorld().getName();
               Combined.feedYawRate(player, to.getYaw(), now, toWorldName, data);
               if (!player.isInsideVehicle() && fromWorldName.equals(toWorldName)) {
                  MovingData.getData(player).setTo(to);
               } else {
                  MovingData.getData(player).resetPositions(to);
               }
            } else {
               Combined.feedYawRate(player, from.getYaw(), now, fromWorldName, data);
               MovingData.getData(player).resetPositions(from);
            }

         }
      }
   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.MONITOR
   )
   public void onPlayerPortal(PlayerPortalEvent event) {
      MovingData data = MovingData.getData(event.getPlayer());
      data.clearFlyData();
      data.clearMorePacketsData();
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerRespawn(PlayerRespawnEvent event) {
      Player player = event.getPlayer();
      MovingData data = MovingData.getData(player);
      data.clearFlyData();
      data.clearMorePacketsData();
      data.resetSetBack();
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerDeath(PlayerDeathEvent event) {
      Player player = event.getEntity();
      MovingData data = MovingData.getData(player);
      data.clearFlyData();
      data.clearMorePacketsData();
      data.setSetBack(player.getLocation());
   }

   @EventHandler(
      ignoreCancelled = false,
      priority = EventPriority.HIGHEST
   )
   public void onPlayerTeleport(PlayerTeleportEvent event) {
      Player player = event.getPlayer();
      MovingData data = MovingData.getData(player);
      Location teleported = data.getTeleported();
      Location to = event.getTo();
      Location ref;
      if (teleported != null && teleported.equals(to)) {
         if (event.isCancelled()) {
            event.setCancelled(false);
            event.setTo(teleported);
            event.setFrom(teleported);
            ref = teleported;
         } else {
            ref = to;
         }

         data.onSetBack(teleported);
      } else {
         MovingConfig cc = MovingConfig.getConfig(player);
         if (to == null || event.isCancelled()) {
            data.resetTeleported();
            if (cc.debug && BuildParameters.debugLevel > 0) {
               System.out.println(player.getName() + " TP (cancelled): " + to);
            }

            return;
         }

         boolean smallRange = false;
         boolean cancel = false;
         double margin = 0.67;
         Location from = event.getFrom();
         PlayerTeleportEvent.TeleportCause cause = event.getCause();
         if (cause == TeleportCause.UNKNOWN) {
            if (from != null && from.getWorld().equals(to.getWorld())) {
               if (TrigUtil.distance(from, to) < 0.67) {
                  smallRange = true;
               } else if (data.toX != Double.MAX_VALUE && data.hasSetBack()) {
                  Location setBack = data.getSetBack(to);
                  if (TrigUtil.distance(to.getX(), to.getY(), to.getZ(), setBack.getX(), setBack.getY(), setBack.getZ()) < 0.67) {
                     smallRange = true;
                  }
               }
            }
         } else if (cause == TeleportCause.ENDER_PEARL && CombinedConfig.getConfig(player).enderPearlCheck && !BlockProperties.isPassable(to)) {
            cancel = true;
         }

         if (cancel) {
            if (data.hasSetBack() && !data.hasSetBackWorldChanged(to)) {
               ref = data.getSetBack(to);
               event.setTo(ref);
            } else {
               ref = from;
               event.setCancelled(true);
            }
         } else if (smallRange) {
            ref = to;
         } else {
            ref = to;
            double fallDistance = (double)data.noFallFallDistance;
            data.clearMorePacketsData();
            data.clearFlyData();
            data.resetPositions(to);
            data.setSetBack(to);
            if (fallDistance > (double)1.0F && fallDistance - (double)player.getFallDistance() > (double)0.0F && !cc.noFallTpReset) {
               player.setFallDistance((float)fallDistance);
            }

            if (event.getCause() == TeleportCause.ENDER_PEARL) {
               data.noFallSkipAirCheck = true;
            }

            data.sfHoverTicks = -1;
         }

         if (cc.debug && BuildParameters.debugLevel > 0) {
            System.out.println(player.getName() + " TP" + (smallRange ? " (small-range)" : "") + (cancel ? " (cancelled)" : "") + ": " + to);
         }
      }

      Combined.resetYawRate(player, ref.getYaw(), System.currentTimeMillis(), true);
      data.resetTeleported();
      this.processingEvents.remove(player.getName());
   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.MONITOR
   )
   public void onPlayerVelocity(PlayerVelocityEvent event) {
      Player player = event.getPlayer();
      MovingData data = MovingData.getData(player);
      if (player.isInsideVehicle()) {
         data.removeAllVelocity();
      } else {
         MovingConfig cc = MovingConfig.getConfig(player);
         int tick = TickTask.getTick();
         data.removeInvalidVelocity(tick - cc.velocityActivationTicks);
         Vector velocity = event.getVelocity();
         if (cc.debug) {
            System.out.println(event.getPlayer().getName() + " new velocity: " + velocity);
         }

         double newVal = velocity.getY();
         boolean used = false;
         if (newVal >= (double)0.0F) {
            used = true;
            if (data.verticalFreedom <= 0.001 && data.verticalVelocityCounter >= 0) {
               data.verticalVelocity = (double)0.0F;
            }

            data.verticalVelocity += newVal;
            data.verticalFreedom += data.verticalVelocity;
            data.verticalVelocityCounter = Math.min(100, Math.max(data.verticalVelocityCounter, cc.velocityGraceTicks) + 1 + (int)Math.round(newVal * (double)10.0F));
            data.verticalVelocityUsed = 0;
         }

         newVal = Math.sqrt(velocity.getX() * velocity.getX() + velocity.getZ() * velocity.getZ());
         if (newVal > (double)0.0F) {
            used = true;
            Velocity vel = new Velocity(tick, newVal, cc.velocityActivationCounter, Math.max(20, 1 + (int)Math.round(newVal * (double)10.0F)));
            data.addHorizontalVelocity(vel);
         }

         if (used) {
            data.sfDirty = true;
            data.sfNoLowJump = true;
         }

      }
   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.MONITOR
   )
   public void onVehicleMove(VehicleMoveEvent event) {
      Vehicle vehicle = event.getVehicle();
      EntityType entityType = vehicle.getType();
      if (!this.normalVehicles.contains(entityType)) {
         this.normalVehicles.add(entityType);
         if (MovingConfig.getConfig(vehicle.getWorld().getName()).debug) {
            System.out.println("[NoCheatPlus] VehicleMoveEvent fired for: " + entityType);
         }
      }

      if (vehicle.getVehicle() == null) {
         this.onVehicleMove(vehicle, event.getFrom(), event.getTo(), false);
      }
   }

   public void onVehicleMove(Entity vehicle, Location from, Location to, boolean fake) {
      Player player = CheckUtils.getFirstPlayerPassenger(vehicle);
      if (player != null) {
         if (from.getWorld().equals(to.getWorld())) {
            Location newTo = null;
            final MovingData data = MovingData.getData(player);
            data.sfNoLowJump = true;
            MovingConfig cc = MovingConfig.getConfig(player);
            if (cc.noFallVehicleReset) {
               data.noFallSkipAirCheck = true;
               data.sfLowJump = false;
               data.clearNoFallData();
            }

            if (this.morePacketsVehicle.isEnabled(player)) {
               newTo = this.morePacketsVehicle.check(player, from, to, data, cc);
            } else {
               data.clearMorePacketsData();
            }

            if (newTo != null && data.morePacketsVehicleTaskId == -1) {
               data.morePacketsVehicleTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, (new Runnable() {
                  private Entity vehicle;
                  private Player player;
                  private Location location;

                  public void run() {
                     data.morePacketsVehicleTaskId = -1;

                     try {
                        MovingData.getData(this.player).setTeleported(this.location);
                        TeleportUtil.teleport(this.vehicle, this.player, this.location, MovingConfig.getConfig(this.player).debug);
                     } catch (Throwable t) {
                        LogUtil.logSevere(t);
                     }

                  }

                  public Runnable set(Entity vehicle, Player player, Location location) {
                     this.vehicle = vehicle;
                     this.player = player;
                     this.location = location;
                     return this;
                  }
               }).set(vehicle, player, newTo), 1L);
            }

         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = false
   )
   public void onEntityDamage(EntityDamageEvent event) {
      if (event.getCause() == DamageCause.FALL) {
         Entity entity = event.getEntity();
         if (entity instanceof Player) {
            Player player = (Player)entity;
            MovingData data = MovingData.getData(player);
            MovingConfig cc = MovingConfig.getConfig(player);
            if (!event.isCancelled() && shouldCheckSurvivalFly(player, data, cc) && this.noFall.isEnabled(player)) {
               Location loc = player.getLocation();
               boolean allowReset = true;
               if (!data.noFallSkipAirCheck) {
                  MoveInfo moveInfo;
                  if (this.parkedInfo.isEmpty()) {
                     moveInfo = new MoveInfo(this.mcAccess);
                  } else {
                     moveInfo = (MoveInfo)this.parkedInfo.remove(this.parkedInfo.size() - 1);
                  }

                  moveInfo.set(player, loc, (Location)null, cc.noFallyOnGround);
                  PlayerLocation pLoc = moveInfo.from;
                  moveInfo.from.collectBlockFlags(cc.noFallyOnGround);
                  data.noFallFallDistance = (float)((double)data.noFallFallDistance + (double)1.0F);
                  if (!pLoc.isOnGround((double)1.0F, 0.3, 0.1) && !pLoc.isResetCond() && !pLoc.isAboveLadder() && !pLoc.isAboveStairs()) {
                     ++data.noFallVL;
                     if (this.noFall.executeActions(player, data.noFallVL, (double)1.0F, cc.noFallActions, true) && data.hasSetBack()) {
                        allowReset = false;
                     }
                  } else {
                     data.vDistAcc.clear();
                  }

                  moveInfo.cleanup();
                  this.parkedInfo.add(moveInfo);
               }

               float fallDistance = player.getFallDistance();
               double damage = BridgeHealth.getDamage(event);
               float yDiff = (float)(data.noFallMaxY - loc.getY());
               if (cc.debug) {
                  System.out.println(player.getName() + " damage(FALL): " + damage + " / dist=" + player.getFallDistance() + " nf=" + data.noFallFallDistance + " yDiff=" + yDiff);
               }

               double maxD = NoFall.getDamage(Math.max(yDiff, Math.max(data.noFallFallDistance, fallDistance))) + (double)(allowReset ? 0 : 3);
               if (maxD > damage) {
                  BridgeHealth.setDamage(event, maxD);
                  if (cc.debug) {
                     System.out.println(player.getName() + " Adjust fall damage to: " + maxD);
                  }
               }

               if (allowReset) {
                  data.clearNoFallData();
               } else {
                  if (cc.noFallViolationReset) {
                     data.clearNoFallData();
                  }

                  if (cc.sfHoverCheck && data.sfHoverTicks < 0) {
                     data.sfHoverTicks = 0;
                     this.hoverTicks.add(player.getName());
                  }
               }

            } else {
               data.clearNoFallData();
            }
         }
      }
   }

   public void playerJoins(Player player) {
      MovingData data = MovingData.getData(player);
      data.clearMorePacketsData();
      data.removeAllVelocity();
      Location loc = player.getLocation();
      if (loc == null) {
         data.clearFlyData();
      } else if (!data.hasSetBack()) {
         data.setSetBack(loc);
      } else if (data.hasSetBackWorldChanged(loc)) {
         data.clearFlyData();
         data.setSetBack(loc);
      }

      if (data.fromX == Double.MAX_VALUE && data.toX == Double.MAX_VALUE) {
         data.resetPositions(loc);
      }

      data.vDistAcc.clear();
      data.toWasReset = false;
      data.fromWasReset = false;
      MovingConfig cc = MovingConfig.getConfig(player);
      if (cc.sfHoverCheck) {
         data.sfHoverTicks = 0;
         data.sfHoverLoginTicks = cc.sfHoverLoginTicks;
         this.hoverTicks.add(player.getName());
      } else {
         data.sfHoverLoginTicks = 0;
         data.sfHoverTicks = -1;
      }

      if (cc.loadChunksOnJoin) {
         int loaded = BlockCache.ensureChunksLoaded(loc.getWorld(), loc.getX(), loc.getZ(), (double)3.0F);
         if (loaded > 0 && cc.debug && BuildParameters.debugLevel > 0) {
            LogUtil.logInfo("[NoCheatPlus] Player join: Loaded " + loaded + " chunk" + (loaded == 1 ? "" : "s") + " for the world " + loc.getWorld().getName() + " for player: " + player.getName());
         }
      }

   }

   public void playerLeaves(Player player) {
      this.survivalFly.setReallySneaking(player, false);
      this.noFall.onLeave(player);
      MovingData data = MovingData.getData(player);
      data.onPlayerLeave();
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onWorldunload(WorldUnloadEvent event) {
      MovingData.onWorldUnload(event.getWorld());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onVehicleExit(VehicleExitEvent event) {
      Entity entity = event.getExited();
      if (entity instanceof Player) {
         this.onPlayerVehicleLeave((Player)entity);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onVehicleDestroy(VehicleDestroyEvent event) {
      Entity entity = event.getVehicle().getPassenger();
      if (entity instanceof Player) {
         this.onPlayerVehicleLeave((Player)entity);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public final void onPlayerVehicleEnter(VehicleEnterEvent event) {
      Entity entity = event.getEntered();
      if (entity instanceof Player) {
         Player player = (Player)entity;
         MovingData data = MovingData.getData(player);
         data.removeAllVelocity();
      }
   }

   private final void onPlayerVehicleLeave(Player player) {
      MovingData data = MovingData.getData(player);
      data.wasInVehicle = false;
      Location loc = player.getLocation();
      if (BlockProperties.isLiquid(loc.getBlock().getTypeId())) {
         loc.setY((double)Location.locToBlock(loc.getY()) + (double)1.25F);
      }

      data.resetPositions(loc);
      data.setSetBack(loc);
      data.removeAllVelocity();
      data.addHorizontalVelocity(new Velocity(0.9, 1, 1));
      data.verticalVelocityCounter = 1;
      data.verticalFreedom = 1.2;
      data.verticalVelocity = 0.15;
      data.verticalVelocityUsed = 0;
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
      this.survivalFly.setReallySneaking(event.getPlayer(), event.isSneaking());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
      if (!event.isSprinting()) {
         MovingData.getData(event.getPlayer()).timeSprinting = 0L;
      }

   }

   public final void onTick(int tick, long timeLast) {
      if (tick % this.hoverTicksStep == 0) {
         MoveInfo info;
         if (this.parkedInfo.isEmpty()) {
            info = new MoveInfo(this.mcAccess);
         } else {
            info = (MoveInfo)this.parkedInfo.remove(this.parkedInfo.size() - 1);
         }

         List<String> rem = new ArrayList(this.hoverTicks.size());

         for(String playerName : this.hoverTicks) {
            Player player = DataManager.getPlayerExact(playerName);
            if (player != null && player.isOnline()) {
               MovingData data = MovingData.getData(player);
               if (player.isDead() || player.isSleeping() || player.isInsideVehicle()) {
                  data.sfHoverTicks = -1;
               }

               if (data.sfHoverTicks < 0) {
                  data.sfHoverLoginTicks = 0;
                  rem.add(playerName);
               } else if (data.sfHoverLoginTicks > 0) {
                  --data.sfHoverLoginTicks;
               } else {
                  MovingConfig cc = MovingConfig.getConfig(player);
                  if (!cc.sfHoverCheck) {
                     rem.add(playerName);
                     data.sfHoverTicks = -1;
                  } else {
                     data.sfHoverTicks += this.hoverTicksStep;
                     if (data.sfHoverTicks >= cc.sfHoverTicks && this.checkHover(player, data, cc, info)) {
                        rem.add(playerName);
                     }
                  }
               }
            } else {
               rem.add(playerName);
            }
         }

         info.cleanup();
         this.parkedInfo.add(info);
         this.hoverTicks.removeAll(rem);
         rem.clear();
      }
   }

   private final boolean checkHover(Player player, MovingData data, MovingConfig cc, MoveInfo info) {
      Location loc = player.getLocation();
      info.set(player, loc, (Location)null, cc.yOnGround);
      int loaded = info.from.ensureChunksLoaded();
      if (loaded > 0 && cc.debug && BuildParameters.debugLevel > 0) {
         LogUtil.logInfo("[NoCheatPlus] Hover check: Needed to load " + loaded + " chunk" + (loaded == 1 ? "" : "s") + " for the world " + loc.getWorld().getName() + " around " + loc.getBlockX() + "," + loc.getBlockZ() + " in order to check player: " + player.getName());
      }

      boolean res;
      if (!info.from.isOnGround() && !info.from.isResetCond() && !info.from.isAboveLadder() && !info.from.isAboveStairs()) {
         if (data.sfHoverTicks > cc.sfHoverTicks) {
            if (shouldCheckSurvivalFly(player, data, cc)) {
               this.handleHoverViolation(player, loc, cc, data);
               res = false;
               data.sfHoverTicks = 0;
            } else {
               res = false;
               data.sfHoverTicks = 0;
            }
         } else {
            res = false;
         }
      } else {
         res = true;
         data.sfHoverTicks = 0;
      }

      info.cleanup();
      return res;
   }

   private final void handleHoverViolation(Player player, Location loc, MovingConfig cc, MovingData data) {
      if (cc.sfHoverFallDamage && this.noFall.isEnabled(player)) {
         this.noFall.checkDamage(player, data, loc.getY());
      }

      this.survivalFly.handleHoverViolation(player, loc, cc, data);
   }

   public CheckType getCheckType() {
      return CheckType.MOVING_SURVIVALFLY;
   }

   public IData removeData(String playerName) {
      this.hoverTicks.remove(playerName);
      return null;
   }

   public void removeAllData() {
      this.hoverTicks.clear();
      this.parkedInfo.clear();
   }

   public void onReload() {
      for(MoveInfo info : this.parkedInfo) {
         info.cleanup();
      }

      this.parkedInfo.clear();
      this.hoverTicksStep = Math.max(1, ConfigManager.getConfigFile().getInt("checks.moving.survivalfly.hover.step"));
   }
}
