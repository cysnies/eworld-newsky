package fr.neatmonster.nocheatplus.utilities;

import fr.neatmonster.nocheatplus.compat.AlmostBoolean;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class PlayerLocation {
   private final MCAccess mcAccess;
   private double yOnGround = 0.001;
   private int blockX;
   private int blockY;
   private int blockZ;
   private double x;
   private double y;
   private double z;
   private float yaw;
   private float pitch;
   private double width;
   private double minX;
   private double maxX;
   private double minY;
   private double maxY;
   private double minZ;
   private double maxZ;
   private Integer typeId = null;
   private Integer typeIdBelow = null;
   private Integer data = null;
   private Boolean aboveStairs = null;
   private Boolean inLava = null;
   private Boolean inWater = null;
   private Boolean inWeb = null;
   private Boolean onGround = null;
   private double onGroundMinY = Double.MAX_VALUE;
   private double notOnGroundMaxY = Double.MIN_VALUE;
   private Boolean onIce = null;
   private Boolean onClimbable = null;
   private Boolean passable = null;
   private Long blockFlags = null;
   private Player player = null;
   private World world = null;
   private BlockCache blockCache = null;

   public PlayerLocation(MCAccess mcAccess, BlockCache blockCache) {
      this.mcAccess = mcAccess;
      this.blockCache = blockCache;
   }

   public Player getPlayer() {
      return this.player;
   }

   public Location getLocation() {
      return new Location(this.world, this.x, this.y, this.z);
   }

   public World getWorld() {
      return this.world;
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   public double getZ() {
      return this.z;
   }

   public float getYaw() {
      return this.yaw;
   }

   public float getPitch() {
      return this.pitch;
   }

   public Vector getVector() {
      return new Vector(this.x, this.y, this.z);
   }

   public double getWidth() {
      return this.width;
   }

   public int getBlockX() {
      return this.blockX;
   }

   public int getBlockY() {
      return this.blockY;
   }

   public int getBlockZ() {
      return this.blockZ;
   }

   public final boolean isSameBlock(PlayerLocation other) {
      return this.blockX == other.getBlockX() && this.blockZ == other.getBlockZ() && this.blockY == other.getBlockY();
   }

   public final boolean isSameBlock(int x, int y, int z) {
      return this.blockX == x && this.blockZ == z && this.blockY == y;
   }

   public final boolean isSameBlock(Location loc) {
      return this.blockX == loc.getBlockX() && this.blockZ == loc.getBlockZ() && this.blockY == loc.getBlockY();
   }

   public boolean isBlockAbove(PlayerLocation loc) {
      return this.blockY == loc.getBlockY() + 1 && this.blockX == loc.getBlockX() && this.blockZ == loc.getBlockZ();
   }

   public boolean isBlockAbove(Location loc) {
      return this.blockY == loc.getBlockY() + 1 && this.blockX == loc.getBlockX() && this.blockZ == loc.getBlockZ();
   }

   public boolean isSamePos(PlayerLocation loc) {
      return this.x == loc.getX() && this.z == loc.getZ() && this.y == loc.getY();
   }

   public boolean isSamePos(Location loc) {
      return this.x == loc.getX() && this.z == loc.getZ() && this.y == loc.getY();
   }

   public boolean isAboveStairs() {
      if (this.aboveStairs == null) {
         if (this.blockFlags != null && (this.blockFlags & 1L) == 0L) {
            this.aboveStairs = false;
            return false;
         }

         double diff = (double)0.0F;
         this.aboveStairs = BlockProperties.collides(this.blockCache, this.minX - (double)0.0F, this.minY - (double)1.0F, this.minZ - (double)0.0F, this.maxX + (double)0.0F, this.minY + (double)0.25F, this.maxZ + (double)0.0F, 1L);
      }

      return this.aboveStairs;
   }

   public boolean isInLava() {
      if (this.inLava == null) {
         if (this.blockFlags != null && (this.blockFlags & 32L) == 0L) {
            this.inLava = false;
            return false;
         }

         this.inLava = BlockProperties.collides(this.blockCache, this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ, 32L);
      }

      return this.inLava;
   }

   public boolean isInWater() {
      if (this.inWater == null) {
         if (this.blockFlags != null && (this.blockFlags & 16L) == 0L) {
            this.inWater = false;
            return false;
         }

         this.inWater = BlockProperties.collides(this.blockCache, this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ, 16L);
      }

      return this.inWater;
   }

   public boolean isInLiquid() {
      if (this.blockFlags != null && (this.blockFlags & 2L) == 0L) {
         return false;
      } else {
         return this.isInWater() || this.isInLava();
      }
   }

   public boolean isOnIce() {
      if (this.onIce == null) {
         if (!this.player.isSneaking() && !this.player.isBlocking()) {
            this.onIce = this.getTypeIdBelow() == Material.ICE.getId();
         } else {
            this.onIce = this.getTypeId(this.blockX, Location.locToBlock(this.minY - 0.1), this.blockZ) == Material.ICE.getId();
         }
      }

      return this.onIce;
   }

   public boolean isOnClimbable() {
      if (this.onClimbable == null) {
         if (this.blockFlags != null && (this.blockFlags & 512L) == 0L) {
            this.onClimbable = false;
            return false;
         }

         this.onClimbable = (BlockProperties.getBlockFlags(this.getTypeId()) & 512L) != 0L;
      }

      return this.onClimbable;
   }

   public boolean canClimbUp(double jumpHeigth) {
      if (this.getTypeId() == Material.VINE.getId()) {
         if (BlockProperties.canClimbUp(this.blockCache, this.blockX, this.blockY, this.blockZ)) {
            return true;
         } else {
            int headY = Location.locToBlock(this.y + this.player.getEyeHeight());
            if (headY > this.blockY) {
               for(int cy = this.blockY + 1; cy <= headY; ++cy) {
                  if (BlockProperties.canClimbUp(this.blockCache, this.blockX, cy, this.blockZ)) {
                     return true;
                  }
               }
            }

            return this.isOnGround(jumpHeigth);
         }
      } else {
         return true;
      }
   }

   public boolean isAboveLadder() {
      if (this.blockFlags != null && (this.blockFlags & 512L) == 0L) {
         return false;
      } else {
         return (BlockProperties.getBlockFlags(this.getTypeIdBelow()) & 512L) != 0L;
      }
   }

   public boolean isInWeb() {
      if (this.inWeb == null) {
         double inset = 0.001;
         this.inWeb = BlockProperties.collidesId(this.blockCache, this.minX + 0.001, this.minY + 0.001, this.minZ + 0.001, this.maxX - 0.001, this.maxY - 0.001, this.maxZ - 0.001, Material.WEB.getId());
      }

      return this.inWeb;
   }

   public boolean isOnGround() {
      if (this.onGround != null) {
         return this.onGround;
      } else {
         if (this.notOnGroundMaxY >= this.yOnGround) {
            this.onGround = false;
         } else if (this.onGroundMinY <= this.yOnGround) {
            this.onGround = true;
         } else if (this.blockFlags != null && (this.blockFlags & 128L) == 0L) {
            this.onGround = false;
         } else {
            int bY = Location.locToBlock(this.y - this.yOnGround);
            int id = bY == this.blockY ? this.getTypeId() : (bY == this.blockY - 1 ? this.getTypeIdBelow() : this.blockCache.getTypeId(this.blockX, bY, this.blockZ));
            long flags = BlockProperties.getBlockFlags(id);
            if ((flags & 128L) != 0L && (flags & 1024L) == 0L) {
               double[] bounds = this.blockCache.getBounds(this.blockX, bY, this.blockZ);
               if (bounds != null && this.y - (double)bY >= bounds[4] && BlockProperties.collidesBlock(this.blockCache, this.x, this.minY - this.yOnGround, this.z, this.x, this.minY, this.z, this.blockX, bY, this.blockZ, id, bounds, flags) && (!BlockProperties.isPassableWorkaround(this.blockCache, this.blockX, bY, this.blockZ, this.minX - (double)this.blockX, this.minY - this.yOnGround - (double)bY, this.minZ - (double)this.blockZ, id, this.maxX - this.minX, this.yOnGround, this.maxZ - this.minZ, (double)1.0F) || (flags & 4096L) != 0L && BlockProperties.getGroundMinHeight(this.blockCache, this.blockX, bY, this.blockZ, id, bounds, flags) <= this.y - (double)bY)) {
                  this.onGround = true;
               }
            }

            if (this.onGround == null) {
               this.onGround = BlockProperties.isOnGround(this.blockCache, this.minX, this.minY - this.yOnGround, this.minZ, this.maxX, this.minY, this.maxZ, 0L);
            }
         }

         if (this.onGround) {
            this.onGroundMinY = Math.min(this.onGroundMinY, this.yOnGround);
         } else {
            this.notOnGroundMaxY = Math.max(this.notOnGroundMaxY, this.yOnGround);
            double d1 = (double)0.25F;
            this.onGround = this.blockCache.standsOnEntity(this.player, this.minX - (double)0.25F, this.minY - this.yOnGround - (double)0.25F, this.minZ - (double)0.25F, this.maxX + (double)0.25F, this.minY + (double)0.25F + (double)0.25F, this.maxZ + (double)0.25F);
         }

         return this.onGround;
      }
   }

   public boolean isOnGround(double yOnGround) {
      if (this.notOnGroundMaxY >= yOnGround) {
         return false;
      } else {
         return this.onGroundMinY <= yOnGround ? true : this.isOnGround(yOnGround, (double)0.0F, (double)0.0F, 0L);
      }
   }

   public boolean isOnGround(double yOnGround, long ignoreFlags) {
      if (ignoreFlags == 0L) {
         if (this.notOnGroundMaxY >= yOnGround) {
            return false;
         }

         if (this.onGroundMinY <= yOnGround) {
            return true;
         }
      }

      return this.isOnGround(yOnGround, (double)0.0F, (double)0.0F, ignoreFlags);
   }

   public boolean isOnGround(double yOnGround, double xzMargin, double yMargin) {
      if (xzMargin >= (double)0.0F && this.onGroundMinY <= yOnGround) {
         return true;
      } else {
         return xzMargin <= (double)0.0F && yMargin == (double)0.0F && this.notOnGroundMaxY >= yOnGround ? false : this.isOnGround(yOnGround, xzMargin, yMargin, 0L);
      }
   }

   public boolean isOnGround(double yOnGround, double xzMargin, double yMargin, long ignoreFlags) {
      if (ignoreFlags == 0L) {
         if (xzMargin >= (double)0.0F && this.onGroundMinY <= yOnGround) {
            return true;
         }

         if (xzMargin <= (double)0.0F && yMargin == (double)0.0F && this.notOnGroundMaxY >= yOnGround) {
            return false;
         }
      }

      boolean onGround = BlockProperties.isOnGround(this.blockCache, this.minX - xzMargin, this.minY - yOnGround - yMargin, this.minZ - xzMargin, this.maxX + xzMargin, this.minY + yMargin, this.maxZ + xzMargin, ignoreFlags);
      if (ignoreFlags == 0L) {
         if (onGround) {
            if (xzMargin <= (double)0.0F && yMargin == (double)0.0F) {
               this.onGroundMinY = Math.min(this.onGroundMinY, yOnGround);
            }
         } else if (xzMargin >= (double)0.0F) {
            this.notOnGroundMaxY = Math.max(this.notOnGroundMaxY, yOnGround);
         }
      }

      return onGround;
   }

   public boolean standsOnEntity(double yOnGround, double xzMargin, double yMargin) {
      return this.blockCache.standsOnEntity(this.player, this.minX - xzMargin, this.minY - yOnGround - yMargin, this.minZ - xzMargin, this.maxX + xzMargin, this.minY + yMargin, this.maxZ + xzMargin);
   }

   public boolean isNextToSolid(double xzMargin, double yMargin) {
      return BlockProperties.collides(this.blockCache, this.minX - xzMargin, this.minY - yMargin, this.minZ - xzMargin, this.maxX + xzMargin, this.maxY + yMargin, this.maxZ + xzMargin, 4L);
   }

   public boolean isNextToGround(double xzMargin, double yMargin) {
      return BlockProperties.collides(this.blockCache, this.minX - xzMargin, this.minY - yMargin, this.minZ - xzMargin, this.maxX + xzMargin, this.maxY + yMargin, this.maxZ + xzMargin, 128L);
   }

   public boolean isResetCond() {
      return this.isInLiquid() || this.isOnClimbable() || this.isInWeb();
   }

   public double getyOnGround() {
      return this.yOnGround;
   }

   public void setyOnGround(double yOnGround) {
      this.yOnGround = yOnGround;
      this.onGround = null;
      this.blockFlags = null;
   }

   public boolean isPassable() {
      if (this.passable == null) {
         this.passable = BlockProperties.isPassable(this.blockCache, this.x, this.y, this.z, this.getTypeId());
      }

      return this.passable;
   }

   public boolean isDownStream(double xDistance, double zDistance) {
      return BlockProperties.isDownStream(this.blockCache, this.blockX, this.blockY, this.blockZ, this.getData(), xDistance, zDistance);
   }

   public Integer getTypeId() {
      if (this.typeId == null) {
         this.typeId = this.getTypeId(this.blockX, this.blockY, this.blockZ);
      }

      return this.typeId;
   }

   public Integer getTypeIdBelow() {
      if (this.typeIdBelow == null) {
         this.typeIdBelow = this.getTypeId(this.blockX, this.blockY - 1, this.blockZ);
      }

      return this.typeIdBelow;
   }

   public Integer getData() {
      if (this.data == null) {
         this.data = this.getData(this.blockX, this.blockY, this.blockZ);
      }

      return this.data;
   }

   public final int getTypeId(int x, int y, int z) {
      return this.blockCache.getTypeId(x, y, z);
   }

   public final int getData(int x, int y, int z) {
      return this.blockCache.getData(x, y, z);
   }

   public void setBlockCache(BlockCache cache) {
      this.blockCache = cache;
   }

   public final BlockCache getBlockCache() {
      return this.blockCache;
   }

   public void set(Location location, Player player) {
      this.set(location, player, 0.001);
   }

   public void set(Location location, Player player, double yOnGround) {
      this.player = player;
      this.blockX = location.getBlockX();
      this.blockY = location.getBlockY();
      this.blockZ = location.getBlockZ();
      this.x = location.getX();
      this.y = location.getY();
      this.z = location.getZ();
      this.yaw = location.getYaw();
      this.pitch = location.getPitch();
      this.width = this.mcAccess.getWidth(player);
      double dxz = (double)Math.round(this.width * (double)500.0F) / (double)1000.0F;
      this.minX = this.x - dxz;
      this.minY = this.y;
      this.minZ = this.z - dxz;
      this.maxX = this.x + dxz;
      this.maxY = this.y + player.getEyeHeight();
      this.maxZ = this.z + dxz;
      this.world = location.getWorld();
      this.typeId = this.typeIdBelow = this.data = null;
      this.aboveStairs = this.inLava = this.inWater = this.inWeb = this.onGround = this.onIce = this.onClimbable = this.passable = null;
      this.onGroundMinY = Double.MAX_VALUE;
      this.notOnGroundMaxY = Double.MIN_VALUE;
      this.blockFlags = null;
      this.yOnGround = yOnGround;
   }

   public void collectBlockFlags(double maxYonGround) {
      maxYonGround = Math.max(this.yOnGround, maxYonGround);
      double yExtra = 0.6;
      double xzM = (double)0.0F;
      this.blockFlags = BlockProperties.collectFlagsSimple(this.blockCache, this.minX - (double)0.0F, this.minY - 0.6 - maxYonGround, this.minZ - (double)0.0F, this.maxX + (double)0.0F, Math.max(this.maxY, this.minY + (double)1.5F), this.maxZ + (double)0.0F);
   }

   public int ensureChunksLoaded() {
      return this.ensureChunksLoaded((double)1.0F);
   }

   public int ensureChunksLoaded(double xzMargin) {
      return BlockCache.ensureChunksLoaded(this.world, this.x, this.z, xzMargin);
   }

   public void cleanup() {
      this.player = null;
      this.world = null;
      this.blockCache = null;
   }

   public boolean isIllegal() {
      AlmostBoolean spec = this.mcAccess.isIllegalBounds(this.player);
      if (spec != AlmostBoolean.MAYBE) {
         return spec.decide();
      } else {
         return Math.abs(this.minX) > (double)3.2E7F || Math.abs(this.maxX) > (double)3.2E7F || Math.abs(this.minY) > (double)3.2E7F || Math.abs(this.maxY) > (double)3.2E7F || Math.abs(this.minZ) > (double)3.2E7F || Math.abs(this.maxZ) > (double)3.2E7F;
      }
   }

   public Long getBlockFlags() {
      return this.blockFlags;
   }

   public void setBlockFlags(Long blockFlags) {
      this.blockFlags = blockFlags;
   }

   public int getTypeIdAbove() {
      return this.blockCache.getTypeId(this.blockX, this.blockY + 1, this.blockZ);
   }

   public void prepare(PlayerLocation other) {
      this.onGround = other.isOnGround();
      this.inWater = other.isInWater();
      this.inLava = other.isInLava();
      this.inWeb = other.isInWeb();
      this.onClimbable = other.isOnClimbable();
      if (!this.onGround && !this.isResetCond()) {
         this.aboveStairs = other.isAboveStairs();
      }

      this.onIce = other.isOnIce();
      this.typeId = other.getTypeId();
      this.typeIdBelow = other.getTypeIdBelow();
      this.notOnGroundMaxY = other.notOnGroundMaxY;
      this.onGroundMinY = other.onGroundMinY;
      this.blockFlags = other.blockFlags;
   }
}
