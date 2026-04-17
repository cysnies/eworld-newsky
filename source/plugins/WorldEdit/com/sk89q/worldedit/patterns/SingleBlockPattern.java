package com.sk89q.worldedit.patterns;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;

public class SingleBlockPattern implements Pattern {
   private BaseBlock block;

   public SingleBlockPattern(BaseBlock block) {
      this.block = block;
   }

   public BaseBlock next(Vector pos) {
      return this.block;
   }

   public BaseBlock next(int x, int y, int z) {
      return this.block;
   }

   public BaseBlock getBlock() {
      return this.block;
   }
}
