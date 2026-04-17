package com.sk89q.worldedit.foundation;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.data.DataException;

public interface NbtValued {
   boolean hasNbtData();

   CompoundTag getNbtData();

   void setNbtData(CompoundTag var1) throws DataException;
}
