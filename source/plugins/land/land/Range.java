package land;

import java.io.Serializable;
import java.util.Random;

public class Range implements Serializable, Cloneable {
   private static final long serialVersionUID = 0L;
   private static final Random r = new Random();
   private Pos p1;
   private Pos p2;

   public Range(Pos p1, Pos p2) {
      this.p1 = p1;
      this.p2 = p2;
   }

   public boolean isFit() {
      return this.p1.compare(this.p2);
   }

   public void fit() {
      int x1 = this.p1.getX();
      int y1 = this.p1.getY();
      int z1 = this.p1.getZ();
      int x2 = this.p2.getX();
      int y2 = this.p2.getY();
      int z2 = this.p2.getZ();
      if (x1 > x2) {
         this.p1.setX(x2);
         this.p2.setX(x1);
      }

      if (y1 > y2) {
         this.p1.setY(y2);
         this.p2.setY(y1);
      }

      if (z1 > z2) {
         this.p1.setZ(z2);
         this.p2.setZ(z1);
      }

   }

   public Pos getP1() {
      return this.p1.clone();
   }

   public void setP1(Pos p1) {
      this.p1 = p1;
   }

   public Pos getP2() {
      return this.p2.clone();
   }

   public void setP2(Pos p2) {
      this.p2 = p2;
   }

   public int getXLength() {
      return Math.abs(this.p2.getX() - this.p1.getX()) + 1;
   }

   public int getYLength() {
      return Math.abs(this.p2.getY() - this.p1.getY()) + 1;
   }

   public int getZLength() {
      return Math.abs(this.p2.getZ() - this.p1.getZ()) + 1;
   }

   public int getTotalLength(boolean repeatCorner) {
      if (repeatCorner) {
         return this.getXLength() * 4 + this.getYLength() * 4 + this.getZLength() * 4;
      } else {
         int result = 0;
         int xLength = this.getXLength();
         if (xLength > 2) {
            result += 4 * (xLength - 2);
         }

         int yLength = this.getYLength();
         if (yLength > 2) {
            result += 4 * (yLength - 2);
         }

         int zLength = this.getZLength();
         if (zLength > 2) {
            result += 4 * (zLength - 2);
         }

         result += 8;
         if (xLength == 1) {
            if (yLength == 1 && zLength == 1) {
               result -= 7;
            } else if (yLength != 1 && zLength != 1) {
               result -= 4;
            } else {
               result -= 6;
            }
         } else if (yLength == 1 && zLength == 1) {
            result -= 6;
         } else if (yLength == 1 || zLength == 1) {
            result -= 4;
         }

         return result;
      }
   }

   public int getXCenter() {
      return (this.p1.getX() + this.p2.getX()) / 2;
   }

   public int getYCenter() {
      return (this.p1.getY() + this.p2.getY()) / 2;
   }

   public int getZCenter() {
      return (this.p1.getZ() + this.p2.getZ()) / 2;
   }

   public boolean checkPos(Pos pos) {
      Range r = this.clone();
      r.fit();
      return r.getP1().compare(pos) && pos.compare(r.getP2());
   }

   public long getSize() {
      int xLength = Math.abs(this.p2.getX() - this.p1.getX()) + 1;
      int yLength = Math.abs(this.p2.getY() - this.p1.getY()) + 1;
      int zLength = Math.abs(this.p2.getZ() - this.p1.getZ()) + 1;
      return (long)xLength * (long)yLength * (long)zLength;
   }

   public Pos getCenter() {
      int x1 = this.p1.getX();
      int y1 = this.p1.getY();
      int z1 = this.p1.getZ();
      int x2 = this.p2.getX();
      int y2 = this.p2.getY();
      int z2 = this.p2.getZ();
      Pos result = new Pos(this.p1.getWorld(), (x1 + x2) / 2, (y1 + y2) / 2, (z1 + z2) / 2);
      return result;
   }

   public Pos getRandomPos() {
      int x1 = this.p1.getX();
      int y1 = this.p1.getY();
      int z1 = this.p1.getZ();
      int x2 = this.p2.getX();
      int y2 = this.p2.getY();
      int z2 = this.p2.getZ();
      if (x1 > x2) {
         int temp = x1;
         x1 = x2;
         x2 = temp;
      }

      if (y1 > y2) {
         int temp = y1;
         y1 = y2;
         y2 = temp;
      }

      if (z1 > z2) {
         int temp = z1;
         z1 = z2;
         z2 = temp;
      }

      Pos result = new Pos(this.p1.getWorld(), r.nextInt(x2 - x1 + 1) + x1, r.nextInt(y2 - y1 + 1) + y1, r.nextInt(z2 - z1 + 1) + z1);
      return result;
   }

   public void expand(int xDir, int yDir, int zDir) {
      if (xDir > 0) {
         if (this.p1.getX() <= this.p2.getX()) {
            this.p2.setX(this.p2.getX() + xDir);
         } else {
            this.p1.setX(this.p1.getX() + xDir);
         }
      } else if (xDir < 0) {
         if (this.p1.getX() <= this.p2.getX()) {
            this.p1.setX(this.p1.getX() + xDir);
         } else {
            this.p2.setX(this.p2.getX() + xDir);
         }
      }

      if (yDir > 0) {
         if (this.p1.getY() <= this.p2.getY()) {
            this.p2.setY(this.p2.getY() + yDir);
         } else {
            this.p1.setY(this.p1.getY() + yDir);
         }
      } else if (yDir < 0) {
         if (this.p1.getY() <= this.p2.getY()) {
            this.p1.setY(this.p1.getY() + yDir);
         } else {
            this.p2.setY(this.p2.getY() + yDir);
         }
      }

      if (zDir > 0) {
         if (this.p1.getZ() <= this.p2.getZ()) {
            this.p2.setZ(this.p2.getZ() + zDir);
         } else {
            this.p1.setZ(this.p1.getZ() + zDir);
         }
      } else if (zDir < 0) {
         if (this.p1.getZ() <= this.p2.getZ()) {
            this.p1.setZ(this.p1.getZ() + zDir);
         } else {
            this.p2.setZ(this.p2.getZ() + zDir);
         }
      }

   }

   public void contract(int xDir, int yDir, int zDir) {
      if (xDir > 0) {
         if (this.p1.getX() <= this.p2.getX()) {
            this.p1.setX(Math.min(this.p2.getX(), this.p1.getX() + xDir));
         } else {
            this.p2.setX(Math.min(this.p1.getX(), this.p2.getX() + xDir));
         }
      } else if (xDir < 0) {
         if (this.p1.getX() <= this.p2.getX()) {
            this.p2.setX(Math.max(this.p1.getX(), this.p2.getX() + xDir));
         } else {
            this.p1.setX(Math.max(this.p2.getX(), this.p1.getX() + xDir));
         }
      }

      if (yDir > 0) {
         if (this.p1.getY() <= this.p2.getY()) {
            this.p1.setY(Math.min(this.p2.getY(), this.p1.getY() + yDir));
         } else {
            this.p2.setY(Math.min(this.p1.getY(), this.p2.getY() + yDir));
         }
      } else if (yDir < 0) {
         if (this.p1.getY() <= this.p2.getY()) {
            this.p2.setY(Math.max(this.p1.getY(), this.p2.getY() + yDir));
         } else {
            this.p1.setY(Math.max(this.p2.getY(), this.p1.getY() + yDir));
         }
      }

      if (zDir > 0) {
         if (this.p1.getZ() <= this.p2.getZ()) {
            this.p1.setZ(Math.min(this.p2.getZ(), this.p1.getZ() + zDir));
         } else {
            this.p2.setZ(Math.min(this.p1.getZ(), this.p2.getZ() + zDir));
         }
      } else if (zDir < 0) {
         if (this.p1.getZ() <= this.p2.getZ()) {
            this.p2.setZ(Math.max(this.p1.getZ(), this.p2.getZ() + zDir));
         } else {
            this.p1.setZ(Math.max(this.p2.getZ(), this.p1.getZ() + zDir));
         }
      }

   }

   public void move(int xDir, int yDir, int zDir) {
      this.p1.setX(this.p1.getX() + xDir);
      this.p2.setX(this.p2.getX() + xDir);
      this.p1.setY(this.p1.getY() + yDir);
      this.p2.setY(this.p2.getY() + yDir);
      this.p1.setZ(this.p1.getZ() + zDir);
      this.p2.setZ(this.p2.getZ() + zDir);
   }

   public boolean isIn(Range range) {
      Range r1 = this.clone();
      Range r2 = range.clone();
      r1.fit();
      r2.fit();
      if (!r1.getP1().getWorld().equals(r2.getP1().getWorld())) {
         return false;
      } else {
         return r2.getP1().compare(r1.getP1()) && r1.getP2().compare(r2.getP2());
      }
   }

   public Range clone() {
      Range range = new Range(this.p1.clone(), this.p2.clone());
      return range;
   }

   public int hashCode() {
      return this.p1.hashCode() + this.p2.hashCode();
   }

   public boolean equals(Object obj) {
      Range r1 = this.clone();
      Range r2 = ((Range)obj).clone();
      r1.fit();
      r2.fit();
      return r1.p1.equals(this.p1) && r2.p2.equals(this.p2);
   }
}
