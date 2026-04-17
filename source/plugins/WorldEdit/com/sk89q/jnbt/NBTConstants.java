package com.sk89q.jnbt;

import java.nio.charset.Charset;

public final class NBTConstants {
   public static final Charset CHARSET = Charset.forName("UTF-8");
   public static final int TYPE_END = 0;
   public static final int TYPE_BYTE = 1;
   public static final int TYPE_SHORT = 2;
   public static final int TYPE_INT = 3;
   public static final int TYPE_LONG = 4;
   public static final int TYPE_FLOAT = 5;
   public static final int TYPE_DOUBLE = 6;
   public static final int TYPE_BYTE_ARRAY = 7;
   public static final int TYPE_STRING = 8;
   public static final int TYPE_LIST = 9;
   public static final int TYPE_COMPOUND = 10;
   public static final int TYPE_INT_ARRAY = 11;

   private NBTConstants() {
   }

   public static Class getClassFromType(int id) {
      switch (id) {
         case 0:
            return EndTag.class;
         case 1:
            return ByteTag.class;
         case 2:
            return ShortTag.class;
         case 3:
            return IntTag.class;
         case 4:
            return LongTag.class;
         case 5:
            return FloatTag.class;
         case 6:
            return DoubleTag.class;
         case 7:
            return ByteArrayTag.class;
         case 8:
            return StringTag.class;
         case 9:
            return ListTag.class;
         case 10:
            return CompoundTag.class;
         case 11:
            return IntArrayTag.class;
         default:
            throw new IllegalArgumentException("Unknown tag type ID of " + id);
      }
   }
}
