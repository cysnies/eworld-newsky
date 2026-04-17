package com.comphenix.protocol.reflect.instances;

import com.comphenix.net.sf.cglib.proxy.Enhancer;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

public class DefaultInstances implements InstanceProvider {
   public static final DefaultInstances DEFAULT;
   private int maximumRecursion;
   private ImmutableList registered;
   private boolean nonNull;

   public DefaultInstances(ImmutableList registered) {
      this.maximumRecursion = 20;
      this.registered = registered;
   }

   public DefaultInstances(DefaultInstances other) {
      this.maximumRecursion = 20;
      this.nonNull = other.nonNull;
      this.maximumRecursion = other.maximumRecursion;
      this.registered = other.registered;
   }

   public DefaultInstances(InstanceProvider... instaceProviders) {
      this(ImmutableList.copyOf(instaceProviders));
   }

   public static DefaultInstances fromArray(InstanceProvider... instanceProviders) {
      return new DefaultInstances(ImmutableList.copyOf(instanceProviders));
   }

   public static DefaultInstances fromCollection(Collection instanceProviders) {
      return new DefaultInstances(ImmutableList.copyOf(instanceProviders));
   }

   public ImmutableList getRegistered() {
      return this.registered;
   }

   public boolean isNonNull() {
      return this.nonNull;
   }

   public void setNonNull(boolean nonNull) {
      this.nonNull = nonNull;
   }

   public int getMaximumRecursion() {
      return this.maximumRecursion;
   }

   public void setMaximumRecursion(int maximumRecursion) {
      if (maximumRecursion < 1) {
         throw new IllegalArgumentException("Maxmimum recursion height must be one or higher.");
      } else {
         this.maximumRecursion = maximumRecursion;
      }
   }

   public Object getDefault(Class type) {
      return this.getDefaultInternal(type, this.registered, 0);
   }

   public Constructor getMinimumConstructor(Class type) {
      return this.getMinimumConstructor(type, this.registered, 0);
   }

   private Constructor getMinimumConstructor(Class type, List providers, int recursionLevel) {
      Constructor<T> minimum = null;
      int lastCount = Integer.MAX_VALUE;

      for(Constructor candidate : type.getConstructors()) {
         Class<?>[] types = candidate.getParameterTypes();
         if (types.length < lastCount && !this.contains(types, type) && (!this.nonNull || !this.isAnyNull(types, providers, recursionLevel))) {
            minimum = candidate;
            lastCount = types.length;
            if (lastCount == 0) {
               break;
            }
         }
      }

      return minimum;
   }

   private boolean isAnyNull(Class[] types, List providers, int recursionLevel) {
      for(Class type : types) {
         if (this.getDefaultInternal(type, providers, recursionLevel) == null) {
            return true;
         }
      }

      return false;
   }

   public Object getDefault(Class type, List providers) {
      return this.getDefaultInternal(type, providers, 0);
   }

   private Object getDefaultInternal(Class type, List providers, int recursionLevel) {
      try {
         for(InstanceProvider generator : providers) {
            Object value = generator.create(type);
            if (value != null) {
               return value;
            }
         }
      } catch (NotConstructableException var10) {
         return null;
      }

      if (recursionLevel >= this.maximumRecursion) {
         return null;
      } else {
         Constructor<T> minimum = this.getMinimumConstructor(type, providers, recursionLevel + 1);

         try {
            if (minimum != null) {
               int parameterCount = minimum.getParameterTypes().length;
               Object[] params = new Object[parameterCount];
               Class<?>[] types = minimum.getParameterTypes();

               for(int i = 0; i < parameterCount; ++i) {
                  params[i] = this.getDefaultInternal(types[i], providers, recursionLevel + 1);
                  if (params[i] == null && this.nonNull) {
                     return null;
                  }
               }

               return this.createInstance(type, minimum, types, params);
            }
         } catch (Exception var9) {
         }

         return null;
      }
   }

   public DefaultInstances forEnhancer(final Enhancer enhancer) {
      return new DefaultInstances(this) {
         protected Object createInstance(Class type, Constructor constructor, Class[] types, Object[] params) {
            return enhancer.create(types, params);
         }
      };
   }

   protected Object createInstance(Class type, Constructor constructor, Class[] types, Object[] params) {
      try {
         return constructor.newInstance(params);
      } catch (Exception var6) {
         return null;
      }
   }

   protected boolean contains(Object[] elements, Object elementToFind) {
      for(Object element : elements) {
         if (Objects.equal(elementToFind, element)) {
            return true;
         }
      }

      return false;
   }

   public Object create(@Nullable Class type) {
      return this.getDefault(type);
   }

   static {
      DEFAULT = fromArray(PrimitiveGenerator.INSTANCE, CollectionGenerator.INSTANCE);
   }
}
