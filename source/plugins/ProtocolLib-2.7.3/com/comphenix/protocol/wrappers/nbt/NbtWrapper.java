package com.comphenix.protocol.wrappers.nbt;

import java.io.DataOutput;

public interface NbtWrapper extends NbtBase {
   Object getHandle();

   void write(DataOutput var1);
}
