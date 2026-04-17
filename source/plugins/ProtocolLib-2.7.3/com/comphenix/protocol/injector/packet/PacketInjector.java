package com.comphenix.protocol.injector.packet;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import java.util.Set;
import org.bukkit.entity.Player;

public interface PacketInjector {
   boolean isCancelled(Object var1);

   void setCancelled(Object var1, boolean var2);

   boolean addPacketHandler(int var1);

   boolean removePacketHandler(int var1);

   boolean hasPacketHandler(int var1);

   void inputBuffersChanged(Set var1);

   Set getPacketHandlers();

   PacketEvent packetRecieved(PacketContainer var1, Player var2, byte[] var3);

   void cleanupAll();
}
