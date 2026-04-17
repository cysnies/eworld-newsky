package com.comphenix.protocol.injector.spigot;

import com.comphenix.protocol.concurrency.IntegerSet;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.injector.PacketFilterManager;
import com.comphenix.protocol.injector.player.PlayerInjectionHandler;
import java.io.DataInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.Set;
import org.bukkit.entity.Player;

class DummyPlayerHandler implements PlayerInjectionHandler {
   private SpigotPacketInjector injector;
   private IntegerSet sendingFilters;

   public DummyPlayerHandler(SpigotPacketInjector injector, IntegerSet sendingFilters) {
      this.injector = injector;
      this.sendingFilters = sendingFilters;
   }

   public boolean uninjectPlayer(InetSocketAddress address) {
      return true;
   }

   public boolean uninjectPlayer(Player player) {
      this.injector.uninjectPlayer(player);
      return true;
   }

   public void setPlayerHook(GamePhase phase, PacketFilterManager.PlayerInjectHooks playerHook) {
      throw new UnsupportedOperationException("This is not needed in Spigot.");
   }

   public void setPlayerHook(PacketFilterManager.PlayerInjectHooks playerHook) {
      throw new UnsupportedOperationException("This is not needed in Spigot.");
   }

   public void addPacketHandler(int packetID) {
      this.sendingFilters.add(packetID);
   }

   public void removePacketHandler(int packetID) {
      this.sendingFilters.remove(packetID);
   }

   public Set getSendingFilters() {
      return this.sendingFilters.toSet();
   }

   public void close() {
      this.sendingFilters.clear();
   }

   public void sendServerPacket(Player reciever, PacketContainer packet, NetworkMarker marker, boolean filters) throws InvocationTargetException {
      this.injector.sendServerPacket(reciever, packet, marker, filters);
   }

   public void recieveClientPacket(Player player, Object mcPacket) throws IllegalAccessException, InvocationTargetException {
      this.injector.processPacket(player, mcPacket);
   }

   public void injectPlayer(Player player, PlayerInjectionHandler.ConflictStrategy strategy) {
      this.injector.injectPlayer(player);
   }

   public void handleDisconnect(Player player) {
   }

   public PacketFilterManager.PlayerInjectHooks getPlayerHook(GamePhase phase) {
      return PacketFilterManager.PlayerInjectHooks.NETWORK_SERVER_OBJECT;
   }

   public boolean canRecievePackets() {
      return true;
   }

   public PacketEvent handlePacketRecieved(PacketContainer packet, InputStream input, byte[] buffered) {
      if (buffered != null) {
         this.injector.saveBuffered(packet.getHandle(), buffered);
      }

      return null;
   }

   public PacketFilterManager.PlayerInjectHooks getPlayerHook() {
      return PacketFilterManager.PlayerInjectHooks.NETWORK_SERVER_OBJECT;
   }

   public Player getPlayerByConnection(DataInputStream inputStream) throws InterruptedException {
      throw new UnsupportedOperationException("This is not needed in Spigot.");
   }

   public void checkListener(PacketListener listener) {
   }

   public void checkListener(Set listeners) {
   }

   public void updatePlayer(Player player) {
   }
}
