package com.goncalomb.bukkit.nbteditor.nbt;

import com.goncalomb.bukkit.reflect.NBTUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public class DroppedItemNBT extends ItemsNBT {
   public void setItem(ItemStack item) {
      if (item == null) {
         this._data.remove("Item");
      } else {
         this._data.setCompound("Item", NBTUtils.nbtTagCompoundFromItemStack(item));
      }

   }

   public ItemStack getItem() {
      return this._data.hasKey("Item") ? NBTUtils.itemStackFromNBTTagCompound(this._data.getCompound("Item")) : null;
   }

   public boolean isSet() {
      return this._data.hasKey("Item");
   }

   public Entity spawn(Location location) {
      if (this._data.hasKey("Item")) {
         Entity entity = location.getWorld().dropItem(location, this.getItem());
         NBTUtils.setEntityNBTTagCompound(entity, this._data);
         return entity;
      } else {
         return null;
      }
   }
}
