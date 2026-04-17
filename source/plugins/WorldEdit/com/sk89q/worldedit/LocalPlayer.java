package com.sk89q.worldedit;

import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.cui.CUIEvent;
import com.sk89q.worldedit.util.TargetBlock;
import java.io.File;

public abstract class LocalPlayer {
   protected ServerInterface server;

   protected LocalPlayer(ServerInterface server) {
      this.server = server;
   }

   public boolean isHoldingPickAxe() {
      int item = this.getItemInHand();
      return item == 257 || item == 270 || item == 274 || item == 278 || item == 285;
   }

   public void findFreePosition(WorldVector searchPos) {
      LocalWorld world = searchPos.getWorld();
      int x = searchPos.getBlockX();
      int y = Math.max(0, searchPos.getBlockY());
      int origY = y;
      int z = searchPos.getBlockZ();

      for(byte free = 0; y <= world.getMaxY() + 2; ++y) {
         if (BlockType.canPassThrough(world.getBlockType(new Vector(x, y, z)))) {
            ++free;
         } else {
            free = 0;
         }

         if (free == 2) {
            if (y - 1 != origY) {
               Vector pos = new Vector(x, y - 2, z);
               int id = world.getBlockType(pos);
               int data = world.getBlockData(pos);
               this.setPosition(new Vector((double)x + (double)0.5F, (double)(y - 2) + BlockType.centralTopLimit(id, data), (double)z + (double)0.5F));
            }

            return;
         }
      }

   }

   public void setOnGround(WorldVector searchPos) {
      LocalWorld world = searchPos.getWorld();
      int x = searchPos.getBlockX();
      int y = Math.max(0, searchPos.getBlockY());

      for(int z = searchPos.getBlockZ(); y >= 0; --y) {
         Vector pos = new Vector(x, y, z);
         int id = world.getBlockType(pos);
         if (!BlockType.canPassThrough(id)) {
            int data = world.getBlockData(pos);
            this.setPosition(new Vector((double)x + (double)0.5F, (double)y + BlockType.centralTopLimit(id, data), (double)z + (double)0.5F));
            return;
         }
      }

   }

   public void findFreePosition() {
      this.findFreePosition(this.getBlockIn());
   }

   public boolean ascendLevel() {
      Vector pos = this.getBlockIn();
      int x = pos.getBlockX();
      int y = Math.max(0, pos.getBlockY());
      int z = pos.getBlockZ();
      LocalWorld world = this.getPosition().getWorld();
      byte free = 0;

      for(byte spots = 0; y <= world.getMaxY() + 2; ++y) {
         if (BlockType.canPassThrough(world.getBlockType(new Vector(x, y, z)))) {
            ++free;
         } else {
            free = 0;
         }

         if (free == 2) {
            ++spots;
            if (spots == 2) {
               int type = world.getBlockType(new Vector(x, y - 2, z));
               if (type != 10 && type != 11) {
                  this.setPosition(new Vector((double)x + (double)0.5F, (double)(y - 1), (double)z + (double)0.5F));
                  return true;
               }

               return false;
            }
         }
      }

      return false;
   }

   public boolean descendLevel() {
      Vector pos = this.getBlockIn();
      int x = pos.getBlockX();
      int y = Math.max(0, pos.getBlockY() - 1);
      int z = pos.getBlockZ();
      LocalWorld world = this.getPosition().getWorld();

      for(byte free = 0; y >= 1; --y) {
         if (BlockType.canPassThrough(world.getBlockType(new Vector(x, y, z)))) {
            ++free;
         } else {
            free = 0;
         }

         if (free == 2) {
            while(y >= 0) {
               int type = world.getBlockType(new Vector(x, y, z));
               if (type != 0 && type != 10 && type != 11) {
                  this.setPosition(new Vector((double)x + (double)0.5F, (double)(y + 1), (double)z + (double)0.5F));
                  return true;
               }

               --y;
            }

            return false;
         }
      }

      return false;
   }

   public boolean ascendToCeiling(int clearance) {
      Vector pos = this.getBlockIn();
      int x = pos.getBlockX();
      int initialY = Math.max(0, pos.getBlockY());
      int y = Math.max(0, pos.getBlockY() + 2);
      int z = pos.getBlockZ();
      LocalWorld world = this.getPosition().getWorld();
      if (world.getBlockType(new Vector(x, y, z)) != 0) {
         return false;
      } else {
         while(y <= world.getMaxY()) {
            if (!BlockType.canPassThrough(world.getBlockType(new Vector(x, y, z)))) {
               int platformY = Math.max(initialY, y - 3 - clearance);
               world.setBlockType(new Vector(x, platformY, z), 20);
               this.setPosition(new Vector((double)x + (double)0.5F, (double)(platformY + 1), (double)z + (double)0.5F));
               return true;
            }

            ++y;
         }

         return false;
      }
   }

   public boolean ascendUpwards(int distance) {
      Vector pos = this.getBlockIn();
      int x = pos.getBlockX();
      int initialY = Math.max(0, pos.getBlockY());
      int y = Math.max(0, pos.getBlockY() + 1);
      int z = pos.getBlockZ();
      int maxY = Math.min(this.getWorld().getMaxY() + 1, initialY + distance);

      for(LocalWorld world = this.getPosition().getWorld(); y <= world.getMaxY() + 2 && BlockType.canPassThrough(world.getBlockType(new Vector(x, y, z))) && y <= maxY + 1; ++y) {
         if (y == maxY + 1) {
            world.setBlockType(new Vector(x, y - 2, z), 20);
            this.setPosition(new Vector((double)x + (double)0.5F, (double)(y - 1), (double)z + (double)0.5F));
            return true;
         }
      }

      return false;
   }

   public WorldVector getBlockIn() {
      WorldVector pos = this.getPosition();
      return WorldVector.toBlockPoint(pos.getWorld(), pos.getX(), pos.getY(), pos.getZ());
   }

   public WorldVector getBlockOn() {
      WorldVector pos = this.getPosition();
      return WorldVector.toBlockPoint(pos.getWorld(), pos.getX(), pos.getY() - (double)1.0F, pos.getZ());
   }

   public WorldVector getBlockTrace(int range, boolean useLastBlock) {
      TargetBlock tb = new TargetBlock(this, range, 0.2);
      return useLastBlock ? tb.getAnyTargetBlock() : tb.getTargetBlock();
   }

   public WorldVectorFace getBlockTraceFace(int range, boolean useLastBlock) {
      TargetBlock tb = new TargetBlock(this, range, 0.2);
      return useLastBlock ? tb.getAnyTargetBlockFace() : tb.getTargetBlockFace();
   }

   public WorldVector getBlockTrace(int range) {
      return this.getBlockTrace(range, false);
   }

   public WorldVector getSolidBlockTrace(int range) {
      TargetBlock tb = new TargetBlock(this, range, 0.2);
      return tb.getSolidTargetBlock();
   }

   public PlayerDirection getCardinalDirection() {
      return this.getCardinalDirection(0);
   }

   public PlayerDirection getCardinalDirection(int yawOffset) {
      if (this.getPitch() > (double)67.5F) {
         return PlayerDirection.DOWN;
      } else if (this.getPitch() < (double)-67.5F) {
         return PlayerDirection.UP;
      } else {
         double rot = (this.getYaw() + (double)yawOffset) % (double)360.0F;
         if (rot < (double)0.0F) {
            rot += (double)360.0F;
         }

         return getDirection(rot);
      }
   }

   private static PlayerDirection getDirection(double rot) {
      if ((double)0.0F <= rot && rot < (double)22.5F) {
         return PlayerDirection.SOUTH;
      } else if ((double)22.5F <= rot && rot < (double)67.5F) {
         return PlayerDirection.SOUTH_WEST;
      } else if ((double)67.5F <= rot && rot < (double)112.5F) {
         return PlayerDirection.WEST;
      } else if ((double)112.5F <= rot && rot < (double)157.5F) {
         return PlayerDirection.NORTH_WEST;
      } else if ((double)157.5F <= rot && rot < (double)202.5F) {
         return PlayerDirection.NORTH;
      } else if ((double)202.5F <= rot && rot < (double)247.5F) {
         return PlayerDirection.NORTH_EAST;
      } else if ((double)247.5F <= rot && rot < (double)292.5F) {
         return PlayerDirection.EAST;
      } else if ((double)292.5F <= rot && rot < (double)337.5F) {
         return PlayerDirection.SOUTH_EAST;
      } else {
         return (double)337.5F <= rot && rot < (double)360.0F ? PlayerDirection.SOUTH : null;
      }
   }

   public abstract int getItemInHand();

   public abstract String getName();

   public abstract WorldVector getPosition();

   public abstract LocalWorld getWorld();

   public abstract double getPitch();

   public abstract double getYaw();

   public abstract void giveItem(int var1, int var2);

   public boolean passThroughForwardWall(int range) {
      int searchDist = 0;
      TargetBlock hitBlox = new TargetBlock(this, range, 0.2);
      LocalWorld world = this.getPosition().getWorld();
      boolean firstBlock = true;
      int freeToFind = 2;
      boolean inFree = false;

      BlockWorldVector block;
      while((block = hitBlox.getNextBlock()) != null) {
         boolean free = BlockType.canPassThrough(world.getBlockType(block));
         if (firstBlock) {
            firstBlock = false;
            if (!free) {
               --freeToFind;
               continue;
            }
         }

         ++searchDist;
         if (searchDist > 20) {
            return false;
         }

         if (inFree != free && free) {
            --freeToFind;
         }

         if (freeToFind == 0) {
            this.setOnGround(block);
            return true;
         }

         inFree = free;
      }

      return false;
   }

   public abstract void printRaw(String var1);

   public abstract void printDebug(String var1);

   public abstract void print(String var1);

   public abstract void printError(String var1);

   public abstract void setPosition(Vector var1, float var2, float var3);

   public void setPosition(Vector pos) {
      this.setPosition(pos, (float)this.getPitch(), (float)this.getYaw());
   }

   public abstract String[] getGroups();

   public abstract BlockBag getInventoryBlockBag();

   public abstract boolean hasPermission(String var1);

   public File openFileOpenDialog(String[] extensions) {
      this.printError("File dialogs are not supported in your environment.");
      return null;
   }

   public File openFileSaveDialog(String[] extensions) {
      this.printError("File dialogs are not supported in your environment.");
      return null;
   }

   public boolean canDestroyBedrock() {
      return this.hasPermission("worldedit.override.bedrock");
   }

   public void dispatchCUIEvent(CUIEvent event) {
   }

   /** @deprecated */
   @Deprecated
   public void dispatchCUIHandshake() {
   }

   public boolean equals(Object other) {
      if (!(other instanceof LocalPlayer)) {
         return false;
      } else {
         LocalPlayer other2 = (LocalPlayer)other;
         return other2.getName().equals(this.getName());
      }
   }

   public int hashCode() {
      return this.getName().hashCode();
   }

   public void checkPermission(String permission) throws WorldEditPermissionException {
      if (!this.hasPermission(permission)) {
         throw new WorldEditPermissionException();
      }
   }

   public boolean isPlayer() {
      return true;
   }

   public boolean hasCreativeMode() {
      return false;
   }
}
