package com.sk89q.worldedit.tools.brushes;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.patterns.Pattern;

public class SphereBrush implements Brush {
   public void build(EditSession editSession, Vector pos, Pattern mat, double size) throws MaxChangedBlocksException {
      editSession.makeSphere(pos, mat, size, size, size, true);
   }
}
