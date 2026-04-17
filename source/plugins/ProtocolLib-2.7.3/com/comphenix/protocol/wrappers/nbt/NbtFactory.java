package com.comphenix.protocol.wrappers.nbt;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.bukkit.inventory.ItemStack;

public class NbtFactory {
   private static Method methodCreateTag;
   private static StructureModifier itemStackModifier;

   public static NbtCompound asCompound(NbtBase tag) {
      if (tag instanceof NbtCompound) {
         return (NbtCompound)tag;
      } else if (tag != null) {
         throw new UnsupportedOperationException("Cannot cast a " + tag.getClass() + "( " + tag.getType() + ") to TAG_COMPUND.");
      } else {
         throw new IllegalArgumentException("Tag cannot be NULL.");
      }
   }

   public static NbtList asList(NbtBase tag) {
      if (tag instanceof NbtList) {
         return (NbtList)tag;
      } else if (tag != null) {
         throw new UnsupportedOperationException("Cannot cast a " + tag.getClass() + "( " + tag.getType() + ") to TAG_LIST.");
      } else {
         throw new IllegalArgumentException("Tag cannot be NULL.");
      }
   }

   public static NbtWrapper fromBase(NbtBase base) {
      if (base instanceof NbtWrapper) {
         return (NbtWrapper)base;
      } else if (base.getType() == NbtType.TAG_COMPOUND) {
         WrappedCompound copy = WrappedCompound.fromName(base.getName());
         T value = (T)base.getValue();
         copy.setValue((Map)value);
         return copy;
      } else if (base.getType() == NbtType.TAG_LIST) {
         NbtList<T> copy = WrappedList.fromName(base.getName());
         copy.setValue((List)base.getValue());
         return (NbtWrapper)copy;
      } else {
         NbtWrapper<T> copy = ofWrapper(base.getType(), base.getName());
         copy.setValue(base.getValue());
         return copy;
      }
   }

   public static void setItemTag(ItemStack stack, NbtCompound compound) {
      checkItemStack(stack);
      StructureModifier<NbtBase<?>> modifier = getStackModifier(stack);
      modifier.write(0, compound);
   }

   public static NbtWrapper fromItemTag(ItemStack stack) {
      checkItemStack(stack);
      StructureModifier<NbtBase<?>> modifier = getStackModifier(stack);
      NbtBase<?> result = (NbtBase)modifier.read(0);
      if (result == null) {
         result = ofCompound("tag");
         modifier.write(0, result);
      }

      return fromBase(result);
   }

   private static void checkItemStack(ItemStack stack) {
      if (stack == null) {
         throw new IllegalArgumentException("Stack cannot be NULL.");
      } else if (!MinecraftReflection.isCraftItemStack(stack)) {
         throw new IllegalArgumentException("Stack must be a CraftItemStack.");
      } else if (stack.getTypeId() == 0) {
         throw new IllegalArgumentException("ItemStacks representing air cannot store NMS information.");
      }
   }

   private static StructureModifier getStackModifier(ItemStack stack) {
      Object nmsStack = MinecraftReflection.getMinecraftItemStack(stack);
      if (itemStackModifier == null) {
         itemStackModifier = new StructureModifier(nmsStack.getClass(), Object.class, false);
      }

      return itemStackModifier.withTarget(nmsStack).withType(MinecraftReflection.getNBTBaseClass(), BukkitConverters.getNbtConverter());
   }

   public static NbtWrapper fromNMS(Object handle) {
      WrappedElement<T> partial = new WrappedElement(handle);
      if (partial.getType() == NbtType.TAG_COMPOUND) {
         return new WrappedCompound(handle);
      } else {
         return (NbtWrapper)(partial.getType() == NbtType.TAG_LIST ? new WrappedList(handle) : partial);
      }
   }

   public static NbtCompound fromNMSCompound(@Nonnull Object handle) {
      if (handle == null) {
         throw new IllegalArgumentException("handle cannot be NULL.");
      } else {
         return (NbtCompound)fromNMS(handle);
      }
   }

   public static NbtBase of(String name, String value) {
      return ofWrapper((NbtType)NbtType.TAG_STRING, name, value);
   }

   public static NbtBase of(String name, byte value) {
      return ofWrapper((NbtType)NbtType.TAG_BYTE, name, value);
   }

   public static NbtBase of(String name, short value) {
      return ofWrapper((NbtType)NbtType.TAG_SHORT, name, value);
   }

   public static NbtBase of(String name, int value) {
      return ofWrapper((NbtType)NbtType.TAG_INT, name, value);
   }

   public static NbtBase of(String name, long value) {
      return ofWrapper((NbtType)NbtType.TAG_LONG, name, value);
   }

   public static NbtBase of(String name, float value) {
      return ofWrapper((NbtType)NbtType.TAG_FLOAT, name, value);
   }

   public static NbtBase of(String name, double value) {
      return ofWrapper((NbtType)NbtType.TAG_DOUBLE, name, value);
   }

   public static NbtBase of(String name, byte[] value) {
      return ofWrapper((NbtType)NbtType.TAG_BYTE_ARRAY, name, value);
   }

   public static NbtBase of(String name, int[] value) {
      return ofWrapper((NbtType)NbtType.TAG_INT_ARRAY, name, value);
   }

   public static NbtCompound ofCompound(String name, Collection list) {
      return WrappedCompound.fromList(name, list);
   }

   public static NbtCompound ofCompound(String name) {
      return WrappedCompound.fromName(name);
   }

   public static NbtList ofList(String name, Object... elements) {
      return WrappedList.fromArray(name, elements);
   }

   public static NbtList ofList(String name, Collection elements) {
      return WrappedList.fromList(name, elements);
   }

   public static NbtWrapper ofWrapper(NbtType type, String name) {
      if (type == null) {
         throw new IllegalArgumentException("type cannot be NULL.");
      } else if (type == NbtType.TAG_END) {
         throw new IllegalArgumentException("Cannot create a TAG_END.");
      } else {
         if (methodCreateTag == null) {
            Class<?> base = MinecraftReflection.getNBTBaseClass();
            methodCreateTag = FuzzyReflection.fromClass(base).getMethodByParameters("createTag", base, new Class[]{Byte.TYPE, String.class});
         }

         try {
            Object handle = methodCreateTag.invoke((Object)null, (byte)type.getRawID(), name);
            if (type == NbtType.TAG_COMPOUND) {
               return new WrappedCompound(handle);
            } else {
               return (NbtWrapper)(type == NbtType.TAG_LIST ? new WrappedList(handle) : new WrappedElement(handle));
            }
         } catch (Exception e) {
            throw new FieldAccessException(String.format("Cannot create NBT element %s (type: %s)", name, type), e);
         }
      }
   }

   public static NbtWrapper ofWrapper(NbtType type, String name, Object value) {
      NbtWrapper<T> created = ofWrapper(type, name);
      created.setValue(value);
      return created;
   }

   public static NbtWrapper ofWrapper(Class type, String name, Object value) {
      return ofWrapper(NbtType.getTypeFromClass(type), name, value);
   }
}
