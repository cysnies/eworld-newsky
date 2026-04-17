package com.sk89q.worldedit.bags;

import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BlockType;

public abstract class BlockBag {
   /** @deprecated */
   @Deprecated
   public void storeDroppedBlock(int id) throws BlockBagException {
      this.storeDroppedBlock(id, 0);
   }

   public void storeDroppedBlock(int id, int data) throws BlockBagException {
      BaseItem dropped = BlockType.getBlockBagItem(id, data);
      if (dropped != null) {
         if (dropped.getType() != 0) {
            this.storeItem(dropped);
         }
      }
   }

   /** @deprecated */
   @Deprecated
   public void fetchPlacedBlock(int id) throws BlockBagException {
      this.fetchPlacedBlock(id, 0);
   }

   public void fetchPlacedBlock(int id, int data) throws BlockBagException {
      try {
         switch (id) {
            case 7:
            case 14:
            case 15:
            case 16:
            case 46:
            case 52:
            case 56:
            case 59:
            case 73:
            case 74:
            case 78:
            case 89:
            case 90:
               throw new UnplaceableBlockException();
            case 8:
            case 9:
            case 10:
            case 11:
               return;
            default:
               this.fetchBlock(id);
         }
      } catch (OutOfBlocksException e) {
         BaseItem placed = BlockType.getBlockBagItem(id, data);
         if (placed == null) {
            throw e;
         }

         if (placed.getType() == 0) {
            throw e;
         }

         this.fetchItem(placed);
      }

   }

   public void fetchBlock(int id) throws BlockBagException {
      this.fetchItem(new BaseItem(id));
   }

   public void fetchItem(BaseItem item) throws BlockBagException {
      this.fetchBlock(item.getType());
   }

   public void storeBlock(int id) throws BlockBagException {
      this.storeItem(new BaseItem(id));
   }

   public void storeItem(BaseItem item) throws BlockBagException {
      this.storeBlock(item.getType());
   }

   public boolean peekBlock(int id) {
      try {
         this.fetchBlock(id);
         this.storeBlock(id);
         return true;
      } catch (BlockBagException var3) {
         return false;
      }
   }

   public abstract void flushChanges();

   public abstract void addSourcePosition(WorldVector var1);

   public abstract void addSingleSourcePosition(WorldVector var1);
}
