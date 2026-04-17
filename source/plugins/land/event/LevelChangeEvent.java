package event;

import land.Land;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LevelChangeEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private Land land;
   private int oldLevel;

   public LevelChangeEvent(Land land, int oldLevel) {
      this.land = land;
      this.oldLevel = oldLevel;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public int getOldLevel() {
      return this.oldLevel;
   }

   public Land getLand() {
      return this.land;
   }
}
