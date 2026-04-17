package com.sk89q.worldedit.foundation;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.blocks.TileEntityBlock;
import com.sk89q.worldedit.data.DataException;

public class Block implements TileEntityBlock {
   public static final int MAX_ID = 4095;
   public static final int MAX_DATA = 15;
   private short id;
   private short data;
   private CompoundTag nbtData;

   public Block(int id) {
      this.setId(id);
      this.setData(0);
   }

   public Block(int id, int data) {
      this.setId(id);
      this.setData(data);
   }

   public Block(int id, int data, CompoundTag nbtData) throws DataException {
      this.setId(id);
      this.setData(data);
      this.setNbtData(nbtData);
   }

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      if (id > 4095) {
         throw new IllegalArgumentException("Can't have a block ID above 4095 (" + id + " given)");
      } else if (id < 0) {
         throw new IllegalArgumentException("Can't have a block ID below 0");
      } else {
         this.id = (short)id;
      }
   }

   public int getData() {
      return this.data;
   }

   public void setData(int data) {
      if (data > 15) {
         throw new IllegalArgumentException("Can't have a block data value above 15 (" + data + " given)");
      } else if (data < -1) {
         throw new IllegalArgumentException("Can't have a block data value below -1");
      } else {
         this.data = (short)data;
      }
   }

   public void setIdAndData(int id, int data) {
      this.setId(id);
      this.setData(data);
   }

   public boolean hasWildcardData() {
      return this.getData() == -1;
   }

   public boolean hasNbtData() {
      return this.getNbtData() != null;
   }

   public String getNbtId() {
      CompoundTag nbtData = this.getNbtData();
      if (nbtData == null) {
         return "";
      } else {
         Tag idTag = (Tag)nbtData.getValue().get("id");
         return idTag != null && idTag instanceof StringTag ? ((StringTag)idTag).getValue() : "";
      }
   }

   public CompoundTag getNbtData() {
      return this.nbtData;
   }

   public void setNbtData(CompoundTag nbtData) throws DataException {
      this.nbtData = nbtData;
   }

   public int hashCode() {
      int ret = this.getId() << 3;
      if (this.getData() != -1) {
         ret |= this.getData();
      }

      return ret;
   }

   public String toString() {
      return "Block{ID:" + this.getId() + ", Data: " + this.getData() + "}";
   }
}
