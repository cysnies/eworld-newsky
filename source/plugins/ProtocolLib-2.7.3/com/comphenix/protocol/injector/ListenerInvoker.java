package com.comphenix.protocol.injector;

import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.packet.InterceptWritePacket;

public interface ListenerInvoker {
   void invokePacketRecieving(PacketEvent var1);

   void invokePacketSending(PacketEvent var1);

   int getPacketID(Object var1);

   InterceptWritePacket getInterceptWritePacket();

   boolean requireInputBuffer(int var1);

   void unregisterPacketClass(Class var1);

   void registerPacketClass(Class var1, int var2);

   Class getPacketClassFromID(int var1, boolean var2);
}
