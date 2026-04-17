package com.goncalomb.bukkit.nbteditor.nbt;

import com.goncalomb.bukkit.nbteditor.nbt.variable.NBTGenericVariableContainer;
import com.goncalomb.bukkit.nbteditor.nbt.variable.StringVariable;
import com.goncalomb.bukkit.reflect.WorldUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class EnderPearlNBT extends EntityNBT {
   static {
      NBTGenericVariableContainer variables = new NBTGenericVariableContainer("Enderpearl");
      variables.add("owner", new StringVariable("ownerName"));
      EntityNBTVariableManager.registerVariables(EntityType.ENDER_PEARL, variables);
   }

   public Entity spawn(Location location) {
      return WorldUtils.spawnEnderpearl(location, this._data);
   }
}
