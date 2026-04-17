package com.comphenix.protocol.events;

import org.bukkit.plugin.Plugin;

public interface PacketListener {
   void onPacketSending(PacketEvent var1);

   void onPacketReceiving(PacketEvent var1);

   ListeningWhitelist getSendingWhitelist();

   ListeningWhitelist getReceivingWhitelist();

   Plugin getPlugin();
}
