package com.earth2me.essentials.craftbukkit;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public final class InventoryWorkaround {
   private InventoryWorkaround() {
   }

   private static int firstPartial(Inventory inventory, ItemStack item, int maxAmount) {
      if (item == null) {
         return -1;
      } else {
         ItemStack[] stacks = inventory.getContents();

         for(int i = 0; i < stacks.length; ++i) {
            ItemStack cItem = stacks[i];
            if (cItem != null && cItem.getAmount() < maxAmount && cItem.isSimilar(item)) {
               return i;
            }
         }

         return -1;
      }
   }

   public static boolean addAllItems(Inventory inventory, ItemStack... items) {
      Inventory fakeInventory = Bukkit.getServer().createInventory((InventoryHolder)null, inventory.getType());
      fakeInventory.setContents(inventory.getContents());
      if (addItems(fakeInventory, items).isEmpty()) {
         addItems(inventory, items);
         return true;
      } else {
         return false;
      }
   }

   public static Map addItems(Inventory inventory, ItemStack... items) {
      return addOversizedItems(inventory, 0, items);
   }

   public static Map addOversizedItems(Inventory inventory, int oversizedStacks, ItemStack... items) {
      Map<Integer, ItemStack> leftover = new HashMap();
      ItemStack[] combined = new ItemStack[items.length];

      for(int i = 0; i < items.length; ++i) {
         if (items[i] != null && items[i].getAmount() >= 1) {
            for(int j = 0; j < combined.length; ++j) {
               if (combined[j] == null) {
                  combined[j] = items[i].clone();
                  break;
               }

               if (combined[j].isSimilar(items[i])) {
                  combined[j].setAmount(combined[j].getAmount() + items[i].getAmount());
                  break;
               }
            }
         }
      }

      for(int i = 0; i < combined.length; ++i) {
         ItemStack item = combined[i];
         if (item != null) {
            while(true) {
               int maxAmount = oversizedStacks > item.getType().getMaxStackSize() ? oversizedStacks : item.getType().getMaxStackSize();
               int firstPartial = firstPartial(inventory, item, maxAmount);
               if (firstPartial == -1) {
                  int firstFree = inventory.firstEmpty();
                  if (firstFree == -1) {
                     leftover.put(i, item);
                     break;
                  }

                  if (item.getAmount() <= maxAmount) {
                     inventory.setItem(firstFree, item);
                     break;
                  }

                  ItemStack stack = item.clone();
                  stack.setAmount(maxAmount);
                  inventory.setItem(firstFree, stack);
                  item.setAmount(item.getAmount() - maxAmount);
               } else {
                  ItemStack partialItem = inventory.getItem(firstPartial);
                  int amount = item.getAmount();
                  int partialAmount = partialItem.getAmount();
                  if (amount + partialAmount <= maxAmount) {
                     partialItem.setAmount(amount + partialAmount);
                     break;
                  }

                  partialItem.setAmount(maxAmount);
                  item.setAmount(amount + partialAmount - maxAmount);
               }
            }
         }
      }

      return leftover;
   }
}
