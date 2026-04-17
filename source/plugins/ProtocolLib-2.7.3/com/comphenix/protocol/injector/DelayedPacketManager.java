package com.comphenix.protocol.injector;

import com.comphenix.protocol.AsynchronousManager;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class DelayedPacketManager implements ProtocolManager, InternalManager {
   public static final ReportType REPORT_CANNOT_SEND_QUEUED_PACKET = new ReportType("Cannot send queued packet %s.");
   public static final ReportType REPORT_CANNOT_REGISTER_QUEUED_LISTENER = new ReportType("Cannot register queued listener %s.");
   private volatile InternalManager delegate;
   private final List queuedActions = Collections.synchronizedList(Lists.newArrayList());
   private final List queuedListeners = Collections.synchronizedList(Lists.newArrayList());
   private AsynchronousManager asyncManager;
   private ErrorReporter reporter;
   private PacketFilterManager.PlayerInjectHooks hook;
   private boolean closed;
   private PluginManager queuedManager;
   private Plugin queuedPlugin;
   private MinecraftVersion version;

   public DelayedPacketManager(@Nonnull ErrorReporter reporter, @Nonnull MinecraftVersion version) {
      this.hook = PacketFilterManager.PlayerInjectHooks.NETWORK_SERVER_OBJECT;
      Preconditions.checkNotNull(reporter, "reporter cannot be NULL.");
      Preconditions.checkNotNull(version, "version cannot be NULL.");
      this.reporter = reporter;
      this.version = version;
   }

   public InternalManager getDelegate() {
      return this.delegate;
   }

   public MinecraftVersion getMinecraftVersion() {
      return this.delegate != null ? this.delegate.getMinecraftVersion() : this.version;
   }

   protected void setDelegate(InternalManager delegate) {
      this.delegate = delegate;
      if (delegate != null) {
         if (!Objects.equal(delegate.getPlayerHook(), this.hook)) {
            delegate.setPlayerHook(this.hook);
         }

         if (this.queuedManager != null && this.queuedPlugin != null) {
            delegate.registerEvents(this.queuedManager, this.queuedPlugin);
         }

         synchronized(this.queuedListeners) {
            for(PacketListener listener : this.queuedListeners) {
               try {
                  delegate.addPacketListener(listener);
               } catch (IllegalArgumentException e) {
                  this.reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_REGISTER_QUEUED_LISTENER).callerParam(delegate).messageParam(listener).error(e));
               }
            }
         }

         synchronized(this.queuedActions) {
            for(Runnable action : this.queuedActions) {
               action.run();
            }
         }

         this.queuedListeners.clear();
         this.queuedActions.clear();
      }

   }

   private Runnable queuedAddPacket(final ConnectionSide side, final Player player, final PacketContainer packet, final NetworkMarker marker, final boolean filtered) {
      return new Runnable() {
         public void run() {
            try {
               switch (side) {
                  case CLIENT_SIDE:
                     DelayedPacketManager.this.delegate.recieveClientPacket(player, packet, marker, filtered);
                     break;
                  case SERVER_SIDE:
                     DelayedPacketManager.this.delegate.sendServerPacket(player, packet, marker, filtered);
                     break;
                  default:
                     throw new IllegalArgumentException("side cannot be " + side);
               }
            } catch (Exception e) {
               DelayedPacketManager.this.reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(DelayedPacketManager.REPORT_CANNOT_SEND_QUEUED_PACKET).callerParam(DelayedPacketManager.this.delegate).messageParam(packet).error(e));
            }

         }
      };
   }

   public void setPlayerHook(PacketFilterManager.PlayerInjectHooks playerHook) {
      this.hook = playerHook;
   }

   public PacketFilterManager.PlayerInjectHooks getPlayerHook() {
      return this.hook;
   }

   public void sendServerPacket(Player reciever, PacketContainer packet) throws InvocationTargetException {
      this.sendServerPacket(reciever, packet, (NetworkMarker)null, true);
   }

   public void sendServerPacket(Player reciever, PacketContainer packet, boolean filters) throws InvocationTargetException {
      this.sendServerPacket(reciever, packet, (NetworkMarker)null, filters);
   }

   public void sendServerPacket(Player reciever, PacketContainer packet, NetworkMarker marker, boolean filters) throws InvocationTargetException {
      if (this.delegate != null) {
         this.delegate.sendServerPacket(reciever, packet, marker, filters);
      } else {
         this.queuedActions.add(this.queuedAddPacket(ConnectionSide.SERVER_SIDE, reciever, packet, marker, filters));
      }

   }

   public void recieveClientPacket(Player sender, PacketContainer packet) throws IllegalAccessException, InvocationTargetException {
      this.recieveClientPacket(sender, packet, (NetworkMarker)null, true);
   }

   public void recieveClientPacket(Player sender, PacketContainer packet, boolean filters) throws IllegalAccessException, InvocationTargetException {
      this.recieveClientPacket(sender, packet, (NetworkMarker)null, filters);
   }

   public void recieveClientPacket(Player sender, PacketContainer packet, NetworkMarker marker, boolean filters) throws IllegalAccessException, InvocationTargetException {
      if (this.delegate != null) {
         this.delegate.recieveClientPacket(sender, packet, marker, filters);
      } else {
         this.queuedActions.add(this.queuedAddPacket(ConnectionSide.CLIENT_SIDE, sender, packet, marker, filters));
      }

   }

   public void broadcastServerPacket(final PacketContainer packet, final Entity entity, final boolean includeTracker) {
      if (this.delegate != null) {
         this.delegate.broadcastServerPacket(packet, entity, includeTracker);
      } else {
         this.queuedActions.add(new Runnable() {
            public void run() {
               DelayedPacketManager.this.delegate.broadcastServerPacket(packet, entity, includeTracker);
            }
         });
      }

   }

   public void broadcastServerPacket(final PacketContainer packet, final Location origin, final int maxObserverDistance) {
      if (this.delegate != null) {
         this.delegate.broadcastServerPacket(packet, origin, maxObserverDistance);
      } else {
         this.queuedActions.add(new Runnable() {
            public void run() {
               DelayedPacketManager.this.delegate.broadcastServerPacket(packet, origin, maxObserverDistance);
            }
         });
      }

   }

   public void broadcastServerPacket(final PacketContainer packet) {
      if (this.delegate != null) {
         this.delegate.broadcastServerPacket(packet);
      } else {
         this.queuedActions.add(new Runnable() {
            public void run() {
               DelayedPacketManager.this.delegate.broadcastServerPacket(packet);
            }
         });
      }

   }

   public ImmutableSet getPacketListeners() {
      return this.delegate != null ? this.delegate.getPacketListeners() : ImmutableSet.copyOf(this.queuedListeners);
   }

   public void addPacketListener(PacketListener listener) {
      if (this.delegate != null) {
         this.delegate.addPacketListener(listener);
      } else {
         this.queuedListeners.add(listener);
      }

   }

   public void removePacketListener(PacketListener listener) {
      if (this.delegate != null) {
         this.delegate.removePacketListener(listener);
      } else {
         this.queuedListeners.remove(listener);
      }

   }

   public void removePacketListeners(Plugin plugin) {
      if (this.delegate != null) {
         this.delegate.removePacketListeners(plugin);
      } else {
         Iterator<PacketListener> it = this.queuedListeners.iterator();

         while(it.hasNext()) {
            if (Objects.equal(((PacketListener)it.next()).getPlugin(), plugin)) {
               it.remove();
            }
         }
      }

   }

   public PacketContainer createPacket(int id) {
      return this.delegate != null ? this.delegate.createPacket(id) : this.createPacket(id, true);
   }

   public PacketContainer createPacket(int id, boolean forceDefaults) {
      if (this.delegate != null) {
         return this.delegate.createPacket(id);
      } else {
         PacketContainer packet = new PacketContainer(id);
         if (forceDefaults) {
            try {
               packet.getModifier().writeDefaults();
            } catch (FieldAccessException e) {
               throw new RuntimeException("Security exception.", e);
            }
         }

         return packet;
      }
   }

   public PacketConstructor createPacketConstructor(int id, Object... arguments) {
      return this.delegate != null ? this.delegate.createPacketConstructor(id, arguments) : PacketConstructor.DEFAULT.withPacket(id, arguments);
   }

   public Set getSendingFilters() {
      if (this.delegate != null) {
         return this.delegate.getSendingFilters();
      } else {
         Set<Integer> sending = Sets.newHashSet();

         for(PacketListener listener : this.queuedListeners) {
            sending.addAll(listener.getSendingWhitelist().getWhitelist());
         }

         return sending;
      }
   }

   public Set getReceivingFilters() {
      if (this.delegate != null) {
         return this.delegate.getReceivingFilters();
      } else {
         Set<Integer> recieving = Sets.newHashSet();

         for(PacketListener listener : this.queuedListeners) {
            recieving.addAll(listener.getReceivingWhitelist().getWhitelist());
         }

         return recieving;
      }
   }

   public void updateEntity(Entity entity, List observers) throws FieldAccessException {
      if (this.delegate != null) {
         this.delegate.updateEntity(entity, observers);
      } else {
         EntityUtilities.updateEntity(entity, observers);
      }

   }

   public Entity getEntityFromID(World container, int id) throws FieldAccessException {
      return this.delegate != null ? this.delegate.getEntityFromID(container, id) : EntityUtilities.getEntityFromID(container, id);
   }

   public List getEntityTrackers(Entity entity) throws FieldAccessException {
      return this.delegate != null ? this.delegate.getEntityTrackers(entity) : EntityUtilities.getEntityTrackers(entity);
   }

   public boolean isClosed() {
      return this.closed || this.delegate != null && this.delegate.isClosed();
   }

   public AsynchronousManager getAsynchronousManager() {
      return this.delegate != null ? this.delegate.getAsynchronousManager() : this.asyncManager;
   }

   public void setAsynchronousManager(AsynchronousManager asyncManager) {
      this.asyncManager = asyncManager;
   }

   public void registerEvents(PluginManager manager, Plugin plugin) {
      if (this.delegate != null) {
         this.delegate.registerEvents(manager, plugin);
      } else {
         this.queuedManager = manager;
         this.queuedPlugin = plugin;
      }

   }

   public void close() {
      if (this.delegate != null) {
         this.delegate.close();
      }

      this.closed = true;
   }
}
