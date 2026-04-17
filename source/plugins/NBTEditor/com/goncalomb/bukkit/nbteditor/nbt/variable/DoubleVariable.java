package com.goncalomb.bukkit.nbteditor.nbt.variable;

import com.goncalomb.bukkit.betterplugin.Lang;
import com.goncalomb.bukkit.reflect.NBTTagCompoundWrapper;

public final class DoubleVariable extends NBTGenericVariable {
   public DoubleVariable(String nbtKey) {
      super(nbtKey);
   }

   boolean set(NBTTagCompoundWrapper data, String value) {
      try {
         data.setDouble(this._nbtKey, Double.parseDouble(value));
         return true;
      } catch (NumberFormatException var4) {
         return false;
      }
   }

   String get(NBTTagCompoundWrapper data) {
      return data.hasKey(this._nbtKey) ? String.valueOf(data.getDouble(this._nbtKey)) : null;
   }

   String getFormat() {
      return Lang._format("nbt.variable.formats.double");
   }
}
