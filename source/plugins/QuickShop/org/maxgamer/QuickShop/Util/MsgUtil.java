package org.maxgamer.QuickShop.Util;

import java.io.File;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import lib.util.UtilNames;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Shop.Shop;

public class MsgUtil {
   private static QuickShop plugin;
   private static YamlConfiguration messages;
   private static HashMap player_messages = new HashMap();

   static {
      plugin = QuickShop.instance;
   }

   public static void loadCfgMessages() {
      File messageFile = new File(plugin.getDataFolder(), "messages.yml");
      if (!messageFile.exists()) {
         plugin.getLogger().info("Creating messages.yml");
         plugin.saveResource("messages.yml", true);
      }

      messages = YamlConfiguration.loadConfiguration(messageFile);
      messages.options().copyDefaults(true);
      InputStream defMessageStream = plugin.getResource("messages.yml");
      YamlConfiguration defMessages = YamlConfiguration.loadConfiguration(defMessageStream);
      messages.setDefaults(defMessages);
      Util.parseColours(messages);
   }

   public static void loadTransactionMessages() {
      player_messages.clear();

      String message;
      LinkedList<String> msgs;
      try {
         for(ResultSet rs = plugin.getDB().getConnection().prepareStatement("SELECT * FROM messages").executeQuery(); rs.next(); msgs.add(message)) {
            String owner = rs.getString("owner");
            message = rs.getString("message");
            msgs = (LinkedList)player_messages.get(owner);
            if (msgs == null) {
               msgs = new LinkedList();
               player_messages.put(owner, msgs);
            }
         }
      } catch (SQLException e) {
         e.printStackTrace();
         System.out.println("Could not load transaction messages from database. Skipping.");
      }

   }

   public static void send(String player, String message) {
      Player p = Bukkit.getPlayerExact(player);
      if (p == null) {
         player = player.toLowerCase();
         LinkedList<String> msgs = (LinkedList)player_messages.get(player);
         if (msgs == null) {
            msgs = new LinkedList();
            player_messages.put(player, msgs);
         }

         msgs.add(message);
         String q = "INSERT INTO messages (owner, message, time) VALUES (?, ?, ?)";
         plugin.getDB().execute(q, player, message, System.currentTimeMillis());
      } else {
         p.sendMessage(message);
      }

   }

   public static void clean() {
      System.out.println("Cleaning purchase messages from database that are over a week old...");
      long weekAgo = System.currentTimeMillis() - 604800000L;
      plugin.getDB().execute("DELETE FROM messages WHERE time < ?", weekAgo);
   }

   public static boolean flush(Player p) {
      if (p != null && p.isOnline()) {
         String pName = p.getName().toLowerCase();
         LinkedList<String> msgs = (LinkedList)player_messages.get(pName);
         if (msgs != null) {
            for(String msg : msgs) {
               p.sendMessage(msg);
            }

            plugin.getDB().execute("DELETE FROM messages WHERE owner = ?", pName);
            msgs.clear();
         }

         return true;
      } else {
         return false;
      }
   }

   public static void sendShopInfo(Player p, Shop shop) {
      sendShopInfo(p, shop, shop.getRemainingStock());
   }

   public static void sendShopInfo(Player p, Shop shop, int stock) {
      ItemStack items = shop.getItem();
      p.sendMessage("");
      p.sendMessage("");
      p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
      p.sendMessage(ChatColor.DARK_PURPLE + "| " + getMessage("menu.shop-information"));
      p.sendMessage(ChatColor.DARK_PURPLE + "| " + getMessage("menu.owner", shop.getOwner()));
      p.sendMessage(ChatColor.DARK_PURPLE + "| " + getMessage("menu.item", shop.getDataName()));
      if (Util.isTool(items.getType())) {
         p.sendMessage(ChatColor.DARK_PURPLE + "| " + getMessage("menu.damage-percent-remaining", Util.getToolPercentage(items)));
      }

      if (shop.isSelling()) {
         p.sendMessage(ChatColor.DARK_PURPLE + "| " + getMessage("menu.stock", "" + stock));
      } else {
         int space = shop.getRemainingSpace();
         p.sendMessage(ChatColor.DARK_PURPLE + "| " + getMessage("menu.space", "" + space));
      }

      p.sendMessage(ChatColor.DARK_PURPLE + "| " + getMessage("menu.price-per", shop.getDataName(), Util.format(shop.getPrice())));
      if (shop.isBuying()) {
         p.sendMessage(ChatColor.DARK_PURPLE + "| " + getMessage("menu.this-shop-is-buying"));
      } else {
         p.sendMessage(ChatColor.DARK_PURPLE + "| " + getMessage("menu.this-shop-is-selling"));
      }

      Map<Enchantment, Integer> enchs = items.getItemMeta().getEnchants();
      if (enchs != null && !enchs.isEmpty()) {
         p.sendMessage(ChatColor.DARK_PURPLE + "+--------------------" + getMessage("menu.enchants") + "-----------------------+");

         for(Map.Entry entries : enchs.entrySet()) {
            p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW + UtilNames.getEnchantName(((Enchantment)entries.getKey()).getId()) + " " + entries.getValue());
         }
      }

      try {
         Class.forName("org.bukkit.inventory.meta.EnchantmentStorageMeta");
         if (items.getItemMeta() instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta stor = (EnchantmentStorageMeta)items.getItemMeta();
            stor.getStoredEnchants();
            enchs = stor.getStoredEnchants();
            if (enchs != null && !enchs.isEmpty()) {
               p.sendMessage(ChatColor.DARK_PURPLE + "+-----------------" + getMessage("menu.stored-enchants") + "--------------------+");

               for(Map.Entry entries : enchs.entrySet()) {
                  p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW + UtilNames.getEnchantName(((Enchantment)entries.getKey()).getId()) + " " + entries.getValue());
               }
            }
         }
      } catch (ClassNotFoundException var8) {
      }

      p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
   }

   public static void sendPurchaseSuccess(Player p, Shop shop, int amount) {
      p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
      p.sendMessage(ChatColor.DARK_PURPLE + "| " + getMessage("menu.successful-purchase"));
      p.sendMessage(ChatColor.DARK_PURPLE + "| " + getMessage("menu.item-name-and-price", "" + amount, shop.getDataName(), Util.format((double)amount * shop.getPrice())));
      Map<Enchantment, Integer> enchs = shop.getItem().getItemMeta().getEnchants();
      if (enchs != null && !enchs.isEmpty()) {
         p.sendMessage(ChatColor.DARK_PURPLE + "+--------------------" + getMessage("menu.enchants") + "-----------------------+");

         for(Map.Entry entries : enchs.entrySet()) {
            p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW + UtilNames.getEnchantName(((Enchantment)entries.getKey()).getId()) + " " + entries.getValue());
         }
      }

      enchs = shop.getItem().getItemMeta().getEnchants();
      if (enchs != null && !enchs.isEmpty()) {
         p.sendMessage(ChatColor.DARK_PURPLE + "+-----------------" + getMessage("menu.stored-enchants") + "--------------------+");

         for(Map.Entry entries : enchs.entrySet()) {
            p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW + UtilNames.getEnchantName(((Enchantment)entries.getKey()).getId()) + " " + entries.getValue());
         }
      }

      try {
         Class.forName("org.bukkit.inventory.meta.EnchantmentStorageMeta");
         if (shop.getItem().getItemMeta() instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta stor = (EnchantmentStorageMeta)shop.getItem().getItemMeta();
            stor.getStoredEnchants();
            enchs = stor.getStoredEnchants();
            if (enchs != null && !enchs.isEmpty()) {
               p.sendMessage(ChatColor.DARK_PURPLE + "+-----------------" + getMessage("menu.stored-enchants") + "--------------------+");

               for(Map.Entry entries : enchs.entrySet()) {
                  p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW + UtilNames.getEnchantName(((Enchantment)entries.getKey()).getId()) + " " + entries.getValue());
               }
            }
         }
      } catch (ClassNotFoundException var7) {
      }

      p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
   }

   public static void sendSellSuccess(Player p, Shop shop, int amount) {
      p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
      p.sendMessage(ChatColor.DARK_PURPLE + "| " + getMessage("menu.successfully-sold"));
      p.sendMessage(ChatColor.DARK_PURPLE + "| " + getMessage("menu.item-name-and-price", "" + amount, shop.getDataName(), Util.format((double)amount * shop.getPrice())));
      if (plugin.getConfig().getBoolean("show-tax")) {
         double tax = plugin.getConfig().getDouble("tax");
         double total = (double)amount * shop.getPrice();
         if (tax != (double)0.0F) {
            if (!p.getName().equalsIgnoreCase(shop.getOwner())) {
               p.sendMessage(ChatColor.DARK_PURPLE + "| " + getMessage("menu.sell-tax", Util.format(tax * total)));
            } else {
               p.sendMessage(ChatColor.DARK_PURPLE + "| " + getMessage("menu.sell-tax-self"));
            }
         }
      }

      Map<Enchantment, Integer> enchs = shop.getItem().getItemMeta().getEnchants();
      if (enchs != null && !enchs.isEmpty()) {
         p.sendMessage(ChatColor.DARK_PURPLE + "+--------------------" + getMessage("menu.enchants") + "-----------------------+");

         for(Map.Entry entries : enchs.entrySet()) {
            p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW + UtilNames.getEnchantName(((Enchantment)entries.getKey()).getId()) + " " + entries.getValue());
         }
      }

      try {
         Class.forName("org.bukkit.inventory.meta.EnchantmentStorageMeta");
         if (shop.getItem().getItemMeta() instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta stor = (EnchantmentStorageMeta)shop.getItem().getItemMeta();
            stor.getStoredEnchants();
            enchs = stor.getStoredEnchants();
            if (enchs != null && !enchs.isEmpty()) {
               p.sendMessage(ChatColor.DARK_PURPLE + "+--------------------" + getMessage("menu.stored-enchants") + "-----------------------+");

               for(Map.Entry entries : enchs.entrySet()) {
                  p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW + UtilNames.getEnchantName(((Enchantment)entries.getKey()).getId()) + " " + entries.getValue());
               }
            }
         }
      } catch (ClassNotFoundException var7) {
      }

      p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
   }

   public static String getMessage(String loc, String... args) {
      String raw = messages.getString(loc);
      if (raw != null && !raw.isEmpty()) {
         if (args == null) {
            return raw;
         } else {
            for(int i = 0; i < args.length; ++i) {
               raw = raw.replace("{" + i + "}", args[i]);
            }

            return raw;
         }
      } else {
         return "Invalid message: " + loc;
      }
   }
}
