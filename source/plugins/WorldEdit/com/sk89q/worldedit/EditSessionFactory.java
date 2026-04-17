package com.sk89q.worldedit;

import com.sk89q.worldedit.bags.BlockBag;

public class EditSessionFactory {
   public EditSession getEditSession(LocalWorld world, int maxBlocks) {
      return new EditSession(world, maxBlocks);
   }

   public EditSession getEditSession(LocalWorld world, int maxBlocks, LocalPlayer player) {
      return this.getEditSession(world, maxBlocks);
   }

   public EditSession getEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag) {
      return new EditSession(world, maxBlocks, blockBag);
   }

   public EditSession getEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag, LocalPlayer player) {
      return this.getEditSession(world, maxBlocks, blockBag);
   }
}
