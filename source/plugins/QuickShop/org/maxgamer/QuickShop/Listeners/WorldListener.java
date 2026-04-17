package org.maxgamer.QuickShop.Listeners;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Shop.Shop;
import org.maxgamer.QuickShop.Shop.ShopChunk;

public class WorldListener implements Listener {
   QuickShop plugin;

   public WorldListener(QuickShop plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent e) {
      World world = e.getWorld();
      HashMap<ShopChunk, HashMap<Location, Shop>> inWorld = new HashMap(1);
      HashMap<ShopChunk, HashMap<Location, Shop>> oldInWorld = this.plugin.getShopManager().getShops(world.getName());
      if (oldInWorld != null) {
         for(Map.Entry oldInChunk : oldInWorld.entrySet()) {
            HashMap<Location, Shop> inChunk = new HashMap(1);
            inWorld.put((ShopChunk)oldInChunk.getKey(), inChunk);

            for(Map.Entry entry : ((HashMap)oldInChunk.getValue()).entrySet()) {
               Shop shop = (Shop)entry.getValue();
               shop.getLocation().setWorld(world);
               inChunk.put(shop.getLocation(), shop);
            }
         }

         this.plugin.getShopManager().getShops().put(world.getName(), inWorld);

         Chunk[] var15;
         for(Chunk chunk : var15 = world.getLoadedChunks()) {
            HashMap<Location, Shop> inChunk = this.plugin.getShopManager().getShops(chunk);
            if (inChunk != null) {
               for(Shop shop : inChunk.values()) {
                  shop.onLoad();
               }
            }
         }

      }
   }

   @EventHandler
   public void onWorldUnload(WorldUnloadEvent e) {
      Chunk[] var5;
      for(Chunk chunk : var5 = e.getWorld().getLoadedChunks()) {
         HashMap<Location, Shop> inChunk = this.plugin.getShopManager().getShops(chunk);
         if (inChunk != null) {
            for(Shop shop : inChunk.values()) {
               shop.onUnload();
            }
         }
      }

   }
}
