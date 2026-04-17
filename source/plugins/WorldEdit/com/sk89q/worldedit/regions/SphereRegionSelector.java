package com.sk89q.worldedit.regions;

import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;

public class SphereRegionSelector extends EllipsoidRegionSelector {
   public SphereRegionSelector(LocalWorld world) {
      super(world);
   }

   public SphereRegionSelector() {
   }

   public SphereRegionSelector(RegionSelector oldSelector) {
      super(oldSelector);
      Vector radius = this.region.getRadius();
      double radiusScalar = Math.max(Math.max(radius.getX(), radius.getY()), radius.getZ());
      this.region.setRadius(new Vector(radiusScalar, radiusScalar, radiusScalar));
   }

   public SphereRegionSelector(LocalWorld world, Vector center, int radius) {
      super(world, center, new Vector(radius, radius, radius));
   }

   public boolean selectSecondary(Vector pos) {
      double radiusScalar = Math.ceil(pos.distance(this.region.getCenter()));
      this.region.setRadius(new Vector(radiusScalar, radiusScalar, radiusScalar));
      return true;
   }

   public void explainSecondarySelection(LocalPlayer player, LocalSession session, Vector pos) {
      if (this.isDefined()) {
         player.print("Radius set to " + this.region.getRadius().getX() + " (" + this.region.getArea() + ").");
      } else {
         player.print("Radius set to " + this.region.getRadius().getX() + ".");
      }

      session.describeCUI(player);
   }

   public String getTypeName() {
      return "sphere";
   }
}
