package com.goncalomb.bukkit.nbteditor.nbt.variable;

import com.goncalomb.bukkit.betterplugin.Lang;

public abstract class NumericVariable extends NBTGenericVariable {
   protected int _min;
   protected int _max;

   public NumericVariable(String nbtKey, int min, int max) {
      super(nbtKey);
      this._min = min;
      this._max = max;
   }

   String getFormat() {
      return Lang._format("nbt.variable.formats.integer", this._min, this._max);
   }
}
