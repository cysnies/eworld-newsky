package com.comphenix.protocol.wrappers.nbt;

import java.util.Collection;
import java.util.Iterator;

public interface NbtList extends NbtBase, Iterable {
   String EMPTY_NAME = "";

   NbtType getElementType();

   void setElementType(NbtType var1);

   void addClosest(Object var1);

   void add(NbtBase var1);

   void add(String var1);

   void add(byte var1);

   void add(short var1);

   void add(int var1);

   void add(long var1);

   void add(double var1);

   void add(byte[] var1);

   void add(int[] var1);

   void remove(Object var1);

   Object getValue(int var1);

   int size();

   Collection asCollection();

   Iterator iterator();
}
