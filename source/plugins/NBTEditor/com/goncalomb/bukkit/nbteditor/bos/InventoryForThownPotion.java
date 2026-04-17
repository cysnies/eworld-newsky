package com.goncalomb.bukkit.nbteditor.bos;

import com.goncalomb.bukkit.betterplugin.Lang;
import com.goncalomb.bukkit.nbteditor.nbt.ThrownPotionNBT;
import java.util.HashMap;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public final class InventoryForThownPotion extends InventoryForSingleItem {
   private static HashMap _placeholders = new HashMap();
   private BookOfSouls _bos;

   static {
      _placeholders.put(4, createPlaceholder(Material.GLASS_BOTTLE, Lang._("nbt.bos.potion.pholder")));
   }

   public InventoryForThownPotion(BookOfSouls bos, Player owner) {
      super(Lang._("nbt.bos.potion.title"), _placeholders, ((ThrownPotionNBT)bos.getEntityNBT()).getPotion(), bos, owner);
      this._bos = bos;
   }

   protected void inventoryClick(InventoryClickEvent event) {
      super.inventoryClick(event);
      ItemStack itemToCheck = this.getItemToCheck(event);
      if (itemToCheck != null && itemToCheck.getType() != Material.POTION) {
         ((Player)event.getWhoClicked()).sendMessage(Lang._("nbt.bos.mob.potion"));
         event.setCancelled(true);
      }

   }

   protected void inventoryClose(InventoryCloseEvent event) {
      ((ThrownPotionNBT)this._bos.getEntityNBT()).setPotion(this.getContents()[4]);
      this._bos.saveBook();
      ((Player)event.getPlayer()).sendMessage(Lang._("nbt.bos.potion.done"));
   }
}
