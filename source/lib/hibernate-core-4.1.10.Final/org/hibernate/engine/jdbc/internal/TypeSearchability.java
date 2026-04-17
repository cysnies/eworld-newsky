package org.hibernate.engine.jdbc.internal;

public enum TypeSearchability {
   NONE,
   FULL,
   CHAR,
   BASIC;

   public static TypeSearchability interpret(short code) {
      switch (code) {
         case 0:
            return NONE;
         case 1:
            return CHAR;
         case 2:
            return BASIC;
         case 3:
            return FULL;
         default:
            throw new IllegalArgumentException("Unknown type searchability code [" + code + "] enountered");
      }
   }
}
