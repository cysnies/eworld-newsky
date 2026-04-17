package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class InventoryUtil {
   public static int getFreeSlots(Inventory inventory) {
      ItemStack[] contents = inventory.getContents();
      int count = 0;

      for(int i = 0; i < contents.length; ++i) {
         ItemStack stack = contents[i];
         if (stack == null || stack.getTypeId() == 0) {
            ++count;
         }
      }

      return count;
   }

   public static int getStackCount(Inventory inventory, ItemStack reference) {
      if (inventory == null) {
         return 0;
      } else if (reference == null) {
         return getFreeSlots(inventory);
      } else {
         int id = reference.getTypeId();
         int durability = reference.getDurability();
         ItemStack[] contents = inventory.getContents();
         int count = 0;

         for(int i = 0; i < contents.length; ++i) {
            ItemStack stack = contents[i];
            if (stack != null && stack.getTypeId() == id && stack.getDurability() == durability) {
               ++count;
            }
         }

         return count;
      }
   }

   public static int getStackCount(InventoryView view, ItemStack reference) {
      return getStackCount(view.getBottomInventory(), reference) + getStackCount(view.getTopInventory(), reference);
   }
}
