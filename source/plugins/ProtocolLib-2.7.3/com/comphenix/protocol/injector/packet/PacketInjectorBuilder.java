package com.comphenix.protocol.injector.packet;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.injector.PacketFilterManager;
import com.comphenix.protocol.injector.player.PlayerInjectionHandler;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;

public class PacketInjectorBuilder {
   protected ClassLoader classLoader;
   protected ListenerInvoker invoker;
   protected ErrorReporter reporter;
   protected PlayerInjectionHandler playerInjection;

   protected PacketInjectorBuilder() {
   }

   public static PacketInjectorBuilder newBuilder() {
      return new PacketInjectorBuilder();
   }

   public PacketInjectorBuilder classLoader(@Nonnull ClassLoader classLoader) {
      Preconditions.checkNotNull(classLoader, "classLoader cannot be NULL");
      this.classLoader = classLoader;
      return this;
   }

   public PacketInjectorBuilder reporter(@Nonnull ErrorReporter reporter) {
      Preconditions.checkNotNull(reporter, "reporter cannot be NULL");
      this.reporter = reporter;
      return this;
   }

   public PacketInjectorBuilder invoker(@Nonnull ListenerInvoker invoker) {
      Preconditions.checkNotNull(invoker, "invoker cannot be NULL");
      this.invoker = invoker;
      return this;
   }

   @Nonnull
   public PacketInjectorBuilder playerInjection(@Nonnull PlayerInjectionHandler playerInjection) {
      Preconditions.checkNotNull(playerInjection, "playerInjection cannot be NULL");
      this.playerInjection = playerInjection;
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

      if (this.playerInjection == null) {
         throw new IllegalStateException("Player injection parameter must be initialized.");
      }
   }

   public PacketInjector buildInjector() throws FieldAccessException {
      this.initializeDefaults();
      return new ProxyPacketInjector(this.classLoader, this.invoker, this.playerInjection, this.reporter);
   }
}
