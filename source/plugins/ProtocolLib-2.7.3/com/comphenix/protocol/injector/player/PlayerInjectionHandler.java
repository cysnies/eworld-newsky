package com.comphenix.protocol.injector.player;

import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.injector.PacketFilterManager;
import java.io.DataInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.Set;
import org.bukkit.entity.Player;

public interface PlayerInjectionHandler {
   PacketFilterManager.PlayerInjectHooks getPlayerHook();

   PacketFilterManager.PlayerInjectHooks getPlayerHook(GamePhase var1);

   void setPlayerHook(PacketFilterManager.PlayerInjectHooks var1);

   void setPlayerHook(GamePhase var1, PacketFilterManager.PlayerInjectHooks var2);

   void addPacketHandler(int var1);

   void removePacketHandler(int var1);

   Player getPlayerByConnection(DataInputStream var1) throws InterruptedException;

   void injectPlayer(Player var1, ConflictStrategy var2);

   void handleDisconnect(Player var1);

   boolean uninjectPlayer(Player var1);

   boolean uninjectPlayer(InetSocketAddress var1);

   void sendServerPacket(Player var1, PacketContainer var2, NetworkMarker var3, boolean var4) throws InvocationTargetException;

   void recieveClientPacket(Player var1, Object var2) throws IllegalAccessException, InvocationTargetException;

   void updatePlayer(Player var1);

   void checkListener(Set var1);

   void checkListener(PacketListener var1);

   Set getSendingFilters();

   boolean canRecievePackets();

   PacketEvent handlePacketRecieved(PacketContainer var1, InputStream var2, byte[] var3);

   void close();

   public static enum ConflictStrategy {
      OVERRIDE,
      BAIL_OUT;
   }
}
