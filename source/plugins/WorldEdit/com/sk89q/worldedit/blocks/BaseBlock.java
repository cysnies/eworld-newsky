package com.sk89q.worldedit.blocks;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.foundation.Block;

public class BaseBlock extends Block {
   public BaseBlock(int type) {
      this(type, 0);
   }

   public BaseBlock(int type, int data) {
      super(type, data);
   }

   public int getType() {
      return this.getId();
   }

   public void setType(int type) {
      this.setId(type);
   }

   public boolean isAir() {
      return this.getType() == 0;
   }

   public int rotate90() {
      int newData = BlockData.rotate90(this.getType(), this.getData());
      this.setData(newData);
      return newData;
   }

   public int rotate90Reverse() {
      int newData = BlockData.rotate90Reverse(this.getType(), this.getData());
      this.setData((short)newData);
      return newData;
   }

   public int cycleData(int increment) {
      int newData = BlockData.cycle(this.getType(), this.getData(), increment);
      this.setData((short)newData);
      return newData;
   }

   public BaseBlock flip() {
      this.setData((short)BlockData.flip(this.getType(), this.getData()));
      return this;
   }

   public BaseBlock flip(CuboidClipboard.FlipDirection direction) {
      this.setData((short)BlockData.flip(this.getType(), this.getData(), direction));
      return this;
   }

   public boolean equals(Object o) {
      if (!(o instanceof BaseBlock)) {
         return false;
      } else {
         return this.getType() == ((BaseBlock)o).getType() && this.getData() == ((BaseBlock)o).getData();
      }
   }

   public boolean equalsFuzzy(BaseBlock o) {
      return this.getType() == o.getType() && (this.getData() == o.getData() || this.getData() == -1 || o.getData() == -1);
   }

   /** @deprecated */
   @Deprecated
   public boolean inIterable(Iterable iter) {
      for(BaseBlock block : iter) {
         if (block.equalsFuzzy(this)) {
            return true;
         }
      }

      return false;
   }
}
