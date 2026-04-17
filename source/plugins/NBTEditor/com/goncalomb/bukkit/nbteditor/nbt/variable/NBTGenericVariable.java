package com.goncalomb.bukkit.nbteditor.nbt.variable;

import com.goncalomb.bukkit.reflect.NBTTagCompoundWrapper;

public abstract class NBTGenericVariable {
   protected String _nbtKey;

   NBTGenericVariable(String nbtKey) {
      this._nbtKey = nbtKey;
   }

   abstract boolean set(NBTTagCompoundWrapper var1, String var2);

   abstract String get(NBTTagCompoundWrapper var1);

   void clear(NBTTagCompoundWrapper data) {
      data.remove(this._nbtKey);
   }

   abstract String getFormat();
}
