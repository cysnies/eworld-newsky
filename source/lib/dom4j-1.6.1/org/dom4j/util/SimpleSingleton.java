package org.dom4j.util;

public class SimpleSingleton implements SingletonStrategy {
   private String singletonClassName = null;
   private Object singletonInstance = null;

   public Object instance() {
      return this.singletonInstance;
   }

   public void reset() {
      if (this.singletonClassName != null) {
         Class clazz = null;

         try {
            clazz = Thread.currentThread().getContextClassLoader().loadClass(this.singletonClassName);
            this.singletonInstance = clazz.newInstance();
         } catch (Exception var5) {
            try {
               clazz = Class.forName(this.singletonClassName);
               this.singletonInstance = clazz.newInstance();
            } catch (Exception var4) {
            }
         }
      }

   }

   public void setSingletonClassName(String singletonClassName) {
      this.singletonClassName = singletonClassName;
      this.reset();
   }
}
