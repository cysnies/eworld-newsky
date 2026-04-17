package com.sk89q.worldedit.tools.brushes;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.patterns.Pattern;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GravityBrush implements Brush {
   private final boolean fullHeight;

   public GravityBrush(boolean fullHeight) {
      this.fullHeight = fullHeight;
   }

   public void build(EditSession editSession, Vector pos, Pattern mat, double size) throws MaxChangedBlocksException {
      BaseBlock air = new BaseBlock(0, 0);
      double startY = this.fullHeight ? (double)editSession.getWorld().getMaxY() : (double)pos.getBlockY() + size;

      for(double x = (double)pos.getBlockX() + size; x > (double)pos.getBlockX() - size; --x) {
         for(double z = (double)pos.getBlockZ() + size; z > (double)pos.getBlockZ() - size; --z) {
            double y = startY;

            List<BaseBlock> blockTypes;
            for(blockTypes = new ArrayList(); y > (double)pos.getBlockY() - size; --y) {
               Vector pt = new Vector(x, y, z);
               BaseBlock block = editSession.getBlock(pt);
               if (!block.isAir()) {
                  blockTypes.add(block);
                  editSession.setBlock(pt, air);
               }
            }

            Vector pt = new Vector(x, y, z);
            Collections.reverse(blockTypes);

            for(int i = 0; i < blockTypes.size(); pt = pt.add(0, 1, 0)) {
               if (editSession.getBlock(pt).getType() == 0) {
                  editSession.setBlock(pt, (BaseBlock)blockTypes.get(i++));
               }
            }
         }
      }

   }
}
