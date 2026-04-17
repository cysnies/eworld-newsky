package fr.neatmonster.nocheatplus.checks.moving;

import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.ACheckConfig;
import fr.neatmonster.nocheatplus.checks.access.CheckConfigFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckConfig;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

public class MovingConfig extends ACheckConfig {
   public static final CheckConfigFactory factory = new CheckConfigFactory() {
      public final ICheckConfig getConfig(Player player) {
         return MovingConfig.getConfig(player);
      }
   };
   private static final Map worldsMap = new HashMap();
   public final boolean ignoreCreative;
   public final boolean ignoreAllowFlight;
   public final boolean creativeFlyCheck;
   public final int creativeFlyHorizontalSpeed;
   public final int creativeFlyMaxHeight;
   public final int creativeFlyVerticalSpeed;
   public final ActionList creativeFlyActions;
   public final boolean morePacketsCheck;
   public final ActionList morePacketsActions;
   public final boolean morePacketsVehicleCheck;
   public final ActionList morePacketsVehicleActions;
   public final boolean noFallCheck;
   public final boolean noFallDealDamage;
   public final boolean noFallViolationReset;
   public final boolean noFallTpReset;
   public final boolean noFallVehicleReset;
   public final boolean noFallAntiCriticals;
   public final ActionList noFallActions;
   public final boolean passableCheck;
   public final boolean passableRayTracingCheck;
   public final boolean passableRayTracingBlockChangeOnly;
   public final boolean passableRayTracingVclipOnly;
   public final ActionList passableActions;
   public final boolean survivalFlyCheck;
   public final int survivalFlyBlockingSpeed;
   public final int survivalFlySneakingSpeed;
   public final int survivalFlySpeedingSpeed;
   public final int survivalFlySprintingSpeed;
   public final int survivalFlySwimmingSpeed;
   public final int survivalFlyWalkingSpeed;
   public final boolean survivalFlyCobwebHack;
   public final boolean survivalFlyAccountingH;
   public final boolean survivalFlyAccountingV;
   public final boolean sfFallDamage;
   public final long survivalFlyVLFreeze;
   public final ActionList survivalFlyActions;
   public final boolean sfHoverCheck;
   public final int sfHoverTicks;
   public final int sfHoverLoginTicks;
   public final boolean sfHoverFallDamage;
   public final double sfHoverViolation;
   public final int velocityGraceTicks;
   public final int velocityActivationCounter;
   public final int velocityActivationTicks;
   public final boolean velocityStrictInvalidation;
   public final double noFallyOnGround;
   public final double yOnGround;
   public final double yStep;
   public final boolean tempKickIllegal;
   public final boolean loadChunksOnJoin;
   public final long sprintingGrace;

   public static void clear() {
      worldsMap.clear();
   }

   public static MovingConfig getConfig(Player player) {
      return getConfig(player.getWorld().getName());
   }

   public static MovingConfig getConfig(String worldName) {
      MovingConfig cc = (MovingConfig)worldsMap.get(worldName);
      if (cc != null) {
         return cc;
      } else {
         MovingConfig ccNew = new MovingConfig(ConfigManager.getConfigFile(worldName));
         worldsMap.put(worldName, ccNew);
         return ccNew;
      }
   }

   public MovingConfig(ConfigFile config) {
      super(config, "checks.moving.");
      this.ignoreCreative = config.getBoolean("checks.moving.creativefly.ignorecreative");
      this.ignoreAllowFlight = config.getBoolean("checks.moving.creativefly.ignoreallowflight");
      this.creativeFlyCheck = config.getBoolean("checks.moving.creativefly.active");
      this.creativeFlyHorizontalSpeed = config.getInt("checks.moving.creativefly.horizontalspeed");
      this.creativeFlyMaxHeight = config.getInt("checks.moving.creativefly.maxheight");
      this.creativeFlyVerticalSpeed = config.getInt("checks.moving.creativefly.verticalspeed");
      this.creativeFlyActions = (ActionList)config.getOptimizedActionList("checks.moving.creativefly.actions", "nocheatplus.checks.moving.creativefly");
      this.morePacketsCheck = config.getBoolean("checks.moving.morepackets.active");
      this.morePacketsActions = (ActionList)config.getOptimizedActionList("checks.moving.morepackets.actions", "nocheatplus.checks.moving.morepackets");
      this.morePacketsVehicleCheck = config.getBoolean("checks.moving.morepacketsvehicle.active");
      this.morePacketsVehicleActions = (ActionList)config.getOptimizedActionList("checks.moving.morepacketsvehicle.actions", "nocheatplus.checks.moving.morepackets");
      this.noFallCheck = config.getBoolean("checks.moving.nofall.active");
      this.noFallDealDamage = config.getBoolean("checks.moving.nofall.dealdamage");
      this.noFallViolationReset = config.getBoolean("checks.moving.nofall.resetonviolation");
      this.noFallTpReset = config.getBoolean("checks.moving.nofall.resetonteleport");
      this.noFallVehicleReset = config.getBoolean("checks.moving.nofall.resetonvehicle");
      this.noFallAntiCriticals = config.getBoolean("checks.moving.nofall.anticriticals");
      this.noFallActions = (ActionList)config.getOptimizedActionList("checks.moving.nofall.actions", "nocheatplus.checks.moving.nofall");
      this.passableCheck = config.getBoolean("checks.moving.passable.active");
      this.passableRayTracingCheck = config.getBoolean("checks.moving.passable.raytracing.active");
      this.passableRayTracingBlockChangeOnly = config.getBoolean("checks.moving.passable.raytracing.blockchangeonly");
      this.passableRayTracingVclipOnly = config.getBoolean("checks.moving.passable.raytracing.vcliponly");
      this.passableActions = (ActionList)config.getOptimizedActionList("checks.moving.passable.actions", "nocheatplus.checks.moving.passable");
      this.survivalFlyCheck = config.getBoolean("checks.moving.survivalfly.active");
      this.survivalFlyBlockingSpeed = config.getInt("checks.moving.survivalfly.blockingspeed", 100);
      this.survivalFlySneakingSpeed = config.getInt("checks.moving.survivalfly.sneakingspeed", 100);
      this.survivalFlySpeedingSpeed = config.getInt("checks.moving.survivalfly.speedingspeed", 200);
      this.survivalFlySprintingSpeed = config.getInt("checks.moving.survivalfly.sprintingspeed", 100);
      this.survivalFlySwimmingSpeed = config.getInt("checks.moving.survivalfly.swimmingspeed", 100);
      this.survivalFlyWalkingSpeed = config.getInt("checks.moving.survivalfly.walkingspeed", 100);
      this.survivalFlyCobwebHack = config.getBoolean("checks.moving.survivalfly.cobwebhack", true);
      this.survivalFlyAccountingH = config.getBoolean("checks.moving.survivalfly.extended.horizontal-accounting", false);
      this.survivalFlyAccountingV = config.getBoolean("checks.moving.survivalfly.extended.vertical-accounting");
      this.sfFallDamage = config.getBoolean("checks.moving.survivalfly.falldamage");
      this.survivalFlyVLFreeze = config.getLong("checks.moving.survivalfly.vlfreeze", 2000L);
      this.survivalFlyActions = (ActionList)config.getOptimizedActionList("checks.moving.survivalfly.actions", "nocheatplus.checks.moving.survivalfly");
      this.sfHoverCheck = config.getBoolean("checks.moving.survivalfly.hover.active");
      this.sfHoverTicks = config.getInt("checks.moving.survivalfly.hover.ticks");
      this.sfHoverLoginTicks = Math.max(0, config.getInt("checks.moving.survivalfly.hover.loginticks"));
      this.sfHoverFallDamage = config.getBoolean("checks.moving.survivalfly.hover.falldamage");
      this.sfHoverViolation = config.getDouble("checks.moving.survivalfly.hover.sfviolation");
      this.velocityGraceTicks = config.getInt("checks.moving.velocity.graceticks");
      this.velocityActivationCounter = config.getInt("checks.moving.velocity.activationcounter");
      this.velocityActivationTicks = config.getInt("checks.moving.velocity.activationticks");
      this.velocityStrictInvalidation = config.getBoolean("checks.moving.velocity.strictinvalidation");
      this.yOnGround = config.getDouble("checks.moving.yonground", 0.001, (double)2.0F, 0.0626);
      this.noFallyOnGround = config.getDouble("checks.moving.nofall.yonground", 0.001, (double)2.0F, this.yOnGround);
      this.yStep = config.getDouble("checks.moving.survivalfly.ystep", 0.001, 0.45, 0.1);
      this.tempKickIllegal = config.getBoolean("checks.moving.tempkickillegal");
      this.loadChunksOnJoin = config.getBoolean("checks.moving.loadchunks.join");
      this.sprintingGrace = Math.max(0L, (long)(config.getDouble("checks.moving.sprintinggrace") * (double)1000.0F));
   }

   public final boolean isEnabled(CheckType checkType) {
      switch (checkType) {
         case MOVING_NOFALL:
            return this.noFallCheck;
         case MOVING_SURVIVALFLY:
            return this.survivalFlyCheck;
         case MOVING_PASSABLE:
            return this.passableCheck;
         case MOVING_MOREPACKETS:
            return this.morePacketsCheck;
         case MOVING_MOREPACKETSVEHICLE:
            return this.morePacketsVehicleCheck;
         case MOVING_CREATIVEFLY:
            return this.creativeFlyCheck;
         default:
            return true;
      }
   }
}
