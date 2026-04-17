package com.goncalomb.bukkit.customitems.api;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

final class ListenerForItemEvents extends CustomItemListener {
   public boolean put(CustomItem customItem) {
      try {
         return !this.isOverriden(customItem, "onPickup", new Class[]{PlayerPickupItemEvent.class}) && !this.isOverriden(customItem, "onDrop", new Class[]{PlayerDropItemEvent.class}) && !this.isOverriden(customItem, "onDespawn", new Class[]{ItemDespawnEvent.class}) && !this.isOverriden(customItem, "onDropperPickup", new Class[]{InventoryPickupItemEvent.class}) ? false : super.put(customItem);
      } catch (NoSuchMethodException e) {
         throw new Error(e);
      }
   }

   @EventHandler
   private void playerPickupItem(PlayerPickupItemEvent event) {
      CustomItem customItem = this.get(event.getItem().getItemStack());
      if (this.verifyCustomItem(customItem, event.getPlayer(), true)) {
         customItem.onPickup(event);
      }

   }

   @EventHandler
   private void playerDropItem(PlayerDropItemEvent event) {
      CustomItem customItem = this.get(event.getItemDrop().getItemStack());
      if (this.verifyCustomItem(customItem, event.getPlayer(), true)) {
         customItem.onDrop(event);
      }

   }

   @EventHandler
   private void itemDespawnItem(ItemDespawnEvent event) {
      CustomItem customItem = this.get(event.getEntity().getItemStack());
      if (this.verifyCustomItem(customItem, event.getEntity().getWorld())) {
         customItem.onDespawn(event);
      }

   }

   @EventHandler
   private void inventoryPickupItemItem(InventoryPickupItemEvent event) {
      CustomItem customItem = this.get(event.getItem().getItemStack());
      if (this.verifyCustomItem(customItem, event.getItem().getWorld())) {
         customItem.onDropperPickup(event);
      }

   }
}
