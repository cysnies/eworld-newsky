package event;

import land.Land;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FlagRemoveEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private Land land;
   private String flagName;

   public FlagRemoveEvent(Land land, String flagName) {
      this.land = land;
      this.flagName = flagName;
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

   public String getFlagName() {
      return this.flagName;
   }
}
