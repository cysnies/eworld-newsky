package event;

import land.Land;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LandCreateEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private Land land;

   public LandCreateEvent(Land land) {
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
