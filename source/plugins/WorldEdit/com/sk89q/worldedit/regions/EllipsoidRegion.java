package com.sk89q.worldedit.regions;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import java.util.HashSet;
import java.util.Set;

public class EllipsoidRegion extends AbstractRegion {
   private Vector center;
   private Vector radius;

   public EllipsoidRegion(Vector pos1, Vector pos2) {
      this((LocalWorld)null, pos1, pos2);
   }

   public EllipsoidRegion(LocalWorld world, Vector center, Vector radius) {
      super(world);
      this.center = center;
      this.setRadius(radius);
   }

   public EllipsoidRegion(EllipsoidRegion ellipsoidRegion) {
      this(ellipsoidRegion.world, ellipsoidRegion.center, ellipsoidRegion.getRadius());
   }

   public Vector getMinimumPoint() {
      return this.center.subtract(this.getRadius());
   }

   public Vector getMaximumPoint() {
      return this.center.add(this.getRadius());
   }

   public int getArea() {
      return (int)Math.floor(4.1887902047863905 * this.radius.getX() * this.radius.getY() * this.radius.getZ());
   }

   public int getWidth() {
      return (int)((double)2.0F * this.radius.getX());
   }

   public int getHeight() {
      return (int)((double)2.0F * this.radius.getY());
   }

   public int getLength() {
      return (int)((double)2.0F * this.radius.getZ());
   }

   private Vector calculateDiff(Vector... changes) throws RegionOperationException {
      Vector diff = (new Vector()).add(changes);
      if ((diff.getBlockX() & 1) + (diff.getBlockY() & 1) + (diff.getBlockZ() & 1) != 0) {
         throw new RegionOperationException("Ellipsoid changes must be even for each dimensions.");
      } else {
         return diff.divide(2).floor();
      }
   }

   private Vector calculateChanges(Vector... changes) {
      Vector total = new Vector();

      for(Vector change : changes) {
         total = total.add(change.positive());
      }

      return total.divide(2).floor();
   }

   public void expand(Vector... changes) throws RegionOperationException {
      this.center = this.center.add(this.calculateDiff(changes));
      this.radius = this.radius.add(this.calculateChanges(changes));
   }

   public void contract(Vector... changes) throws RegionOperationException {
      this.center = this.center.subtract(this.calculateDiff(changes));
      Vector newRadius = this.radius.subtract(this.calculateChanges(changes));
      this.radius = Vector.getMaximum(new Vector((double)1.5F, (double)1.5F, (double)1.5F), newRadius);
   }

   public void shift(Vector change) throws RegionOperationException {
      this.center = this.center.add(change);
   }

   public Vector getCenter() {
      return this.center;
   }

   public void setCenter(Vector center) {
      this.center = center;
   }

   public Vector getRadius() {
      return this.radius.subtract((double)0.5F, (double)0.5F, (double)0.5F);
   }

   public void setRadius(Vector radius) {
      this.radius = radius.add((double)0.5F, (double)0.5F, (double)0.5F);
   }

   public Set getChunks() {
      Set<Vector2D> chunks = new HashSet();
      Vector min = this.getMinimumPoint();
      Vector max = this.getMaximumPoint();
      int centerY = this.getCenter().getBlockY();

      for(int x = min.getBlockX(); x <= max.getBlockX(); ++x) {
         for(int z = min.getBlockZ(); z <= max.getBlockZ(); ++z) {
            if (this.contains(new BlockVector(x, centerY, z))) {
               chunks.add(new BlockVector2D(x >> 4, z >> 4));
            }
         }
      }

      return chunks;
   }

   public boolean contains(Vector pt) {
      return pt.subtract(this.center).divide(this.radius).lengthSq() <= (double)1.0F;
   }

   public String toString() {
      return this.center + " - " + this.getRadius();
   }

   public void extendRadius(Vector minRadius) {
      this.setRadius(Vector.getMaximum(minRadius, this.getRadius()));
   }

   public EllipsoidRegion clone() {
      return (EllipsoidRegion)super.clone();
   }
}
