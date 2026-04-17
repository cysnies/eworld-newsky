package com.sk89q.worldedit.masks;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.Region;

public class DynamicRegionMask implements Mask {
   private Region region;

   public void prepare(LocalSession session, LocalPlayer player, Vector target) {
      try {
         this.region = session.getSelection(player.getWorld());
      } catch (IncompleteRegionException var5) {
         this.region = null;
      }

   }

   public boolean matches(EditSession editSession, Vector pos) {
      return this.region == null || this.region.contains(pos);
   }
}
