package com.goncalomb.bukkit.nbteditor.bos;

import com.goncalomb.bukkit.betterplugin.Lang;
import com.goncalomb.bukkit.nbteditor.nbt.FireworkNBT;
import java.util.HashMap;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public final class InventoryForFirework extends InventoryForSingleItem {
   private static HashMap _placeholders = new HashMap();
   private BookOfSouls _bos;

   static {
      _placeholders.put(4, createPlaceholder(Material.PAPER, Lang._("nbt.bos.firework.pholder")));
   }

   public InventoryForFirework(BookOfSouls bos, Player owner) {
      super(Lang._("nbt.bos.firework.title"), _placeholders, ((FireworkNBT)bos.getEntityNBT()).getFirework(), bos, owner);
      this._bos = bos;
   }

   protected void inventoryClick(InventoryClickEvent event) {
      super.inventoryClick(event);
      ItemStack itemToCheck = this.getItemToCheck(event);
      if (itemToCheck != null && itemToCheck.getType() != Material.FIREWORK) {
         ((Player)event.getWhoClicked()).sendMessage(Lang._("nbt.bos.firework.nop"));
         event.setCancelled(true);
      }

   }

   protected void inventoryClose(InventoryCloseEvent event) {
      ((FireworkNBT)this._bos.getEntityNBT()).setFirework(this.getContents()[4]);
      this._bos.saveBook();
      ((Player)event.getPlayer()).sendMessage(Lang._("nbt.bos.firework.done"));
   }
}
