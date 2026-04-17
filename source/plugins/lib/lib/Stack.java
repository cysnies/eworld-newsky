package lib;

import java.util.HashMap;
import lib.config.ReloadConfigEvent;
import lib.util.UtilItems;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class Stack implements Listener {
   private Server server;
   private String pn;
   private HashMap stackHash;

   public Stack(Lib lib) {
      this.server = lib.getServer();
      this.pn = lib.getPn();
      this.loadConfig(lib.getCon().getConfig(this.pn));
      this.server.getPluginManager().registerEvents(this, lib);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onInventoryClick(InventoryClickEvent e) {
      if (e.getCurrentItem() != null && e.getCursor() != null) {
         ItemStack currentItem = e.getCurrentItem();
         ItemStack cursor = e.getCursor();
         if (UtilItems.isSame(currentItem, cursor) && this.stackHash.containsKey(currentItem.getTypeId())) {
            int maxAmount = (Integer)this.stackHash.get(currentItem.getTypeId());
            if (cursor.getAmount() >= maxAmount || currentItem.getAmount() >= maxAmount) {
               e.setCancelled(true);
               return;
            }

            if (cursor.getAmount() + currentItem.getAmount() > maxAmount) {
               e.setCancelled(true);
               return;
            }

            cursor.setAmount(cursor.getAmount() + currentItem.getAmount());
            e.setCurrentItem((ItemStack)null);
         }
      }

   }

   private void loadConfig(FileConfiguration config) {
      this.stackHash = new HashMap();

      for(String s : config.getStringList("stack")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         int maxAmount = Integer.parseInt(s.split(" ")[1]);
         this.stackHash.put(id, maxAmount);
      }

   }
}
