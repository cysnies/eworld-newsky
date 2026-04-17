package com.sk89q.worldedit.regions;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CylinderRegion extends AbstractRegion implements FlatRegion {
   private Vector2D center;
   private Vector2D radius;
   private int minY;
   private int maxY;
   private boolean hasY;

   public CylinderRegion() {
      this((LocalWorld)null);
   }

   public CylinderRegion(LocalWorld world) {
      this(world, new Vector(), new Vector2D(), 0, 0);
      this.hasY = false;
   }

   public CylinderRegion(LocalWorld world, Vector center, Vector2D radius, int minY, int maxY) {
      super(world);
      this.hasY = false;
      this.setCenter(center.toVector2D());
      this.setRadius(radius);
      this.minY = minY;
      this.maxY = maxY;
      this.hasY = true;
   }

   public CylinderRegion(CylinderRegion region) {
      this(region.world, region.getCenter(), region.getRadius(), region.minY, region.maxY);
      this.hasY = region.hasY;
   }

   public Vector getCenter() {
      return this.center.toVector((double)((this.maxY + this.minY) / 2));
   }

   /** @deprecated */
   @Deprecated
   public void setCenter(Vector center) {
      this.setCenter(center.toVector2D());
   }

   public void setCenter(Vector2D center) {
      this.center = center;
   }

   public Vector2D getRadius() {
      return this.radius.subtract((double)0.5F, (double)0.5F);
   }

   public void setRadius(Vector2D radius) {
      this.radius = radius.add((double)0.5F, (double)0.5F);
   }

   public void extendRadius(Vector2D minRadius) {
      this.setRadius(Vector2D.getMaximum(minRadius, this.getRadius()));
   }

   public void setMinimumY(int y) {
      this.hasY = true;
      this.minY = y;
   }

   public void setMaximumY(int y) {
      this.hasY = true;
      this.maxY = y;
   }

   public Vector getMinimumPoint() {
      return this.center.subtract(this.getRadius()).toVector((double)this.minY);
   }

   public Vector getMaximumPoint() {
      return this.center.add(this.getRadius()).toVector((double)this.maxY);
   }

   public int getMaximumY() {
      return this.maxY;
   }

   public int getMinimumY() {
      return this.minY;
   }

   public int getArea() {
      return (int)Math.floor(this.radius.getX() * this.radius.getZ() * Math.PI * (double)this.getHeight());
   }

   public int getWidth() {
      return (int)((double)2.0F * this.radius.getX());
   }

   public int getHeight() {
      return this.maxY - this.minY + 1;
   }

   public int getLength() {
      return (int)((double)2.0F * this.radius.getZ());
   }

   private Vector2D calculateDiff2D(Vector... changes) throws RegionOperationException {
      Vector2D diff = new Vector2D();

      for(Vector change : changes) {
         diff = diff.add(change.toVector2D());
      }

      if ((diff.getBlockX() & 1) + (diff.getBlockZ() & 1) != 0) {
         throw new RegionOperationException("Cylinders changes must be even for each horizontal dimensions.");
      } else {
         return diff.divide(2).floor();
      }
   }

   private Vector2D calculateChanges2D(Vector... changes) {
      Vector2D total = new Vector2D();

      for(Vector change : changes) {
         total = total.add(change.toVector2D().positive());
      }

      return total.divide(2).floor();
   }

   public void expand(Vector... changes) throws RegionOperationException {
      this.center = this.center.add(this.calculateDiff2D(changes));
      this.radius = this.radius.add(this.calculateChanges2D(changes));

      for(Vector change : changes) {
         int changeY = change.getBlockY();
         if (changeY > 0) {
            this.maxY += changeY;
         } else {
            this.minY += changeY;
         }
      }

   }

   public void contract(Vector... changes) throws RegionOperationException {
      this.center = this.center.subtract(this.calculateDiff2D(changes));
      Vector2D newRadius = this.radius.subtract(this.calculateChanges2D(changes));
      this.radius = Vector2D.getMaximum(new Vector2D((double)1.5F, (double)1.5F), newRadius);

      for(Vector change : changes) {
         int height = this.maxY - this.minY;
         int changeY = change.getBlockY();
         if (changeY > 0) {
            this.minY += Math.min(height, changeY);
         } else {
            this.maxY += Math.max(-height, changeY);
         }
      }

   }

   public void shift(Vector change) throws RegionOperationException {
      this.center = this.center.add(change.toVector2D());
      int changeY = change.getBlockY();
      this.maxY += changeY;
      this.minY += changeY;
   }

   public boolean contains(Vector pt) {
      int blockY = pt.getBlockY();
      if (blockY >= this.minY && blockY <= this.maxY) {
         return pt.toVector2D().subtract(this.center).divide(this.radius).lengthSq() <= (double)1.0F;
      } else {
         return false;
      }
   }

   public boolean setY(int y) {
      if (!this.hasY) {
         this.minY = y;
         this.maxY = y;
         this.hasY = true;
         return true;
      } else if (y < this.minY) {
         this.minY = y;
         return true;
      } else if (y > this.maxY) {
         this.maxY = y;
         return true;
      } else {
         return false;
      }
   }

   public Iterator iterator() {
      return new FlatRegion3DIterator(this);
   }

   public Iterable asFlatRegion() {
      return new Iterable() {
         public Iterator iterator() {
            return new FlatRegionIterator(CylinderRegion.this);
         }
      };
   }

   public String toString() {
      return this.center + " - " + this.radius + "(" + this.minY + ", " + this.maxY + ")";
   }

   public CylinderRegion clone() {
      return (CylinderRegion)super.clone();
   }

   public List polygonize(int maxPoints) {
      Vector2D radius = this.getRadius();
      int nPoints = (int)Math.ceil(Math.PI * radius.length());
      if (maxPoints >= 0 && nPoints >= maxPoints) {
         nPoints = maxPoints - 1;
      }

      List<BlockVector2D> points = new ArrayList(nPoints);

      for(int i = 0; i < nPoints; ++i) {
         double angle = (double)i * (Math.PI * 2D) / (double)nPoints;
         Vector2D pos = new Vector2D(Math.cos(angle), Math.sin(angle));
         BlockVector2D blockVector2D = pos.multiply(radius).add(this.center).toBlockVector2D();
         points.add(blockVector2D);
      }

      return points;
   }
}
