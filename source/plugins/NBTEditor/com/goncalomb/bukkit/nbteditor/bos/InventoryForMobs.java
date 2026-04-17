package com.goncalomb.bukkit.nbteditor.bos;

import com.goncalomb.bukkit.betterplugin.Lang;
import com.goncalomb.bukkit.nbteditor.nbt.MobNBT;
import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class InventoryForMobs extends IInventoryForBos {
   private static HashMap _placeholders = new HashMap();
   private BookOfSouls _bos;

   static {
      _placeholders.put(0, createPlaceholder(Material.PAPER, Lang._("nbt.bos.mob.pholder.head")));
      _placeholders.put(1, createPlaceholder(Material.PAPER, Lang._("nbt.bos.mob.pholder.chest")));
      _placeholders.put(2, createPlaceholder(Material.PAPER, Lang._("nbt.bos.mob.pholder.legs")));
      _placeholders.put(3, createPlaceholder(Material.PAPER, Lang._("nbt.bos.mob.pholder.feet")));
      _placeholders.put(4, createPlaceholder(Material.PAPER, Lang._("nbt.bos.mob.pholder.hand")));
      _placeholders.put(8, createPlaceholder(Material.GLASS_BOTTLE, Lang._("nbt.bos.mob.pholder.effects"), Lang._("nbt.bos.mob.pholder.effects-lore")));
   }

   public InventoryForMobs(BookOfSouls bos, Player owner) {
      super(owner, 9, Lang._("nbt.bos.mob.title") + " - " + ChatColor.BLACK + bos.getEntityNBT().getEntityType().getName(), _placeholders);
      this._bos = bos;
      Inventory inv = this.getInventory();
      MobNBT mob = (MobNBT)this._bos.getEntityNBT();
      ItemStack[] equip = mob.getEquipment();

      for(int i = 0; i < 5; ++i) {
         if (equip[i] != null && equip[i].getType() != Material.AIR) {
            inv.setItem(4 - i, equip[i]);
         }
      }

      inv.setItem(5, _itemFiller);
      inv.setItem(6, _itemFiller);
      inv.setItem(7, _itemFiller);
      ItemStack potion = mob.getEffectsAsPotion();
      if (potion != null) {
         inv.setItem(8, potion);
      }

   }

   protected void inventoryClick(InventoryClickEvent event) {
      super.inventoryClick(event);
      if (event.getRawSlot() > 4 && event.getRawSlot() < 8) {
         event.setCancelled(true);
      }

      int slot = event.getRawSlot();
      boolean isShift = event.isShiftClick();
      ItemStack itemToCheck = null;
      if (isShift && slot > 8 && event.getInventory().firstEmpty() == 8) {
         itemToCheck = event.getCurrentItem();
      } else if (slot == 8 && !isShift && event.getCursor().getType() != Material.AIR) {
         itemToCheck = event.getCursor();
      }

      if (itemToCheck != null && itemToCheck.getType() != Material.POTION) {
         ((Player)event.getWhoClicked()).sendMessage(Lang._("nbt.bos.mob.potion"));
         event.setCancelled(true);
      }

   }

   protected void inventoryClose(InventoryCloseEvent event) {
      MobNBT mob = (MobNBT)this._bos.getEntityNBT();
      ItemStack[] items = this.getContents();
      mob.setEquipment(items[4], items[3], items[2], items[1], items[0]);
      mob.setEffectsFromPotion(items[8]);
      this._bos.saveBook();
      ((Player)event.getPlayer()).sendMessage(Lang._("nbt.bos.mob.done"));
   }
}
