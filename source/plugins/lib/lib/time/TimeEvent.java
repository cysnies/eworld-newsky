package lib.time;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TimeEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private static long Time;

   public TimeEvent() {
      ++Time;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public static long getTime() {
      return Time;
   }
}
