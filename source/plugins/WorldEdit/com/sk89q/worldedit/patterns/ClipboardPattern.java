package com.sk89q.worldedit.patterns;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;

public class ClipboardPattern implements Pattern {
   private CuboidClipboard clipboard;
   private Vector size;

   public ClipboardPattern(CuboidClipboard clipboard) {
      this.clipboard = clipboard;
      this.size = clipboard.getSize();
   }

   public BaseBlock next(Vector pos) {
      return this.next(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
   }

   public BaseBlock next(int x, int y, int z) {
      int xp = Math.abs(x) % this.size.getBlockX();
      int yp = Math.abs(y) % this.size.getBlockY();
      int zp = Math.abs(z) % this.size.getBlockZ();
      return this.clipboard.getPoint(new Vector(xp, yp, zp));
   }
}
