package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.Material;

public class BlockFlags {
   public static void setFlagsAs(int id, Material mat) {
      setFlagsAs(id, mat.getId());
   }

   public static void setFlagsAs(int id, int otherId) {
      BlockProperties.setBlockFlags(id, BlockProperties.getBlockFlags(otherId));
   }

   public static void addFlags(int id, long flags) {
      BlockProperties.setBlockFlags(id, BlockProperties.getBlockFlags(id) | flags);
   }

   public static void removeFlags(int id, long flags) {
      BlockProperties.setBlockFlags(id, BlockProperties.getBlockFlags(id) & ~flags);
   }
}
