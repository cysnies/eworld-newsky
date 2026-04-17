package fr.neatmonster.nocheatplus.compat.blocks.init;

import fr.neatmonster.nocheatplus.utilities.BlockFlags;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import org.bukkit.Material;

public class BlockInit {
   public static void assertMaterialExists(int id) {
      if (Material.getMaterial(id) == null) {
         throw new RuntimeException("Material " + id + " does not exist.");
      }
   }

   public static void assertMaterialName(int id, String name) {
      Material mat = Material.getMaterial(id);
      if (mat == null) {
         throw new RuntimeException("Material " + id + " does not exist.");
      } else if (mat.name().equals(name)) {
         throw new RuntimeException("Name for Material " + id + " ('" + mat.name() + "') does not match '" + name + "'.");
      }
   }

   public static void assertMaterialNameMatch(int id, String... parts) {
      Material mat = Material.getMaterial(id);
      if (mat == null) {
         throw new RuntimeException("Material " + id + " does not exist.");
      } else {
         String name = mat.name().toLowerCase();

         for(String part : parts) {
            if (name.indexOf(part.toLowerCase()) < 0) {
               throw new RuntimeException("Name for Material " + id + " ('" + mat.name() + "') should contain '" + part + "'.");
            }
         }

      }
   }

   public static void setPropsAs(int newId, Material mat) {
      setPropsAs(newId, mat.getId());
   }

   public static void setPropsAs(int newId, int otherId) {
      BlockProperties.setBlockProps(newId, BlockProperties.getBlockProps(otherId));
   }

   public static void setAs(int newId, Material mat) {
      BlockFlags.setFlagsAs(newId, mat);
      setPropsAs(newId, mat);
   }

   public static void setAs(int newId, int otherId) {
      BlockFlags.setFlagsAs(newId, otherId);
      setPropsAs(newId, otherId);
   }
}
