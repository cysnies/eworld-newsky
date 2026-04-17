package com.sk89q.worldedit.regions;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class CuboidRegion extends AbstractRegion implements FlatRegion {
   private Vector pos1;
   private Vector pos2;

   public CuboidRegion(Vector pos1, Vector pos2) {
      this((LocalWorld)null, pos1, pos2);
   }

   public CuboidRegion(LocalWorld world, Vector pos1, Vector pos2) {
      super(world);
      this.pos1 = pos1;
      this.pos2 = pos2;
   }

   public Vector getMinimumPoint() {
      return new Vector(Math.min(this.pos1.getX(), this.pos2.getX()), Math.min(this.pos1.getY(), this.pos2.getY()), Math.min(this.pos1.getZ(), this.pos2.getZ()));
   }

   public Vector getMaximumPoint() {
      return new Vector(Math.max(this.pos1.getX(), this.pos2.getX()), Math.max(this.pos1.getY(), this.pos2.getY()), Math.max(this.pos1.getZ(), this.pos2.getZ()));
   }

   public int getMinimumY() {
      return Math.min(this.pos1.getBlockY(), this.pos2.getBlockY());
   }

   public int getMaximumY() {
      return Math.max(this.pos1.getBlockY(), this.pos2.getBlockY());
   }

   public void expand(Vector... changes) {
      for(Vector change : changes) {
         if (change.getX() > (double)0.0F) {
            if (Math.max(this.pos1.getX(), this.pos2.getX()) == this.pos1.getX()) {
               this.pos1 = this.pos1.add(new Vector(change.getX(), (double)0.0F, (double)0.0F));
            } else {
               this.pos2 = this.pos2.add(new Vector(change.getX(), (double)0.0F, (double)0.0F));
            }
         } else if (Math.min(this.pos1.getX(), this.pos2.getX()) == this.pos1.getX()) {
            this.pos1 = this.pos1.add(new Vector(change.getX(), (double)0.0F, (double)0.0F));
         } else {
            this.pos2 = this.pos2.add(new Vector(change.getX(), (double)0.0F, (double)0.0F));
         }

         if (change.getY() > (double)0.0F) {
            if (Math.max(this.pos1.getY(), this.pos2.getY()) == this.pos1.getY()) {
               this.pos1 = this.pos1.add(new Vector((double)0.0F, change.getY(), (double)0.0F));
            } else {
               this.pos2 = this.pos2.add(new Vector((double)0.0F, change.getY(), (double)0.0F));
            }
         } else if (Math.min(this.pos1.getY(), this.pos2.getY()) == this.pos1.getY()) {
            this.pos1 = this.pos1.add(new Vector((double)0.0F, change.getY(), (double)0.0F));
         } else {
            this.pos2 = this.pos2.add(new Vector((double)0.0F, change.getY(), (double)0.0F));
         }

         if (change.getZ() > (double)0.0F) {
            if (Math.max(this.pos1.getZ(), this.pos2.getZ()) == this.pos1.getZ()) {
               this.pos1 = this.pos1.add(new Vector((double)0.0F, (double)0.0F, change.getZ()));
            } else {
               this.pos2 = this.pos2.add(new Vector((double)0.0F, (double)0.0F, change.getZ()));
            }
         } else if (Math.min(this.pos1.getZ(), this.pos2.getZ()) == this.pos1.getZ()) {
            this.pos1 = this.pos1.add(new Vector((double)0.0F, (double)0.0F, change.getZ()));
         } else {
            this.pos2 = this.pos2.add(new Vector((double)0.0F, (double)0.0F, change.getZ()));
         }
      }

      this.recalculate();
   }

   public void contract(Vector... changes) {
      for(Vector change : changes) {
         if (change.getX() < (double)0.0F) {
            if (Math.max(this.pos1.getX(), this.pos2.getX()) == this.pos1.getX()) {
               this.pos1 = this.pos1.add(new Vector(change.getX(), (double)0.0F, (double)0.0F));
            } else {
               this.pos2 = this.pos2.add(new Vector(change.getX(), (double)0.0F, (double)0.0F));
            }
         } else if (Math.min(this.pos1.getX(), this.pos2.getX()) == this.pos1.getX()) {
            this.pos1 = this.pos1.add(new Vector(change.getX(), (double)0.0F, (double)0.0F));
         } else {
            this.pos2 = this.pos2.add(new Vector(change.getX(), (double)0.0F, (double)0.0F));
         }

         if (change.getY() < (double)0.0F) {
            if (Math.max(this.pos1.getY(), this.pos2.getY()) == this.pos1.getY()) {
               this.pos1 = this.pos1.add(new Vector((double)0.0F, change.getY(), (double)0.0F));
            } else {
               this.pos2 = this.pos2.add(new Vector((double)0.0F, change.getY(), (double)0.0F));
            }
         } else if (Math.min(this.pos1.getY(), this.pos2.getY()) == this.pos1.getY()) {
            this.pos1 = this.pos1.add(new Vector((double)0.0F, change.getY(), (double)0.0F));
         } else {
            this.pos2 = this.pos2.add(new Vector((double)0.0F, change.getY(), (double)0.0F));
         }

         if (change.getZ() < (double)0.0F) {
            if (Math.max(this.pos1.getZ(), this.pos2.getZ()) == this.pos1.getZ()) {
               this.pos1 = this.pos1.add(new Vector((double)0.0F, (double)0.0F, change.getZ()));
            } else {
               this.pos2 = this.pos2.add(new Vector((double)0.0F, (double)0.0F, change.getZ()));
            }
         } else if (Math.min(this.pos1.getZ(), this.pos2.getZ()) == this.pos1.getZ()) {
            this.pos1 = this.pos1.add(new Vector((double)0.0F, (double)0.0F, change.getZ()));
         } else {
            this.pos2 = this.pos2.add(new Vector((double)0.0F, (double)0.0F, change.getZ()));
         }
      }

      this.recalculate();
   }

   private void recalculate() {
      this.pos1 = this.pos1.clampY(0, this.world == null ? 255 : this.world.getMaxY());
      this.pos2 = this.pos2.clampY(0, this.world == null ? 255 : this.world.getMaxY());
   }

   public void shift(Vector change) throws RegionOperationException {
      this.pos1 = this.pos1.add(change);
      this.pos2 = this.pos2.add(change);
      this.recalculate();
   }

   public Vector getPos1() {
      return this.pos1;
   }

   public void setPos1(Vector pos1) {
      this.pos1 = pos1;
   }

   public Vector getPos2() {
      return this.pos2;
   }

   public void setPos2(Vector pos2) {
      this.pos2 = pos2;
   }

   public Set getChunks() {
      Set<Vector2D> chunks = new HashSet();
      Vector min = this.getMinimumPoint();
      Vector max = this.getMaximumPoint();

      for(int x = min.getBlockX(); x <= max.getBlockX(); ++x) {
         for(int z = min.getBlockZ(); z <= max.getBlockZ(); ++z) {
            chunks.add(new BlockVector2D(x >> 4, z >> 4));
         }
      }

      return chunks;
   }

   public Set getChunkCubes() {
      Set<Vector> chunks = new HashSet();
      Vector min = this.getMinimumPoint();
      Vector max = this.getMaximumPoint();

      for(int x = min.getBlockX(); x <= max.getBlockX(); ++x) {
         for(int y = min.getBlockY(); y <= max.getBlockY(); ++y) {
            for(int z = min.getBlockZ(); z <= max.getBlockZ(); ++z) {
               chunks.add(new BlockVector(x >> 4, y >> 4, z >> 4));
            }
         }
      }

      return chunks;
   }

   public boolean contains(Vector pt) {
      double x = pt.getX();
      double y = pt.getY();
      double z = pt.getZ();
      Vector min = this.getMinimumPoint();
      Vector max = this.getMaximumPoint();
      return x >= (double)min.getBlockX() && x <= (double)max.getBlockX() && y >= (double)min.getBlockY() && y <= (double)max.getBlockY() && z >= (double)min.getBlockZ() && z <= (double)max.getBlockZ();
   }

   public Iterator iterator() {
      return new Iterator() {
         private Vector min = CuboidRegion.this.getMinimumPoint();
         private Vector max = CuboidRegion.this.getMaximumPoint();
         private int nextX;
         private int nextY;
         private int nextZ;

         {
            this.nextX = this.min.getBlockX();
            this.nextY = this.min.getBlockY();
            this.nextZ = this.min.getBlockZ();
         }

         public boolean hasNext() {
            return this.nextX != Integer.MIN_VALUE;
         }

         public BlockVector next() {
            if (!this.hasNext()) {
               throw new NoSuchElementException();
            } else {
               BlockVector answer = new BlockVector(this.nextX, this.nextY, this.nextZ);
               if (++this.nextX > this.max.getBlockX()) {
                  this.nextX = this.min.getBlockX();
                  if (++this.nextY > this.max.getBlockY()) {
                     this.nextY = this.min.getBlockY();
                     if (++this.nextZ > this.max.getBlockZ()) {
                        this.nextX = Integer.MIN_VALUE;
                     }
                  }
               }

               return answer;
            }
         }

         public void remove() {
            throw new UnsupportedOperationException();
         }
      };
   }

   public Iterable asFlatRegion() {
      return new Iterable() {
         public Iterator iterator() {
            return new Iterator() {
               private Vector min = CuboidRegion.this.getMinimumPoint();
               private Vector max = CuboidRegion.this.getMaximumPoint();
               private int nextX;
               private int nextZ;

               {
                  this.nextX = this.min.getBlockX();
                  this.nextZ = this.min.getBlockZ();
               }

               public boolean hasNext() {
                  return this.nextX != Integer.MIN_VALUE;
               }

               public Vector2D next() {
                  if (!this.hasNext()) {
                     throw new NoSuchElementException();
                  } else {
                     Vector2D answer = new Vector2D(this.nextX, this.nextZ);
                     if (++this.nextX > this.max.getBlockX()) {
                        this.nextX = this.min.getBlockX();
                        if (++this.nextZ > this.max.getBlockZ()) {
                           this.nextX = Integer.MIN_VALUE;
                        }
                     }

                     return answer;
                  }
               }

               public void remove() {
                  throw new UnsupportedOperationException();
               }
            };
         }
      };
   }

   public String toString() {
      return this.getMinimumPoint() + " - " + this.getMaximumPoint();
   }

   public CuboidRegion clone() {
      return (CuboidRegion)super.clone();
   }
}
