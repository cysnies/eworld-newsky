package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class TrigUtil {
   private static final Vector vec1 = new Vector();
   private static final Vector vec2 = new Vector();
   public static final double fRadToGrad = (180D / Math.PI);
   public static final double DIRECTION_PRECISION = 2.6;

   public static double directionCheck(Player player, double targetX, double targetY, double targetZ, double targetWidth, double targetHeight, double precision) {
      Location loc = player.getLocation();
      Vector dir = loc.getDirection();
      return directionCheck(loc.getX(), loc.getY() + player.getEyeHeight(), loc.getZ(), dir.getX(), dir.getY(), dir.getZ(), targetX, targetY, targetZ, targetWidth, targetHeight, precision);
   }

   public static double directionCheck(Location sourceFoot, double eyeHeight, Vector dir, Block target, double precision) {
      return directionCheck(sourceFoot.getX(), sourceFoot.getY() + eyeHeight, sourceFoot.getZ(), dir.getX(), dir.getY(), dir.getZ(), (double)target.getX(), (double)target.getY(), (double)target.getZ(), (double)1.0F, (double)1.0F, precision);
   }

   public static double directionCheck(Location sourceFoot, double eyeHeight, Vector dir, double targetX, double targetY, double targetZ, double targetWidth, double targetHeight, double precision) {
      return directionCheck(sourceFoot.getX(), sourceFoot.getY() + eyeHeight, sourceFoot.getZ(), dir.getX(), dir.getY(), dir.getZ(), targetX, targetY, targetZ, targetWidth, targetHeight, precision);
   }

   public static double directionCheck(double sourceX, double sourceY, double sourceZ, double dirX, double dirY, double dirZ, double targetX, double targetY, double targetZ, double targetWidth, double targetHeight, double precision) {
      double dirLength = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
      if (dirLength == (double)0.0F) {
         dirLength = (double)1.0F;
      }

      double dX = targetX - sourceX;
      double dY = targetY - sourceY;
      double dZ = targetZ - sourceZ;
      double targetDist = Math.sqrt(dX * dX + dY * dY + dZ * dZ);
      double xPrediction = targetDist * dirX / dirLength;
      double yPrediction = targetDist * dirY / dirLength;
      double zPrediction = targetDist * dirZ / dirLength;
      double off = (double)0.0F;
      off += Math.max(Math.abs(dX - xPrediction) - (targetWidth / (double)2.0F + precision), (double)0.0F);
      off += Math.max(Math.abs(dZ - zPrediction) - (targetWidth / (double)2.0F + precision), (double)0.0F);
      off += Math.max(Math.abs(dY - yPrediction) - (targetHeight / (double)2.0F + precision), (double)0.0F);
      if (off > (double)1.0F) {
         off = Math.sqrt(off);
      }

      return off;
   }

   public static double combinedDirectionCheck(Location sourceFoot, double eyeHeight, Vector dir, double targetX, double targetY, double targetZ, double targetWidth, double targetHeight, double precision, double anglePrecision) {
      return combinedDirectionCheck(sourceFoot.getX(), sourceFoot.getY() + eyeHeight, sourceFoot.getZ(), dir.getX(), dir.getY(), dir.getZ(), targetX, targetY, targetZ, targetWidth, targetHeight, precision, anglePrecision);
   }

   public static double combinedDirectionCheck(Location sourceFoot, double eyeHeight, Vector dir, Block target, double precision, double anglePrecision) {
      return combinedDirectionCheck(sourceFoot.getX(), sourceFoot.getY() + eyeHeight, sourceFoot.getZ(), dir.getX(), dir.getY(), dir.getZ(), (double)target.getX(), (double)target.getY(), (double)target.getZ(), (double)1.0F, (double)1.0F, precision, anglePrecision);
   }

   public static double combinedDirectionCheck(double sourceX, double sourceY, double sourceZ, double dirX, double dirY, double dirZ, double targetX, double targetY, double targetZ, double targetWidth, double targetHeight, double blockPrecision, double anglePrecision) {
      double dirLength = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
      if (dirLength == (double)0.0F) {
         dirLength = (double)1.0F;
      }

      double dX = targetX - sourceX;
      double dY = targetY - sourceY;
      double dZ = targetZ - sourceZ;
      double targetDist = Math.sqrt(dX * dX + dY * dY + dZ * dZ);
      if (targetDist > Math.max(targetHeight, targetWidth) / (double)2.0F && (double)angle(sourceX, sourceY, sourceZ, dirX, dirY, dirZ, targetX, targetY, targetZ) * (180D / Math.PI) > anglePrecision) {
         return targetDist - Math.max(targetHeight, targetWidth) / (double)2.0F;
      } else {
         double xPrediction = targetDist * dirX / dirLength;
         double yPrediction = targetDist * dirY / dirLength;
         double zPrediction = targetDist * dirZ / dirLength;
         double off = (double)0.0F;
         off += Math.max(Math.abs(dX - xPrediction) - (targetWidth / (double)2.0F + blockPrecision), (double)0.0F);
         off += Math.max(Math.abs(dZ - zPrediction) - (targetWidth / (double)2.0F + blockPrecision), (double)0.0F);
         off += Math.max(Math.abs(dY - yPrediction) - (targetHeight / (double)2.0F + blockPrecision), (double)0.0F);
         if (off > (double)1.0F) {
            off = Math.sqrt(off);
         }

         return off;
      }
   }

   public static final double distance(Location location1, Location location2) {
      return distance(location1.getX(), location1.getY(), location1.getZ(), location2.getX(), location2.getY(), location2.getZ());
   }

   public static final double distance(Location location, Block block) {
      return distance(location.getX(), location.getY(), location.getZ(), (double)0.5F + (double)block.getX(), (double)0.5F + (double)block.getY(), (double)0.5F + (double)block.getZ());
   }

   public static final double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
      double dx = Math.abs(x1 - x2);
      double dy = Math.abs(y1 - y2);
      double dz = Math.abs(z1 - z2);
      return Math.sqrt(dx * dx + dy * dy + dz * dz);
   }

   public static final double xzDistance(Location location1, Location location2) {
      return distance(location1.getX(), location1.getZ(), location2.getX(), location2.getZ());
   }

   public static final double distance(double x1, double z1, double x2, double z2) {
      double dx = Math.abs(x1 - x2);
      double dz = Math.abs(z1 - z2);
      return Math.sqrt(dx * dx + dz * dz);
   }

   public static float angle(double sourceX, double sourceY, double sourceZ, double dirX, double dirY, double dirZ, double targetX, double targetY, double targetZ) {
      double dirLength = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
      if (dirLength == (double)0.0F) {
         dirLength = (double)1.0F;
      }

      double dX = targetX - sourceX;
      double dY = targetY - sourceY;
      double dZ = targetZ - sourceZ;
      vec1.setX(dX);
      vec1.setY(dY);
      vec1.setZ(dZ);
      vec2.setX(dirX);
      vec2.setY(dirY);
      vec2.setZ(dirZ);
      return vec2.angle(vec1);
   }

   public static final double angle(double x, double z) {
      double a;
      if (x > (double)0.0F) {
         a = Math.atan(z / x);
      } else if (x < (double)0.0F) {
         a = Math.atan(z / x) + Math.PI;
      } else if (z < (double)0.0F) {
         a = (Math.PI * 1.5D);
      } else {
         if (!(z > (double)0.0F)) {
            return Double.NaN;
         }

         a = (Math.PI / 2D);
      }

      return a < (double)0.0F ? a + (Math.PI * 2D) : a;
   }

   public static final double angleDiff(double a1, double a2) {
      if (!Double.isNaN(a1) && !Double.isNaN(a1)) {
         double diff = a2 - a1;
         if (diff < -Math.PI) {
            return diff + (Math.PI * 2D);
         } else {
            return diff > Math.PI ? diff - (Math.PI * 2D) : diff;
         }
      } else {
         return Double.NaN;
      }
   }

   public static final float yawDiff(float fromYaw, float toYaw) {
      if (fromYaw <= -360.0F) {
         fromYaw = -(-fromYaw % 360.0F);
      } else if (fromYaw >= 360.0F) {
         fromYaw %= 360.0F;
      }

      if (toYaw <= -360.0F) {
         toYaw = -(-toYaw % 360.0F);
      } else if (toYaw >= 360.0F) {
         toYaw %= 360.0F;
      }

      float yawDiff = toYaw - fromYaw;
      if (yawDiff < -180.0F) {
         yawDiff += 360.0F;
      } else if (yawDiff > 180.0F) {
         yawDiff -= 360.0F;
      }

      return yawDiff;
   }
}
