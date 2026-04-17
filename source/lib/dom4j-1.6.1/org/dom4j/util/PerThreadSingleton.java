package org.dom4j.util;

import java.lang.ref.WeakReference;

public class PerThreadSingleton implements SingletonStrategy {
   private String singletonClassName = null;
   private ThreadLocal perThreadCache = new ThreadLocal();

   public void reset() {
      this.perThreadCache = new ThreadLocal();
   }

   public Object instance() {
      Object singletonInstancePerThread = null;
      WeakReference ref = (WeakReference)this.perThreadCache.get();
      if (ref != null && ref.get() != null) {
         singletonInstancePerThread = ref.get();
      } else {
         Class clazz = null;

         try {
            clazz = Thread.currentThread().getContextClassLoader().loadClass(this.singletonClassName);
            singletonInstancePerThread = clazz.newInstance();
         } catch (Exception var7) {
            try {
               clazz = Class.forName(this.singletonClassName);
               singletonInstancePerThread = clazz.newInstance();
            } catch (Exception var6) {
            }
         }

         this.perThreadCache.set(new WeakReference(singletonInstancePerThread));
      }

      return singletonInstancePerThread;
   }

   public void setSingletonClassName(String singletonClassName) {
      this.singletonClassName = singletonClassName;
   }
}
