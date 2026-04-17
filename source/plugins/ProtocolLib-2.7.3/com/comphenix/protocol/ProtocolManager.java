package com.comphenix.protocol;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.PacketConstructor;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.collect.ImmutableSet;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public interface ProtocolManager extends PacketStream {
   void sendServerPacket(Player var1, PacketContainer var2, boolean var3) throws InvocationTargetException;

   void recieveClientPacket(Player var1, PacketContainer var2, boolean var3) throws IllegalAccessException, InvocationTargetException;

   void broadcastServerPacket(PacketContainer var1);

   void broadcastServerPacket(PacketContainer var1, Entity var2, boolean var3);

   void broadcastServerPacket(PacketContainer var1, Location var2, int var3);

   ImmutableSet getPacketListeners();

   void addPacketListener(PacketListener var1);

   void removePacketListener(PacketListener var1);

   void removePacketListeners(Plugin var1);

   PacketContainer createPacket(int var1);

   PacketContainer createPacket(int var1, boolean var2);

   PacketConstructor createPacketConstructor(int var1, Object... var2);

   void updateEntity(Entity var1, List var2) throws FieldAccessException;

   Entity getEntityFromID(World var1, int var2) throws FieldAccessException;

   List getEntityTrackers(Entity var1) throws FieldAccessException;

   Set getSendingFilters();

   Set getReceivingFilters();

   MinecraftVersion getMinecraftVersion();

   boolean isClosed();

   AsynchronousManager getAsynchronousManager();
}
