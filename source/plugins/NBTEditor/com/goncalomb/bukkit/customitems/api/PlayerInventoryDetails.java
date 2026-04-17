package com.goncalomb.bukkit.customitems.api;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class PlayerInventoryDetails extends PlayerDetails {
   private int _slot;

   PlayerInventoryDetails(ItemStack item, Player player, int slot) {
      super(item, player);
      this._slot = slot;
   }

   public boolean isArmor() {
      return this._slot >= this._player.getInventory().getSize();
   }

   public void consumeItem() {
      if (this._player.getGameMode() != GameMode.CREATIVE) {
         if (this._item.getAmount() > 1) {
            this._item.setAmount(this._item.getAmount() - 1);
         } else {
            this._player.getInventory().clear(this._slot);
         }

      }
   }
}
