package avoidDrop;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.jline.internal.InputStreamReader;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

public class AvoidDrop extends JavaPlugin implements Listener {
   private static HashMap banDropLineInvs;

   public void onEnable() {
      loadConfig();
      Bukkit.getPluginManager().registerEvents(this, this);
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onInventoryClose(InventoryCloseEvent e) {
      if (e.getPlayer() instanceof Player) {
         final Player p = (Player)e.getPlayer();
         PlayerInventory pi = p.getInventory();
         boolean result = false;
         Inventory inv = e.getPlayer().getOpenInventory().getTopInventory();
         ItemStack cursorItem = p.getItemOnCursor();
         if (cursorItem != null && cursorItem.getType() != Material.AIR && addItem(pi, cursorItem)) {
            result = true;
            p.setItemOnCursor((ItemStack)null);
         }

         if (banDropLineInvs.containsKey(inv.getType())) {
            int ignoreSlot = -1;
            if (inv.getType() != InventoryType.CRAFTING && inv.getType() != InventoryType.WORKBENCH) {
               if (inv.getType() == InventoryType.ANVIL || inv.getType() == InventoryType.MERCHANT) {
                  ignoreSlot = 2;
               }
            } else {
               ignoreSlot = 0;
            }

            for(int i = 0; i < inv.getSize(); ++i) {
               if (i != ignoreSlot) {
                  ItemStack is = inv.getItem(i);
                  if (is != null && is.getType() != Material.AIR) {
                     if (!addItem(pi, is)) {
                        break;
                     }

                     result = true;
                     inv.setItem(i, (ItemStack)null);
                  }
               }
            }
         }

         if (result) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
               public void run() {
                  if (p.isOnline() && !p.isDead()) {
                     p.updateInventory();
                  }

               }
            });
         }
      }

   }

   public static YamlConfiguration loadConfigByUTF8(InputStream is) {
      YamlConfiguration config = new YamlConfiguration();

      try {
         InputStreamReader reader = new InputStreamReader(is, Charset.forName("utf-8"));
         StringBuilder builder = new StringBuilder();
         BufferedReader input = new BufferedReader(reader);

         String line;
         try {
            while((line = input.readLine()) != null) {
               builder.append(line);
               builder.append('\n');
            }
         } finally {
            input.close();
         }

         config.loadFromString(builder.toString());
         return config;
      } catch (FileNotFoundException var12) {
         return null;
      } catch (IOException var13) {
         return null;
      } catch (InvalidConfigurationException var14) {
         return null;
      }
   }

   private static boolean addItem(PlayerInventory pi, ItemStack is) {
      for(int index = 0; index < pi.getSize(); ++index) {
         if (pi.getItem(index) == null || pi.getItem(index).getType() == Material.AIR) {
            pi.setItem(index, is);
            return true;
         }
      }

      return false;
   }

   private static void loadConfig() {
      YamlConfiguration config = loadConfigByUTF8(AvoidDrop.class.getResourceAsStream("config.yml"));
      if (config != null) {
         banDropLineInvs = new HashMap();

         for(String s : config.getStringList("invs")) {
            banDropLineInvs.put(Enum.valueOf(InventoryType.class, s), true);
         }

      }
   }
}
