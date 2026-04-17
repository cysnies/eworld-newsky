package org.maxgamer.QuickShop.Shop;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Util.NMS;

public class DisplayItem {
   private Shop shop;
   private ItemStack iStack;
   private Item item;

   public DisplayItem(Shop shop, ItemStack iStack) {
      this.shop = shop;
      this.iStack = iStack.clone();
   }

   public void spawn() {
      if (this.shop.getLocation().getWorld() != null) {
         Location dispLoc = this.getDisplayLocation();
         this.item = this.shop.getLocation().getWorld().dropItem(dispLoc, this.iStack);
         this.item.setVelocity(new Vector((double)0.0F, 0.1, (double)0.0F));
         if (QuickShop.instance.debug) {
            System.out.println("Spawned item. Safeguarding.");
         }

         try {
            NMS.safeGuard(this.item);
         } catch (Exception e) {
            e.printStackTrace();
            System.out.println("QuickShop version mismatch! This version of QuickShop is incompatible with this version of bukkit! Try update?");
         }

      }
   }

   public void respawn() {
      this.remove();
      this.spawn();
   }

   public boolean removeDupe() {
      if (this.shop.getLocation().getWorld() == null) {
         return false;
      } else {
         QuickShop qs = (QuickShop)Bukkit.getPluginManager().getPlugin("QuickShop");
         Location displayLoc = this.shop.getLocation().getBlock().getRelative(0, 1, 0).getLocation();
         boolean removed = false;
         Chunk c = displayLoc.getChunk();

         Entity[] var8;
         for(Entity e : var8 = c.getEntities()) {
            if (e instanceof Item && (this.item == null || e.getEntityId() != this.item.getEntityId())) {
               Location eLoc = e.getLocation().getBlock().getLocation();
               if (eLoc.equals(displayLoc) || eLoc.equals(this.shop.getLocation())) {
                  ItemStack near = ((Item)e).getItemStack();
                  if (this.shop.matches(near)) {
                     e.remove();
                     removed = true;
                     if (qs.debug) {
                        System.out.println("Removed rogue item: " + near.getType());
                     }
                  }
               }
            }
         }

         return removed;
      }
   }

   public void remove() {
      if (this.item != null) {
         this.item.remove();
      }
   }

   public Location getDisplayLocation() {
      return this.shop.getLocation().clone().add((double)0.5F, 1.2, (double)0.5F);
   }

   public Item getItem() {
      return this.item;
   }
}
