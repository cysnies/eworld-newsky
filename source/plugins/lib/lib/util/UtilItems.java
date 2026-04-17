package lib.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import lib.Items;
import lib.Lib;
import lib.hashList.HashList;
import lib.types.InvalidTypeException;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class UtilItems {
   private static ItemMeta EmptyItemMeta = (new ItemStack(1)).getItemMeta();
   private static Items Items;

   public static void init(Lib lib) {
      Items = lib.getItems();
   }

   public static boolean hasDurability(ItemStack is) {
      return is.getMaxStackSize() == 1 && is.getType().getMaxDurability() > 1;
   }

   public static int getEmptySlots(Inventory inv) {
      int sum = 0;

      for(int i = 0; i < inv.getSize(); ++i) {
         if (inv.getItem(i) == null || inv.getItem(i).getTypeId() == 0) {
            ++sum;
         }
      }

      return sum;
   }

   public static Inventory getInv(String name) {
      return Items.getInv(name);
   }

   public static int getSize(String plugin, String type) {
      return Items.getSize(plugin, type);
   }

   public static boolean checkEmpty(Player p) {
      PlayerInventory pi = p.getInventory();

      for(int i = 0; i < 40; ++i) {
         if (pi.getItem(i) != null && pi.getItem(i).getTypeId() != 0) {
            return false;
         }
      }

      return true;
   }

   public static boolean hasItem(Inventory inv, int id, int amount) {
      int sum = 0;

      ItemStack[] var7;
      for(ItemStack is : var7 = inv.getContents()) {
         if (is != null && is.getTypeId() == id && isItemMetaEmpty(is.getItemMeta())) {
            sum += is.getAmount();
            if (sum >= amount) {
               return true;
            }
         }
      }

      return false;
   }

   public static boolean removeItem(Inventory inv, int id, int amount, boolean force) {
      if (amount <= 0) {
         return true;
      } else if (hasItem(inv, id, amount)) {
         for(int i = 0; i < inv.getSize(); ++i) {
            if (inv.getItem(i) != null) {
               ItemStack is = inv.getItem(i);
               if (is.getTypeId() == id && isItemMetaEmpty(is.getItemMeta())) {
                  if (amount >= is.getAmount()) {
                     amount -= is.getAmount();
                     inv.setItem(i, (ItemStack)null);
                  } else {
                     is.setAmount(is.getAmount() - amount);
                     amount = 0;
                  }

                  if (amount <= 0) {
                     break;
                  }
               }
            }
         }

         return true;
      } else if (!force) {
         return false;
      } else {
         for(int i = 0; i < inv.getSize(); ++i) {
            if (inv.getItem(i) != null) {
               ItemStack is = inv.getItem(i);
               if (is.getTypeId() == id && isItemMetaEmpty(is.getItemMeta())) {
                  if (amount >= is.getAmount()) {
                     amount -= is.getAmount();
                     inv.setItem(i, (ItemStack)null);
                  } else {
                     is.setAmount(is.getAmount() - amount);
                     amount = 0;
                  }

                  if (amount <= 0) {
                     break;
                  }
               }
            }
         }

         return false;
      }
   }

   public static boolean removeItem(Inventory inv, ItemStack is, int amount, boolean force) {
      if (amount <= 0) {
         return true;
      } else if (!inv.containsAtLeast(is, amount) && !force) {
         return false;
      } else {
         int need = amount;

         for(int i = 0; i < inv.getSize(); ++i) {
            ItemStack is2 = inv.getItem(i);
            if (is2 != null && is2.getTypeId() == is.getTypeId() && is2.getDurability() == is.getDurability()) {
               if (is.hasItemMeta()) {
                  if (!is2.hasItemMeta() || !is.getItemMeta().equals(is2.getItemMeta())) {
                     continue;
                  }
               } else if (is2.hasItemMeta()) {
                  continue;
               }

               int has = is2.getAmount();
               if (need <= has) {
                  if (has == need) {
                     inv.setItem(i, (ItemStack)null);
                  } else {
                     is2.setAmount(has - need);
                  }

                  need = 0;
                  break;
               }

               need -= has;
               inv.setItem(i, (ItemStack)null);
            }
         }

         return need <= 0;
      }
   }

   public static int getAmount(Inventory inv, int id) {
      int sum = 0;

      for(int i = 0; i < inv.getSize(); ++i) {
         if (inv.getItem(i) != null && inv.getItem(i).getTypeId() == id && isItemMetaEmpty(inv.getItem(i).getItemMeta())) {
            sum += inv.getItem(i).getAmount();
         }
      }

      return sum;
   }

   public static int getAmount(Inventory inv, ItemStack is) {
      int sum = 0;
      int id = is.getTypeId();
      short damage = is.getDurability();
      ItemMeta im = is.getItemMeta();

      for(int i = 0; i < inv.getSize(); ++i) {
         ItemStack check = inv.getItem(i);
         if (check != null && check.getTypeId() == id && check.getDurability() == damage) {
            if (check.getItemMeta() == null) {
               if (im != null) {
                  continue;
               }
            } else if (im == null || !check.getItemMeta().equals(im)) {
               continue;
            }

            sum += inv.getItem(i).getAmount();
         }
      }

      return sum;
   }

   public static boolean reloadItems(String plugin, YamlConfiguration config) {
      return Items.reloadItems(plugin, config);
   }

   public static HashList addItems(CommandSender sender, Inventory inv, String plugin, String type, boolean all, boolean force) throws FileNotFoundException, IOException, InvalidConfigurationException, InvalidTypeException {
      return Items.addItems(sender, inv, plugin, type, all, force);
   }

   public static HashList getItems(String plugin, String type, boolean all, boolean force) throws InvalidTypeException {
      return Items.getItems(plugin, type, all, force);
   }

   public static HashList getItems(String itemsPlugin, String itemsType, boolean itemsAll, boolean itemsForce, String enchantsPlugin, String enchantsType, boolean enchantsAll, boolean enchantsForce) throws InvalidTypeException {
      return Items.getItems(itemsPlugin, itemsType, itemsAll, itemsForce, enchantsPlugin, enchantsType, enchantsAll, enchantsForce);
   }

   public static boolean dropItems(CommandSender sender, Location l, boolean naturally, String plugin, String type, boolean all, boolean force) {
      return Items.dropItems(sender, l, naturally, plugin, type, all, force);
   }

   public static ItemStack getItem(String plugin, String type) {
      return Items.getItem(plugin, type);
   }

   public static Items.CheckResult check(Inventory inv, String plugin, String type) {
      return Items.check(inv, plugin, type);
   }

   public static boolean isSame(ItemStack is1, ItemStack is2) {
      if (is1 != null && is2 != null) {
         if (is1.getTypeId() == is2.getTypeId() && is1.getTypeId() != 0) {
            if (is1.getDurability() != is2.getDurability()) {
               return false;
            } else if (is1.hasItemMeta()) {
               return !is2.hasItemMeta() ? false : is1.getItemMeta().equals(is2.getItemMeta());
            } else {
               return !is2.hasItemMeta();
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean isItemMetaEmpty(ItemMeta itemMeta) {
      return itemMeta == null ? true : itemMeta.equals(EmptyItemMeta);
   }

   public static String saveItem(ItemStack is) {
      return lib.Items.saveItem(is);
   }

   public static ItemStack loadItem(String s) {
      return lib.Items.loadItem(s);
   }
}
