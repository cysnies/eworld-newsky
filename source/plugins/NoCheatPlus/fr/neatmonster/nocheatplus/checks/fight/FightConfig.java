package fr.neatmonster.nocheatplus.checks.fight;

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

public class FightConfig extends ACheckConfig {
   public static final CheckConfigFactory factory = new CheckConfigFactory() {
      public final ICheckConfig getConfig(Player player) {
         return FightConfig.getConfig(player);
      }
   };
   private static final Map worldsMap = new HashMap();
   public final boolean angleCheck;
   public final int angleThreshold;
   public final ActionList angleActions;
   public final boolean criticalCheck;
   public final double criticalFallDistance;
   public final double criticalVelocity;
   public final ActionList criticalActions;
   public final boolean directionCheck;
   public final boolean directionStrict;
   public final long directionPenalty;
   public final ActionList directionActions;
   public final boolean fastHealCheck;
   public final long fastHealInterval;
   public final long fastHealBuffer;
   public final ActionList fastHealActions;
   public final boolean godModeCheck;
   public final long godModeLagMinAge;
   public final long godModeLagMaxAge;
   public final ActionList godModeActions;
   public final boolean knockbackCheck;
   public final long knockbackInterval;
   public final ActionList knockbackActions;
   public final boolean noSwingCheck;
   public final ActionList noSwingActions;
   public final boolean reachCheck;
   public final long reachPenalty;
   public final boolean reachPrecision;
   public final boolean reachReduce;
   public final double reachSurvivalDistance;
   public final double reachReduceDistance;
   public final double reachReduceStep;
   public final ActionList reachActions;
   public final boolean selfHitCheck;
   public final ActionList selfHitActions;
   public final boolean speedCheck;
   public final int speedLimit;
   public final int speedBuckets;
   public final long speedBucketDur;
   public final float speedBucketFactor;
   public final int speedShortTermLimit;
   public final int speedShortTermTicks;
   public final ActionList speedActions;
   public final boolean yawRateCheck;
   public final boolean cancelDead;

   public static void clear() {
      worldsMap.clear();
   }

   public static FightConfig getConfig(Player player) {
      if (!worldsMap.containsKey(player.getWorld().getName())) {
         worldsMap.put(player.getWorld().getName(), new FightConfig(ConfigManager.getConfigFile(player.getWorld().getName())));
      }

      return (FightConfig)worldsMap.get(player.getWorld().getName());
   }

   public FightConfig(ConfigFile data) {
      super(data, "checks.fight.");
      this.angleCheck = data.getBoolean("checks.fight.angle.active");
      this.angleThreshold = data.getInt("checks.fight.angle.threshold");
      this.angleActions = (ActionList)data.getOptimizedActionList("checks.fight.angle.actions", "nocheatplus.checks.fight.angle");
      this.criticalCheck = data.getBoolean("checks.fight.critical.active");
      this.criticalFallDistance = data.getDouble("checks.fight.critical.falldistance");
      this.criticalVelocity = data.getDouble("checks.fight.critical.velocity");
      this.criticalActions = (ActionList)data.getOptimizedActionList("checks.fight.critical.actions", "nocheatplus.checks.fight.critical");
      this.directionCheck = data.getBoolean("checks.fight.direction.active");
      this.directionStrict = data.getBoolean("checks.fight.direction.strict");
      this.directionPenalty = data.getLong("checks.fight.direction.penalty");
      this.directionActions = (ActionList)data.getOptimizedActionList("checks.fight.direction.actions", "nocheatplus.checks.fight.direction");
      this.fastHealCheck = data.getBoolean("checks.fight.fastheal.active");
      this.fastHealInterval = data.getLong("checks.fight.fastheal.interval");
      this.fastHealBuffer = data.getLong("checks.fight.fastheal.buffer");
      this.fastHealActions = (ActionList)data.getOptimizedActionList("checks.fight.fastheal.actions", "nocheatplus.checks.fight.fastheal");
      this.godModeCheck = data.getBoolean("checks.fight.godmode.active");
      this.godModeLagMinAge = data.getLong("checks.fight.godmode.minage");
      this.godModeLagMaxAge = data.getLong("checks.fight.godmode.maxage");
      this.godModeActions = (ActionList)data.getOptimizedActionList("checks.fight.godmode.actions", "nocheatplus.checks.fight.godmode");
      this.knockbackCheck = data.getBoolean("checks.fight.knockback.active");
      this.knockbackInterval = data.getLong("checks.fight.knockback.interval");
      this.knockbackActions = (ActionList)data.getOptimizedActionList("checks.fight.knockback.actions", "nocheatplus.checks.fight.knockback");
      this.noSwingCheck = data.getBoolean("checks.fight.noswing.active");
      this.noSwingActions = (ActionList)data.getOptimizedActionList("checks.fight.noswing.actions", "nocheatplus.checks.fight.noswing");
      this.reachCheck = data.getBoolean("checks.fight.reach.active");
      this.reachSurvivalDistance = data.getDouble("checks.fight.reach.survivaldistance", (double)3.5F, (double)6.0F, 4.4);
      this.reachPenalty = data.getLong("checks.fight.reach.penalty");
      this.reachPrecision = data.getBoolean("checks.fight.reach.precision");
      this.reachReduce = data.getBoolean("checks.fight.reach.reduce");
      this.reachReduceDistance = data.getDouble("checks.fight.reach.reducedistance", (double)0.0F, this.reachSurvivalDistance, 0.9);
      this.reachReduceStep = data.getDouble("checks.fight.reach.reducestep", (double)0.0F, this.reachReduceDistance, 0.15);
      this.reachActions = (ActionList)data.getOptimizedActionList("checks.fight.reach.actions", "nocheatplus.checks.fight.reach");
      this.selfHitCheck = data.getBoolean("checks.fight.selfhit.active");
      this.selfHitActions = (ActionList)data.getOptimizedActionList("checks.fight.selfhit.actions", "nocheatplus.checks.fight.selfhit");
      this.speedCheck = data.getBoolean("checks.fight.speed.active");
      this.speedLimit = data.getInt("checks.fight.speed.limit");
      this.speedBuckets = data.getInt("checks.fight.speed.buckets.number", 6);
      this.speedBucketDur = data.getLong("checks.fight.speed.buckets.duration", 333L);
      this.speedBucketFactor = (float)data.getDouble("checks.fight.speed.buckets.factor", (double)1.0F);
      this.speedShortTermLimit = data.getInt("checks.fight.speed.shortterm.limit");
      this.speedShortTermTicks = data.getInt("checks.fight.speed.shortterm.ticks");
      this.speedActions = (ActionList)data.getOptimizedActionList("checks.fight.speed.actions", "nocheatplus.checks.fight.speed");
      this.yawRateCheck = data.getBoolean("checks.fight.yawrate.active", true);
      this.cancelDead = data.getBoolean("checks.fight.canceldead");
   }

   public final boolean isEnabled(CheckType checkType) {
      switch (checkType) {
         case FIGHT_ANGLE:
            return this.angleCheck;
         case FIGHT_CRITICAL:
            return this.criticalCheck;
         case FIGHT_DIRECTION:
            return this.directionCheck;
         case FIGHT_GODMODE:
            return this.godModeCheck;
         case FIGHT_KNOCKBACK:
            return this.knockbackCheck;
         case FIGHT_NOSWING:
            return this.noSwingCheck;
         case FIGHT_REACH:
            return this.reachCheck;
         case FIGHT_SPEED:
            return this.speedCheck;
         case FIGHT_SELFHIT:
            return this.selfHitCheck;
         case FIGHT_FASTHEAL:
            return this.fastHealCheck;
         default:
            return true;
      }
   }
}
