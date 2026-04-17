package com.comphenix.protocol.wrappers.nbt;

public interface NbtBase {
   boolean accept(NbtVisitor var1);

   NbtType getType();

   String getName();

   void setName(String var1);

   Object getValue();

   void setValue(Object var1);

   NbtBase deepClone();
}
