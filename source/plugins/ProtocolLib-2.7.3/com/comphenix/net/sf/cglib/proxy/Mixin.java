package com.comphenix.net.sf.cglib.proxy;

import com.comphenix.net.sf.cglib.asm.ClassVisitor;
import com.comphenix.net.sf.cglib.core.AbstractClassGenerator;
import com.comphenix.net.sf.cglib.core.ClassesKey;
import com.comphenix.net.sf.cglib.core.KeyFactory;
import com.comphenix.net.sf.cglib.core.ReflectUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class Mixin {
   private static final MixinKey KEY_FACTORY;
   private static final Map ROUTE_CACHE;
   public static final int STYLE_INTERFACES = 0;
   public static final int STYLE_BEANS = 1;
   public static final int STYLE_EVERYTHING = 2;
   // $FF: synthetic field
   static Class class$net$sf$cglib$proxy$Mixin$MixinKey;
   // $FF: synthetic field
   static Class class$net$sf$cglib$proxy$Mixin;

   public abstract Mixin newInstance(Object[] var1);

   public static Mixin create(Object[] delegates) {
      Generator gen = new Generator();
      gen.setDelegates(delegates);
      return gen.create();
   }

   public static Mixin create(Class[] interfaces, Object[] delegates) {
      Generator gen = new Generator();
      gen.setClasses(interfaces);
      gen.setDelegates(delegates);
      return gen.create();
   }

   public static Mixin createBean(Object[] beans) {
      return createBean((ClassLoader)null, beans);
   }

   public static Mixin createBean(ClassLoader loader, Object[] beans) {
      Generator gen = new Generator();
      gen.setStyle(1);
      gen.setDelegates(beans);
      gen.setClassLoader(loader);
      return gen.create();
   }

   public static Class[] getClasses(Object[] delegates) {
      return (Class[])route(delegates).classes.clone();
   }

   private static Route route(Object[] delegates) {
      Object key = ClassesKey.create(delegates);
      Route route = (Route)ROUTE_CACHE.get(key);
      if (route == null) {
         ROUTE_CACHE.put(key, route = new Route(delegates));
      }

      return route;
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
      KEY_FACTORY = (MixinKey)KeyFactory.create(class$net$sf$cglib$proxy$Mixin$MixinKey == null ? (class$net$sf$cglib$proxy$Mixin$MixinKey = class$("com.comphenix.net.sf.cglib.proxy.Mixin$MixinKey")) : class$net$sf$cglib$proxy$Mixin$MixinKey, KeyFactory.CLASS_BY_NAME);
      ROUTE_CACHE = Collections.synchronizedMap(new HashMap());
   }

   public static class Generator extends AbstractClassGenerator {
      private static final AbstractClassGenerator.Source SOURCE;
      private Class[] classes;
      private Object[] delegates;
      private int style = 0;
      private int[] route;

      public Generator() {
         super(SOURCE);
      }

      protected ClassLoader getDefaultClassLoader() {
         return this.classes[0].getClassLoader();
      }

      public void setStyle(int style) {
         switch (style) {
            case 0:
            case 1:
            case 2:
               this.style = style;
               return;
            default:
               throw new IllegalArgumentException("Unknown mixin style: " + style);
         }
      }

      public void setClasses(Class[] classes) {
         this.classes = classes;
      }

      public void setDelegates(Object[] delegates) {
         this.delegates = delegates;
      }

      public Mixin create() {
         if (this.classes == null && this.delegates == null) {
            throw new IllegalStateException("Either classes or delegates must be set");
         } else {
            switch (this.style) {
               case 0:
                  if (this.classes == null) {
                     Route r = Mixin.route(this.delegates);
                     this.classes = r.classes;
                     this.route = r.route;
                  }
                  break;
               case 1:
               case 2:
                  if (this.classes == null) {
                     this.classes = ReflectUtils.getClasses(this.delegates);
                  } else if (this.delegates != null) {
                     Class[] temp = ReflectUtils.getClasses(this.delegates);
                     if (this.classes.length != temp.length) {
                        throw new IllegalStateException("Specified classes are incompatible with delegates");
                     }

                     for(int i = 0; i < this.classes.length; ++i) {
                        if (!this.classes[i].isAssignableFrom(temp[i])) {
                           throw new IllegalStateException("Specified class " + this.classes[i] + " is incompatible with delegate class " + temp[i] + " (index " + i + ")");
                        }
                     }
                  }
            }

            this.setNamePrefix(this.classes[ReflectUtils.findPackageProtected(this.classes)].getName());
            return (Mixin)super.create(Mixin.KEY_FACTORY.newInstance(this.style, ReflectUtils.getNames(this.classes), this.route));
         }
      }

      public void generateClass(ClassVisitor v) {
         switch (this.style) {
            case 0:
               new MixinEmitter(v, this.getClassName(), this.classes, this.route);
               break;
            case 1:
               new MixinBeanEmitter(v, this.getClassName(), this.classes);
               break;
            case 2:
               new MixinEverythingEmitter(v, this.getClassName(), this.classes);
         }

      }

      protected Object firstInstance(Class type) {
         return ((Mixin)ReflectUtils.newInstance(type)).newInstance(this.delegates);
      }

      protected Object nextInstance(Object instance) {
         return ((Mixin)instance).newInstance(this.delegates);
      }

      static {
         SOURCE = new AbstractClassGenerator.Source((Mixin.class$net$sf$cglib$proxy$Mixin == null ? (Mixin.class$net$sf$cglib$proxy$Mixin = Mixin.class$("com.comphenix.net.sf.cglib.proxy.Mixin")) : Mixin.class$net$sf$cglib$proxy$Mixin).getName());
      }
   }

   private static class Route {
      private Class[] classes;
      private int[] route;

      Route(Object[] delegates) {
         Map map = new HashMap();
         ArrayList collect = new ArrayList();

         for(int i = 0; i < delegates.length; ++i) {
            Class delegate = delegates[i].getClass();
            collect.clear();
            ReflectUtils.addAllInterfaces(delegate, collect);

            for(Class iface : collect) {
               if (!map.containsKey(iface)) {
                  map.put(iface, new Integer(i));
               }
            }
         }

         this.classes = new Class[map.size()];
         this.route = new int[map.size()];
         int index = 0;

         for(Class key : map.keySet()) {
            this.classes[index] = key;
            this.route[index] = (Integer)map.get(key);
            ++index;
         }

      }
   }

   interface MixinKey {
      Object newInstance(int var1, String[] var2, int[] var3);
   }
}
