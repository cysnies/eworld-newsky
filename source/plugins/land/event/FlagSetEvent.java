package event;

import land.Land;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FlagSetEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private Land land;
   private String flagName;
   private int oldValue;
   private int newValue;

   public FlagSetEvent(Land land, String flagName, int oldValue, int newValue) {
      this.land = land;
      this.flagName = flagName;
      this.oldValue = oldValue;
      this.newValue = newValue;
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

   public int getOldValue() {
      return this.oldValue;
   }

   public int getNewValue() {
      return this.newValue;
   }
}
