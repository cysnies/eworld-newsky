package fr.neatmonster.nocheatplus.checks.blockinteract;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.utilities.BlockCache;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.InteractRayTracing;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

public class Visible extends Check {
   private static final double offset = 1.0E-4;
   private BlockCache blockCache;
   private final InteractRayTracing rayTracing = new InteractRayTracing(false);

   public Visible() {
      super(CheckType.BLOCKINTERACT_VISIBLE);
      this.blockCache = this.mcAccess.getBlockCache((World)null);
      this.rayTracing.setMaxSteps(60);
   }

   public void setMCAccess(MCAccess mcAccess) {
      super.setMCAccess(mcAccess);
      this.blockCache = mcAccess.getBlockCache((World)null);
   }

   private static final double getEnd(double[] bounds, int index, int mod) {
      if (bounds == null) {
         return (double)0.5F + 0.5001 * (double)mod;
      } else if (mod == 0) {
         return (bounds[index] + bounds[index + 3]) / (double)2.0F;
      } else if (mod == 1) {
         return Math.min((double)1.0F, bounds[index + 3]) + 1.0E-4;
      } else if (mod == -1) {
         return Math.max((double)0.0F, bounds[index]) - 1.0E-4;
      } else {
         throw new IllegalArgumentException("BlockFace.getModX|Y|Z must be 0, 1 or -1.");
      }
   }

   public boolean check(Player player, Location loc, Block block, BlockFace face, Action action, BlockInteractData data, BlockInteractConfig cc) {
      int blockX = block.getX();
      int blockY = block.getY();
      int blockZ = block.getZ();
      double eyeX = loc.getX();
      double eyeY = loc.getY() + player.getEyeHeight();
      double eyeZ = loc.getZ();
      boolean collides;
      if (blockX == Location.locToBlock(eyeX) && blockZ == Location.locToBlock(eyeZ) && block.getY() == Location.locToBlock(eyeY)) {
         collides = false;
      } else {
         this.blockCache.setAccess(loc.getWorld());
         this.rayTracing.setBlockCache(this.blockCache);
         collides = this.checkRayTracing(eyeX, eyeY, eyeZ, blockX, blockY, blockZ, face);
         this.rayTracing.cleanup();
         this.blockCache.cleanup();
      }

      if (cc.debug && player.hasPermission("nocheatplus.admin.debug")) {
         player.sendMessage("Interact visible: " + (action == Action.RIGHT_CLICK_BLOCK ? "right" : "left") + " collide=" + this.rayTracing.collides());
      }

      boolean cancel = false;
      if (collides) {
         ++data.visibleVL;
         if (this.executeActions(player, data.visibleVL, (double)1.0F, cc.visibleActions)) {
            cancel = true;
         }
      } else {
         data.visibleVL *= 0.99;
      }

      return cancel;
   }

   private boolean checkRayTracing(double eyeX, double eyeY, double eyeZ, int blockX, int blockY, int blockZ, BlockFace face) {
      double[] bounds = BlockProperties.getCorrectedBounds(this.blockCache, blockX, blockY, blockZ);
      int modX = face.getModX();
      int modY = face.getModY();
      int modZ = face.getModZ();
      double estX = (double)blockX + getEnd(bounds, 0, modX);
      double estY = (double)blockY + getEnd(bounds, 1, modY);
      double estZ = (double)blockZ + getEnd(bounds, 2, modZ);
      int bEstX = Location.locToBlock(estX);
      int bEstY = Location.locToBlock(estY);
      int bEstZ = Location.locToBlock(estZ);
      int estId = this.blockCache.getTypeId(bEstX, bEstY, bEstZ);
      boolean skipPassable = blockX == bEstX && blockY == bEstY && blockZ == bEstZ;
      return this.checkCollision(eyeX, eyeY, eyeZ, estX, estY, estZ, estId, bounds, modX, modY, modZ, skipPassable);
   }

   private boolean checkCollision(double eyeX, double eyeY, double eyeZ, double estX, double estY, double estZ, int estId, double[] bounds, int modX, int modY, int modZ, boolean skipPassable) {
      if (skipPassable || BlockProperties.isPassable(this.blockCache, estX, estY, estZ, estId)) {
         this.rayTracing.set(eyeX, eyeY, eyeZ, estX, estY, estZ);
         this.rayTracing.loop();
         if (!this.rayTracing.collides() && this.rayTracing.getStepsDone() < this.rayTracing.getMaxSteps()) {
            return false;
         }
      }

      if (modX == 0) {
         double d = bounds == null ? (double)0.5F : (bounds[3] - bounds[0]) / (double)2.0F;
         if (d >= 0.05) {
            if (!this.checkCollision(eyeX, eyeY, eyeZ, estX - d, estY, estZ, estId, bounds, 1, modY, modZ, skipPassable)) {
               return false;
            }

            if (!this.checkCollision(eyeX, eyeY, eyeZ, estX + d, estY, estZ, estId, bounds, 1, modY, modZ, skipPassable)) {
               return false;
            }
         }
      }

      if (modZ == 0) {
         double d = bounds == null ? (double)0.5F : (bounds[5] - bounds[2]) / (double)2.0F;
         if (d >= 0.05) {
            if (!this.checkCollision(eyeX, eyeY, eyeZ, estX, estY, estZ - d, estId, bounds, 1, modY, 1, skipPassable)) {
               return false;
            }

            if (!this.checkCollision(eyeX, eyeY, eyeZ, estX, estY, estZ + d, estId, bounds, 1, modY, 1, skipPassable)) {
               return false;
            }
         }
      }

      if (modY == 0) {
         double d = bounds == null ? (double)0.5F : (bounds[4] - bounds[1]) / (double)2.0F;
         if (d >= 0.05) {
            if (!this.checkCollision(eyeX, eyeY, eyeZ, estX, estY - d, estZ, estId, bounds, 1, 1, 1, skipPassable)) {
               return false;
            }

            if (!this.checkCollision(eyeX, eyeY, eyeZ, estX, estY + d, estZ, estId, bounds, 1, 1, 1, skipPassable)) {
               return false;
            }
         }
      }

      return true;
   }
}
