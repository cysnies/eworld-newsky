package org.maxgamer.QuickShop.Shop;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ShopPreCreateEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private boolean cancelled;
   private Player p;
   private Location loc;

   public ShopPreCreateEvent(Player p, Location loc) {
      this.loc = loc;
      this.p = p;
   }

   public Location getLocation() {
      return this.loc;
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
