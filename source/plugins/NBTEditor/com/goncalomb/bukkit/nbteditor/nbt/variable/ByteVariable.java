package com.goncalomb.bukkit.nbteditor.nbt.variable;

import com.goncalomb.bukkit.reflect.NBTTagCompoundWrapper;

public final class ByteVariable extends NumericVariable {
   public ByteVariable(String nbtKey) {
      this(nbtKey, (byte)-128);
   }

   public ByteVariable(String nbtKey, byte min) {
      this(nbtKey, min, (byte)127);
   }

   public ByteVariable(String nbtKey, byte min, byte max) {
      super(nbtKey, min, max);
   }

   boolean set(NBTTagCompoundWrapper data, String value) {
      try {
         byte v = Byte.parseByte(value);
         if (v >= this._min && v <= this._max) {
            data.setByte(this._nbtKey, v);
            return true;
         } else {
            return false;
         }
      } catch (NumberFormatException var4) {
         return false;
      }
   }

   String get(NBTTagCompoundWrapper data) {
      return data.hasKey(this._nbtKey) ? String.valueOf(data.getByte(this._nbtKey)) : null;
   }
}
