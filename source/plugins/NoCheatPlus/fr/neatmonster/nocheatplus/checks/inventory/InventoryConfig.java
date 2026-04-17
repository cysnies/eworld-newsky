package fr.neatmonster.nocheatplus.checks.inventory;

import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.ACheckConfig;
import fr.neatmonster.nocheatplus.checks.access.CheckConfigFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckConfig;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.entity.Player;

public class InventoryConfig extends ACheckConfig {
   public static final CheckConfigFactory factory = new CheckConfigFactory() {
      public final ICheckConfig getConfig(Player player) {
         return InventoryConfig.getConfig(player);
      }
   };
   private static final Map worldsMap = new HashMap();
   public final boolean dropCheck;
   public final int dropLimit;
   public final long dropTimeFrame;
   public final ActionList dropActions;
   public final boolean fastClickCheck;
   public final boolean fastClickSpareCreative;
   public final boolean fastClickTweaks1_5;
   public final float fastClickShortTermLimit;
   public final float fastClickNormalLimit;
   public final ActionList fastClickActions;
   public final boolean fastConsumeCheck;
   public final long fastConsumeDuration;
   public final boolean fastConsumeWhitelist;
   public final Set fastConsumeItems = new HashSet();
   public final ActionList fastConsumeActions;
   public final boolean instantBowCheck;
   public final boolean instantBowStrict;
   public final long instantBowDelay;
   public final ActionList instantBowActions;
   public final boolean instantEatCheck;
   public final ActionList instantEatActions;
   public final boolean itemsCheck;

   public static void clear() {
      worldsMap.clear();
   }

   public static InventoryConfig getConfig(Player player) {
      if (!worldsMap.containsKey(player.getWorld().getName())) {
         worldsMap.put(player.getWorld().getName(), new InventoryConfig(ConfigManager.getConfigFile(player.getWorld().getName())));
      }

      return (InventoryConfig)worldsMap.get(player.getWorld().getName());
   }

   public InventoryConfig(ConfigFile data) {
      super(data, "checks.inventory.");
      this.dropCheck = data.getBoolean("checks.inventory.drop.active");
      this.dropLimit = data.getInt("checks.inventory.drop.limit");
      this.dropTimeFrame = data.getLong("checks.inventory.drop.timeframe");
      this.dropActions = (ActionList)data.getOptimizedActionList("checks.inventory.drop.actions", "nocheatplus.checks.inventory.drop");
      this.fastClickCheck = data.getBoolean("checks.inventory.fastclick.active");
      this.fastClickSpareCreative = data.getBoolean("checks.inventory.fastclick.sparecreative");
      this.fastClickTweaks1_5 = data.getBoolean("checks.inventory.fastclick.tweaks1_5");
      this.fastClickShortTermLimit = (float)data.getDouble("checks.inventory.fastclick.limit.shortterm");
      this.fastClickNormalLimit = (float)data.getDouble("checks.inventory.fastclick.limit.normal");
      this.fastClickActions = (ActionList)data.getOptimizedActionList("checks.inventory.fastclick.actions", "nocheatplus.checks.inventory.fastclick");
      this.fastConsumeCheck = data.getBoolean("checks.inventory.fastconsume.active");
      this.fastConsumeDuration = (long)((double)1000.0F * data.getDouble("checks.inventory.fastconsume.duration"));
      this.fastConsumeWhitelist = data.getBoolean("checks.inventory.fastconsume.whitelist");
      data.readMaterialFromList("checks.inventory.fastconsume.items", this.fastConsumeItems);
      this.fastConsumeActions = (ActionList)data.getOptimizedActionList("checks.inventory.fastconsume.actions", "nocheatplus.checks.inventory.fastconsume");
      this.instantBowCheck = data.getBoolean("checks.inventory.instantbow.active");
      this.instantBowStrict = data.getBoolean("checks.inventory.instantbow.strict");
      this.instantBowDelay = (long)data.getInt("checks.inventory.instantbow.delay");
      this.instantBowActions = (ActionList)data.getOptimizedActionList("checks.inventory.instantbow.actions", "nocheatplus.checks.inventory.instantbow");
      this.instantEatCheck = data.getBoolean("checks.inventory.instanteat.active");
      this.instantEatActions = (ActionList)data.getOptimizedActionList("checks.inventory.instanteat.actions", "nocheatplus.checks.inventory.instanteat");
      this.itemsCheck = data.getBoolean("checks.inventory..items.active");
   }

   public final boolean isEnabled(CheckType checkType) {
      switch (checkType) {
         case INVENTORY_FASTCLICK:
            return this.fastClickCheck;
         case INVENTORY_ITEMS:
            return this.itemsCheck;
         case INVENTORY_DROP:
            return this.dropCheck;
         case INVENTORY_INSTANTBOW:
            return this.instantBowCheck;
         case INVENTORY_INSTANTEAT:
            return this.instantEatCheck;
         case INVENTORY_FASTCONSUME:
            return this.fastConsumeCheck;
         default:
            return true;
      }
   }
}
