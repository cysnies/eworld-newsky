package com.sk89q.worldedit;

import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.blocks.ChestBlock;
import com.sk89q.worldedit.blocks.DispenserBlock;
import com.sk89q.worldedit.blocks.FurnaceBlock;
import com.sk89q.worldedit.blocks.MobSpawnerBlock;
import com.sk89q.worldedit.blocks.NoteBlock;
import com.sk89q.worldedit.blocks.SignBlock;
import com.sk89q.worldedit.blocks.SkullBlock;
import com.sk89q.worldedit.foundation.Block;
import com.sk89q.worldedit.foundation.World;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.TreeGenerator;
import java.util.PriorityQueue;
import java.util.Random;

public abstract class LocalWorld implements World {
   protected Random random = new Random();
   private final PriorityQueue effectQueue = new PriorityQueue();
   private int taskId = -1;

   public abstract String getName();

   /** @deprecated */
   @Deprecated
   public abstract boolean setBlockType(Vector var1, int var2);

   /** @deprecated */
   @Deprecated
   public boolean setBlockTypeFast(Vector pt, int type) {
      return this.setBlockType(pt, type);
   }

   public abstract int getBlockType(Vector var1);

   /** @deprecated */
   @Deprecated
   public abstract void setBlockData(Vector var1, int var2);

   /** @deprecated */
   @Deprecated
   public abstract void setBlockDataFast(Vector var1, int var2);

   public abstract BiomeType getBiome(Vector2D var1);

   public abstract void setBiome(Vector2D var1, BiomeType var2);

   /** @deprecated */
   @Deprecated
   public boolean setTypeIdAndData(Vector pt, int type, int data) {
      boolean ret = this.setBlockType(pt, type);
      this.setBlockData(pt, data);
      return ret;
   }

   /** @deprecated */
   @Deprecated
   public boolean setTypeIdAndDataFast(Vector pt, int type, int data) {
      boolean ret = this.setBlockTypeFast(pt, type);
      this.setBlockDataFast(pt, data);
      return ret;
   }

   public abstract int getBlockData(Vector var1);

   public abstract int getBlockLightLevel(Vector var1);

   public abstract boolean regenerate(Region var1, EditSession var2);

   public abstract boolean copyToWorld(Vector var1, BaseBlock var2);

   public abstract boolean copyFromWorld(Vector var1, BaseBlock var2);

   public abstract boolean clearContainerBlockContents(Vector var1);

   /** @deprecated */
   @Deprecated
   public boolean generateTree(EditSession editSession, Vector pt) throws MaxChangedBlocksException {
      return false;
   }

   /** @deprecated */
   @Deprecated
   public boolean generateBigTree(EditSession editSession, Vector pt) throws MaxChangedBlocksException {
      return false;
   }

   /** @deprecated */
   @Deprecated
   public boolean generateBirchTree(EditSession editSession, Vector pt) throws MaxChangedBlocksException {
      return false;
   }

   /** @deprecated */
   @Deprecated
   public boolean generateRedwoodTree(EditSession editSession, Vector pt) throws MaxChangedBlocksException {
      return false;
   }

   /** @deprecated */
   @Deprecated
   public boolean generateTallRedwoodTree(EditSession editSession, Vector pt) throws MaxChangedBlocksException {
      return false;
   }

   public boolean generateTree(TreeGenerator.TreeType type, EditSession editSession, Vector pt) throws MaxChangedBlocksException {
      switch (type) {
         case BIG_TREE:
            return this.generateBigTree(editSession, pt);
         case BIRCH:
            return this.generateBirchTree(editSession, pt);
         case REDWOOD:
            return this.generateRedwoodTree(editSession, pt);
         case TALL_REDWOOD:
            return this.generateTallRedwoodTree(editSession, pt);
         case TREE:
         default:
            return this.generateTree(editSession, pt);
      }
   }

   public void dropItem(Vector pt, BaseItemStack item, int times) {
      for(int i = 0; i < times; ++i) {
         this.dropItem(pt, item);
      }

   }

   public abstract void dropItem(Vector var1, BaseItemStack var2);

   public void simulateBlockMine(Vector pt) {
      BaseItemStack stack = BlockType.getBlockDrop(this.getBlockType(pt), (short)this.getBlockData(pt));
      if (stack != null) {
         int amount = stack.getAmount();
         if (amount > 1) {
            this.dropItem(pt, new BaseItemStack(stack.getType(), 1, stack.getData()), amount);
         } else {
            this.dropItem(pt, stack, amount);
         }

      }
   }

   /** @deprecated */
   @Deprecated
   public int killMobs(Vector origin, int radius) {
      return this.killMobs(origin, radius, false);
   }

   /** @deprecated */
   @Deprecated
   public int killMobs(Vector origin, int radius, boolean killPets) {
      return this.killMobs(origin, (double)radius, killPets ? 1 : 0);
   }

   public int killMobs(Vector origin, double radius, int flags) {
      return this.killMobs(origin, (int)radius, (flags & 1) != 0);
   }

   public abstract int removeEntities(EntityType var1, Vector var2, int var3);

   public boolean isValidBlockType(int type) {
      return BlockType.fromID(type) != null;
   }

   public boolean usesBlockData(int type) {
      return BlockType.usesData(type) || BlockType.fromID(type) == null;
   }

   public void checkLoadedChunk(Vector pt) {
   }

   public abstract boolean equals(Object var1);

   public abstract int hashCode();

   public int getMaxY() {
      return 255;
   }

   public void fixAfterFastMode(Iterable chunks) {
   }

   public void fixLighting(Iterable chunks) {
   }

   public boolean playEffect(Vector position, int type, int data) {
      return false;
   }

   public boolean queueBlockBreakEffect(ServerInterface server, Vector position, int blockId, double priority) {
      if (this.taskId == -1) {
         this.taskId = server.schedule(0L, 1L, new Runnable() {
            public void run() {
               int max = Math.max(1, Math.min(30, LocalWorld.this.effectQueue.size() / 3));

               for(int i = 0; i < max; ++i) {
                  if (LocalWorld.this.effectQueue.isEmpty()) {
                     return;
                  }

                  ((QueuedEffect)LocalWorld.this.effectQueue.poll()).play();
               }

            }
         });
      }

      if (this.taskId == -1) {
         return false;
      } else {
         this.effectQueue.offer(new QueuedEffect(position, blockId, priority));
         return true;
      }
   }

   public LocalEntity[] getEntities(Region region) {
      return new LocalEntity[0];
   }

   public int killEntities(LocalEntity... entities) {
      return 0;
   }

   public boolean setBlock(Vector pt, Block block, boolean notifyAdjacent) {
      boolean successful;
      if (notifyAdjacent) {
         successful = this.setTypeIdAndData(pt, block.getId(), block.getData());
      } else {
         successful = this.setTypeIdAndDataFast(pt, block.getId(), block.getData());
      }

      if (block instanceof BaseBlock) {
         this.copyToWorld(pt, (BaseBlock)block);
      }

      return successful;
   }

   public BaseBlock getBlock(Vector pt) {
      this.checkLoadedChunk(pt);
      int type = this.getBlockType(pt);
      int data = this.getBlockData(pt);
      switch (type) {
         case 23:
            DispenserBlock block = new DispenserBlock(data);
            this.copyFromWorld(pt, block);
            return block;
         case 25:
            NoteBlock block = new NoteBlock(data);
            this.copyFromWorld(pt, block);
            return block;
         case 52:
            MobSpawnerBlock block = new MobSpawnerBlock(data);
            this.copyFromWorld(pt, block);
            return block;
         case 54:
            ChestBlock block = new ChestBlock(data);
            this.copyFromWorld(pt, block);
            return block;
         case 61:
         case 62:
            FurnaceBlock block = new FurnaceBlock(type, data);
            this.copyFromWorld(pt, block);
            return block;
         case 63:
         case 68:
            SignBlock block = new SignBlock(type, data);
            this.copyFromWorld(pt, block);
            return block;
         case 144:
            SkullBlock block = new SkullBlock(data);
            this.copyFromWorld(pt, block);
            return block;
         default:
            return new BaseBlock(type, data);
      }
   }

   public class KillFlags {
      public static final int PETS = 1;
      public static final int NPCS = 2;
      public static final int ANIMALS = 4;
      public static final int GOLEMS = 8;
      public static final int AMBIENT = 16;
      public static final int FRIENDLY = 31;
      public static final int WITH_LIGHTNING = 1048576;
   }

   private class QueuedEffect implements Comparable {
      private final Vector position;
      private final int blockId;
      private final double priority;

      public QueuedEffect(Vector position, int blockId, double priority) {
         this.position = position;
         this.blockId = blockId;
         this.priority = priority;
      }

      public void play() {
         LocalWorld.this.playEffect(this.position, 2001, this.blockId);
      }

      public int compareTo(QueuedEffect other) {
         return Double.compare(this.priority, other.priority);
      }
   }
}
