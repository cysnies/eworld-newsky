package fr.neatmonster.nocheatplus.checks.blockinteract;

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

public class BlockInteractConfig extends ACheckConfig {
   public static final CheckConfigFactory factory = new CheckConfigFactory() {
      public final ICheckConfig getConfig(Player player) {
         return BlockInteractConfig.getConfig(player);
      }
   };
   private static final Map worldsMap = new HashMap();
   public final boolean directionCheck;
   public final ActionList directionActions;
   public final boolean reachCheck;
   public final ActionList reachActions;
   public final boolean speedCheck;
   public final long speedInterval;
   public final int speedLimit;
   public final ActionList speedActions;
   public final boolean visibleCheck;
   public final ActionList visibleActions;

   public static void clear() {
      worldsMap.clear();
   }

   public static BlockInteractConfig getConfig(Player player) {
      if (!worldsMap.containsKey(player.getWorld().getName())) {
         worldsMap.put(player.getWorld().getName(), new BlockInteractConfig(ConfigManager.getConfigFile(player.getWorld().getName())));
      }

      return (BlockInteractConfig)worldsMap.get(player.getWorld().getName());
   }

   public BlockInteractConfig(ConfigFile data) {
      super(data, "checks.blockinteract.");
      this.directionCheck = data.getBoolean("checks.blockinteract.direction.active");
      this.directionActions = (ActionList)data.getOptimizedActionList("checks.blockinteract.direction.actions", "nocheatplus.checks.blockinteract.direction");
      this.reachCheck = data.getBoolean("checks.blockinteract.reach.active");
      this.reachActions = (ActionList)data.getOptimizedActionList("checks.blockinteract.reach.actions", "nocheatplus.checks.blockinteract.reach");
      this.speedCheck = data.getBoolean("checks.blockinteract.speed.active");
      this.speedInterval = data.getLong("checks.blockinteract.speed.interval");
      this.speedLimit = data.getInt("checks.blockinteract.speed.limit");
      this.speedActions = (ActionList)data.getOptimizedActionList("checks.blockinteract.speed.actions", "nocheatplus.checks.blockinteract.speed");
      this.visibleCheck = data.getBoolean("checks.blockinteract.visible.active");
      this.visibleActions = (ActionList)data.getOptimizedActionList("checks.blockinteract.visible.actions", "nocheatplus.checks.blockinteract.visible");
   }

   public final boolean isEnabled(CheckType checkType) {
      switch (checkType) {
         case BLOCKINTERACT_SPEED:
            return this.speedCheck;
         case BLOCKINTERACT_DIRECTION:
            return this.directionCheck;
         case BLOCKINTERACT_REACH:
            return this.reachCheck;
         case BLOCKINTERACT_VISIBLE:
            return this.visibleCheck;
         default:
            return true;
      }
   }
}
