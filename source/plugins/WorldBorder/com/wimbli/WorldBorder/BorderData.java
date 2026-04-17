package com.wimbli.WorldBorder;

import java.util.Arrays;
import java.util.LinkedHashSet;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;

public class BorderData {
   private double x = (double)0.0F;
   private double z = (double)0.0F;
   private int radiusX = 0;
   private int radiusZ = 0;
   private Boolean shapeRound = null;
   private boolean wrapping = false;
   private double maxX;
   private double minX;
   private double maxZ;
   private double minZ;
   private double radiusXSquared;
   private double radiusZSquared;
   private double DefiniteRectangleX;
   private double DefiniteRectangleZ;
   private double radiusSquaredQuotient;
   public static final LinkedHashSet safeOpenBlocks = new LinkedHashSet(Arrays.asList(0, 6, 8, 9, 27, 28, 30, 31, 32, 37, 38, 39, 40, 50, 55, 59, 63, 64, 65, 66, 68, 69, 70, 71, 72, 75, 76, 77, 78, 83, 90, 93, 94, 96, 104, 105, 106, 115, 131, 132, 141, 142, 149, 150, 157, 171));
   public static final LinkedHashSet painfulBlocks = new LinkedHashSet(Arrays.asList(10, 11, 51, 81, 119));
   private static final int limBot = 1;

   public BorderData(double x, double z, int radiusX, int radiusZ, Boolean shapeRound, boolean wrap) {
      this.setData(x, z, radiusX, radiusZ, shapeRound, wrap);
   }

   public BorderData(double x, double z, int radiusX, int radiusZ) {
      this.setData(x, z, radiusX, radiusZ, (Boolean)null);
   }

   public BorderData(double x, double z, int radiusX, int radiusZ, Boolean shapeRound) {
      this.setData(x, z, radiusX, radiusZ, shapeRound);
   }

   public BorderData(double x, double z, int radius) {
      this.setData(x, z, radius, (Boolean)null);
   }

   public BorderData(double x, double z, int radius, Boolean shapeRound) {
      this.setData(x, z, radius, shapeRound);
   }

   public final void setData(double x, double z, int radiusX, int radiusZ, Boolean shapeRound, boolean wrap) {
      this.x = x;
      this.z = z;
      this.shapeRound = shapeRound;
      this.wrapping = wrap;
      this.setRadiusX(radiusX);
      this.setRadiusZ(radiusZ);
   }

   public final void setData(double x, double z, int radiusX, int radiusZ, Boolean shapeRound) {
      this.setData(x, z, radiusX, radiusZ, shapeRound, false);
   }

   public final void setData(double x, double z, int radius, Boolean shapeRound) {
      this.setData(x, z, radius, radius, shapeRound, false);
   }

   public BorderData copy() {
      return new BorderData(this.x, this.z, this.radiusX, this.radiusZ, this.shapeRound, this.wrapping);
   }

   public double getX() {
      return this.x;
   }

   public void setX(double x) {
      this.x = x;
      this.maxX = x + (double)this.radiusX;
      this.minX = x - (double)this.radiusX;
   }

   public double getZ() {
      return this.z;
   }

   public void setZ(double z) {
      this.z = z;
      this.maxZ = z + (double)this.radiusZ;
      this.minZ = z - (double)this.radiusZ;
   }

   public int getRadiusX() {
      return this.radiusX;
   }

   public int getRadiusZ() {
      return this.radiusZ;
   }

   public void setRadiusX(int radiusX) {
      this.radiusX = radiusX;
      this.maxX = this.x + (double)radiusX;
      this.minX = this.x - (double)radiusX;
      this.radiusXSquared = (double)radiusX * (double)radiusX;
      this.radiusSquaredQuotient = this.radiusXSquared / this.radiusZSquared;
      this.DefiniteRectangleX = Math.sqrt((double)0.5F * this.radiusXSquared);
   }

   public void setRadiusZ(int radiusZ) {
      this.radiusZ = radiusZ;
      this.maxZ = this.z + (double)radiusZ;
      this.minZ = this.z - (double)radiusZ;
      this.radiusZSquared = (double)radiusZ * (double)radiusZ;
      this.radiusSquaredQuotient = this.radiusXSquared / this.radiusZSquared;
      this.DefiniteRectangleZ = Math.sqrt((double)0.5F * this.radiusZSquared);
   }

   /** @deprecated */
   public int getRadius() {
      return (this.radiusX + this.radiusZ) / 2;
   }

   public void setRadius(int radius) {
      this.setRadiusX(radius);
      this.setRadiusZ(radius);
   }

   public Boolean getShape() {
      return this.shapeRound;
   }

   public void setShape(Boolean shapeRound) {
      this.shapeRound = shapeRound;
   }

   public boolean getWrapping() {
      return this.wrapping;
   }

   public void setWrapping(boolean wrap) {
      this.wrapping = wrap;
   }

   public String toString() {
      return "radius " + (this.radiusX == this.radiusZ ? this.radiusX : this.radiusX + "x" + this.radiusZ) + " at X: " + Config.coord.format(this.x) + " Z: " + Config.coord.format(this.z) + (this.shapeRound != null ? " (shape override: " + Config.ShapeName(this.shapeRound) + ")" : "") + (this.wrapping ? " (wrapping)" : "");
   }

   public boolean insideBorder(double xLoc, double zLoc, boolean round) {
      if (this.shapeRound != null) {
         round = this.shapeRound;
      }

      if (round) {
         double X = Math.abs(this.x - xLoc);
         double Z = Math.abs(this.z - zLoc);
         if (X < this.DefiniteRectangleX && Z < this.DefiniteRectangleZ) {
            return true;
         } else if (!(X >= (double)this.radiusX) && !(Z >= (double)this.radiusZ)) {
            return X * X + Z * Z * this.radiusSquaredQuotient < this.radiusXSquared;
         } else {
            return false;
         }
      } else {
         return !(xLoc < this.minX) && !(xLoc > this.maxX) && !(zLoc < this.minZ) && !(zLoc > this.maxZ);
      }
   }

   public boolean insideBorder(double xLoc, double zLoc) {
      return this.insideBorder(xLoc, zLoc, Config.ShapeRound());
   }

   public boolean insideBorder(Location loc) {
      return this.insideBorder(loc.getX(), loc.getZ(), Config.ShapeRound());
   }

   public boolean insideBorder(CoordXZ coord, boolean round) {
      return this.insideBorder((double)coord.x, (double)coord.z, round);
   }

   public boolean insideBorder(CoordXZ coord) {
      return this.insideBorder((double)coord.x, (double)coord.z, Config.ShapeRound());
   }

   public Location correctedPosition(Location loc, boolean round, boolean flying) {
      if (this.shapeRound != null) {
         round = this.shapeRound;
      }

      double xLoc = loc.getX();
      double zLoc = loc.getZ();
      double yLoc = loc.getY();
      if (!round) {
         if (this.wrapping) {
            if (xLoc <= this.minX) {
               xLoc = this.maxX - Config.KnockBack();
            } else if (xLoc >= this.maxX) {
               xLoc = this.minX + Config.KnockBack();
            }

            if (zLoc <= this.minZ) {
               zLoc = this.maxZ - Config.KnockBack();
            } else if (zLoc >= this.maxZ) {
               zLoc = this.minZ + Config.KnockBack();
            }
         } else {
            if (xLoc <= this.minX) {
               xLoc = this.minX + Config.KnockBack();
            } else if (xLoc >= this.maxX) {
               xLoc = this.maxX - Config.KnockBack();
            }

            if (zLoc <= this.minZ) {
               zLoc = this.minZ + Config.KnockBack();
            } else if (zLoc >= this.maxZ) {
               zLoc = this.maxZ - Config.KnockBack();
            }
         }
      } else {
         double dX = xLoc - this.x;
         double dZ = zLoc - this.z;
         double dU = Math.sqrt(dX * dX + dZ * dZ);
         double dT = Math.sqrt(dX * dX / this.radiusXSquared + dZ * dZ / this.radiusZSquared);
         double f = (double)1.0F / dT - Config.KnockBack() / dU;
         if (this.wrapping) {
            xLoc = this.x - dX * f;
            zLoc = this.z - dZ * f;
         } else {
            xLoc = this.x + dX * f;
            zLoc = this.z + dZ * f;
         }
      }

      int ixLoc = Location.locToBlock(xLoc);
      int izLoc = Location.locToBlock(zLoc);
      Chunk tChunk = loc.getWorld().getChunkAt(CoordXZ.blockToChunk(ixLoc), CoordXZ.blockToChunk(izLoc));
      if (!tChunk.isLoaded()) {
         tChunk.load();
      }

      yLoc = this.getSafeY(loc.getWorld(), ixLoc, Location.locToBlock(yLoc), izLoc, flying);
      return yLoc == (double)-1.0F ? null : new Location(loc.getWorld(), Math.floor(xLoc) + (double)0.5F, yLoc, Math.floor(zLoc) + (double)0.5F, loc.getYaw(), loc.getPitch());
   }

   public Location correctedPosition(Location loc, boolean round) {
      return this.correctedPosition(loc, round, false);
   }

   public Location correctedPosition(Location loc) {
      return this.correctedPosition(loc, Config.ShapeRound(), false);
   }

   private boolean isSafeSpot(World world, int X, int Y, int Z, boolean flying) {
      boolean safe = safeOpenBlocks.contains(world.getBlockTypeIdAt(X, Y, Z)) && safeOpenBlocks.contains(world.getBlockTypeIdAt(X, Y + 1, Z));
      if (safe && !flying) {
         Integer below = world.getBlockTypeIdAt(X, Y - 1, Z);
         return safe && (!safeOpenBlocks.contains(below) || below == 8 || below == 9) && !painfulBlocks.contains(below);
      } else {
         return safe;
      }
   }

   private double getSafeY(World world, int X, int Y, int Z, boolean flying) {
      int limTop = world.getEnvironment() == Environment.NETHER ? 125 : world.getMaxHeight() - 2;
      int y1 = Y;

      for(int y2 = Y; y1 > 1 || y2 < limTop; ++y2) {
         if (y1 > 1 && this.isSafeSpot(world, X, y1, Z, flying)) {
            return (double)y1;
         }

         if (y2 < limTop && y2 != y1 && this.isSafeSpot(world, X, y2, Z, flying)) {
            return (double)y2;
         }

         --y1;
      }

      return (double)-1.0F;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && obj.getClass() == this.getClass()) {
         BorderData test = (BorderData)obj;
         return test.x == this.x && test.z == this.z && test.radiusX == this.radiusX && test.radiusZ == this.radiusZ;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return ((int)(this.x * (double)10.0F) << 4) + (int)this.z + (this.radiusX << 2) + (this.radiusZ << 3);
   }
}
