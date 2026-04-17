package com.goncalomb.bukkit.nbteditor.nbt.variable;

import com.goncalomb.bukkit.reflect.NBTTagCompoundWrapper;

public final class NBTVariable {
   private String _name;
   private NBTGenericVariable _generic;
   private NBTTagCompoundWrapper _data;

   NBTVariable(String name, NBTGenericVariable generic, NBTTagCompoundWrapper data) {
      this._name = name;
      this._generic = generic;
      this._data = data;
   }

   public String getName() {
      return this._name;
   }

   public boolean setValue(String value) {
      return this._generic.set(this._data, value);
   }

   public String getValue() {
      return this._generic.get(this._data);
   }

   public void clear() {
      this._generic.clear(this._data);
   }

   public String getFormat() {
      return this._generic.getFormat();
   }
}
