package com.goncalomb.bukkit.nbteditor.nbt;

import com.goncalomb.bukkit.reflect.NBTTagCompoundWrapper;
import com.goncalomb.bukkit.reflect.NBTTagListWrapper;
import com.goncalomb.bukkit.reflect.NBTUtils;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class MinecartContainerNBT extends MinecartNBT {
   protected final void internalCopyFromChest(Block block, int count) {
      Inventory inv = ((Chest)block.getState()).getBlockInventory();
      NBTTagListWrapper items = new NBTTagListWrapper();
      int i = 0;

      for(int l = count; i < l; ++i) {
         ItemStack item = inv.getItem(i);
         if (item != null) {
            NBTTagCompoundWrapper itemNBT = NBTUtils.nbtTagCompoundFromItemStack(item);
            itemNBT.setByte("Slot", (byte)i);
            items.add(itemNBT);
         }
      }

      this._data.setList("Items", items);
   }

   public abstract void copyFromChest(Block var1);

   public final void copyToChest(Block block) {
      Inventory inv = ((Chest)block.getState()).getBlockInventory();
      inv.clear();
      if (this._data.hasKey("Items")) {
         NBTTagListWrapper items = this._data.getList("Items");
         int i = 0;

         for(int l = items.size(); i < l; ++i) {
            NBTTagCompoundWrapper itemNBT = (NBTTagCompoundWrapper)items.get(i);
            inv.setItem(itemNBT.getByte("Slot"), NBTUtils.itemStackFromNBTTagCompound(itemNBT));
         }
      }

   }
}
