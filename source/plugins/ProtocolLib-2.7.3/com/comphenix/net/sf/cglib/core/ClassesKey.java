package com.comphenix.net.sf.cglib.core;

public class ClassesKey {
   private static final Key FACTORY;
   // $FF: synthetic field
   static Class class$net$sf$cglib$core$ClassesKey$Key;

   private ClassesKey() {
   }

   public static Object create(Object[] array) {
      return FACTORY.newInstance(array);
   }

   // $FF: synthetic method
   static Class class$(String x0) {
      try {
         return Class.forName(x0);
      } catch (ClassNotFoundException x1) {
         throw new NoClassDefFoundError(x1.getMessage());
      }
   }

   static {
      FACTORY = (Key)KeyFactory.create(class$net$sf$cglib$core$ClassesKey$Key == null ? (class$net$sf$cglib$core$ClassesKey$Key = class$("com.comphenix.net.sf.cglib.core.ClassesKey$Key")) : class$net$sf$cglib$core$ClassesKey$Key, KeyFactory.OBJECT_BY_CLASS);
   }

   interface Key {
      Object newInstance(Object[] var1);
   }
}
