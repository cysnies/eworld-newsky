package org.maxgamer.QuickShop.Util;

import java.text.DecimalFormat;
import java.util.HashSet;
import lib.util.UtilItems;
import lib.util.UtilNames;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Sign;
import org.maxgamer.QuickShop.QuickShop;

public class Util {
   private static HashSet tools = new HashSet();
   private static HashSet blacklist = new HashSet();
   private static HashSet shoppables = new HashSet();
   private static HashSet transparent = new HashSet();
   private static QuickShop plugin;
   private static final String[] ROMAN;
   private static final int[] DECIMAL;

   static {
      plugin = QuickShop.instance;

      for(String s : plugin.getConfig().getStringList("shop-blocks")) {
         Material mat = Material.getMaterial(s.toUpperCase());
         if (mat == null) {
            try {
               mat = Material.getMaterial(Integer.parseInt(s));
            } catch (NumberFormatException var4) {
            }
         }

         if (mat == null) {
            plugin.getLogger().info("Invalid shop-block: " + s);
         } else {
            shoppables.add(mat);
         }
      }

      tools.add(Material.BOW);
      tools.add(Material.SHEARS);
      tools.add(Material.FISHING_ROD);
      tools.add(Material.FLINT_AND_STEEL);
      tools.add(Material.CHAINMAIL_BOOTS);
      tools.add(Material.CHAINMAIL_CHESTPLATE);
      tools.add(Material.CHAINMAIL_HELMET);
      tools.add(Material.CHAINMAIL_LEGGINGS);
      tools.add(Material.WOOD_AXE);
      tools.add(Material.WOOD_HOE);
      tools.add(Material.WOOD_PICKAXE);
      tools.add(Material.WOOD_SPADE);
      tools.add(Material.WOOD_SWORD);
      tools.add(Material.LEATHER_BOOTS);
      tools.add(Material.LEATHER_CHESTPLATE);
      tools.add(Material.LEATHER_HELMET);
      tools.add(Material.LEATHER_LEGGINGS);
      tools.add(Material.DIAMOND_AXE);
      tools.add(Material.DIAMOND_HOE);
      tools.add(Material.DIAMOND_PICKAXE);
      tools.add(Material.DIAMOND_SPADE);
      tools.add(Material.DIAMOND_SWORD);
      tools.add(Material.DIAMOND_BOOTS);
      tools.add(Material.DIAMOND_CHESTPLATE);
      tools.add(Material.DIAMOND_HELMET);
      tools.add(Material.DIAMOND_LEGGINGS);
      tools.add(Material.STONE_AXE);
      tools.add(Material.STONE_HOE);
      tools.add(Material.STONE_PICKAXE);
      tools.add(Material.STONE_SPADE);
      tools.add(Material.STONE_SWORD);
      tools.add(Material.GOLD_AXE);
      tools.add(Material.GOLD_HOE);
      tools.add(Material.GOLD_PICKAXE);
      tools.add(Material.GOLD_SPADE);
      tools.add(Material.GOLD_SWORD);
      tools.add(Material.GOLD_BOOTS);
      tools.add(Material.GOLD_CHESTPLATE);
      tools.add(Material.GOLD_HELMET);
      tools.add(Material.GOLD_LEGGINGS);
      tools.add(Material.IRON_AXE);
      tools.add(Material.IRON_HOE);
      tools.add(Material.IRON_PICKAXE);
      tools.add(Material.IRON_SPADE);
      tools.add(Material.IRON_SWORD);
      tools.add(Material.IRON_BOOTS);
      tools.add(Material.IRON_CHESTPLATE);
      tools.add(Material.IRON_HELMET);
      tools.add(Material.IRON_LEGGINGS);

      for(String s : plugin.getConfig().getStringList("blacklist")) {
         Material mat = Material.getMaterial(s.toUpperCase());
         if (mat == null) {
            mat = Material.getMaterial(Integer.parseInt(s));
            if (mat == null) {
               plugin.getLogger().info(s + " is not a valid material.  Check your spelling or ID");
               continue;
            }
         }

         blacklist.add(mat);
      }

      transparent.clear();
      addTransparentBlock(Material.AIR);
      addTransparentBlock(Material.CAKE_BLOCK);
      addTransparentBlock(Material.REDSTONE_WIRE);
      addTransparentBlock(Material.REDSTONE_TORCH_OFF);
      addTransparentBlock(Material.REDSTONE_TORCH_ON);
      addTransparentBlock(Material.DIODE_BLOCK_OFF);
      addTransparentBlock(Material.DIODE_BLOCK_ON);
      addTransparentBlock(Material.DETECTOR_RAIL);
      addTransparentBlock(Material.LEVER);
      addTransparentBlock(Material.STONE_BUTTON);
      addTransparentBlock(Material.WOOD_BUTTON);
      addTransparentBlock(Material.STONE_PLATE);
      addTransparentBlock(Material.WOOD_PLATE);
      addTransparentBlock(Material.RED_MUSHROOM);
      addTransparentBlock(Material.BROWN_MUSHROOM);
      addTransparentBlock(Material.RED_ROSE);
      addTransparentBlock(Material.YELLOW_FLOWER);
      addTransparentBlock(Material.FLOWER_POT);
      addTransparentBlock(Material.LONG_GRASS);
      addTransparentBlock(Material.VINE);
      addTransparentBlock(Material.WATER_LILY);
      addTransparentBlock(Material.MELON_STEM);
      addTransparentBlock(Material.PUMPKIN_STEM);
      addTransparentBlock(Material.CROPS);
      addTransparentBlock(Material.NETHER_WARTS);
      addTransparentBlock(Material.SNOW);
      addTransparentBlock(Material.FIRE);
      addTransparentBlock(Material.WEB);
      addTransparentBlock(Material.TRIPWIRE);
      addTransparentBlock(Material.TRIPWIRE_HOOK);
      addTransparentBlock(Material.COBBLESTONE_STAIRS);
      addTransparentBlock(Material.BRICK_STAIRS);
      addTransparentBlock(Material.SANDSTONE_STAIRS);
      addTransparentBlock(Material.NETHER_BRICK_STAIRS);
      addTransparentBlock(Material.SMOOTH_STAIRS);
      addTransparentBlock(Material.BIRCH_WOOD_STAIRS);
      addTransparentBlock(Material.WOOD_STAIRS);
      addTransparentBlock(Material.JUNGLE_WOOD_STAIRS);
      addTransparentBlock(Material.SPRUCE_WOOD_STAIRS);
      addTransparentBlock(Material.LAVA);
      addTransparentBlock(Material.STATIONARY_LAVA);
      addTransparentBlock(Material.WATER);
      addTransparentBlock(Material.STATIONARY_WATER);
      addTransparentBlock(Material.SAPLING);
      addTransparentBlock(Material.DEAD_BUSH);
      addTransparentBlock(Material.FENCE);
      addTransparentBlock(Material.FENCE_GATE);
      addTransparentBlock(Material.IRON_FENCE);
      addTransparentBlock(Material.NETHER_FENCE);
      addTransparentBlock(Material.LADDER);
      addTransparentBlock(Material.SIGN_POST);
      addTransparentBlock(Material.WALL_SIGN);
      addTransparentBlock(Material.BED_BLOCK);
      addTransparentBlock(Material.PISTON_EXTENSION);
      addTransparentBlock(Material.PISTON_MOVING_PIECE);
      addTransparentBlock(Material.RAILS);
      addTransparentBlock(Material.TORCH);
      addTransparentBlock(Material.TRAP_DOOR);
      addTransparentBlock(Material.BREWING_STAND);
      addTransparentBlock(Material.WOODEN_DOOR);
      addTransparentBlock(Material.WOOD_STEP);
      ROMAN = new String[]{"X", "IX", "V", "IV", "I"};
      DECIMAL = new int[]{10, 9, 5, 4, 1};
   }

   public static boolean isTransparent(Material m) {
      boolean trans = transparent.contains(m);
      return trans;
   }

   public static void addTransparentBlock(Material m) {
      if (!transparent.add(m)) {
         System.out.println("Already added as transparent: " + m.toString());
      }

      if (!m.isBlock()) {
         System.out.println(m + " is not a block!");
      }

   }

   public static void parseColours(YamlConfiguration config) {
      for(String key : config.getKeys(true)) {
         String filtered = config.getString(key);
         if (!filtered.startsWith("MemorySection")) {
            filtered = ChatColor.translateAlternateColorCodes('&', filtered);
            config.set(key, filtered);
         }
      }

   }

   public static boolean canBeShop(Block b) {
      BlockState bs = b.getState();
      return !(bs instanceof InventoryHolder) ? false : shoppables.contains(bs.getType());
   }

   public static String getToolPercentage(ItemStack item) {
      double dura = (double)item.getDurability();
      double max = (double)item.getType().getMaxDurability();
      DecimalFormat formatter = new DecimalFormat("0");
      return formatter.format(((double)1.0F - dura / max) * (double)100.0F);
   }

   public static Block getSecondHalf(Block b) {
      if (!b.getType().toString().contains("CHEST")) {
         return null;
      } else {
         Block[] blocks = new Block[]{b.getRelative(1, 0, 0), b.getRelative(-1, 0, 0), b.getRelative(0, 0, 1), b.getRelative(0, 0, -1)};

         for(Block c : blocks) {
            if (c.getType() == b.getType()) {
               return c;
            }
         }

         return null;
      }
   }

   public static ItemStack makeItem(String itemString) {
      String[] itemInfo = itemString.split(":");
      ItemStack item = new ItemStack(Material.getMaterial(itemInfo[0]));
      MaterialData data = new MaterialData(Integer.parseInt(itemInfo[1]));
      item.setData(data);
      item.setDurability(Short.parseShort(itemInfo[2]));
      item.setAmount(Integer.parseInt(itemInfo[3]));

      for(int i = 4; i < itemInfo.length; i += 2) {
         int level = Integer.parseInt(itemInfo[i + 1]);
         Enchantment ench = Enchantment.getByName(itemInfo[i]);
         if (ench != null && ench.canEnchantItem(item) && level > 0) {
            level = Math.min(ench.getMaxLevel(), level);
            item.addEnchantment(ench, level);
         }
      }

      return item;
   }

   public static String serialize(ItemStack iStack) {
      YamlConfiguration cfg = new YamlConfiguration();
      cfg.set("item", iStack);
      return cfg.saveToString();
   }

   public static ItemStack deserialize(String config) throws InvalidConfigurationException {
      YamlConfiguration cfg = new YamlConfiguration();
      cfg.loadFromString(config);
      ItemStack stack = cfg.getItemStack("item");
      return stack;
   }

   public static String getName(ItemStack i) {
      String vanillaName = getDataName(i.getType(), i.getDurability());
      return prettifyText(vanillaName);
   }

   public static String prettifyText(String ugly) {
      if (!ugly.contains("_") && !ugly.equals(ugly.toUpperCase())) {
         return ugly;
      } else {
         String fin = "";
         ugly = ugly.toLowerCase();
         if (ugly.contains("_")) {
            String[] splt = ugly.split("_");
            int i = 0;

            for(String s : splt) {
               ++i;
               fin = fin + Character.toUpperCase(s.charAt(0)) + s.substring(1);
               if (i < splt.length) {
                  fin = fin + " ";
               }
            }
         } else {
            fin = fin + Character.toUpperCase(ugly.charAt(0)) + ugly.substring(1);
         }

         return fin;
      }
   }

   public static String toRomain(Integer value) {
      return toRoman(value);
   }

   public static String toRoman(int n) {
      if (n > 0 && n < 40) {
         String roman = "";

         for(int i = 0; i < ROMAN.length; ++i) {
            while(n >= DECIMAL[i]) {
               n -= DECIMAL[i];
               roman = roman + ROMAN[i];
            }
         }

         return roman;
      } else {
         return "" + n;
      }
   }

   private static String getDataName(Material mat, short damage) {
      return UtilNames.getItemName(mat.getId(), damage);
   }

   public static boolean isTool(Material mat) {
      return tools.contains(mat);
   }

   public static boolean matches(ItemStack stack1, ItemStack stack2) {
      return UtilItems.isSame(stack1, stack2);
   }

   public static String format(double n) {
      try {
         return plugin.getEcon().format(n);
      } catch (NumberFormatException var3) {
         return "$" + n;
      }
   }

   public static boolean isBlacklisted(Material m) {
      return blacklist.contains(m);
   }

   public static Block getAttached(Block b) {
      try {
         Sign sign = (Sign)b.getState().getData();
         BlockFace attached = sign.getAttachedFace();
         return attached == null ? null : b.getRelative(attached);
      } catch (NullPointerException var3) {
         return null;
      }
   }

   public static int countItems(Inventory inv, ItemStack item) {
      int items = 0;

      ItemStack[] var6;
      for(ItemStack iStack : var6 = inv.getContents()) {
         if (iStack != null && matches(item, iStack)) {
            items += iStack.getAmount();
         }
      }

      return items;
   }

   public static int countSpace(Inventory inv, ItemStack item) {
      int space = 0;

      ItemStack[] var6;
      for(ItemStack iStack : var6 = inv.getContents()) {
         if (iStack != null && iStack.getType() != Material.AIR) {
            if (matches(item, iStack)) {
               space += item.getMaxStackSize() - iStack.getAmount();
            }
         } else {
            space += item.getMaxStackSize();
         }
      }

      return space;
   }

   public static boolean isLoaded(Location loc) {
      if (loc.getWorld() == null) {
         return false;
      } else {
         int x = (int)Math.floor((double)loc.getBlockX() / (double)16.0F);
         int z = (int)Math.floor((double)loc.getBlockZ() / (double)16.0F);
         return loc.getWorld().isChunkLoaded(x, z);
      }
   }
}
