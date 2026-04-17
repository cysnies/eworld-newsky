package com.comphenix.protocol.events;

import org.bukkit.plugin.Plugin;

public interface PacketOutputHandler {
   ListenerPriority getPriority();

   Plugin getPlugin();

   byte[] handle(PacketEvent var1, byte[] var2);
}
