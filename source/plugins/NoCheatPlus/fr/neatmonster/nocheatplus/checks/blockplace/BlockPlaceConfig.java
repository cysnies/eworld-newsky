package fr.neatmonster.nocheatplus.checks.blockplace;

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

public class BlockPlaceConfig extends ACheckConfig {
   public static final CheckConfigFactory factory = new CheckConfigFactory() {
      public final ICheckConfig getConfig(Player player) {
         return BlockPlaceConfig.getConfig(player);
      }
   };
   private static final Map worldsMap = new HashMap();
   public final boolean autoSignCheck;
   public final ActionList autoSignActions;
   public final boolean directionCheck;
   public final ActionList directionActions;
   public final boolean fastPlaceCheck;
   public final int fastPlaceLimit;
   public final int fastPlaceShortTermTicks;
   public final int fastPlaceShortTermLimit;
   public final ActionList fastPlaceActions;
   public final boolean noSwingCheck;
   public final ActionList noSwingActions;
   public final boolean reachCheck;
   public final ActionList reachActions;
   public final boolean speedCheck;
   public final long speedInterval;
   public final ActionList speedActions;

   public static void clear() {
      worldsMap.clear();
   }

   public static BlockPlaceConfig getConfig(Player player) {
      if (!worldsMap.containsKey(player.getWorld().getName())) {
         worldsMap.put(player.getWorld().getName(), new BlockPlaceConfig(ConfigManager.getConfigFile(player.getWorld().getName())));
      }

      return (BlockPlaceConfig)worldsMap.get(player.getWorld().getName());
   }

   public BlockPlaceConfig(ConfigFile data) {
      super(data, "checks.blockplace.");
      this.autoSignCheck = data.getBoolean("checks.blockplace.autosign.active");
      this.autoSignActions = (ActionList)data.getOptimizedActionList("checks.blockplace.autosign.actions", "nocheatplus.checks.blockplace.autosign");
      this.directionCheck = data.getBoolean("checks.blockplace.direction.active");
      this.directionActions = (ActionList)data.getOptimizedActionList("checks.blockplace.direction.actions", "nocheatplus.checks.blockplace.direction");
      this.fastPlaceCheck = data.getBoolean("checks.blockplace.fastplace.active");
      this.fastPlaceLimit = data.getInt("checks.blockplace.fastplace.limit");
      this.fastPlaceShortTermTicks = data.getInt("checks.blockplace.fastplace.shortterm.ticks");
      this.fastPlaceShortTermLimit = data.getInt("checks.blockplace.fastplace.shortterm.limit");
      this.fastPlaceActions = (ActionList)data.getOptimizedActionList("checks.blockplace.fastplace.actions", "nocheatplus.checks.blockplace.fastplace");
      this.noSwingCheck = data.getBoolean("checks.blockplace.noswing.active");
      this.noSwingActions = (ActionList)data.getOptimizedActionList("checks.blockplace.noswing.actions", "nocheatplus.checks.blockplace.noswing");
      this.reachCheck = data.getBoolean("checks.blockplace.reach.active");
      this.reachActions = (ActionList)data.getOptimizedActionList("checks.blockplace.reach.actions", "nocheatplus.checks.blockplace.reach");
      this.speedCheck = data.getBoolean("checks.blockplace.speed.active");
      this.speedInterval = data.getLong("checks.blockplace.speed.interval");
      this.speedActions = (ActionList)data.getOptimizedActionList("checks.blockplace.speed.actions", "nocheatplus.checks.blockplace.speed");
   }

   public final boolean isEnabled(CheckType checkType) {
      switch (checkType) {
         case BLOCKPLACE_DIRECTION:
            return this.directionCheck;
         case BLOCKPLACE_FASTPLACE:
            return this.fastPlaceCheck;
         case BLOCKPLACE_NOSWING:
            return this.noSwingCheck;
         case BLOCKPLACE_REACH:
            return this.reachCheck;
         case BLOCKPLACE_SPEED:
            return this.speedCheck;
         case BLOCKPLACE_AGAINST:
            return true;
         case BLOCKPLACE_AUTOSIGN:
            return this.autoSignCheck;
         default:
            return true;
      }
   }
}
