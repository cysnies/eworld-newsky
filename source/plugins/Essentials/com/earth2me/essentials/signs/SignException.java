package com.earth2me.essentials.signs;

public class SignException extends Exception {
   public SignException(String message) {
      super(message);
   }

   public SignException(String message, Throwable throwable) {
      super(message, throwable);
   }
}
