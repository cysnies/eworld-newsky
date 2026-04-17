package com.comphenix.protocol.injector.player;

import com.comphenix.protocol.concurrency.IntegerSet;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.injector.PacketFilterManager;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.VolatileField;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.collect.Sets;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

class NetworkFieldInjector extends PlayerInjector {
   private MinecraftVersion safeVersion = new MinecraftVersion("1.4.4");
   private Set ignoredPackets = Sets.newSetFromMap(new ConcurrentHashMap());
   private List overridenLists = new ArrayList();
   private static Field syncField;
   private Object syncObject;
   private IntegerSet sendingFilters;

   public NetworkFieldInjector(ClassLoader classLoader, ErrorReporter reporter, Player player, ListenerInvoker manager, IntegerSet sendingFilters) throws IllegalAccessException {
      super(classLoader, reporter, player, manager);
      this.sendingFilters = sendingFilters;
   }

   protected boolean hasListener(int packetID) {
      return this.sendingFilters.contains(packetID);
   }

   public synchronized void initialize(Object injectionSource) throws IllegalAccessException {
      super.initialize(injectionSource);
      if (this.hasInitialized) {
         if (syncField == null) {
            syncField = FuzzyReflection.fromObject(this.networkManager, true).getFieldByType("java\\.lang\\.Object");
         }

         this.syncObject = FieldUtils.readField(syncField, this.networkManager, true);
      }

   }

   public void sendServerPacket(Object packet, NetworkMarker marker, boolean filtered) throws InvocationTargetException {
      if (this.networkManager != null) {
         try {
            if (!filtered) {
               this.ignoredPackets.add(packet);
            }

            if (marker != null) {
               this.queuedMarkers.put(packet, marker);
            }

            queueMethod.invoke(this.networkManager, packet);
         } catch (IllegalArgumentException e) {
            throw e;
         } catch (InvocationTargetException e) {
            throw e;
         } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to access queue method.", e);
         }
      } else {
         throw new IllegalStateException("Unable to load network mananager. Cannot send packet.");
      }
   }

   public UnsupportedListener checkListener(MinecraftVersion version, PacketListener listener) {
      if (version != null && version.compareTo(this.safeVersion) > 0) {
         return null;
      } else {
         int[] unsupported = new int[]{51, 56};
         return ListeningWhitelist.containsAny(listener.getSendingWhitelist(), unsupported) ? new UnsupportedListener("The NETWORK_FIELD_INJECTOR hook doesn't support map chunk listeners.", unsupported) : null;
      }
   }

   public void injectManager() {
      if (this.networkManager != null) {
         StructureModifier<List> list = networkModifier.withType(List.class);

         for(Field field : list.getFields()) {
            VolatileField overwriter = new VolatileField(field, this.networkManager, true);
            List<Object> minecraftList = (List)overwriter.getOldValue();
            synchronized(this.syncObject) {
               List<Object> hackedList = new InjectedArrayList(this.classLoader, this, this.ignoredPackets);

               for(Object packet : minecraftList) {
                  hackedList.add(packet);
               }

               minecraftList.clear();
               overwriter.setValue(Collections.synchronizedList(hackedList));
            }

            this.overridenLists.add(overwriter);
         }
      }

   }

   protected void cleanHook() {
      for(VolatileField overriden : this.overridenLists) {
         List<Object> minecraftList = (List)overriden.getOldValue();
         List<Object> hacketList = (List)overriden.getValue();
         if (minecraftList == hacketList) {
            return;
         }

         synchronized(this.syncObject) {
            try {
               for(Object packet : (List)overriden.getValue()) {
                  minecraftList.add(packet);
               }
            } finally {
               overriden.revertValue();
            }
         }
      }

      this.overridenLists.clear();
   }

   public void handleDisconnect() {
   }

   public boolean canInject(GamePhase phase) {
      return true;
   }

   public PacketFilterManager.PlayerInjectHooks getHookType() {
      return PacketFilterManager.PlayerInjectHooks.NETWORK_HANDLER_FIELDS;
   }

   public interface FakePacket {
   }
}
