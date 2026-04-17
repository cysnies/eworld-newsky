package event;

import land.Land;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SellChangeEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private Land land;

   public SellChangeEvent(Land land) {
      this.land = land;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public Land getLand() {
      return this.land;
   }
}
