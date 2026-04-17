package com.comphenix.protocol.injector;

import com.comphenix.executors.BukkitFutures;
import com.comphenix.protocol.async.AsyncFilterManager;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.injector.player.InjectedServerConnection;
import com.comphenix.protocol.injector.spigot.SpigotPacketInjector;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import javax.annotation.Nonnull;
import org.bukkit.Server;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.plugin.Plugin;

public class PacketFilterBuilder {
   public static final ReportType REPORT_TEMPORARY_EVENT_ERROR = new ReportType("Unable to register or handle temporary event.");
   private ClassLoader classLoader;
   private Server server;
   private Plugin library;
   private MinecraftVersion mcVersion;
   private DelayedSingleTask unhookTask;
   private ErrorReporter reporter;
   private AsyncFilterManager asyncManager;
   private boolean nettyEnabled;

   public PacketFilterBuilder classLoader(@Nonnull ClassLoader classLoader) {
      if (classLoader == null) {
         throw new IllegalArgumentException("classLoader cannot be NULL.");
      } else {
         this.classLoader = classLoader;
         return this;
      }
   }

   public PacketFilterBuilder server(@Nonnull Server server) {
      if (server == null) {
         throw new IllegalArgumentException("server cannot be NULL.");
      } else {
         this.server = server;
         return this;
      }
   }

   public PacketFilterBuilder library(@Nonnull Plugin library) {
      if (library == null) {
         throw new IllegalArgumentException("library cannot be NULL.");
      } else {
         this.library = library;
         return this;
      }
   }

   public PacketFilterBuilder minecraftVersion(@Nonnull MinecraftVersion mcVersion) {
      if (mcVersion == null) {
         throw new IllegalArgumentException("minecraftVersion cannot be NULL.");
      } else {
         this.mcVersion = mcVersion;
         return this;
      }
   }

   public PacketFilterBuilder unhookTask(@Nonnull DelayedSingleTask unhookTask) {
      if (unhookTask == null) {
         throw new IllegalArgumentException("unhookTask cannot be NULL.");
      } else {
         this.unhookTask = unhookTask;
         return this;
      }
   }

   public PacketFilterBuilder reporter(@Nonnull ErrorReporter reporter) {
      if (reporter == null) {
         throw new IllegalArgumentException("reporter cannot be NULL.");
      } else {
         this.reporter = reporter;
         return this;
      }
   }

   public boolean isNettyEnabled() {
      return this.nettyEnabled;
   }

   public ClassLoader getClassLoader() {
      return this.classLoader;
   }

   public Server getServer() {
      return this.server;
   }

   public Plugin getLibrary() {
      return this.library;
   }

   public MinecraftVersion getMinecraftVersion() {
      return this.mcVersion;
   }

   public DelayedSingleTask getUnhookTask() {
      return this.unhookTask;
   }

   public ErrorReporter getReporter() {
      return this.reporter;
   }

   public AsyncFilterManager getAsyncManager() {
      return this.asyncManager;
   }

   public InternalManager build() {
      if (this.reporter == null) {
         throw new IllegalArgumentException("reporter cannot be NULL.");
      } else if (this.classLoader == null) {
         throw new IllegalArgumentException("classLoader cannot be NULL.");
      } else {
         this.asyncManager = new AsyncFilterManager(this.reporter, this.server.getScheduler());
         this.nettyEnabled = false;
         if (SpigotPacketInjector.canUseSpigotListener()) {
            if (InjectedServerConnection.getServerConnection(this.reporter, this.server) == null) {
               final DelayedPacketManager delayed = new DelayedPacketManager(this.reporter, this.mcVersion);
               delayed.setAsynchronousManager(this.asyncManager);
               this.asyncManager.setManager(delayed);
               Futures.addCallback(BukkitFutures.nextEvent(this.library, WorldInitEvent.class), new FutureCallback() {
                  public void onSuccess(WorldInitEvent event) {
                     if (!delayed.isClosed()) {
                        try {
                           PacketFilterBuilder.this.registerSpigot(delayed);
                        } catch (Exception e) {
                           this.onFailure(e);
                        }

                     }
                  }

                  public void onFailure(Throwable error) {
                     PacketFilterBuilder.this.reporter.reportWarning(PacketFilterBuilder.this, (Report.ReportBuilder)Report.newBuilder(PacketFilterBuilder.REPORT_TEMPORARY_EVENT_ERROR).error(error));
                  }
               });
               System.out.println("Delaying due to Spigot");
               return delayed;
            }

            this.nettyEnabled = !MinecraftReflection.isMinecraftObject(InjectedServerConnection.getServerConnection(this.reporter, this.server));
         }

         return this.buildInternal();
      }
   }

   private void registerSpigot(DelayedPacketManager delayed) {
      this.nettyEnabled = !MinecraftReflection.isMinecraftObject(InjectedServerConnection.getServerConnection(this.reporter, this.server));
      delayed.setDelegate(this.buildInternal());
   }

   private PacketFilterManager buildInternal() {
      PacketFilterManager manager = new PacketFilterManager(this);
      this.asyncManager.setManager(manager);
      return manager;
   }
}
