package event;

import land.Land;
import land.Range;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RangeChangeEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private Land land;
   private Range oldRange;

   public RangeChangeEvent(Land land, Range oldRange) {
      this.land = land;
      this.oldRange = oldRange;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public Range getOldRange() {
      return this.oldRange;
   }

   public Land getLand() {
      return this.land;
   }
}
