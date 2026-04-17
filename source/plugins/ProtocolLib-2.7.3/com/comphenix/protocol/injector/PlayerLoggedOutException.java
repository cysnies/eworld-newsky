package com.comphenix.protocol.injector;

public class PlayerLoggedOutException extends RuntimeException {
   private static final long serialVersionUID = 4889257862160145234L;

   public PlayerLoggedOutException() {
      super("Cannot inject a player that has already logged out.");
   }

   public PlayerLoggedOutException(String message, Throwable cause) {
      super(message, cause);
   }

   public PlayerLoggedOutException(String message) {
      super(message);
   }

   public PlayerLoggedOutException(Throwable cause) {
      super(cause);
   }

   public static PlayerLoggedOutException fromFormat(String message, Object... params) {
      return new PlayerLoggedOutException(String.format(message, params));
   }
}
