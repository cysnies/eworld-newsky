package com.goncalomb.bukkit.nbteditor.nbt.variable;

import com.goncalomb.bukkit.reflect.NBTTagCompoundWrapper;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public final class NBTVariableIterator implements Iterator {
   Iterator _state;
   NBTTagCompoundWrapper _data;
   String _separator;

   NBTVariableIterator(LinkedHashMap hashMap, NBTTagCompoundWrapper data) {
      this._state = hashMap.entrySet().iterator();
      this._data = data;
   }

   public boolean hasNext() {
      return this._state.hasNext();
   }

   public NBTVariable next() {
      Map.Entry<String, NBTGenericVariable> entry = (Map.Entry)this._state.next();
      return new NBTVariable((String)entry.getKey(), (NBTGenericVariable)entry.getValue(), this._data);
   }

   public void remove() {
      throw new Error("Cannot remove NBTVariables.");
   }
}
