package com.sk89q.worldedit.masks;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;

public interface Mask {
   void prepare(LocalSession var1, LocalPlayer var2, Vector var3);

   boolean matches(EditSession var1, Vector var2);
}
