package com.sk89q.worldedit.patterns;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomFillPattern implements Pattern {
   private static final Random random = new Random();
   private List blocks;

   public RandomFillPattern(List blocks) {
      double max = (double)0.0F;

      for(BlockChance block : blocks) {
         max += block.getChance();
      }

      List<BlockChance> finalBlocks = new ArrayList();
      double i = (double)0.0F;

      for(BlockChance block : blocks) {
         double v = block.getChance() / max;
         i += v;
         finalBlocks.add(new BlockChance(block.getBlock(), i));
      }

      this.blocks = finalBlocks;
   }

   public BaseBlock next(Vector pos) {
      double r = random.nextDouble();

      for(BlockChance block : this.blocks) {
         if (r <= block.getChance()) {
            return block.getBlock();
         }
      }

      throw new RuntimeException("ProportionalFillPattern");
   }

   public BaseBlock next(int x, int y, int z) {
      return this.next((Vector)null);
   }
}
