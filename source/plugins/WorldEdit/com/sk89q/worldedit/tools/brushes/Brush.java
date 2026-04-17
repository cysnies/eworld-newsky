package com.sk89q.worldedit.tools.brushes;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.patterns.Pattern;

public interface Brush {
   void build(EditSession var1, Vector var2, Pattern var3, double var4) throws MaxChangedBlocksException;
}
