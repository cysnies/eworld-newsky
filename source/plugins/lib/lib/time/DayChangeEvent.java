package lib.time;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DayChangeEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private World w;
   private State to;

   public DayChangeEvent(World w, State to) {
      this.w = w;
      this.to = to;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public World getW() {
      return this.w;
   }

   public State getTo() {
      return this.to;
   }

   public static enum State {
      day,
      night;
   }
}
