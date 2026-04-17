package com.comphenix.protocol.reflect;

import com.comphenix.protocol.injector.StructureCache;
import com.comphenix.protocol.utility.MinecraftReflection;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ObjectWriter {
   private static ConcurrentMap cache = new ConcurrentHashMap();

   private StructureModifier getModifier(Class type) {
      Class<?> packetClass = MinecraftReflection.getPacketClass();
      if (!type.equals(packetClass) && packetClass.isAssignableFrom(type)) {
         return StructureCache.getStructure(type);
      } else {
         StructureModifier<Object> modifier = (StructureModifier)cache.get(type);
         if (modifier == null) {
            StructureModifier<Object> value = new StructureModifier(type, (Class)null, false);
            modifier = (StructureModifier)cache.putIfAbsent(type, value);
            if (modifier == null) {
               modifier = value;
            }
         }

         return modifier;
      }
   }

   public void copyTo(Object source, Object destination, Class commonType) {
      this.copyToInternal(source, destination, commonType, true);
   }

   protected void transformField(StructureModifier modifierSource, StructureModifier modifierDest, int fieldIndex) {
      Object value = modifierSource.read(fieldIndex);
      modifierDest.write(fieldIndex, value);
   }

   private void copyToInternal(Object source, Object destination, Class commonType, boolean copyPublic) {
      if (source == null) {
         throw new IllegalArgumentException("Source cannot be NULL");
      } else if (destination == null) {
         throw new IllegalArgumentException("Destination cannot be NULL");
      } else {
         StructureModifier<Object> modifier = this.getModifier(commonType);
         StructureModifier<Object> modifierSource = modifier.withTarget(source);
         StructureModifier<Object> modifierDest = modifier.withTarget(destination);

         try {
            for(int i = 0; i < modifierSource.size(); ++i) {
               Field field = modifierSource.getField(i);
               int mod = field.getModifiers();
               if (!Modifier.isStatic(mod) && (!Modifier.isPublic(mod) || copyPublic)) {
                  this.transformField(modifierSource, modifierDest, i);
               }
            }

            Class<?> superclass = commonType.getSuperclass();
            if (!superclass.equals(Object.class)) {
               this.copyToInternal(source, destination, superclass, false);
            }

         } catch (FieldAccessException e) {
            throw new RuntimeException("Unable to copy fields from " + commonType.getName(), e);
         }
      }
   }
}
