package com.goncalomb.bukkit.nbteditor.nbt.variable;

import com.goncalomb.bukkit.betterplugin.Lang;
import com.goncalomb.bukkit.reflect.NBTTagCompoundWrapper;

public final class FloatVariable extends NBTGenericVariable {
   private float _min;
   float _max;

   public FloatVariable(String nbtKey) {
      this(nbtKey, -Float.MAX_VALUE);
   }

   public FloatVariable(String nbtKey, float min) {
      this(nbtKey, min, Float.MAX_VALUE);
   }

   public FloatVariable(String nbtKey, float min, float max) {
      super(nbtKey);
      this._min = min;
      this._max = max;
   }

   boolean set(NBTTagCompoundWrapper data, String value) {
      try {
         float v = Float.parseFloat(value);
         if (!(v < this._min) && !(v > this._max)) {
            data.setFloat(this._nbtKey, v);
            return true;
         } else {
            return false;
         }
      } catch (NumberFormatException var4) {
         return false;
      }
   }

   String get(NBTTagCompoundWrapper data) {
      return data.hasKey(this._nbtKey) ? String.valueOf(data.getFloat(this._nbtKey)) : null;
   }

   String getFormat() {
      return Lang._format("nbt.variable.formats.float", this._min, this._max);
   }
}
