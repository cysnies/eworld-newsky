package com.goncalomb.bukkit.nbteditor.nbt;

import com.goncalomb.bukkit.reflect.NBTUtils;
import com.goncalomb.bukkit.reflect.WorldUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public class ThrownPotionNBT extends EntityNBT {
   public void setPotion(ItemStack potion) {
      if (potion == null) {
         this._data.remove("Potion");
      } else {
         this._data.setCompound("Potion", NBTUtils.nbtTagCompoundFromItemStack(potion));
      }

   }

   public ItemStack getPotion() {
      return this._data.hasKey("Potion") ? NBTUtils.itemStackFromNBTTagCompound(this._data.getCompound("Potion")) : null;
   }

   public boolean isSet() {
      return this._data.hasKey("Potion");
   }

   public Entity spawn(Location location) {
      return this._data.hasKey("Potion") ? WorldUtils.spawnPotion(location, this._data) : null;
   }
}
