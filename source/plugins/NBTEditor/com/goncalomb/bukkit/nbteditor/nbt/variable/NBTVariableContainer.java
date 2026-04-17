package com.goncalomb.bukkit.nbteditor.nbt.variable;

import com.goncalomb.bukkit.reflect.NBTTagCompoundWrapper;
import java.util.Iterator;
import java.util.Set;

public final class NBTVariableContainer implements Iterable {
   NBTGenericVariableContainer _generic;
   NBTTagCompoundWrapper _data;

   NBTVariableContainer(NBTGenericVariableContainer generic, NBTTagCompoundWrapper data) {
      this._generic = generic;
      this._data = data;
   }

   public boolean hasVariable(String name) {
      return this._generic._variables.containsKey(name);
   }

   public String getName() {
      return this._generic._name;
   }

   public Set getVarNames() {
      return this._generic.getVarNames();
   }

   public NBTVariable getVariable(String name) {
      return new NBTVariable(name, (NBTGenericVariable)this._generic._variables.get(name), this._data);
   }

   public Iterator iterator() {
      return new NBTVariableIterator(this._generic._variables, this._data);
   }
}
