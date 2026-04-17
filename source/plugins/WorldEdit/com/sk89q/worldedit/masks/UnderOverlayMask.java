package com.sk89q.worldedit.masks;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import java.util.Set;

public class UnderOverlayMask implements Mask {
   private int yMod;
   private Mask mask;

   /** @deprecated */
   @Deprecated
   public UnderOverlayMask(Set ids, boolean overlay) {
      this((Mask)(new BlockTypeMask(ids)), overlay);
   }

   public UnderOverlayMask(Mask mask, boolean overlay) {
      this.yMod = overlay ? -1 : 1;
      this.mask = mask;
   }

   /** @deprecated */
   @Deprecated
   public void addAll(Set ids) {
      if (this.mask instanceof BlockTypeMask) {
         BlockTypeMask blockTypeMask = (BlockTypeMask)this.mask;

         for(Integer id : ids) {
            blockTypeMask.add(id);
         }
      } else if (this.mask instanceof ExistingBlockMask) {
         this.mask = new BlockTypeMask(ids);
      }

   }

   public void prepare(LocalSession session, LocalPlayer player, Vector target) {
      this.mask.prepare(session, player, target);
   }

   public boolean matches(EditSession editSession, Vector pos) {
      return this.mask.matches(editSession, pos.add(0, this.yMod, 0));
   }
}
