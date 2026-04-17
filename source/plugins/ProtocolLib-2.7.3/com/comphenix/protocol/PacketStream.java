package com.comphenix.protocol;

import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketContainer;
import java.lang.reflect.InvocationTargetException;
import org.bukkit.entity.Player;

public interface PacketStream {
   void sendServerPacket(Player var1, PacketContainer var2) throws InvocationTargetException;

   void sendServerPacket(Player var1, PacketContainer var2, boolean var3) throws InvocationTargetException;

   void sendServerPacket(Player var1, PacketContainer var2, NetworkMarker var3, boolean var4) throws InvocationTargetException;

   void recieveClientPacket(Player var1, PacketContainer var2) throws IllegalAccessException, InvocationTargetException;

   void recieveClientPacket(Player var1, PacketContainer var2, boolean var3) throws IllegalAccessException, InvocationTargetException;

   void recieveClientPacket(Player var1, PacketContainer var2, NetworkMarker var3, boolean var4) throws IllegalAccessException, InvocationTargetException;
}
