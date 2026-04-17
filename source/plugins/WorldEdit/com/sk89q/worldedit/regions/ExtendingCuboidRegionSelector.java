package com.sk89q.worldedit.regions;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;

public class ExtendingCuboidRegionSelector extends CuboidRegionSelector {
   public ExtendingCuboidRegionSelector(LocalWorld world) {
      super(world);
   }

   public ExtendingCuboidRegionSelector(RegionSelector oldSelector) {
      super(oldSelector);
      if (this.pos1 != null && this.pos2 != null) {
         this.pos1 = this.region.getMinimumPoint().toBlockVector();
         this.pos2 = this.region.getMaximumPoint().toBlockVector();
         this.region.setPos1(this.pos1);
         this.region.setPos2(this.pos2);
      }
   }

   public ExtendingCuboidRegionSelector(LocalWorld world, Vector pos1, Vector pos2) {
      this(world);
      pos1 = Vector.getMinimum(pos1, pos2);
      pos2 = Vector.getMaximum(pos1, pos2);
      this.region.setPos1(pos1);
      this.region.setPos2(pos2);
   }

   public boolean selectPrimary(Vector pos) {
      if (this.pos1 != null && this.pos2 != null && pos.compareTo((Vector)this.pos1) == 0 && pos.compareTo((Vector)this.pos2) == 0) {
         return false;
      } else {
         this.pos1 = this.pos2 = pos.toBlockVector();
         this.region.setPos1(this.pos1);
         this.region.setPos2(this.pos2);
         return true;
      }
   }

   public boolean selectSecondary(Vector pos) {
      if (this.pos1 != null && this.pos2 != null) {
         if (this.region.contains(pos)) {
            return false;
         } else {
            double x1 = Math.min(pos.getX(), this.pos1.getX());
            double y1 = Math.min(pos.getY(), this.pos1.getY());
            double z1 = Math.min(pos.getZ(), this.pos1.getZ());
            double x2 = Math.max(pos.getX(), this.pos2.getX());
            double y2 = Math.max(pos.getY(), this.pos2.getY());
            double z2 = Math.max(pos.getZ(), this.pos2.getZ());
            BlockVector o1 = this.pos1;
            BlockVector o2 = this.pos2;
            this.pos1 = new BlockVector(x1, y1, z1);
            this.pos2 = new BlockVector(x2, y2, z2);
            this.region.setPos1(this.pos1);
            this.region.setPos2(this.pos2);

            assert this.region.contains(o1);

            assert this.region.contains(o2);

            assert this.region.contains(pos);

            return true;
         }
      } else {
         return this.selectPrimary(pos);
      }
   }

   public void explainPrimarySelection(LocalPlayer player, LocalSession session, Vector pos) {
      player.print("Started selection at " + pos + " (" + this.region.getArea() + ").");
      this.explainRegionAdjust(player, session);
   }

   public void explainSecondarySelection(LocalPlayer player, LocalSession session, Vector pos) {
      player.print("Extended selection to encompass " + pos + " (" + this.region.getArea() + ").");
      this.explainRegionAdjust(player, session);
   }
}
