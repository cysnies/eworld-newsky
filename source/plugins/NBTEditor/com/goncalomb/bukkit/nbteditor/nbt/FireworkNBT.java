package com.goncalomb.bukkit.nbteditor.nbt;

import com.goncalomb.bukkit.nbteditor.nbt.variable.IntegerVariable;
import com.goncalomb.bukkit.nbteditor.nbt.variable.NBTGenericVariableContainer;
import com.goncalomb.bukkit.reflect.NBTUtils;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

public final class FireworkNBT extends EntityNBT {
   static {
      NBTGenericVariableContainer variables = new NBTGenericVariableContainer("Firework");
      variables.add("life", new IntegerVariable("Life", 0, 200));
      variables.add("lifetime", new IntegerVariable("LifeTime", 0, 200));
      EntityNBTVariableManager.registerVariables(FireworkNBT.class, variables);
   }

   public FireworkNBT() {
      super(EntityType.FIREWORK);
   }

   public FireworkNBT(ItemStack firework) {
      this();
      if (firework.getType() != Material.FIREWORK) {
         throw new IllegalArgumentException("Invalid argument firework.");
      } else {
         this._data.setInt("Life", 0);
         this._data.setCompound("FireworksItem", NBTUtils.nbtTagCompoundFromItemStack(firework));
         this.setLifeTimeFromItem(firework);
      }
   }

   private void setLifeTimeFromItem(ItemStack firework) {
      if (firework == null) {
         this._data.remove("FireworksItem");
      } else {
         this._data.setInt("LifeTime", 12 + 12 * ((FireworkMeta)firework.getItemMeta()).getPower());
      }

   }

   public void setFirework(ItemStack firework) {
      if (firework == null) {
         this._data.remove("FireworksItem");
      } else {
         this._data.setCompound("FireworksItem", NBTUtils.nbtTagCompoundFromItemStack(firework));
      }

      this.setLifeTimeFromItem(firework);
   }

   public ItemStack getFirework() {
      return this._data.hasKey("FireworksItem") ? NBTUtils.itemStackFromNBTTagCompound(this._data.getCompound("FireworksItem")) : null;
   }

   public boolean isSet() {
      return this._data.hasKey("FireworksItem");
   }
}
