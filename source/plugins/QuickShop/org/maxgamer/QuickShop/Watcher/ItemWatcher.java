package org.maxgamer.QuickShop.Watcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Shop.Shop;
import org.maxgamer.QuickShop.Shop.ShopChunk;

public class ItemWatcher implements Runnable {
   private QuickShop plugin;

   public ItemWatcher(QuickShop plugin) {
      this.plugin = plugin;
   }

   public void run() {
      List<Shop> toRemove = new ArrayList(1);

      for(Map.Entry inWorld : this.plugin.getShopManager().getShops().entrySet()) {
         World world = Bukkit.getWorld((String)inWorld.getKey());
         if (world != null) {
            for(Map.Entry inChunk : ((HashMap)inWorld.getValue()).entrySet()) {
               if (world.isChunkLoaded(((ShopChunk)inChunk.getKey()).getX(), ((ShopChunk)inChunk.getKey()).getZ())) {
                  for(Shop shop : ((HashMap)inChunk.getValue()).values()) {
                     if (!shop.isValid()) {
                        toRemove.add(shop);
                     }
                  }
               }
            }
         }
      }

      for(Shop shop : toRemove) {
         shop.delete();
      }

   }
}
