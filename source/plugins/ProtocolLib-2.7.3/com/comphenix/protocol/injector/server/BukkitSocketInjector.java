package com.comphenix.protocol.injector.server;

import com.comphenix.protocol.events.NetworkMarker;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.entity.Player;

public class BukkitSocketInjector implements SocketInjector {
   private Player player;
   private List syncronizedQueue = Collections.synchronizedList(new ArrayList());

   public BukkitSocketInjector(Player player) {
      if (player == null) {
         throw new IllegalArgumentException("Player cannot be NULL.");
      } else {
         this.player = player;
      }
   }

   public Socket getSocket() throws IllegalAccessException {
      throw new UnsupportedOperationException("Cannot get socket from Bukkit player.");
   }

   public SocketAddress getAddress() throws IllegalAccessException {
      return this.player.getAddress();
   }

   public void disconnect(String message) throws InvocationTargetException {
      this.player.kickPlayer(message);
   }

   public void sendServerPacket(Object packet, NetworkMarker marker, boolean filtered) throws InvocationTargetException {
      QueuedSendPacket command = new QueuedSendPacket(packet, marker, filtered);
      this.syncronizedQueue.add(command);
   }

   public Player getPlayer() {
      return this.player;
   }

   public Player getUpdatedPlayer() {
      return this.player;
   }

   public void transferState(SocketInjector delegate) {
      try {
         synchronized(this.syncronizedQueue) {
            for(QueuedSendPacket command : this.syncronizedQueue) {
               delegate.sendServerPacket(command.getPacket(), command.getMarker(), command.isFiltered());
            }

            this.syncronizedQueue.clear();
         }
      } catch (InvocationTargetException e) {
         throw new RuntimeException("Unable to transmit packets to " + delegate + " from old injector.", e);
      }
   }

   public void setUpdatedPlayer(Player updatedPlayer) {
      this.player = updatedPlayer;
   }
}
