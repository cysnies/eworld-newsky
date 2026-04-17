package com.comphenix.protocol.wrappers.nbt;

import com.google.common.primitives.Primitives;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum NbtType {
   TAG_END(0, Void.class),
   TAG_BYTE(1, Byte.TYPE),
   TAG_SHORT(2, Short.TYPE),
   TAG_INT(3, Integer.TYPE),
   TAG_LONG(4, Long.TYPE),
   TAG_FLOAT(5, Float.TYPE),
   TAG_DOUBLE(6, Double.TYPE),
   TAG_BYTE_ARRAY(7, byte[].class),
   TAG_INT_ARRAY(11, int[].class),
   TAG_STRING(8, String.class),
   TAG_LIST(9, List.class),
   TAG_COMPOUND(10, Map.class);

   private int rawID;
   private Class valueType;
   private static NbtType[] lookup;
   private static Map classLookup;

   private NbtType(int rawID, Class valueType) {
      this.rawID = rawID;
      this.valueType = valueType;
   }

   public boolean isComposite() {
      return this == TAG_COMPOUND || this == TAG_LIST;
   }

   public int getRawID() {
      return this.rawID;
   }

   public Class getValueType() {
      return this.valueType;
   }

   public static NbtType getTypeFromID(int rawID) {
      if (rawID >= 0 && rawID < lookup.length) {
         return lookup[rawID];
      } else {
         throw new IllegalArgumentException("Unrecognized raw ID " + rawID);
      }
   }

   public static NbtType getTypeFromClass(Class clazz) {
      NbtType result = (NbtType)classLookup.get(clazz);
      if (result != null) {
         return result;
      } else {
         for(Class implemented : clazz.getInterfaces()) {
            if (classLookup.containsKey(implemented)) {
               return (NbtType)classLookup.get(implemented);
            }
         }

         throw new IllegalArgumentException("No NBT tag can represent a " + clazz);
      }
   }

   static {
      NbtType[] values = values();
      lookup = new NbtType[values.length];
      classLookup = new HashMap();

      for(NbtType type : values) {
         lookup[type.getRawID()] = type;
         classLookup.put(type.getValueType(), type);
         if (type.getValueType().isPrimitive()) {
            classLookup.put(Primitives.wrap(type.getValueType()), type);
         }
      }

      classLookup.put(NbtList.class, TAG_LIST);
      classLookup.put(NbtCompound.class, TAG_COMPOUND);
   }
}
