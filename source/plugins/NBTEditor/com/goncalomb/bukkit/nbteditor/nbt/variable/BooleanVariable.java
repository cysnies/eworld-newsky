package com.goncalomb.bukkit.nbteditor.nbt.variable;

import com.goncalomb.bukkit.betterplugin.Lang;
import com.goncalomb.bukkit.reflect.NBTTagCompoundWrapper;

public final class BooleanVariable extends NBTGenericVariable {
   public BooleanVariable(String nbtKey) {
      super(nbtKey);
   }

   boolean set(NBTTagCompoundWrapper data, String value) {
      if (value.equalsIgnoreCase("true")) {
         data.setByte(this._nbtKey, (byte)1);
      } else {
         if (!value.equalsIgnoreCase("false")) {
            return false;
         }

         data.setByte(this._nbtKey, (byte)0);
      }

      return true;
   }

   String get(NBTTagCompoundWrapper data) {
      if (data.hasKey(this._nbtKey)) {
         return data.getByte(this._nbtKey) > 0 ? "true" : "false";
      } else {
         return null;
      }
   }

   String getFormat() {
      return Lang._("nbt.variable.formats.boolean");
   }
}
