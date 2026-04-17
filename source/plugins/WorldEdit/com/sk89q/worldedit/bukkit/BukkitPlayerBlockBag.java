package com.sk89q.worldedit.bukkit;

import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.bags.BlockBagException;
import com.sk89q.worldedit.bags.OutOfBlocksException;
import com.sk89q.worldedit.bags.OutOfSpaceException;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.blocks.ItemType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BukkitPlayerBlockBag extends BlockBag {
   private Player player;
   private ItemStack[] items;

   public BukkitPlayerBlockBag(Player player) {
      this.player = player;
   }

   private void loadInventory() {
      if (this.items == null) {
         this.items = this.player.getInventory().getContents();
      }

   }

   public Player getPlayer() {
      return this.player;
   }

   public void fetchItem(BaseItem item) throws BlockBagException {
      int id = item.getType();
      int damage = item.getData();
      int amount = item instanceof BaseItemStack ? ((BaseItemStack)item).getAmount() : 1;

      assert amount == 1;

      boolean usesDamageValue = ItemType.usesDamageValue(id);
      if (id == 0) {
         throw new IllegalArgumentException("Can't fetch air block");
      } else {
         this.loadInventory();
         boolean found = false;

         for(int slot = 0; slot < this.items.length; ++slot) {
            ItemStack bukkitItem = this.items[slot];
            if (bukkitItem != null && bukkitItem.getTypeId() == id && (!usesDamageValue || bukkitItem.getDurability() == damage)) {
               int currentAmount = bukkitItem.getAmount();
               if (currentAmount < 0) {
                  return;
               }

               if (currentAmount > 1) {
                  bukkitItem.setAmount(currentAmount - 1);
                  found = true;
               } else {
                  this.items[slot] = null;
                  found = true;
               }
               break;
            }
         }

         if (!found) {
            throw new OutOfBlocksException();
         }
      }
   }

   public void storeItem(BaseItem item) throws BlockBagException {
      int id = item.getType();
      int damage = item.getData();
      int amount = item instanceof BaseItemStack ? ((BaseItemStack)item).getAmount() : 1;

      assert amount <= 64;

      boolean usesDamageValue = ItemType.usesDamageValue(id);
      if (id == 0) {
         throw new IllegalArgumentException("Can't store air block");
      } else {
         this.loadInventory();
         int freeSlot = -1;

         for(int slot = 0; slot < this.items.length; ++slot) {
            ItemStack bukkitItem = this.items[slot];
            if (bukkitItem == null) {
               if (freeSlot == -1) {
                  freeSlot = slot;
               }
            } else if (bukkitItem.getTypeId() == id && (!usesDamageValue || bukkitItem.getDurability() == damage)) {
               int currentAmount = bukkitItem.getAmount();
               if (currentAmount < 0) {
                  return;
               }

               if (currentAmount < 64) {
                  int spaceLeft = 64 - currentAmount;
                  if (spaceLeft >= amount) {
                     bukkitItem.setAmount(currentAmount + amount);
                     return;
                  }

                  bukkitItem.setAmount(64);
                  amount -= spaceLeft;
               }
            }
         }

         if (freeSlot > -1) {
            this.items[freeSlot] = new ItemStack(id, amount);
         } else {
            throw new OutOfSpaceException(id);
         }
      }
   }

   public void flushChanges() {
      if (this.items != null) {
         this.player.getInventory().setContents(this.items);
         this.items = null;
      }

   }

   public void addSourcePosition(WorldVector pos) {
   }

   public void addSingleSourcePosition(WorldVector pos) {
   }
}
