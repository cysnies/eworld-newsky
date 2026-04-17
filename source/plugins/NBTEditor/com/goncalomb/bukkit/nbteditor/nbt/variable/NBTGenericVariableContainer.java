package com.goncalomb.bukkit.nbteditor.nbt.variable;

import com.goncalomb.bukkit.reflect.NBTTagCompoundWrapper;
import java.util.LinkedHashMap;
import java.util.Set;

public final class NBTGenericVariableContainer {
   String _name;
   LinkedHashMap _variables;

   public NBTGenericVariableContainer(String name) {
      this._name = name;
      this._variables = new LinkedHashMap();
   }

   public void add(String name, NBTGenericVariable variable) {
      this._variables.put(name, variable);
   }

   public boolean hasVariable(String name) {
      return this._variables.containsKey(name);
   }

   public String getName() {
      return this._name;
   }

   public Set getVarNames() {
      return this._variables.keySet();
   }

   public NBTVariableContainer boundToData(NBTTagCompoundWrapper data) {
      return new NBTVariableContainer(this, data);
   }

   public NBTVariable getVariable(String name, NBTTagCompoundWrapper data) {
      return this.hasVariable(name) ? new NBTVariable(name, (NBTGenericVariable)this._variables.get(name), data) : null;
   }
}
