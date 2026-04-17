package com.comphenix.protocol.reflect.cloning;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.ChunkPosition;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

public class BukkitCloner implements Cloner {
   private Class[] clonableClasses = new Class[]{MinecraftReflection.getItemStackClass(), MinecraftReflection.getChunkPositionClass(), MinecraftReflection.getDataWatcherClass()};

   private int findMatchingClass(Class type) {
      for(int i = 0; i < this.clonableClasses.length; ++i) {
         if (this.clonableClasses[i].isAssignableFrom(type)) {
            return i;
         }
      }

      return -1;
   }

   public boolean canClone(Object source) {
      if (source == null) {
         return false;
      } else {
         return this.findMatchingClass(source.getClass()) >= 0;
      }
   }

   public Object clone(Object source) {
      if (source == null) {
         throw new IllegalArgumentException("source cannot be NULL.");
      } else {
         switch (this.findMatchingClass(source.getClass())) {
            case 0:
               return MinecraftReflection.getMinecraftItemStack(MinecraftReflection.getBukkitItemStack(source).clone());
            case 1:
               EquivalentConverter<ChunkPosition> chunkConverter = ChunkPosition.getConverter();
               return chunkConverter.getGeneric(this.clonableClasses[1], chunkConverter.getSpecific(source));
            case 2:
               EquivalentConverter<WrappedDataWatcher> dataConverter = BukkitConverters.getDataWatcherConverter();
               return dataConverter.getGeneric(this.clonableClasses[2], ((WrappedDataWatcher)dataConverter.getSpecific(source)).deepClone());
            default:
               throw new IllegalArgumentException("Cannot clone objects of type " + source.getClass());
         }
      }
   }
}
