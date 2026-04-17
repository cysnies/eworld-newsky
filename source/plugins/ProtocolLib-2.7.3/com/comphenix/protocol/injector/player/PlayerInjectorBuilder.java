package com.comphenix.protocol.injector.player;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.injector.PacketFilterManager;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import java.util.Set;
import javax.annotation.Nonnull;
import org.bukkit.Bukkit;
import org.bukkit.Server;

public class PlayerInjectorBuilder {
   protected ClassLoader classLoader;
   protected ErrorReporter reporter;
   protected Predicate injectionFilter;
   protected ListenerInvoker invoker;
   protected Set packetListeners;
   protected Server server;
   protected MinecraftVersion version;

   public static PlayerInjectorBuilder newBuilder() {
      return new PlayerInjectorBuilder();
   }

   protected PlayerInjectorBuilder() {
   }

   public PlayerInjectorBuilder classLoader(@Nonnull ClassLoader classLoader) {
      Preconditions.checkNotNull(classLoader, "classLoader cannot be NULL");
      this.classLoader = classLoader;
      return this;
   }

   public PlayerInjectorBuilder reporter(@Nonnull ErrorReporter reporter) {
      Preconditions.checkNotNull(reporter, "reporter cannot be NULL");
      this.reporter = reporter;
      return this;
   }

   @Nonnull
   public PlayerInjectorBuilder injectionFilter(@Nonnull Predicate injectionFilter) {
      Preconditions.checkNotNull(injectionFilter, "injectionFilter cannot be NULL");
      this.injectionFilter = injectionFilter;
      return this;
   }

   public PlayerInjectorBuilder invoker(@Nonnull ListenerInvoker invoker) {
      Preconditions.checkNotNull(invoker, "invoker cannot be NULL");
      this.invoker = invoker;
      return this;
   }

   @Nonnull
   public PlayerInjectorBuilder packetListeners(@Nonnull Set packetListeners) {
      Preconditions.checkNotNull(packetListeners, "packetListeners cannot be NULL");
      this.packetListeners = packetListeners;
      return this;
   }

   public PlayerInjectorBuilder server(@Nonnull Server server) {
      Preconditions.checkNotNull(server, "server cannot be NULL");
      this.server = server;
      return this;
   }

   public PlayerInjectorBuilder version(MinecraftVersion version) {
      this.version = version;
      return this;
   }

   private void initializeDefaults() {
      ProtocolManager manager = ProtocolLibrary.getProtocolManager();
      if (this.classLoader == null) {
         this.classLoader = this.getClass().getClassLoader();
      }

      if (this.reporter == null) {
         this.reporter = ProtocolLibrary.getErrorReporter();
      }

      if (this.invoker == null) {
         this.invoker = (PacketFilterManager)manager;
      }

      if (this.server == null) {
         this.server = Bukkit.getServer();
      }

      if (this.injectionFilter == null) {
         throw new IllegalStateException("injectionFilter must be initialized.");
      } else if (this.packetListeners == null) {
         throw new IllegalStateException("packetListeners must be initialized.");
      }
   }

   public PlayerInjectionHandler buildHandler() {
      this.initializeDefaults();
      return new ProxyPlayerInjectionHandler(this.classLoader, this.reporter, this.injectionFilter, this.invoker, this.packetListeners, this.server, this.version);
   }
}
