package com.sk89q.worldedit.regions;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class RegionIterator implements Iterator {
   private final Region region;
   private final int maxX;
   private final int maxY;
   private final int maxZ;
   private final Vector min;
   private int nextX;
   private int nextY;
   private int nextZ;

   public RegionIterator(Region region) {
      this.region = region;
      Vector max = region.getMaximumPoint();
      this.maxX = max.getBlockX();
      this.maxY = max.getBlockY();
      this.maxZ = max.getBlockZ();
      this.min = region.getMinimumPoint();
      this.nextX = this.min.getBlockX();
      this.nextY = this.min.getBlockY();
      this.nextZ = this.min.getBlockZ();
      this.forward();
   }

   public boolean hasNext() {
      return this.nextX != Integer.MIN_VALUE;
   }

   private void forward() {
      while(this.hasNext() && !this.region.contains(new BlockVector(this.nextX, this.nextY, this.nextZ))) {
         this.forwardOne();
      }

   }

   public BlockVector next() {
      if (!this.hasNext()) {
         throw new NoSuchElementException();
      } else {
         BlockVector answer = new BlockVector(this.nextX, this.nextY, this.nextZ);
         this.forwardOne();
         this.forward();
         return answer;
      }
   }

   private void forwardOne() {
      if (++this.nextX > this.maxX) {
         this.nextX = this.min.getBlockX();
         if (++this.nextY > this.maxY) {
            this.nextY = this.min.getBlockY();
            if (++this.nextZ > this.maxZ) {
               this.nextX = Integer.MIN_VALUE;
            }
         }
      }
   }

   public void remove() {
      throw new UnsupportedOperationException();
   }
}
