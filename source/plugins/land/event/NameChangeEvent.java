package event;

import land.Land;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NameChangeEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private Land land;
   private String oldName;

   public NameChangeEvent(Land land, String oldName) {
      this.land = land;
      this.oldName = oldName;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public String getOldName() {
      return this.oldName;
   }

   public Land getLand() {
      return this.land;
   }
}
