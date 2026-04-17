package com.goncalomb.bukkit.customitems.api;

import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

final class ListenerForPlayerDeathEvent extends CustomItemListener {
   public boolean put(CustomItem customItem) {
      try {
         return this.isOverriden(customItem, "onPlayerDeath", new Class[]{PlayerDeathEvent.class, PlayerInventoryDetails.class}) ? super.put(customItem) : false;
      } catch (NoSuchMethodException e) {
         throw new Error(e);
      }
   }

   @EventHandler
   private void playerDeath(PlayerDeathEvent event) {
      if (this.size() > 0) {
         Player player = event.getEntity();
         PlayerInventory inv = player.getInventory();
         int i = 0;

         for(int l = inv.getSize() + 4; i < l; ++i) {
            ItemStack item = inv.getItem(i);
            CustomItem customItem = this.get(item);
            if (this.verifyCustomItem(customItem, player, true)) {
               customItem.onPlayerDeath(event, new PlayerInventoryDetails(item, player, i));
               List<ItemStack> drops = event.getDrops();
               drops.clear();

               ItemStack[] var12;
               for(ItemStack drop : var12 = inv.getContents()) {
                  drops.add(drop);
               }

               for(ItemStack drop : var12 = inv.getArmorContents()) {
                  drops.add(drop);
               }

               return;
            }
         }
      }

   }
}
