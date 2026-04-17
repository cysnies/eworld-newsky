package event;

import land.Land;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FlagAddEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private Land land;
   private String flagName;
   private int flagValue;

   public FlagAddEvent(Land land, String flagName, int flagValue) {
      this.land = land;
      this.flagName = flagName;
      this.flagValue = flagValue;
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

   public int getFlagValue() {
      return this.flagValue;
   }
}
