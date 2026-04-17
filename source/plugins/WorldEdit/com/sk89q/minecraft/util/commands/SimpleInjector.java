package com.sk89q.minecraft.util.commands;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

public class SimpleInjector implements Injector {
   private static final Logger logger = Logger.getLogger(SimpleInjector.class.getCanonicalName());
   private Object[] args;
   private Class[] argClasses;

   public SimpleInjector(Object... args) {
      this.args = args;
      this.argClasses = new Class[args.length];

      for(int i = 0; i < args.length; ++i) {
         this.argClasses[i] = args[i].getClass();
      }

   }

   public Object getInstance(Class clazz) {
      try {
         Constructor<?> ctr = clazz.getConstructor(this.argClasses);
         ctr.setAccessible(true);
         return ctr.newInstance(this.args);
      } catch (NoSuchMethodException e) {
         logger.severe("Error initializing commands class " + clazz + ": ");
         e.printStackTrace();
         return null;
      } catch (InvocationTargetException e) {
         logger.severe("Error initializing commands class " + clazz + ": ");
         e.printStackTrace();
         return null;
      } catch (InstantiationException e) {
         logger.severe("Error initializing commands class " + clazz + ": ");
         e.printStackTrace();
         return null;
      } catch (IllegalAccessException e) {
         logger.severe("Error initializing commands class " + clazz + ": ");
         e.printStackTrace();
         return null;
      }
   }
}
