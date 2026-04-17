package com.sk89q.worldedit;

public class MaxChangedBlocksException extends WorldEditException {
   private static final long serialVersionUID = -2621044030640945259L;
   int maxBlocks;

   public MaxChangedBlocksException(int maxBlocks) {
      this.maxBlocks = maxBlocks;
   }

   public int getBlockLimit() {
      return this.maxBlocks;
   }
}
