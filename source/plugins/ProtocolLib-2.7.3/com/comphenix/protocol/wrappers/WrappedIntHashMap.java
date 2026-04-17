package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.base.Preconditions;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.annotation.Nonnull;

public class WrappedIntHashMap {
   private static final Class INT_HASH_MAP = MinecraftReflection.getIntHashMapClass();
   private static Method PUT_METHOD;
   private static Method GET_METHOD;
   private static Method REMOVE_METHOD;
   private Object handle;

   private WrappedIntHashMap(Object handle) {
      this.handle = handle;
   }

   public static WrappedIntHashMap newMap() {
      try {
         return new WrappedIntHashMap(INT_HASH_MAP.newInstance());
      } catch (Exception e) {
         throw new RuntimeException("Unable to construct IntHashMap.", e);
      }
   }

   public static WrappedIntHashMap fromHandle(@Nonnull Object handle) {
      Preconditions.checkNotNull(handle, "handle cannot be NULL");
      Preconditions.checkState(MinecraftReflection.isIntHashMap(handle), "handle is a " + handle.getClass() + ", not an IntHashMap.");
      return new WrappedIntHashMap(handle);
   }

   public void put(int key, Object value) {
      Preconditions.checkNotNull(value, "value cannot be NULL.");
      this.initializePutMethod();
      this.putInternal(key, value);
   }

   private void putInternal(int key, Object value) {
      this.invokeMethod(PUT_METHOD, key, value);
   }

   public Object get(int key) {
      this.initializeGetMethod();
      return this.invokeMethod(GET_METHOD, key);
   }

   public Object remove(int key) {
      this.initializeGetMethod();
      return REMOVE_METHOD == null ? this.removeFallback(key) : this.invokeMethod(REMOVE_METHOD, key);
   }

   private Object removeFallback(int key) {
      Object old = this.get(key);
      this.invokeMethod(PUT_METHOD, key, null);
      return old;
   }

   private Object invokeMethod(Method method, Object... params) {
      try {
         return method.invoke(this.handle, params);
      } catch (IllegalArgumentException e) {
         throw new RuntimeException("Illegal argument.", e);
      } catch (IllegalAccessException e) {
         throw new RuntimeException("Cannot access method.", e);
      } catch (InvocationTargetException e) {
         throw new RuntimeException("Unable to invoke " + method + " on " + this.handle, e);
      }
   }

   private void initializePutMethod() {
      if (PUT_METHOD == null) {
         PUT_METHOD = FuzzyReflection.fromClass(INT_HASH_MAP).getMethod(FuzzyMethodContract.newBuilder().banModifier(8).parameterCount(2).parameterExactType(Integer.TYPE).parameterExactType(Object.class).build());
      }

   }

   private void initializeGetMethod() {
      if (GET_METHOD == null) {
         WrappedIntHashMap temp = newMap();
         String expected = "hello";

         for(Method method : FuzzyReflection.fromClass(INT_HASH_MAP).getMethodListByParameters(Object.class, new Class[]{Integer.TYPE})) {
            temp.put(1, expected);
            if (!Modifier.isStatic(method.getModifiers())) {
               try {
                  boolean first = expected.equals(method.invoke(temp.getHandle(), 1));
                  boolean second = expected.equals(method.invoke(temp.getHandle(), 1));
                  if (first && !second) {
                     REMOVE_METHOD = method;
                  } else if (first && second) {
                     GET_METHOD = method;
                  }
               } catch (Exception var7) {
               }
            }
         }

         if (GET_METHOD == null) {
            throw new IllegalStateException("Unable to find appropriate GET_METHOD for IntHashMap.");
         }
      }

   }

   public Object getHandle() {
      return this.handle;
   }
}
