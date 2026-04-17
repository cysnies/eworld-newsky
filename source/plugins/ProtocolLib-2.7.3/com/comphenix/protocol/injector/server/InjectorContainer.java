package com.comphenix.protocol.injector.server;

class InjectorContainer {
   private volatile SocketInjector injector;

   public SocketInjector getInjector() {
      return this.injector;
   }

   public void setInjector(SocketInjector injector) {
      if (injector == null) {
         throw new IllegalArgumentException("Injector cannot be NULL.");
      } else {
         this.injector = injector;
      }
   }
}
