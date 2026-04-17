package com.goncalomb.bukkit.nbteditor.nbt.variable;

import com.goncalomb.bukkit.reflect.NBTTagCompoundWrapper;

public final class IntegerVariable extends NumericVariable {
   public IntegerVariable(String nbtKey) {
      this(nbtKey, Integer.MIN_VALUE);
   }

   public IntegerVariable(String nbtKey, int min) {
      this(nbtKey, min, Integer.MAX_VALUE);
   }

   public IntegerVariable(String nbtKey, int min, int max) {
      super(nbtKey, min, max);
   }

   boolean set(NBTTagCompoundWrapper data, String value) {
      try {
         int v = Integer.parseInt(value);
         if (v >= this._min && v <= this._max) {
            data.setInt(this._nbtKey, v);
            return true;
         } else {
            return false;
         }
      } catch (NumberFormatException var4) {
         return false;
      }
   }

   String get(NBTTagCompoundWrapper data) {
      return data.hasKey(this._nbtKey) ? String.valueOf(data.getInt(this._nbtKey)) : null;
   }
}
