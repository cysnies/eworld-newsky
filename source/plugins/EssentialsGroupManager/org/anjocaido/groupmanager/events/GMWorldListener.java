package org.anjocaido.groupmanager.events;

import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;

public class GMWorldListener implements Listener {
   private final GroupManager plugin;

   public GMWorldListener(GroupManager instance) {
      this.plugin = instance;
      this.registerEvents();
   }

   private void registerEvents() {
      this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onWorldInit(WorldInitEvent event) {
      String worldName = event.getWorld().getName();
      if (GroupManager.isLoaded() && !this.plugin.getWorldsHolder().isInList(worldName)) {
         GroupManager.logger.info("New world detected...");
         GroupManager.logger.info("Creating data for: " + worldName);
         if (this.plugin.getWorldsHolder().isWorldKnown("all_unnamed_worlds")) {
            String usersMirror = (String)this.plugin.getWorldsHolder().getMirrorsUser().get("all_unnamed_worlds");
            String groupsMirror = (String)this.plugin.getWorldsHolder().getMirrorsGroup().get("all_unnamed_worlds");
            if (usersMirror != null) {
               this.plugin.getWorldsHolder().getMirrorsUser().put(worldName.toLowerCase(), usersMirror);
            }

            if (groupsMirror != null) {
               this.plugin.getWorldsHolder().getMirrorsGroup().put(worldName.toLowerCase(), groupsMirror);
            }
         }

         this.plugin.getWorldsHolder().setupWorldFolder(worldName);
         this.plugin.getWorldsHolder().loadWorld(worldName);
         if (this.plugin.getWorldsHolder().isInList(worldName)) {
            GroupManager.logger.info("Don't forget to configure/mirror this world in config.yml.");
         } else {
            GroupManager.logger.severe("Failed to configure this world.");
         }
      }

   }
}
