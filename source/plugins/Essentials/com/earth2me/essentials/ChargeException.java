package com.earth2me.essentials;

public class ChargeException extends Exception {
   public ChargeException(String message) {
      super(message);
   }

   public ChargeException(String message, Throwable throwable) {
      super(message, throwable);
   }
}
