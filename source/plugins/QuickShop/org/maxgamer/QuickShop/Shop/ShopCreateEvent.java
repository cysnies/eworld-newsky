package org.maxgamer.QuickShop.Shop;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ShopCreateEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private Shop shop;
   private boolean cancelled;
   private Player p;

   public ShopCreateEvent(Shop shop, Player p) {
      this.shop = shop;
      this.p = p;
   }

   public Shop getShop() {
      return this.shop;
   }

   public Player getPlayer() {
      return this.p;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancel) {
      this.cancelled = cancel;
   }
}
