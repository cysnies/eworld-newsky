package com.sk89q.worldedit.masks;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.Region;

public class RegionMask implements Mask {
   private Region region;

   public RegionMask(Region region) {
      this.region = region.clone();
   }

   public void prepare(LocalSession session, LocalPlayer player, Vector target) {
   }

   public boolean matches(EditSession editSession, Vector pos) {
      return this.region.contains(pos);
   }
}
