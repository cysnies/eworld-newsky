package com.sk89q.jnbt;

import com.sk89q.worldedit.data.InvalidFormatException;
import java.util.Map;

public final class NBTUtils {
   public static String getTypeName(Class clazz) {
      if (clazz.equals(ByteArrayTag.class)) {
         return "TAG_Byte_Array";
      } else if (clazz.equals(ByteTag.class)) {
         return "TAG_Byte";
      } else if (clazz.equals(CompoundTag.class)) {
         return "TAG_Compound";
      } else if (clazz.equals(DoubleTag.class)) {
         return "TAG_Double";
      } else if (clazz.equals(EndTag.class)) {
         return "TAG_End";
      } else if (clazz.equals(FloatTag.class)) {
         return "TAG_Float";
      } else if (clazz.equals(IntTag.class)) {
         return "TAG_Int";
      } else if (clazz.equals(ListTag.class)) {
         return "TAG_List";
      } else if (clazz.equals(LongTag.class)) {
         return "TAG_Long";
      } else if (clazz.equals(ShortTag.class)) {
         return "TAG_Short";
      } else if (clazz.equals(StringTag.class)) {
         return "TAG_String";
      } else if (clazz.equals(IntArrayTag.class)) {
         return "TAG_Int_Array";
      } else {
         throw new IllegalArgumentException("Invalid tag classs (" + clazz.getName() + ").");
      }
   }

   public static int getTypeCode(Class clazz) {
      if (clazz.equals(ByteArrayTag.class)) {
         return 7;
      } else if (clazz.equals(ByteTag.class)) {
         return 1;
      } else if (clazz.equals(CompoundTag.class)) {
         return 10;
      } else if (clazz.equals(DoubleTag.class)) {
         return 6;
      } else if (clazz.equals(EndTag.class)) {
         return 0;
      } else if (clazz.equals(FloatTag.class)) {
         return 5;
      } else if (clazz.equals(IntTag.class)) {
         return 3;
      } else if (clazz.equals(ListTag.class)) {
         return 9;
      } else if (clazz.equals(LongTag.class)) {
         return 4;
      } else if (clazz.equals(ShortTag.class)) {
         return 2;
      } else if (clazz.equals(StringTag.class)) {
         return 8;
      } else if (clazz.equals(IntArrayTag.class)) {
         return 11;
      } else {
         throw new IllegalArgumentException("Invalid tag classs (" + clazz.getName() + ").");
      }
   }

   public static Class getTypeClass(int type) {
      switch (type) {
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
            throw new IllegalArgumentException("Invalid tag type : " + type + ".");
      }
   }

   private NBTUtils() {
   }

   public static Tag getChildTag(Map items, String key, Class expected) throws InvalidFormatException {
      if (!items.containsKey(key)) {
         throw new InvalidFormatException("Missing a \"" + key + "\" tag");
      } else {
         Tag tag = (Tag)items.get(key);
         if (!expected.isInstance(tag)) {
            throw new InvalidFormatException(key + " tag is not of tag type " + expected.getName());
         } else {
            return (Tag)expected.cast(tag);
         }
      }
   }
}
