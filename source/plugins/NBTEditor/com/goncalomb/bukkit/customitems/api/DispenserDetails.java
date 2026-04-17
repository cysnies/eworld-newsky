package com.goncalomb.bukkit.customitems.api;

import com.goncalomb.bukkit.UtilsMc;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dispenser;
import org.bukkit.plugin.Plugin;

public final class DispenserDetails extends ItemDetails implements IConsumableDetails {
   private Plugin _plugin;
   private Block _block;
   private Location _location;

   DispenserDetails(BlockDispenseEvent event, Plugin plugin) {
      super(event.getItem());
      this._block = event.getBlock();
      this._plugin = plugin;
   }

   public Location getLocation() {
      if (this._location == null) {
         BlockFace face = ((Dispenser)this._block.getState().getData()).getFacing();
         this._location = this._block.getLocation().add(UtilsMc.faceToDelta(face, 0.2)).add((double)0.0F, -0.3, (double)0.0F);
      }

      return this._location;
   }

   public void consumeItem() {
      org.bukkit.block.Dispenser disp = (org.bukkit.block.Dispenser)this._block.getState();
      final Inventory inv = disp.getInventory();
      Bukkit.getScheduler().runTask(this._plugin, new Runnable() {
         public void run() {
            for(int i = 0; i < inv.getSize(); ++i) {
               ItemStack item = inv.getItem(i);
               if (item != null && item.isSimilar(DispenserDetails.this._item)) {
                  if (item.getAmount() > 1) {
                     item.setAmount(item.getAmount() - 1);
                  } else {
                     inv.clear(i);
                  }

                  return;
               }
            }

         }
      });
   }
}
