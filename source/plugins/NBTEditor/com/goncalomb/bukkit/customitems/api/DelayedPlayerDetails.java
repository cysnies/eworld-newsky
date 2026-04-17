package com.goncalomb.bukkit.customitems.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DelayedPlayerDetails extends PlayerDetails {
   private boolean _locked = false;
   Object _userObject;

   DelayedPlayerDetails(ItemStack item, Player player) {
      super(item, player);
   }

   void lock() {
      this._locked = true;
   }

   public final void consumeItem() {
      if (!this._locked && this._player != null) {
         super.consumeItem();
      }

   }

   public final void setUserObject(Object object) {
      this._userObject = object;
   }

   public final Object getUserObject() {
      return this._userObject;
   }
}
