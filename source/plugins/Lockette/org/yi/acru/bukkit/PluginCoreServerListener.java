package org.yi.acru.bukkit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.PluginManager;

public class PluginCoreServerListener implements Listener {
   private static PluginCore plugin;

   public PluginCoreServerListener(PluginCore instance) {
      plugin = instance;
   }

   protected void registerEvents() {
      PluginManager pm = plugin.getServer().getPluginManager();
      pm.registerEvents(this, plugin);
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPluginEnable(PluginEnableEvent event) {
      plugin.setLink(event.getPlugin().getDescription().getName(), true, true);
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPluginDisable(PluginDisableEvent event) {
      plugin.setLink(event.getPlugin().getDescription().getName(), false, true);
   }
}
