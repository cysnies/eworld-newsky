package com.comphenix.protocol.injector.packet;

import com.comphenix.net.sf.cglib.proxy.Factory;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.fuzzy.FuzzyClassContract;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.TroveWrapper;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

public class PacketRegistry {
   public static final ReportType REPORT_CANNOT_CORRECT_TROVE_MAP = new ReportType("Unable to correct no entry value.");
   public static final ReportType REPORT_INSUFFICIENT_SERVER_PACKETS = new ReportType("Too few server packets detected: %s");
   public static final ReportType REPORT_INSUFFICIENT_CLIENT_PACKETS = new ReportType("Too few client packets detected: %s");
   private static final int MIN_SERVER_PACKETS = 5;
   private static final int MIN_CLIENT_PACKETS = 5;
   private static FuzzyReflection packetRegistry;
   private static Map packetToID;
   private static Multimap customIdToPacket;
   private static Map vanillaIdToPacket;
   private static ImmutableSet serverPackets;
   private static ImmutableSet clientPackets;
   private static Set serverPacketsRef;
   private static Set clientPacketsRef;
   private static Map overwrittenPackets = new HashMap();
   private static Map previousValues = new HashMap();

   public static Map getPacketToID() {
      if (packetToID == null) {
         try {
            Field packetsField = getPacketRegistry().getFieldByType("packetsField", Map.class);
            packetToID = (Map)FieldUtils.readStaticField(packetsField, true);
         } catch (IllegalArgumentException e) {
            try {
               packetToID = getSpigotWrapper();
            } catch (Exception e2) {
               throw new IllegalArgumentException(e.getMessage() + "; Spigot workaround failed.", e2);
            }
         } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to retrieve the packetClassToIdMap", e);
         }

         customIdToPacket = InverseMaps.inverseMultimap(packetToID, new Predicate() {
            public boolean apply(@Nullable Map.Entry entry) {
               return !MinecraftReflection.isMinecraftClass((Class)entry.getKey());
            }
         });
         vanillaIdToPacket = InverseMaps.inverseMap(packetToID, new Predicate() {
            public boolean apply(@Nullable Map.Entry entry) {
               return MinecraftReflection.isMinecraftClass((Class)entry.getKey());
            }
         });
      }

      return packetToID;
   }

   private static Map getSpigotWrapper() throws IllegalAccessException {
      FuzzyClassContract mapLike = FuzzyClassContract.newBuilder().method(FuzzyMethodContract.newBuilder().nameExact("size").returnTypeExact(Integer.TYPE)).method(FuzzyMethodContract.newBuilder().nameExact("put").parameterCount(2)).method(FuzzyMethodContract.newBuilder().nameExact("get").parameterCount(1)).build();
      Field packetsField = getPacketRegistry().getField(FuzzyFieldContract.newBuilder().typeMatches(mapLike).build());
      Object troveMap = FieldUtils.readStaticField(packetsField, true);

      try {
         Field field = FieldUtils.getField(troveMap.getClass(), "no_entry_value", true);
         Integer value = (Integer)FieldUtils.readField(field, troveMap, true);
         if (value >= 0 && value < 256) {
            FieldUtils.writeField((Field)field, (Object)troveMap, -1);
         }
      } catch (IllegalArgumentException e) {
         ProtocolLibrary.getErrorReporter().reportWarning(PacketRegistry.class, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_CORRECT_TROVE_MAP).error(e));
      }

      return TroveWrapper.getDecoratedMap(troveMap);
   }

   private static FuzzyReflection getPacketRegistry() {
      if (packetRegistry == null) {
         packetRegistry = FuzzyReflection.fromClass(MinecraftReflection.getPacketClass(), true);
      }

      return packetRegistry;
   }

   public static Map getOverwrittenPackets() {
      return overwrittenPackets;
   }

   public static Map getPreviousPackets() {
      return previousValues;
   }

   public static Set getServerPackets() throws FieldAccessException {
      initializeSets();
      if (serverPackets != null && serverPackets.size() < 5) {
         throw new FieldAccessException("Server packet list is empty. Seems to be unsupported");
      } else {
         return serverPackets;
      }
   }

   public static Set getClientPackets() throws FieldAccessException {
      initializeSets();
      if (clientPackets != null && clientPackets.size() < 5) {
         throw new FieldAccessException("Client packet list is empty. Seems to be unsupported");
      } else {
         return clientPackets;
      }
   }

   private static void initializeSets() throws FieldAccessException {
      if (serverPacketsRef != null && clientPacketsRef != null) {
         if (serverPacketsRef != null && serverPacketsRef.size() != serverPackets.size()) {
            serverPackets = ImmutableSet.copyOf(serverPacketsRef);
         }

         if (clientPacketsRef != null && clientPacketsRef.size() != clientPackets.size()) {
            clientPackets = ImmutableSet.copyOf(clientPacketsRef);
         }
      } else {
         List<Field> sets = getPacketRegistry().getFieldListByType(Set.class);

         try {
            if (sets.size() <= 1) {
               throw new FieldAccessException("Cannot retrieve packet client/server sets.");
            }

            serverPacketsRef = (Set)FieldUtils.readStaticField((Field)sets.get(0), true);
            clientPacketsRef = (Set)FieldUtils.readStaticField((Field)sets.get(1), true);
            if (serverPacketsRef == null || clientPacketsRef == null) {
               throw new FieldAccessException("Packet sets are in an illegal state.");
            }

            serverPackets = ImmutableSet.copyOf(serverPacketsRef);
            clientPackets = ImmutableSet.copyOf(clientPacketsRef);
            if (serverPackets.size() < 5) {
               ProtocolLibrary.getErrorReporter().reportWarning(PacketRegistry.class, (Report.ReportBuilder)Report.newBuilder(REPORT_INSUFFICIENT_SERVER_PACKETS).messageParam(serverPackets.size()));
            }

            if (clientPackets.size() < 5) {
               ProtocolLibrary.getErrorReporter().reportWarning(PacketRegistry.class, (Report.ReportBuilder)Report.newBuilder(REPORT_INSUFFICIENT_CLIENT_PACKETS).messageParam(clientPackets.size()));
            }
         } catch (IllegalAccessException e) {
            throw new FieldAccessException("Cannot access field.", e);
         }
      }

   }

   public static Class getPacketClassFromID(int packetID) {
      return getPacketClassFromID(packetID, false);
   }

   public static Class getPacketClassFromID(int packetID, boolean forceVanilla) {
      Map<Integer, Class> lookup = forceVanilla ? previousValues : overwrittenPackets;
      Class<?> result = null;
      if (lookup.containsKey(packetID)) {
         return removeEnhancer((Class)lookup.get(packetID), forceVanilla);
      } else {
         getPacketToID();
         if (!forceVanilla) {
            result = (Class)Iterables.getFirst(customIdToPacket.get(packetID), (Object)null);
         }

         if (result == null) {
            result = (Class)vanillaIdToPacket.get(packetID);
         }

         if (result != null) {
            return result;
         } else {
            throw new IllegalArgumentException("The packet ID " + packetID + " is not registered.");
         }
      }
   }

   public static int getPacketID(Class packet) {
      if (packet == null) {
         throw new IllegalArgumentException("Packet type class cannot be NULL.");
      } else if (!MinecraftReflection.getPacketClass().isAssignableFrom(packet)) {
         throw new IllegalArgumentException("Type must be a packet.");
      } else {
         return (Integer)getPacketToID().get(packet);
      }
   }

   private static Class removeEnhancer(Class clazz, boolean remove) {
      if (remove) {
         while(Factory.class.isAssignableFrom(clazz) && !clazz.equals(Object.class)) {
            clazz = clazz.getSuperclass();
         }
      }

      return clazz;
   }
}
