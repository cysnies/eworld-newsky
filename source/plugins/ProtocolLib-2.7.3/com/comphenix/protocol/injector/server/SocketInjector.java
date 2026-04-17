package com.comphenix.protocol.injector.server;

import com.comphenix.protocol.events.NetworkMarker;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.SocketAddress;
import org.bukkit.entity.Player;

public interface SocketInjector {
   Socket getSocket() throws IllegalAccessException;

   SocketAddress getAddress() throws IllegalAccessException;

   void disconnect(String var1) throws InvocationTargetException;

   void sendServerPacket(Object var1, NetworkMarker var2, boolean var3) throws InvocationTargetException;

   Player getPlayer();

   Player getUpdatedPlayer();

   void transferState(SocketInjector var1);

   void setUpdatedPlayer(Player var1);
}
