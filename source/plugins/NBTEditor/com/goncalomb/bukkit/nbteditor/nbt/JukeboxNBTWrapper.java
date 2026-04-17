package com.goncalomb.bukkit.nbteditor.nbt;

import com.goncalomb.bukkit.reflect.NBTTagCompoundWrapper;
import com.goncalomb.bukkit.reflect.NBTUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public final class JukeboxNBTWrapper extends TileNBTWrapper {
   public JukeboxNBTWrapper(Block block) {
      super(block);
   }

   public void setRecord(ItemStack item) {
      if (item != null && item.getType() != Material.AIR) {
         this._data.setInt("Record", item.getTypeId());
         this._data.setCompound("RecordItem", NBTUtils.nbtTagCompoundFromItemStack(item));
      } else {
         this._data.setInt("Record", 0);
         this._data.setCompound("RecordItem", new NBTTagCompoundWrapper());
      }

   }

   public void save() {
      if (this._data.getInt("Record") != 0 && this._block.getData() == 0) {
         this._block.setData((byte)1);
      } else if (this._data.getInt("Record") == 0 && this._block.getData() != 0) {
         this._block.setData((byte)0);
      }

      super.save();
   }
}
