package com.comphenix.protocol.events;

public enum ConnectionSide {
   SERVER_SIDE,
   CLIENT_SIDE,
   BOTH;

   public boolean isForClient() {
      return this == CLIENT_SIDE || this == BOTH;
   }

   public boolean isForServer() {
      return this == SERVER_SIDE || this == BOTH;
   }

   public static ConnectionSide add(ConnectionSide a, ConnectionSide b) {
      if (a == null) {
         return b;
      } else if (b == null) {
         return a;
      } else {
         boolean client = a.isForClient() || b.isForClient();
         boolean server = a.isForServer() || b.isForServer();
         if (client && server) {
            return BOTH;
         } else {
            return client ? CLIENT_SIDE : SERVER_SIDE;
         }
      }
   }
}
