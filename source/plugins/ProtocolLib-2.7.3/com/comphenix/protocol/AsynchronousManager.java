package com.comphenix.protocol;

import com.comphenix.protocol.async.AsyncListenerHandler;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import java.util.Set;
import org.bukkit.plugin.Plugin;

public interface AsynchronousManager {
   AsyncListenerHandler registerAsyncHandler(PacketListener var1);

   void unregisterAsyncHandler(AsyncListenerHandler var1);

   void unregisterAsyncHandlers(Plugin var1);

   Set getSendingFilters();

   Set getReceivingFilters();

   boolean hasAsynchronousListeners(PacketEvent var1);

   PacketStream getPacketStream();

   ErrorReporter getErrorReporter();

   void cleanupAll();

   void signalPacketTransmission(PacketEvent var1);

   void registerTimeoutHandler(PacketListener var1);

   void unregisterTimeoutHandler(PacketListener var1);

   Set getTimeoutHandlers();
}
