package event;

import land.Land;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class OwnerChangeEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private Land land;
   private String oldOwner;

   public OwnerChangeEvent(Land land, String oldOwner) {
      this.land = land;
      this.oldOwner = oldOwner;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public String getOldOwner() {
      return this.oldOwner;
   }

   public Land getLand() {
      return this.land;
   }
}
