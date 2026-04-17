package org.anjocaido.groupmanager.events;

import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GMSystemEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   protected Action action;

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public GMSystemEvent(Action action) {
      this.action = action;
   }

   public Action getAction() {
      return this.action;
   }

   public void schedule(final GMSystemEvent event) {
      synchronized(GroupManager.getGMEventHandler().getServer()) {
         if (GroupManager.getGMEventHandler().getServer().getScheduler().scheduleSyncDelayedTask(GroupManager.getGMEventHandler().getPlugin(), new Runnable() {
            public void run() {
               GroupManager.getGMEventHandler().getServer().getPluginManager().callEvent(event);
            }
         }, 1L) == -1) {
            GroupManager.logger.warning("Could not schedule GM Event.");
         }

      }
   }

   public static enum Action {
      RELOADED,
      SAVED,
      DEFAULT_GROUP_CHANGED,
      VALIDATE_TOGGLE;
   }
}
