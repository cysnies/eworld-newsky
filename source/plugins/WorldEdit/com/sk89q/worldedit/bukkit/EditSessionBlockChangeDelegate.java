package com.sk89q.worldedit.bukkit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import org.bukkit.BlockChangeDelegate;

public class EditSessionBlockChangeDelegate implements BlockChangeDelegate {
   private EditSession editSession;

   public EditSessionBlockChangeDelegate(EditSession editSession) {
      this.editSession = editSession;
   }

   public boolean setRawTypeId(int x, int y, int z, int typeId) {
      try {
         return this.editSession.setBlock(new Vector(x, y, z), new BaseBlock(typeId));
      } catch (MaxChangedBlocksException var6) {
         return false;
      }
   }

   public boolean setRawTypeIdAndData(int x, int y, int z, int typeId, int data) {
      try {
         return this.editSession.setBlock(new Vector(x, y, z), new BaseBlock(typeId, data));
      } catch (MaxChangedBlocksException var7) {
         return false;
      }
   }

   public boolean setTypeId(int x, int y, int z, int typeId) {
      return this.setRawTypeId(x, y, z, typeId);
   }

   public boolean setTypeIdAndData(int x, int y, int z, int typeId, int data) {
      return this.setRawTypeIdAndData(x, y, z, typeId, data);
   }

   public int getTypeId(int x, int y, int z) {
      return this.editSession.getBlockType(new Vector(x, y, z));
   }

   public int getHeight() {
      return this.editSession.getWorld().getMaxY() + 1;
   }

   public boolean isEmpty(int x, int y, int z) {
      return this.editSession.getBlockType(new Vector(x, y, z)) == 0;
   }
}
