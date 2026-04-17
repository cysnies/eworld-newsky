package com.sk89q.worldedit.tools.brushes;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.patterns.Pattern;

public class ClipboardBrush implements Brush {
   private CuboidClipboard clipboard;
   private boolean noAir;

   public ClipboardBrush(CuboidClipboard clipboard, boolean noAir) {
      this.clipboard = clipboard;
      this.noAir = noAir;
   }

   public void build(EditSession editSession, Vector pos, Pattern mat, double size) throws MaxChangedBlocksException {
      this.clipboard.place(editSession, pos.subtract(this.clipboard.getSize().divide(2)), this.noAir);
   }
}
