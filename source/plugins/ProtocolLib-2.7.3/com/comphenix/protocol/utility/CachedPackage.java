package com.comphenix.protocol.utility;

import com.google.common.collect.Maps;
import java.util.Map;

class CachedPackage {
   private Map cache;
   private String packageName;

   public CachedPackage(String packageName) {
      this.packageName = packageName;
      this.cache = Maps.newConcurrentMap();
   }

   public void setPackageClass(String className, Class clazz) {
      this.cache.put(className, clazz);
   }

   public Class getPackageClass(String className) {
      try {
         Class<?> result = (Class)this.cache.get(className);
         if (result == null) {
            result = CachedPackage.class.getClassLoader().loadClass(this.combine(this.packageName, className));
            this.cache.put(className, result);
         }

         return result;
      } catch (ClassNotFoundException e) {
         throw new RuntimeException("Cannot find class " + className, e);
      }
   }

   private String combine(String packageName, String className) {
      return packageName.length() == 0 ? className : packageName + "." + className;
   }
}
