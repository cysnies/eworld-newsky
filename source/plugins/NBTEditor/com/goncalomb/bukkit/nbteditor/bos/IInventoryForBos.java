package com.goncalomb.bukkit.nbteditor.bos;

import com.goncalomb.bukkit.CustomInventory;
import com.goncalomb.bukkit.UtilsMc;
import com.goncalomb.bukkit.betterplugin.Lang;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

abstract class IInventoryForBos extends CustomInventory {
   protected static final ItemStack _itemFiller;
   private HashMap _placeholders;
   private boolean _allowBos;

   static {
      _itemFiller = UtilsMc.newItem(Material.TRIPWIRE, Lang._("nbt.bos.inv.nothing"));
   }

   protected static final ItemStack createPlaceholder(Material material, String name) {
      return UtilsMc.newItem(material, name, Lang._("nbt.bos.inv.pholder"));
   }

   protected static final ItemStack createPlaceholder(Material material, String name, String lore) {
      return UtilsMc.newItem(material, name, lore, Lang._("nbt.bos.inv.pholder"));
   }

   public IInventoryForBos(Player owner, int size, String title, HashMap placeholders) {
      this(owner, size, title, placeholders, false);
   }

   public IInventoryForBos(Player owner, int size, String title, HashMap placeholders, boolean allowBos) {
      super(owner, size, title);
      this._placeholders = placeholders;
      this._allowBos = allowBos;

      for(Map.Entry entry : this._placeholders.entrySet()) {
         this._inventory.setItem((Integer)entry.getKey(), (ItemStack)entry.getValue());
      }

   }

   private boolean isPlaceholder(int slot) {
      ItemStack item = this._inventory.getItem(slot);
      return item != null && item.equals(this._placeholders.get(slot));
   }

   protected final ItemStack[] getContents() {
      ItemStack[] items = this._inventory.getContents();

      for(Map.Entry entry : this._placeholders.entrySet()) {
         ItemStack item = items[(Integer)entry.getKey()];
         if (item != null && item.equals(entry.getValue())) {
            items[(Integer)entry.getKey()] = null;
         }
      }

      return items;
   }

   protected void inventoryClick(InventoryClickEvent event) {
      int slot = event.getRawSlot();
      if (slot > 0 && slot < this.getInventory().getSize() && this.isPlaceholder(slot)) {
         event.setCurrentItem(new ItemStack(Material.AIR));
      }

      if (!this._allowBos && BookOfSouls.isValidBook(event.getCurrentItem())) {
         event.setCancelled(true);
      }

   }
}
