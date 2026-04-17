package com.comphenix.protocol.injector.spigot;

import com.comphenix.protocol.concurrency.IntegerSet;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.packet.PacketInjector;
import com.google.common.collect.Sets;
import java.util.Set;
import org.bukkit.entity.Player;

class DummyPacketInjector implements PacketInjector {
   private SpigotPacketInjector injector;
   private IntegerSet reveivedFilters;
   private IntegerSet lastBufferedPackets = new IntegerSet(256);

   public DummyPacketInjector(SpigotPacketInjector injector, IntegerSet reveivedFilters) {
      this.injector = injector;
      this.reveivedFilters = reveivedFilters;
   }

   public boolean isCancelled(Object packet) {
      return false;
   }

   public void setCancelled(Object packet, boolean cancelled) {
      throw new UnsupportedOperationException();
   }

   public void inputBuffersChanged(Set set) {
      Set<Integer> removed = Sets.difference(this.lastBufferedPackets.toSet(), set);
      Set<Integer> added = Sets.difference(set, this.lastBufferedPackets.toSet());

      for(int packet : removed) {
         this.injector.getProxyPacketInjector().removePacketHandler(packet);
      }

      for(int packet : added) {
         this.injector.getProxyPacketInjector().addPacketHandler(packet);
      }

   }

   public boolean addPacketHandler(int packetID) {
      this.reveivedFilters.add(packetID);
      return true;
   }

   public boolean removePacketHandler(int packetID) {
      this.reveivedFilters.remove(packetID);
      return true;
   }

   public boolean hasPacketHandler(int packetID) {
      return this.reveivedFilters.contains(packetID);
   }

   public Set getPacketHandlers() {
      return this.reveivedFilters.toSet();
   }

   public PacketEvent packetRecieved(PacketContainer packet, Player client, byte[] buffered) {
      return this.injector.packetReceived(packet, client, buffered);
   }

   public void cleanupAll() {
      this.reveivedFilters.clear();
   }
}
