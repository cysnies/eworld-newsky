package com.goncalomb.bukkit.nbteditor.nbt;

import com.goncalomb.bukkit.nbteditor.nbt.variable.NBTGenericVariableContainer;
import com.goncalomb.bukkit.nbteditor.nbt.variable.VectorVariable;

public class FireballNBT extends EntityNBT {
   static {
      NBTGenericVariableContainer variables = new NBTGenericVariableContainer("Fireball");
      variables.add("power", new VectorVariable("power"));
      EntityNBTVariableManager.registerVariables(FireballNBT.class, variables);
   }

   public FireballNBT() {
      this._data.setList("power", (double)0.0F, (double)0.0F, (double)0.0F);
   }
}
