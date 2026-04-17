package com.goncalomb.bukkit.nbteditor.nbt.variable;

import com.goncalomb.bukkit.reflect.NBTTagCompoundWrapper;

public final class ShortVariable extends NumericVariable {
   public ShortVariable(String nbtKey) {
      this(nbtKey, (short)Short.MIN_VALUE);
   }

   public ShortVariable(String nbtKey, short min) {
      this(nbtKey, min, (short)32767);
   }

   public ShortVariable(String nbtKey, short min, short max) {
      super(nbtKey, min, max);
   }

   boolean set(NBTTagCompoundWrapper data, String value) {
      try {
         short v = Short.parseShort(value);
         if (v >= this._min && v <= this._max) {
            data.setShort(this._nbtKey, v);
            return true;
         } else {
            return false;
         }
      } catch (NumberFormatException var4) {
         return false;
      }
   }

   String get(NBTTagCompoundWrapper data) {
      return data.hasKey(this._nbtKey) ? String.valueOf(data.getShort(this._nbtKey)) : null;
   }
}
