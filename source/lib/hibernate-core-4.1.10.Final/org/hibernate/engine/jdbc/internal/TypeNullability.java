package org.hibernate.engine.jdbc.internal;

public enum TypeNullability {
   NULLABLE,
   NON_NULLABLE,
   UNKNOWN;

   public static TypeNullability interpret(short code) {
      switch (code) {
         case 0:
            return NON_NULLABLE;
         case 1:
            return NULLABLE;
         case 2:
            return UNKNOWN;
         default:
            throw new IllegalArgumentException("Unknown type nullability code [" + code + "] enountered");
      }
   }
}
