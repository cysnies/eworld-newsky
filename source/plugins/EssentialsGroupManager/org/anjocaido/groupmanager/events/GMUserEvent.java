package org.anjocaido.groupmanager.events;

import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.data.User;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GMUserEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   protected User user;
   protected String userName;
   protected Action action;

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public GMUserEvent(User user, Action action) {
      this.user = user;
      this.action = action;
      this.userName = user.getName();
   }

   public GMUserEvent(String userName, Action action) {
      this.userName = userName;
      this.action = action;
   }

   public Action getAction() {
      return this.action;
   }

   public User getUser() {
      return this.user;
   }

   public String getUserName() {
      return this.userName;
   }

   public void schedule(final GMUserEvent event) {
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
      USER_PERMISSIONS_CHANGED,
      USER_INHERITANCE_CHANGED,
      USER_INFO_CHANGED,
      USER_GROUP_CHANGED,
      USER_SUBGROUP_CHANGED,
      USER_ADDED,
      USER_REMOVED;
   }
}
