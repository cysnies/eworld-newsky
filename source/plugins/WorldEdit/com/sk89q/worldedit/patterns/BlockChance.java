package com.sk89q.worldedit.patterns;

import com.sk89q.worldedit.blocks.BaseBlock;

public class BlockChance {
   private BaseBlock block;
   private double chance;

   public BlockChance(BaseBlock block, double chance) {
      this.block = block;
      this.chance = chance;
   }

   public BaseBlock getBlock() {
      return this.block;
   }

   public double getChance() {
      return this.chance;
   }
}
