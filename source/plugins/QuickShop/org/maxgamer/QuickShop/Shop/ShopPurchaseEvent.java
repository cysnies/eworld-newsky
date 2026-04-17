package org.maxgamer.QuickShop.Shop;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ShopPurchaseEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private Shop shop;
   private Player p;
   private int amount;
   private boolean cancelled;

   public ShopPurchaseEvent(Shop shop, Player p, int amount) {
      this.shop = shop;
      this.p = p;
      this.amount = amount;
   }

   public Shop getShop() {
      return this.shop;
   }

   public Player getPlayer() {
      return this.p;
   }

   public int getAmount() {
      return this.amount;
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
