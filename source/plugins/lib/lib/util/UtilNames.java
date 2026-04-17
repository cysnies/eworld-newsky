package lib.util;

import java.util.HashMap;
import lib.Lib;
import lib.Names;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public class UtilNames {
   private static Names Names;

   public static void init(Lib lib) {
      Names = lib.getNames();
   }

   public static String getWorldName(String world) {
      return Names.getWorldName(world);
   }

   public static String getEnchantName(int id) {
      return Names.getEnchantName(id);
   }

   public static String getItemName(ItemStack is) {
      return Names.getItemName(is);
   }

   public static String getItemName(int id, int smallId) {
      return Names.getItemName(id, smallId);
   }

   public static String getEntityName(Entity entity) {
      return getEntityName(entity, true, true);
   }

   public static String getEntityName(Entity entity, boolean customName, boolean playerName) {
      return Names.getEntityName(entity, customName, playerName);
   }

   public static String getEntityName(int id) {
      return Names.getEntityName(id);
   }

   public static String getPotionName(int id) {
      return Names.getPotionName(id);
   }

   public static HashMap getItemHash() {
      return Names.getItemHash();
   }
}
