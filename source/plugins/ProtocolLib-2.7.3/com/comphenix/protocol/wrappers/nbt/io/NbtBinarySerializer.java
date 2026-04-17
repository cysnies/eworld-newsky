package com.comphenix.protocol.wrappers.nbt.io;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtList;
import com.comphenix.protocol.wrappers.nbt.NbtWrapper;
import java.io.DataInput;
import java.io.DataOutput;
import java.lang.reflect.Method;

public class NbtBinarySerializer {
   private static Method methodWrite;
   private static Method methodLoad;
   public static final NbtBinarySerializer DEFAULT = new NbtBinarySerializer();

   public void serialize(NbtBase value, DataOutput destination) {
      if (methodWrite == null) {
         Class<?> base = MinecraftReflection.getNBTBaseClass();
         methodWrite = FuzzyReflection.fromClass(base).getMethodByParameters("writeNBT", base, DataOutput.class);
      }

      try {
         methodWrite.invoke((Object)null, NbtFactory.fromBase(value).getHandle(), destination);
      } catch (Exception e) {
         throw new FieldAccessException("Unable to write NBT " + value, e);
      }
   }

   public NbtWrapper deserialize(DataInput source) {
      if (methodLoad == null) {
         Class<?> base = MinecraftReflection.getNBTBaseClass();
         methodLoad = FuzzyReflection.fromClass(base).getMethodByParameters("load", base, new Class[]{DataInput.class});
      }

      try {
         return NbtFactory.fromNMS(methodLoad.invoke((Object)null, source));
      } catch (Exception e) {
         throw new FieldAccessException("Unable to read NBT from " + source, e);
      }
   }

   public NbtCompound deserializeCompound(DataInput source) {
      return (NbtCompound)this.deserialize(source);
   }

   public NbtList deserializeList(DataInput source) {
      return (NbtList)this.deserialize(source);
   }
}
