package com.sk89q.worldedit.tools.brushes;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.patterns.Pattern;

public class ButcherBrush implements Brush {
   private int flags;

   public ButcherBrush(int flags) {
      this.flags = flags;
   }

   public void build(EditSession editSession, Vector pos, Pattern mat, double size) throws MaxChangedBlocksException {
      editSession.getWorld().killMobs(pos, size, this.flags);
   }
}
