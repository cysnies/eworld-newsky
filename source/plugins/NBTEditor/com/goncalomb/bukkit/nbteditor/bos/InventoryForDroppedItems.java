package com.goncalomb.bukkit.nbteditor.bos;

import com.goncalomb.bukkit.betterplugin.Lang;
import com.goncalomb.bukkit.nbteditor.nbt.DroppedItemNBT;
import java.util.HashMap;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

public final class InventoryForDroppedItems extends InventoryForSingleItem {
   private static HashMap _placeholders = new HashMap();
   private BookOfSouls _bos;

   static {
      _placeholders.put(4, createPlaceholder(Material.PAPER, Lang._("nbt.bos.item.pholder")));
   }

   public InventoryForDroppedItems(BookOfSouls bos, Player owner) {
      super(Lang._("nbt.bos.item.title"), _placeholders, ((DroppedItemNBT)bos.getEntityNBT()).getItem(), bos, owner);
      this._bos = bos;
   }

   protected void inventoryClose(InventoryCloseEvent event) {
      ((DroppedItemNBT)this._bos.getEntityNBT()).setItem(this.getContents()[4]);
      this._bos.saveBook();
      ((Player)event.getPlayer()).sendMessage(Lang._("nbt.bos.item.done"));
   }
}
