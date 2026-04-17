package com.sk89q.worldedit.regions;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class FlatRegionIterator implements Iterator {
   private Region region;
   private int y;
   private int minX;
   private int nextX;
   private int nextZ;
   private int maxX;
   private int maxZ;

   public FlatRegionIterator(Region region) {
      this.region = region;
      Vector min = region.getMinimumPoint();
      Vector max = region.getMaximumPoint();
      this.y = min.getBlockY();
      this.minX = min.getBlockX();
      this.nextX = this.minX;
      this.nextZ = min.getBlockZ();
      this.maxX = max.getBlockX();
      this.maxZ = max.getBlockZ();
      this.forward();
   }

   public boolean hasNext() {
      return this.nextX != Integer.MIN_VALUE;
   }

   private void forward() {
      while(this.hasNext() && !this.region.contains(new Vector(this.nextX, this.y, this.nextZ))) {
         this.forwardOne();
      }

   }

   public Vector2D next() {
      if (!this.hasNext()) {
         throw new NoSuchElementException();
      } else {
         Vector2D answer = new Vector2D(this.nextX, this.nextZ);
         this.forwardOne();
         this.forward();
         return answer;
      }
   }

   private void forwardOne() {
      if (++this.nextX > this.maxX) {
         this.nextX = this.minX;
         if (++this.nextZ > this.maxZ) {
            this.nextX = Integer.MIN_VALUE;
         }
      }
   }

   public void remove() {
      throw new UnsupportedOperationException();
   }
}
