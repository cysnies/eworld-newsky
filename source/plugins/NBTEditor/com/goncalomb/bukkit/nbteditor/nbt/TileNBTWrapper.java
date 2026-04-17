package com.goncalomb.bukkit.nbteditor.nbt;

import com.goncalomb.bukkit.reflect.NBTTagCompoundWrapper;
import com.goncalomb.bukkit.reflect.NBTUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class TileNBTWrapper {
   protected Block _block;
   protected NBTTagCompoundWrapper _data;

   public static final boolean allowsCustomName(Material mat) {
      return mat == Material.CHEST || mat == Material.FURNACE || mat == Material.DISPENSER || mat == Material.DROPPER || mat == Material.HOPPER || mat == Material.BREWING_STAND || mat == Material.ENCHANTMENT_TABLE || mat == Material.COMMAND;
   }

   public TileNBTWrapper(Block block) {
      this._block = block;
      this._data = NBTUtils.getTileEntityNBTTagCompound(this._block);
   }

   public final boolean allowsCustomName() {
      return allowsCustomName(this._block.getType());
   }

   public final void setCustomName(String name) {
      if (this.allowsCustomName()) {
         if (name == null) {
            this._data.setString("CustomName", "");
         } else {
            this._data.setString("CustomName", name);
         }
      }

   }

   public final String getCustomName() {
      return this.allowsCustomName() ? this._data.getString("CustomName") : null;
   }

   public final Location getLocation() {
      return this._block.getLocation();
   }

   public void save() {
      NBTUtils.setTileEntityNBTTagCompound(this._block, this._data);
   }
}
