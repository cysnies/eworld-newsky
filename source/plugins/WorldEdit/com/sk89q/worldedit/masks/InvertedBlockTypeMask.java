package com.sk89q.worldedit.masks;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import java.util.Set;

/** @deprecated */
@Deprecated
public class InvertedBlockTypeMask extends BlockTypeMask {
   public InvertedBlockTypeMask() {
   }

   public InvertedBlockTypeMask(Set types) {
      super(types);
   }

   public InvertedBlockTypeMask(int type) {
      super(type);
   }

   public boolean matches(EditSession editSession, Vector pos) {
      return !super.matches(editSession, pos);
   }
}
