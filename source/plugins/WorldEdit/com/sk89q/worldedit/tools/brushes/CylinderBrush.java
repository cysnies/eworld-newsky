package com.sk89q.worldedit.tools.brushes;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.patterns.Pattern;

public class CylinderBrush implements Brush {
   private int height;

   public CylinderBrush(int height) {
      this.height = height;
   }

   public void build(EditSession editSession, Vector pos, Pattern mat, double size) throws MaxChangedBlocksException {
      editSession.makeCylinder(pos, mat, size, size, this.height, true);
   }
}
