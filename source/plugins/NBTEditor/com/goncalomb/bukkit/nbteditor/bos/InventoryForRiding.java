package com.goncalomb.bukkit.nbteditor.bos;

import com.goncalomb.bukkit.betterplugin.Lang;
import com.goncalomb.bukkit.nbteditor.nbt.EntityNBT;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class InventoryForRiding extends IInventoryForBos {
   private static HashMap _placeholders = new HashMap();
   private BookOfSouls _bos;

   static {
      _placeholders.put(53, createPlaceholder(Material.PAPER, Lang._("nbt.bos.riding.pholder"), Lang._("nbt.bos.riding.pholder-lore")));
   }

   public InventoryForRiding(BookOfSouls bos, Player owner) {
      super(owner, 54, Lang._("nbt.bos.riding.title"), _placeholders, true);
      this._bos = bos;
      Inventory inv = this.getInventory();
      int i = 0;
      EntityNBT entityNBT = bos.getEntityNBT();

      while((entityNBT = entityNBT.getRiding()) != null) {
         EntityNBT riding = entityNBT.clone();
         riding.setRiding((EntityNBT[])null);
         inv.setItem(i++, (new BookOfSouls(riding)).getBook());
      }

   }

   protected void inventoryClick(InventoryClickEvent event) {
      super.inventoryClick(event);
      ItemStack item = event.getCurrentItem();
      if (item != null && item.getType() != Material.AIR) {
         Player player = (Player)event.getWhoClicked();
         if (item.equals(this._bos.getBook())) {
            event.setCancelled(true);
         } else if (!BookOfSouls.isValidBook(item)) {
            event.setCancelled(true);
            player.sendMessage(Lang._("nbt.bos.riding.only-bos"));
         } else {
            EntityNBT entityNbt = BookOfSouls.bookToEntityNBT(item);
            if (entityNbt == null) {
               player.sendMessage(Lang._("nbt.bos.corrupted"));
               event.setCancelled(true);
            } else if (entityNbt.getRiding() != null) {
               player.sendMessage(Lang._("nbt.bos.riding.has-riding"));
               event.setCancelled(true);
            }
         }
      }

   }

   protected void inventoryClose(InventoryCloseEvent event) {
      List<EntityNBT> rides = new ArrayList(54);
      ItemStack[] items = this.getContents();

      for(ItemStack item : items) {
         if (BookOfSouls.isValidBook(item)) {
            rides.add(BookOfSouls.bookToEntityNBT(item));
         }
      }

      this._bos.getEntityNBT().setRiding((EntityNBT[])rides.toArray(new EntityNBT[rides.size()]));
      this._bos.saveBook();
      ((Player)event.getPlayer()).sendMessage(Lang._("nbt.bos.riding.done"));
   }
}
