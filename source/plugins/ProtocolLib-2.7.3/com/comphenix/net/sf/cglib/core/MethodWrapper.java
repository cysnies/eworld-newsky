package com.comphenix.net.sf.cglib.core;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MethodWrapper {
   private static final MethodWrapperKey KEY_FACTORY;
   // $FF: synthetic field
   static Class class$net$sf$cglib$core$MethodWrapper$MethodWrapperKey;

   private MethodWrapper() {
   }

   public static Object create(Method method) {
      return KEY_FACTORY.newInstance(method.getName(), ReflectUtils.getNames(method.getParameterTypes()), method.getReturnType().getName());
   }

   public static Set createSet(Collection methods) {
      Set set = new HashSet();
      Iterator it = methods.iterator();

      while(it.hasNext()) {
         set.add(create((Method)it.next()));
      }

      return set;
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
      KEY_FACTORY = (MethodWrapperKey)KeyFactory.create(class$net$sf$cglib$core$MethodWrapper$MethodWrapperKey == null ? (class$net$sf$cglib$core$MethodWrapper$MethodWrapperKey = class$("com.comphenix.net.sf.cglib.core.MethodWrapper$MethodWrapperKey")) : class$net$sf$cglib$core$MethodWrapper$MethodWrapperKey);
   }

   public interface MethodWrapperKey {
      Object newInstance(String var1, String[] var2, String var3);
   }
}
