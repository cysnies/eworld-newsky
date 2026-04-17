package com.sk89q.worldedit.foundation;

import com.sk89q.worldedit.Vector;

public interface World {
   boolean setBlock(Vector var1, Block var2, boolean var3);

   Block getBlock(Vector var1);
}
