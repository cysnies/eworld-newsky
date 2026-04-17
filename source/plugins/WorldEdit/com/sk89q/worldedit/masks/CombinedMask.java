package com.sk89q.worldedit.masks;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import java.util.ArrayList;
import java.util.List;

public class CombinedMask implements Mask {
   private List masks = new ArrayList();

   public CombinedMask() {
   }

   public CombinedMask(Mask mask) {
      this.masks.add(mask);
   }

   public CombinedMask(List masks) {
      this.masks.addAll(masks);
   }

   public void add(Mask mask) {
      this.masks.add(mask);
   }

   public boolean remove(Mask mask) {
      return this.masks.remove(mask);
   }

   public boolean has(Mask mask) {
      return this.masks.contains(mask);
   }

   public void prepare(LocalSession session, LocalPlayer player, Vector target) {
      for(Mask mask : this.masks) {
         mask.prepare(session, player, target);
      }

   }

   public boolean matches(EditSession editSession, Vector pos) {
      for(Mask mask : this.masks) {
         if (!mask.matches(editSession, pos)) {
            return false;
         }
      }

      return true;
   }
}
