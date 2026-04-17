package com.sk89q.worldedit.patterns;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;

public interface Pattern {
   BaseBlock next(Vector var1);

   BaseBlock next(int var1, int var2, int var3);
}
