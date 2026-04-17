package fr.neatmonster.nocheatplus.checks.blockbreak;

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

public class BlockBreakConfig extends ACheckConfig {
   public static final CheckConfigFactory factory = new CheckConfigFactory() {
      public final ICheckConfig getConfig(Player player) {
         return BlockBreakConfig.getConfig(player);
      }
   };
   private static final Map worldsMap = new HashMap();
   public final boolean directionCheck;
   public final ActionList directionActions;
   public final boolean fastBreakCheck;
   public final boolean fastBreakStrict;
   public final boolean fastBreakDebug;
   public final int fastBreakBuckets;
   public final long fastBreakBucketDur;
   public final float fastBreakBucketFactor;
   public final long fastBreakGrace;
   public final long fastBreakDelay;
   public final int fastBreakModSurvival;
   public final int fastBreakModCreative;
   public final ActionList fastBreakActions;
   public final boolean frequencyCheck;
   public final int frequencyBuckets;
   public final long frequencyBucketDur;
   public final float frequencyBucketFactor;
   public final int frequencyIntervalCreative;
   public final int frequencyIntervalSurvival;
   public final int frequencyShortTermLimit;
   public final int frequencyShortTermTicks;
   public final ActionList frequencyActions;
   public boolean improbableFastBreakCheck;
   public final boolean noSwingCheck;
   public final ActionList noSwingActions;
   public final boolean reachCheck;
   public final ActionList reachActions;
   public final boolean wrongBlockCheck;
   public final float wrongBLockLevel;
   public final ActionList wrongBlockActions;

   public static void clear() {
      worldsMap.clear();
   }

   public static BlockBreakConfig getConfig(Player player) {
      if (!worldsMap.containsKey(player.getWorld().getName())) {
         worldsMap.put(player.getWorld().getName(), new BlockBreakConfig(ConfigManager.getConfigFile(player.getWorld().getName())));
      }

      return (BlockBreakConfig)worldsMap.get(player.getWorld().getName());
   }

   public BlockBreakConfig(ConfigFile data) {
      super(data, "checks.blockbreak.");
      this.directionCheck = data.getBoolean("checks.blockbreak.direction.active");
      this.directionActions = (ActionList)data.getOptimizedActionList("checks.blockbreak.direction.actions", "nocheatplus.checks.blockbreak.direction");
      this.fastBreakCheck = data.getBoolean("checks.blockbreak.fastbreak.active");
      this.fastBreakStrict = data.getBoolean("checks.blockbreak.fastbreak.strict");
      this.fastBreakDebug = data.getBoolean("checks.blockbreak.fastbreak.debug", false);
      this.fastBreakDelay = data.getLong("checks.blockbreak.fastbreak.delay");
      this.fastBreakGrace = Math.max(data.getLong("checks.blockbreak.buckets.contention", 2000L), data.getLong("checks.blockbreak.fastbreak.grace"));
      this.fastBreakBucketDur = (long)data.getInt("checks.blockbreak.buckets.duration", 4000);
      this.fastBreakBucketFactor = (float)data.getDouble("checks.blockbreak.buckets.factor", 0.99);
      this.fastBreakBuckets = data.getInt("checks.blockbreak.buckets.number", 30);
      this.fastBreakModCreative = data.getInt("checks.blockbreak.fastbreak.intervalcreative", 0);
      this.fastBreakModSurvival = data.getInt("checks.blockbreak.fastbreak.intervalsurvival");
      this.fastBreakActions = (ActionList)data.getOptimizedActionList("checks.blockbreak.fastbreak.actions", "nocheatplus.checks.blockbreak.fastbreak");
      this.frequencyCheck = data.getBoolean("checks.blockbreak.frequency.active");
      this.frequencyBuckets = data.getInt("checks.blockbreak.frequency.buckets.number", 2);
      this.frequencyBucketDur = data.getLong("checks.blockbreak.frequency.buckets.duration", 1000L);
      this.frequencyBucketFactor = (float)data.getDouble("checks.blockbreak.frequency.buckets.factor", (double)1.0F);
      this.frequencyIntervalCreative = data.getInt("checks.blockbreak.frequency.intervalcreative");
      this.frequencyIntervalSurvival = data.getInt("checks.blockbreak.frequency.intervalsurvival");
      this.frequencyShortTermLimit = data.getInt("checks.blockbreak.frequency.shortterm.limit");
      this.frequencyShortTermTicks = data.getInt("checks.blockbreak.frequency.shortterm.ticks");
      this.frequencyActions = (ActionList)data.getOptimizedActionList("checks.blockbreak.frequency.actions", "nocheatplus.checks.blockbreak.frequency");
      this.noSwingCheck = data.getBoolean("checks.blockbreak.noswing.active");
      this.noSwingActions = (ActionList)data.getOptimizedActionList("checks.blockbreak.noswing.actions", "nocheatplus.checks.blockbreak.noswing");
      this.reachCheck = data.getBoolean("checks.blockbreak.reach.active");
      this.reachActions = (ActionList)data.getOptimizedActionList("checks.blockbreak.reach.actions", "nocheatplus.checks.blockbreak.reach");
      this.wrongBlockCheck = data.getBoolean("checks.blockbreak.wrongblock.active");
      this.wrongBLockLevel = (float)data.getInt("checks.blockbreak.wrongblock.level");
      this.wrongBlockActions = (ActionList)data.getOptimizedActionList("checks.blockbreak.wrongblock.actions", "nocheatplus.checks.blockbreak.wrongblock");
   }

   public final boolean isEnabled(CheckType checkType) {
      switch (checkType) {
         case BLOCKBREAK_DIRECTION:
            return this.directionCheck;
         case BLOCKBREAK_FASTBREAK:
            return this.fastBreakCheck;
         case BLOCKBREAK_FREQUENCY:
            return this.frequencyCheck;
         case BLOCKBREAK_NOSWING:
            return this.noSwingCheck;
         case BLOCKBREAK_REACH:
            return this.reachCheck;
         case BLOCKBREAK_WRONGBLOCK:
            return this.wrongBlockCheck;
         case BLOCKBREAK_BREAK:
            return true;
         default:
            return true;
      }
   }
}
