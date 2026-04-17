package com.comphenix.protocol.reflect.cloning;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Map;

public class CollectionCloner implements Cloner {
   private final Cloner defaultCloner;

   public CollectionCloner(Cloner defaultCloner) {
      this.defaultCloner = defaultCloner;
   }

   public boolean canClone(Object source) {
      if (source == null) {
         return false;
      } else {
         Class<?> clazz = source.getClass();
         return Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz) || clazz.isArray();
      }
   }

   public Object clone(Object source) {
      if (source == null) {
         throw new IllegalArgumentException("source cannot be NULL.");
      } else {
         Class<?> clazz = source.getClass();
         if (source instanceof Collection) {
            Collection<Object> copy = (Collection)this.cloneConstructor(Collection.class, clazz, source);
            copy.clear();

            for(Object element : (Collection)source) {
               copy.add(this.getClone(element, source));
            }

            return copy;
         } else if (!(source instanceof Map)) {
            if (clazz.isArray()) {
               int lenght = Array.getLength(source);
               Class<?> component = clazz.getComponentType();
               if (ImmutableDetector.isImmutable(component)) {
                  return this.clonePrimitive(component, source);
               } else {
                  Object copy = Array.newInstance(clazz.getComponentType(), lenght);

                  for(int i = 0; i < lenght; ++i) {
                     Object element = Array.get(source, i);
                     if (!this.defaultCloner.canClone(element)) {
                        throw new IllegalArgumentException("Cannot clone " + element + " in array " + source);
                     }

                     Array.set(copy, i, this.defaultCloner.clone(element));
                  }

                  return copy;
               }
            } else {
               throw new IllegalArgumentException(source + " is not an array nor a Collection.");
            }
         } else {
            Map<Object, Object> copy = (Map)this.cloneConstructor(Map.class, clazz, source);
            copy.clear();

            for(Map.Entry element : ((Map)source).entrySet()) {
               Object key = this.getClone(element.getKey(), source);
               Object value = this.getClone(element.getValue(), source);
               copy.put(key, value);
            }

            return copy;
         }
      }
   }

   private Object getClone(Object element, Object container) {
      if (this.defaultCloner.canClone(element)) {
         return this.defaultCloner.clone(element);
      } else {
         throw new IllegalArgumentException("Cannot clone " + element + " in container " + container);
      }
   }

   private Object clonePrimitive(Class component, Object source) {
      if (Byte.TYPE.equals(component)) {
         return ((byte[])((byte[])source)).clone();
      } else if (Short.TYPE.equals(component)) {
         return ((short[])((short[])source)).clone();
      } else if (Integer.TYPE.equals(component)) {
         return ((int[])((int[])source)).clone();
      } else if (Long.TYPE.equals(component)) {
         return ((long[])((long[])source)).clone();
      } else if (Float.TYPE.equals(component)) {
         return ((float[])((float[])source)).clone();
      } else if (Double.TYPE.equals(component)) {
         return ((double[])((double[])source)).clone();
      } else if (Character.TYPE.equals(component)) {
         return ((char[])((char[])source)).clone();
      } else {
         return Boolean.TYPE.equals(component) ? ((boolean[])((boolean[])source)).clone() : ((Object[])((Object[])source)).clone();
      }
   }

   private Object cloneConstructor(Class superclass, Class clazz, Object source) {
      try {
         Constructor<?> constructCopy = clazz.getConstructor(Collection.class);
         return constructCopy.newInstance(source);
      } catch (NoSuchMethodException var5) {
         return this.cloneObject(clazz, source);
      } catch (Exception e) {
         throw new RuntimeException("Cannot construct collection.", e);
      }
   }

   private Object cloneObject(Class clazz, Object source) {
      try {
         return clazz.getMethod("clone").invoke(source);
      } catch (Exception e1) {
         throw new RuntimeException("Cannot copy " + source + " (" + clazz + ")", e1);
      }
   }

   public Cloner getDefaultCloner() {
      return this.defaultCloner;
   }
}
