package com.goncalomb.bukkit.customitems.api;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerDetails extends ItemDetails implements IConsumableDetails {
   protected Player _player;

   PlayerDetails(PlayerInteractEvent event) {
      super(event.getItem());
      this._player = event.getPlayer();
   }

   PlayerDetails(ItemStack item, Player player) {
      super(item);
      this._player = player;
   }

   public final Player getPlayer() {
      return this._player;
   }

   public void consumeItem() {
      if (this._player.getGameMode() != GameMode.CREATIVE) {
         if (this._item.getAmount() > 1) {
            this._item.setAmount(this._item.getAmount() - 1);
         } else {
            this._player.setItemInHand((ItemStack)null);
         }

      }
   }
}
