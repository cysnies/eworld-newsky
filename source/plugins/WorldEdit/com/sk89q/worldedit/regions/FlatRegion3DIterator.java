package com.sk89q.worldedit.regions;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector2D;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class FlatRegion3DIterator implements Iterator {
   private Iterator flatIterator;
   private int minY;
   private int maxY;
   private Vector2D next2D;
   private int nextY;

   public FlatRegion3DIterator(FlatRegion region, Iterator flatIterator) {
      this.flatIterator = flatIterator;
      this.minY = region.getMinimumY();
      this.maxY = region.getMaximumY();
      if (flatIterator.hasNext()) {
         this.next2D = (Vector2D)flatIterator.next();
      } else {
         this.next2D = null;
      }

      this.nextY = this.minY;
   }

   public FlatRegion3DIterator(FlatRegion region) {
      this(region, region.asFlatRegion().iterator());
   }

   public boolean hasNext() {
      return this.next2D != null;
   }

   public BlockVector next() {
      if (!this.hasNext()) {
         throw new NoSuchElementException();
      } else {
         BlockVector current = new BlockVector(this.next2D.getBlockX(), this.nextY, this.next2D.getBlockZ());
         if (this.nextY < this.maxY) {
            ++this.nextY;
         } else if (this.flatIterator.hasNext()) {
            this.next2D = (Vector2D)this.flatIterator.next();
            this.nextY = this.minY;
         } else {
            this.next2D = null;
         }

         return current;
      }
   }

   public void remove() {
      throw new UnsupportedOperationException();
   }
}
