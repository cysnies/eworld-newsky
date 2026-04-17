package com.comphenix.protocol.injector.player;

import com.comphenix.net.sf.cglib.proxy.Callback;
import com.comphenix.net.sf.cglib.proxy.CallbackFilter;
import com.comphenix.net.sf.cglib.proxy.Enhancer;
import com.comphenix.net.sf.cglib.proxy.LazyLoader;
import com.comphenix.net.sf.cglib.proxy.MethodInterceptor;
import com.comphenix.net.sf.cglib.proxy.MethodProxy;
import com.comphenix.protocol.concurrency.IntegerSet;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.injector.PacketFilterManager;
import com.comphenix.protocol.injector.server.TemporaryPlayerFactory;
import com.comphenix.protocol.utility.MinecraftVersion;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class NetworkObjectInjector extends PlayerInjector {
   private IntegerSet sendingFilters;
   private MinecraftVersion safeVersion = new MinecraftVersion("1.4.4");
   private static volatile CallbackFilter callbackFilter;
   private static volatile TemporaryPlayerFactory tempPlayerFactory;

   public NetworkObjectInjector(ClassLoader classLoader, ErrorReporter reporter, Player player, ListenerInvoker invoker, IntegerSet sendingFilters) throws IllegalAccessException {
      super(classLoader, reporter, player, invoker);
      this.sendingFilters = sendingFilters;
   }

   protected boolean hasListener(int packetID) {
      return this.sendingFilters.contains(packetID);
   }

   public Player createTemporaryPlayer(Server server) {
      if (tempPlayerFactory == null) {
         tempPlayerFactory = new TemporaryPlayerFactory();
      }

      return tempPlayerFactory.createTemporaryPlayer(server, this);
   }

   public void sendServerPacket(Object packet, NetworkMarker marker, boolean filtered) throws InvocationTargetException {
      Object networkDelegate = filtered ? this.networkManagerRef.getValue() : this.networkManagerRef.getOldValue();
      if (networkDelegate != null) {
         try {
            if (marker != null) {
               this.queuedMarkers.put(packet, marker);
            }

            queueMethod.invoke(networkDelegate, packet);
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
         return ListeningWhitelist.containsAny(listener.getSendingWhitelist(), unsupported) ? new UnsupportedListener("The NETWORK_OBJECT_INJECTOR hook doesn't support map chunk listeners.", unsupported) : null;
      }
   }

   public void injectManager() {
      if (this.networkManager != null) {
         Class<?> networkInterface = this.networkManagerRef.getField().getType();
         final Object networkDelegate = this.networkManagerRef.getOldValue();
         if (!networkInterface.isInterface()) {
            throw new UnsupportedOperationException("Must use CraftBukkit 1.3.0 or later to inject into into NetworkMananger.");
         }

         Callback queueFilter = new MethodInterceptor() {
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
               Object packet = args[0];
               if (packet != null) {
                  packet = NetworkObjectInjector.this.handlePacketSending(packet);
                  if (packet == null) {
                     return null;
                  }

                  args[0] = packet;
               }

               return proxy.invokeSuper(networkDelegate, args);
            }
         };
         Callback dispatch = new LazyLoader() {
            public Object loadObject() throws Exception {
               return networkDelegate;
            }
         };
         if (callbackFilter == null) {
            callbackFilter = new CallbackFilter() {
               public int accept(Method method) {
                  return method.equals(PlayerInjector.queueMethod) ? 0 : 1;
               }
            };
         }

         Enhancer ex = new Enhancer();
         ex.setClassLoader(this.classLoader);
         ex.setSuperclass(networkInterface);
         ex.setCallbacks(new Callback[]{queueFilter, dispatch});
         ex.setCallbackFilter(callbackFilter);
         this.networkManagerRef.setValue(ex.create());
      }

   }

   protected void cleanHook() {
      if (this.networkManagerRef != null && this.networkManagerRef.isCurrentSet()) {
         this.networkManagerRef.revertValue();
      }

   }

   public void handleDisconnect() {
   }

   public boolean canInject(GamePhase phase) {
      return true;
   }

   public PacketFilterManager.PlayerInjectHooks getHookType() {
      return PacketFilterManager.PlayerInjectHooks.NETWORK_MANAGER_OBJECT;
   }
}
