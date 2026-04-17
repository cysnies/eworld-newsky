package com.goncalomb.bukkit.nbteditor.nbt.variable;

import com.goncalomb.bukkit.betterplugin.Lang;
import com.goncalomb.bukkit.reflect.NBTTagCompoundWrapper;

public class StringVariable extends NBTGenericVariable {
   public StringVariable(String nbtKey) {
      super(nbtKey);
   }

   boolean set(NBTTagCompoundWrapper data, String value) {
      if (value.length() > 64) {
         return false;
      } else {
         data.setString(this._nbtKey, value);
         return true;
      }
   }

   String get(NBTTagCompoundWrapper data) {
      return data.hasKey(this._nbtKey) ? data.getString(this._nbtKey) : null;
   }

   String getFormat() {
      return Lang._("nbt.variable.formats.string");
   }
}
