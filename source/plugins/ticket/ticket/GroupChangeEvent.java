package ticket;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GroupChangeEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private Player p;

   public GroupChangeEvent(Player p) {
      this.p = p;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public Player getP() {
      return this.p;
   }
}
