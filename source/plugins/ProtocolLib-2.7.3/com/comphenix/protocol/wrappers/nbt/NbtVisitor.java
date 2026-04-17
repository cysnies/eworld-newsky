package com.comphenix.protocol.wrappers.nbt;

public interface NbtVisitor {
   boolean visit(NbtBase var1);

   boolean visitEnter(NbtList var1);

   boolean visitEnter(NbtCompound var1);

   boolean visitLeave(NbtList var1);

   boolean visitLeave(NbtCompound var1);
}
