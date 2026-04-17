package com.sk89q.worldedit.masks;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;

public class ExistingBlockMask implements Mask {
   public void prepare(LocalSession session, LocalPlayer player, Vector target) {
   }

   public boolean matches(EditSession editSession, Vector pos) {
      return editSession.getBlockType(pos) != 0;
   }
}
