package com.goncalomb.bukkit.customitems.api;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDispenseEvent;

final class ListenerForDispenseEvent extends CustomItemListener {
   public boolean put(CustomItem customItem) {
      try {
         return this.isOverriden(customItem, "onDispense", new Class[]{BlockDispenseEvent.class, DispenserDetails.class}) ? super.put(customItem) : false;
      } catch (NoSuchMethodException e) {
         throw new Error(e);
      }
   }

   @EventHandler
   private void blockDispense(BlockDispenseEvent event) {
      if (event.getBlock().getType() == Material.DISPENSER) {
         CustomItem customItem = this.get(event.getItem());
         if (customItem != null) {
            if (this.verifyCustomItem(customItem, event.getBlock().getWorld())) {
               customItem.onDispense(event, new DispenserDetails(event, customItem._owner));
            } else {
               event.setCancelled(true);
            }
         }

      }
   }
}
