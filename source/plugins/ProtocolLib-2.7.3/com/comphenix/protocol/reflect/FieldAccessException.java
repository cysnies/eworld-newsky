package com.comphenix.protocol.reflect;

public class FieldAccessException extends RuntimeException {
   private static final long serialVersionUID = 1911011681494034617L;

   public FieldAccessException() {
   }

   public FieldAccessException(String message, Throwable cause) {
      super(message, cause);
   }

   public FieldAccessException(String message) {
      super(message);
   }

   public FieldAccessException(Throwable cause) {
      super(cause);
   }

   public static FieldAccessException fromFormat(String message, Object... params) {
      return new FieldAccessException(String.format(message, params));
   }
}
