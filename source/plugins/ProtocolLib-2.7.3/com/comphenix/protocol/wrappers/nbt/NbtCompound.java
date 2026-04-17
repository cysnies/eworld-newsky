package com.comphenix.protocol.wrappers.nbt;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

public interface NbtCompound extends NbtBase, Iterable {
   /** @deprecated */
   @Deprecated
   Map getValue();

   boolean containsKey(String var1);

   Set getKeys();

   NbtBase getValue(String var1);

   NbtBase getValueOrDefault(String var1, NbtType var2);

   NbtCompound put(@Nonnull NbtBase var1);

   String getString(String var1);

   String getStringOrDefault(String var1);

   NbtCompound put(String var1, String var2);

   NbtCompound put(String var1, NbtBase var2);

   byte getByte(String var1);

   byte getByteOrDefault(String var1);

   NbtCompound put(String var1, byte var2);

   Short getShort(String var1);

   short getShortOrDefault(String var1);

   NbtCompound put(String var1, short var2);

   int getInteger(String var1);

   int getIntegerOrDefault(String var1);

   NbtCompound put(String var1, int var2);

   long getLong(String var1);

   long getLongOrDefault(String var1);

   NbtCompound put(String var1, long var2);

   float getFloat(String var1);

   float getFloatOrDefault(String var1);

   NbtCompound put(String var1, float var2);

   double getDouble(String var1);

   double getDoubleOrDefault(String var1);

   NbtCompound put(String var1, double var2);

   byte[] getByteArray(String var1);

   NbtCompound put(String var1, byte[] var2);

   int[] getIntegerArray(String var1);

   NbtCompound put(String var1, int[] var2);

   NbtCompound putObject(String var1, Object var2);

   Object getObject(String var1);

   NbtCompound getCompound(String var1);

   NbtCompound getCompoundOrDefault(String var1);

   NbtCompound put(NbtCompound var1);

   NbtList getList(String var1);

   NbtList getListOrDefault(String var1);

   NbtCompound put(NbtList var1);

   NbtCompound put(String var1, Collection var2);

   NbtBase remove(String var1);

   Iterator iterator();
}
