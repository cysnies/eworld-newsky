package com.comphenix.protocol.injector;

import com.comphenix.protocol.ProtocolManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public interface InternalManager extends ProtocolManager {
   PacketFilterManager.PlayerInjectHooks getPlayerHook();

   void setPlayerHook(PacketFilterManager.PlayerInjectHooks var1);

   void registerEvents(PluginManager var1, Plugin var2);

   void close();
}
