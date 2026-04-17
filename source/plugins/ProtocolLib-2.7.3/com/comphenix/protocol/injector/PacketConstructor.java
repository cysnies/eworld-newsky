package com.comphenix.protocol.injector;

import com.comphenix.protocol.error.RethrowErrorReporter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.primitives.Primitives;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class PacketConstructor {
   public static PacketConstructor DEFAULT = new PacketConstructor((Constructor)null);
   private Constructor constructorMethod;
   private int packetID;
   private List unwrappers;
   private boolean[] unwrappable;

   private PacketConstructor(Constructor constructorMethod) {
      this.constructorMethod = constructorMethod;
      this.unwrappers = Lists.newArrayList(new Unwrapper[]{new BukkitUnwrapper(new RethrowErrorReporter())});
   }

   private PacketConstructor(int packetID, Constructor constructorMethod, List unwrappers, boolean[] unwrappable) {
      this.packetID = packetID;
      this.constructorMethod = constructorMethod;
      this.unwrappers = unwrappers;
      this.unwrappable = unwrappable;
   }

   public ImmutableList getUnwrappers() {
      return ImmutableList.copyOf(this.unwrappers);
   }

   public int getPacketID() {
      return this.packetID;
   }

   public PacketConstructor withUnwrappers(List unwrappers) {
      return new PacketConstructor(this.packetID, this.constructorMethod, unwrappers, this.unwrappable);
   }

   public PacketConstructor withPacket(int id, Object[] values) {
      Class<?>[] types = new Class[values.length];
      Throwable lastException = null;
      boolean[] unwrappable = new boolean[values.length];

      for(int i = 0; i < types.length; ++i) {
         if (values[i] != null) {
            types[i] = values[i] instanceof Class ? (Class)values[i] : values[i].getClass();

            for(Unwrapper unwrapper : this.unwrappers) {
               Object result = null;

               try {
                  result = unwrapper.unwrapItem(values[i]);
               } catch (Throwable e) {
                  lastException = e;
               }

               if (result != null) {
                  types[i] = result.getClass();
                  unwrappable[i] = true;
                  break;
               }
            }
         } else {
            types[i] = Object.class;
         }
      }

      Class<?> packetType = PacketRegistry.getPacketClassFromID(id, true);
      if (packetType == null) {
         throw new IllegalArgumentException("Could not find a packet by the id " + id);
      } else {
         for(Constructor constructor : packetType.getConstructors()) {
            Class<?>[] params = constructor.getParameterTypes();
            if (isCompatible(types, params)) {
               return new PacketConstructor(id, constructor, this.unwrappers, unwrappable);
            }
         }

         throw new IllegalArgumentException("No suitable constructor could be found.", lastException);
      }
   }

   public PacketContainer createPacket(Object... values) throws FieldAccessException {
      try {
         for(int i = 0; i < values.length; ++i) {
            if (this.unwrappable[i]) {
               for(Unwrapper unwrapper : this.unwrappers) {
                  Object converted = unwrapper.unwrapItem(values[i]);
                  if (converted != null) {
                     values[i] = converted;
                     break;
                  }
               }
            }
         }

         Object nmsPacket = this.constructorMethod.newInstance(values);
         return new PacketContainer(this.packetID, nmsPacket);
      } catch (IllegalArgumentException e) {
         throw e;
      } catch (InstantiationException e) {
         throw new FieldAccessException("Cannot construct an abstract packet.", e);
      } catch (IllegalAccessException e) {
         throw new FieldAccessException("Cannot construct packet due to a security limitation.", e);
      } catch (InvocationTargetException e) {
         throw new RuntimeException("Minecraft error.", e);
      }
   }

   private static boolean isCompatible(Class[] types, Class[] params) {
      if (params.length == types.length) {
         for(int i = 0; i < params.length; ++i) {
            Class<?> inputType = types[i];
            Class<?> paramType = params[i];
            if (paramType.isPrimitive()) {
               paramType = Primitives.wrap(paramType);
            }

            if (!paramType.isAssignableFrom(inputType)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public interface Unwrapper {
      Object unwrapItem(Object var1);
   }
}
