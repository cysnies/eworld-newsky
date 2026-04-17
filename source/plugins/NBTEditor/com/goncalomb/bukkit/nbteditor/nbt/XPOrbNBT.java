package com.goncalomb.bukkit.nbteditor.nbt;

import com.goncalomb.bukkit.nbteditor.nbt.variable.NBTGenericVariableContainer;
import com.goncalomb.bukkit.nbteditor.nbt.variable.ShortVariable;
import com.goncalomb.bukkit.reflect.NBTUtils;
import com.goncalomb.bukkit.reflect.WorldUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;

public class XPOrbNBT extends EntityNBT {
   static {
      NBTGenericVariableContainer variables = new NBTGenericVariableContainer("XPOrb");
      variables.add("value", new ShortVariable("Value", (short)0));
      EntityNBTVariableManager.registerVariables(EntityType.EXPERIENCE_ORB, variables);
   }

   public Entity spawn(Location location) {
      ExperienceOrb entity = WorldUtils.spawnXPOrb(location, this._data.hasKey("Value") ? this._data.getShort("Value") : 0);
      NBTUtils.setEntityNBTTagCompound(entity, this._data);
      return entity;
   }
}
