package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.Location;

public abstract class RayTracing {
   protected double dX;
   protected double dY;
   protected double dZ;
   protected int blockX;
   protected int blockY;
   protected int blockZ;
   protected double oX;
   protected double oY;
   protected double oZ;
   protected double t = Double.MIN_VALUE;
   protected double tol = (double)0.0F;
   protected int step = 0;
   private int maxSteps = Integer.MAX_VALUE;

   public RayTracing(double x0, double y0, double z0, double x1, double y1, double z1) {
      this.set(x0, y0, z0, x1, y1, z1);
   }

   public RayTracing() {
      this.set((double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F);
   }

   public void set(double x0, double y0, double z0, double x1, double y1, double z1) {
      this.dX = x1 - x0;
      this.dY = y1 - y0;
      this.dZ = z1 - z0;
      this.blockX = Location.locToBlock(x0);
      this.blockY = Location.locToBlock(y0);
      this.blockZ = Location.locToBlock(z0);
      this.oX = x0 - (double)this.blockX;
      this.oY = y0 - (double)this.blockY;
      this.oZ = z0 - (double)this.blockZ;
      this.t = (double)0.0F;
      this.step = 0;
   }

   private static final double tDiff(double dTotal, double offset) {
      if (dTotal > (double)0.0F) {
         return ((double)1.0F - offset) / dTotal;
      } else {
         return dTotal < (double)0.0F ? offset / -dTotal : Double.MAX_VALUE;
      }
   }

   public void loop() {
      while(true) {
         if ((double)1.0F - this.t > this.tol) {
            double tX = tDiff(this.dX, this.oX);
            double tY = tDiff(this.dY, this.oY);
            double tZ = tDiff(this.dZ, this.oZ);
            double tMin = Math.min(tX, Math.min(tY, tZ));
            if (tMin == Double.MAX_VALUE || this.t + tMin > (double)1.0F) {
               tMin = (double)1.0F - this.t;
            }

            ++this.step;
            if (this.step(this.blockX, this.blockY, this.blockZ, this.oX, this.oY, this.oZ, tMin) && !(this.t + tMin >= (double)1.0F - this.tol)) {
               boolean changed = false;
               this.oX += tMin * this.dX;
               if (tX == tMin) {
                  if (this.dX < (double)0.0F) {
                     this.oX = (double)1.0F;
                     --this.blockX;
                     changed = true;
                  } else if (this.dX > (double)0.0F) {
                     this.oX = (double)0.0F;
                     ++this.blockX;
                     changed = true;
                  }
               } else if (this.oX >= (double)1.0F && this.dX > (double)0.0F) {
                  --this.oX;
                  ++this.blockX;
                  changed = true;
               } else if (this.oX < (double)0.0F && this.dX < (double)0.0F) {
                  ++this.oX;
                  --this.blockX;
                  changed = true;
               }

               this.oY += tMin * this.dY;
               if (tY == tMin) {
                  if (this.dY < (double)0.0F) {
                     this.oY = (double)1.0F;
                     --this.blockY;
                     changed = true;
                  } else if (this.dY > (double)0.0F) {
                     this.oY = (double)0.0F;
                     ++this.blockY;
                     changed = true;
                  }
               } else if (this.oY >= (double)1.0F && this.dY > (double)0.0F) {
                  --this.oY;
                  ++this.blockY;
                  changed = true;
               } else if (this.oY < (double)0.0F && this.dY < (double)0.0F) {
                  ++this.oY;
                  --this.blockY;
                  changed = true;
               }

               this.oZ += tMin * this.dZ;
               if (tZ == tMin) {
                  if (this.dZ < (double)0.0F) {
                     this.oZ = (double)1.0F;
                     --this.blockZ;
                     changed = true;
                  } else if (this.dZ > (double)0.0F) {
                     this.oZ = (double)0.0F;
                     ++this.blockZ;
                     changed = true;
                  }
               } else if (this.oZ >= (double)1.0F && this.dZ > (double)0.0F) {
                  --this.oZ;
                  ++this.blockZ;
                  changed = true;
               } else if (this.oZ < (double)0.0F && this.dZ < (double)0.0F) {
                  ++this.oZ;
                  --this.blockZ;
                  changed = true;
               }

               this.t += tMin;
               if (changed && this.step < this.maxSteps) {
                  continue;
               }
            }
         }

         return;
      }
   }

   public int getStepsDone() {
      return this.step;
   }

   public int getMaxSteps() {
      return this.maxSteps;
   }

   public void setMaxSteps(int maxSteps) {
      this.maxSteps = maxSteps;
   }

   protected abstract boolean step(int var1, int var2, int var3, double var4, double var6, double var8, double var10);
}
