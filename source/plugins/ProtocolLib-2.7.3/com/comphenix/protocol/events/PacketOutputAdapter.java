package com.comphenix.protocol.events;

import org.bukkit.plugin.Plugin;

public abstract class PacketOutputAdapter implements PacketOutputHandler {
   private final Plugin plugin;
   private final ListenerPriority priority;

   public PacketOutputAdapter(Plugin plugin, ListenerPriority priority) {
      this.priority = priority;
      this.plugin = plugin;
   }

   public Plugin getPlugin() {
      return this.plugin;
   }

   public ListenerPriority getPriority() {
      return this.priority;
   }
}
