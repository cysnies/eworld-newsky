package com.sk89q.worldedit;

import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.bags.BlockBagException;
import com.sk89q.worldedit.bags.UnplaceableBlockException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.expression.Expression;
import com.sk89q.worldedit.expression.ExpressionException;
import com.sk89q.worldedit.expression.runtime.RValue;
import com.sk89q.worldedit.masks.Mask;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.util.TreeGenerator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

public class EditSession {
   private static Random prng = new Random();
   protected LocalWorld world;
   private DoubleArrayList original = new DoubleArrayList(true);
   private DoubleArrayList current = new DoubleArrayList(false);
   private DoubleArrayList queueAfter = new DoubleArrayList(false);
   private DoubleArrayList queueLast = new DoubleArrayList(false);
   private DoubleArrayList queueFinal = new DoubleArrayList(false);
   private int maxBlocks = -1;
   private boolean queued = false;
   private boolean fastMode = false;
   private BlockBag blockBag;
   private Map missingBlocks = new HashMap();
   private Mask mask;
   Vector[] recurseDirections;

   public EditSession(LocalWorld world, int maxBlocks) {
      this.recurseDirections = new Vector[]{PlayerDirection.NORTH.vector(), PlayerDirection.EAST.vector(), PlayerDirection.SOUTH.vector(), PlayerDirection.WEST.vector(), PlayerDirection.UP.vector(), PlayerDirection.DOWN.vector()};
      if (maxBlocks < -1) {
         throw new IllegalArgumentException("Max blocks must be >= -1");
      } else {
         this.maxBlocks = maxBlocks;
         this.world = world;
      }
   }

   public EditSession(LocalWorld world, int maxBlocks, BlockBag blockBag) {
      this.recurseDirections = new Vector[]{PlayerDirection.NORTH.vector(), PlayerDirection.EAST.vector(), PlayerDirection.SOUTH.vector(), PlayerDirection.WEST.vector(), PlayerDirection.UP.vector(), PlayerDirection.DOWN.vector()};
      if (maxBlocks < -1) {
         throw new IllegalArgumentException("Max blocks must be >= -1");
      } else {
         this.maxBlocks = maxBlocks;
         this.blockBag = blockBag;
         this.world = world;
      }
   }

   public boolean rawSetBlock(Vector pt, BaseBlock block) {
      int y = pt.getBlockY();
      int type = block.getType();
      if (y >= 0 && y <= this.world.getMaxY()) {
         this.world.checkLoadedChunk(pt);
         if (!this.world.isValidBlockType(type)) {
            return false;
         } else if (this.mask != null && !this.mask.matches(this, pt)) {
            return false;
         } else {
            int existing = this.world.getBlockType(pt);
            if (BlockType.isContainerBlock(existing)) {
               this.world.clearContainerBlockContents(pt);
            } else if (existing == 79) {
               this.world.setBlockType(pt, 0);
            }

            if (this.blockBag != null) {
               if (type > 0) {
                  try {
                     this.blockBag.fetchPlacedBlock(type, 0);
                  } catch (UnplaceableBlockException var8) {
                     return false;
                  } catch (BlockBagException var9) {
                     if (!this.missingBlocks.containsKey(type)) {
                        this.missingBlocks.put(type, 1);
                     } else {
                        this.missingBlocks.put(type, (Integer)this.missingBlocks.get(type) + 1);
                     }

                     return false;
                  }
               }

               if (existing > 0) {
                  try {
                     this.blockBag.storeDroppedBlock(existing, this.world.getBlockData(pt));
                  } catch (BlockBagException var7) {
                  }
               }
            }

            boolean result;
            if (type == 0) {
               if (this.fastMode) {
                  result = this.world.setBlockTypeFast(pt, 0);
               } else {
                  result = this.world.setBlockType(pt, 0);
               }
            } else {
               result = this.world.setBlock(pt, block, !this.fastMode);
            }

            return result;
         }
      } else {
         return false;
      }
   }

   public boolean setBlock(Vector pt, BaseBlock block) throws MaxChangedBlocksException {
      BlockVector blockPt = pt.toBlockVector();
      this.original.put(blockPt, this.getBlock(pt));
      if (this.maxBlocks != -1 && this.original.size() > this.maxBlocks) {
         throw new MaxChangedBlocksException(this.maxBlocks);
      } else {
         this.current.put(pt.toBlockVector(), block);
         return this.smartSetBlock(pt, block);
      }
   }

   public void rememberChange(Vector pt, BaseBlock existing, BaseBlock block) {
      BlockVector blockPt = pt.toBlockVector();
      this.original.put(blockPt, existing);
      this.current.put(pt.toBlockVector(), block);
   }

   public boolean setBlock(Vector pt, Pattern pat) throws MaxChangedBlocksException {
      return this.setBlock(pt, pat.next(pt));
   }

   public boolean setBlockIfAir(Vector pt, BaseBlock block) throws MaxChangedBlocksException {
      return !this.getBlock(pt).isAir() ? false : this.setBlock(pt, block);
   }

   public boolean smartSetBlock(Vector pt, BaseBlock block) {
      if (this.queued) {
         if (BlockType.shouldPlaceLast(block.getType())) {
            this.queueLast.put(pt.toBlockVector(), block);
            return this.getBlockType(pt) != block.getType() || this.getBlockData(pt) != block.getData();
         }

         if (BlockType.shouldPlaceFinal(block.getType())) {
            this.queueFinal.put(pt.toBlockVector(), block);
            return this.getBlockType(pt) != block.getType() || this.getBlockData(pt) != block.getData();
         }

         if (!BlockType.shouldPlaceLast(this.getBlockType(pt))) {
            this.queueAfter.put(pt.toBlockVector(), block);
            return this.getBlockType(pt) != block.getType() || this.getBlockData(pt) != block.getData();
         }

         this.rawSetBlock(pt, new BaseBlock(0));
      }

      return this.rawSetBlock(pt, block);
   }

   public BaseBlock getBlock(Vector pt) {
      if (this.queued) {
      }

      return this.rawGetBlock(pt);
   }

   public int getBlockType(Vector pt) {
      if (this.queued) {
      }

      return this.world.getBlockType(pt);
   }

   public int getBlockData(Vector pt) {
      if (this.queued) {
      }

      return this.world.getBlockData(pt);
   }

   public BaseBlock rawGetBlock(Vector pt) {
      return this.world.getBlock(pt);
   }

   public void undo(EditSession sess) {
      for(Map.Entry entry : this.original) {
         BlockVector pt = (BlockVector)entry.getKey();
         sess.smartSetBlock(pt, (BaseBlock)entry.getValue());
      }

      sess.flushQueue();
   }

   public void redo(EditSession sess) {
      for(Map.Entry entry : this.current) {
         BlockVector pt = (BlockVector)entry.getKey();
         sess.smartSetBlock(pt, (BaseBlock)entry.getValue());
      }

      sess.flushQueue();
   }

   public int size() {
      return this.original.size();
   }

   public int getBlockChangeLimit() {
      return this.maxBlocks;
   }

   public void setBlockChangeLimit(int maxBlocks) {
      if (maxBlocks < -1) {
         throw new IllegalArgumentException("Max blocks must be >= -1");
      } else {
         this.maxBlocks = maxBlocks;
      }
   }

   public boolean isQueueEnabled() {
      return this.queued;
   }

   public void enableQueue() {
      this.queued = true;
   }

   public void disableQueue() {
      if (this.queued) {
         this.flushQueue();
      }

      this.queued = false;
   }

   public void setFastMode(boolean fastMode) {
      this.fastMode = fastMode;
   }

   public boolean hasFastMode() {
      return this.fastMode;
   }

   public boolean setChanceBlockIfAir(Vector pos, BaseBlock block, double c) throws MaxChangedBlocksException {
      return Math.random() <= c ? this.setBlockIfAir(pos, block) : false;
   }

   public int countBlock(Region region, Set searchIDs) {
      Set<BaseBlock> passOn = new HashSet();

      for(Integer i : searchIDs) {
         passOn.add(new BaseBlock(i, -1));
      }

      return this.countBlocks(region, passOn);
   }

   private static boolean containsFuzzy(Collection collection, Object o) {
      for(BaseBlock b : collection) {
         if (o instanceof BaseBlock && b.equalsFuzzy((BaseBlock)o)) {
            return true;
         }
      }

      return false;
   }

   public int countBlocks(Region region, Set searchBlocks) {
      int count = 0;
      if (region instanceof CuboidRegion) {
         Vector min = region.getMinimumPoint();
         Vector max = region.getMaximumPoint();
         int minX = min.getBlockX();
         int minY = min.getBlockY();
         int minZ = min.getBlockZ();
         int maxX = max.getBlockX();
         int maxY = max.getBlockY();
         int maxZ = max.getBlockZ();

         for(int x = minX; x <= maxX; ++x) {
            for(int y = minY; y <= maxY; ++y) {
               for(int z = minZ; z <= maxZ; ++z) {
                  Vector pt = new Vector(x, y, z);
                  BaseBlock compare = new BaseBlock(this.getBlockType(pt), this.getBlockData(pt));
                  if (containsFuzzy(searchBlocks, compare)) {
                     ++count;
                  }
               }
            }
         }
      } else {
         for(Vector pt : region) {
            BaseBlock compare = new BaseBlock(this.getBlockType(pt), this.getBlockData(pt));
            if (containsFuzzy(searchBlocks, compare)) {
               ++count;
            }
         }
      }

      return count;
   }

   public int getHighestTerrainBlock(int x, int z, int minY, int maxY) {
      return this.getHighestTerrainBlock(x, z, minY, maxY, false);
   }

   public int getHighestTerrainBlock(int x, int z, int minY, int maxY, boolean naturalOnly) {
      int y = maxY;

      while(true) {
         if (y < minY) {
            return minY;
         }

         Vector pt = new Vector(x, y, z);
         int id = this.getBlockType(pt);
         if (naturalOnly) {
            if (BlockType.isNaturalTerrainBlock(id)) {
               break;
            }
         } else if (!BlockType.canPassThrough(id)) {
            break;
         }

         --y;
      }

      return y;
   }

   public Map popMissingBlocks() {
      Map<Integer, Integer> missingBlocks = this.missingBlocks;
      this.missingBlocks = new HashMap();
      return missingBlocks;
   }

   public BlockBag getBlockBag() {
      return this.blockBag;
   }

   public void setBlockBag(BlockBag blockBag) {
      this.blockBag = blockBag;
   }

   public LocalWorld getWorld() {
      return this.world;
   }

   public int getBlockChangeCount() {
      return this.original.size();
   }

   public Mask getMask() {
      return this.mask;
   }

   public void setMask(Mask mask) {
      this.mask = mask;
   }

   public void flushQueue() {
      if (this.queued) {
         Set<BlockVector2D> dirtyChunks = new HashSet();

         for(Map.Entry entry : this.queueAfter) {
            BlockVector pt = (BlockVector)entry.getKey();
            this.rawSetBlock(pt, (BaseBlock)entry.getValue());
            if (this.fastMode) {
               dirtyChunks.add(new BlockVector2D(pt.getBlockX() >> 4, pt.getBlockZ() >> 4));
            }
         }

         if (this.blockBag == null || this.missingBlocks.size() == 0) {
            for(Map.Entry entry : this.queueLast) {
               BlockVector pt = (BlockVector)entry.getKey();
               this.rawSetBlock(pt, (BaseBlock)entry.getValue());
               if (this.fastMode) {
                  dirtyChunks.add(new BlockVector2D(pt.getBlockX() >> 4, pt.getBlockZ() >> 4));
               }
            }

            Set<BlockVector> blocks = new HashSet();
            Map<BlockVector, BaseBlock> blockTypes = new HashMap();

            for(Map.Entry entry : this.queueFinal) {
               BlockVector pt = (BlockVector)entry.getKey();
               blocks.add(pt);
               blockTypes.put(pt, entry.getValue());
            }

            while(!blocks.isEmpty()) {
               BlockVector current = (BlockVector)blocks.iterator().next();
               if (blocks.contains(current)) {
                  Deque<BlockVector> walked = new LinkedList();

                  do {
                     walked.addFirst(current);

                     assert blockTypes.containsKey(current);

                     BaseBlock baseBlock = (BaseBlock)blockTypes.get(current);
                     int type = baseBlock.getType();
                     int data = baseBlock.getData();
                     switch (type) {
                        case 64:
                        case 71:
                           if ((data & 8) == 0) {
                              BlockVector upperBlock = current.add(0, 1, 0).toBlockVector();
                              if (blocks.contains(upperBlock) && !walked.contains(upperBlock)) {
                                 walked.addFirst(upperBlock);
                              }
                           }
                     }

                     PlayerDirection attachment = BlockType.getAttachment(type, data);
                     if (attachment == null) {
                        break;
                     }

                     current = current.add(attachment.vector()).toBlockVector();
                  } while(blocks.contains(current) && !walked.contains(current));

                  for(BlockVector pt : walked) {
                     this.rawSetBlock(pt, (BaseBlock)blockTypes.get(pt));
                     blocks.remove(pt);
                     if (this.fastMode) {
                        dirtyChunks.add(new BlockVector2D(pt.getBlockX() >> 4, pt.getBlockZ() >> 4));
                     }
                  }
               }
            }
         }

         if (!dirtyChunks.isEmpty()) {
            this.world.fixAfterFastMode(dirtyChunks);
         }

         this.queueAfter.clear();
         this.queueLast.clear();
         this.queueFinal.clear();
      }
   }

   public int fillXZ(Vector origin, BaseBlock block, double radius, int depth, boolean recursive) throws MaxChangedBlocksException {
      int affected = 0;
      int originX = origin.getBlockX();
      int originY = origin.getBlockY();
      int originZ = origin.getBlockZ();
      HashSet<BlockVector> visited = new HashSet();
      Stack<BlockVector> queue = new Stack();
      queue.push(new BlockVector(originX, originY, originZ));

      while(!queue.empty()) {
         BlockVector pt = (BlockVector)queue.pop();
         int cx = pt.getBlockX();
         int cy = pt.getBlockY();
         int cz = pt.getBlockZ();
         if (cy >= 0 && cy <= originY && !visited.contains(pt)) {
            visited.add(pt);
            if (recursive) {
               if (origin.distance(pt) > radius || !this.getBlock(pt).isAir()) {
                  continue;
               }

               if (this.setBlock(pt, (BaseBlock)block)) {
                  ++affected;
               }

               queue.push(new BlockVector(cx, cy - 1, cz));
               queue.push(new BlockVector(cx, cy + 1, cz));
            } else {
               double dist = Math.sqrt(Math.pow((double)(originX - cx), (double)2.0F) + Math.pow((double)(originZ - cz), (double)2.0F));
               int minY = originY - depth + 1;
               if (dist > radius || !this.getBlock(pt).isAir()) {
                  continue;
               }

               affected += this.fillY(cx, originY, cz, block, minY);
            }

            queue.push(new BlockVector(cx + 1, cy, cz));
            queue.push(new BlockVector(cx - 1, cy, cz));
            queue.push(new BlockVector(cx, cy, cz + 1));
            queue.push(new BlockVector(cx, cy, cz - 1));
         }
      }

      return affected;
   }

   private int fillY(int x, int cy, int z, BaseBlock block, int minY) throws MaxChangedBlocksException {
      int affected = 0;

      for(int y = cy; y >= minY; --y) {
         Vector pt = new Vector(x, y, z);
         if (!this.getBlock(pt).isAir()) {
            break;
         }

         this.setBlock(pt, block);
         ++affected;
      }

      return affected;
   }

   public int fillXZ(Vector origin, Pattern pattern, double radius, int depth, boolean recursive) throws MaxChangedBlocksException {
      int affected = 0;
      int originX = origin.getBlockX();
      int originY = origin.getBlockY();
      int originZ = origin.getBlockZ();
      HashSet<BlockVector> visited = new HashSet();
      Stack<BlockVector> queue = new Stack();
      queue.push(new BlockVector(originX, originY, originZ));

      while(!queue.empty()) {
         BlockVector pt = (BlockVector)queue.pop();
         int cx = pt.getBlockX();
         int cy = pt.getBlockY();
         int cz = pt.getBlockZ();
         if (cy >= 0 && cy <= originY && !visited.contains(pt)) {
            visited.add(pt);
            if (recursive) {
               if (origin.distance(pt) > radius || !this.getBlock(pt).isAir()) {
                  continue;
               }

               if (this.setBlock(pt, (BaseBlock)pattern.next(pt))) {
                  ++affected;
               }

               queue.push(new BlockVector(cx, cy - 1, cz));
               queue.push(new BlockVector(cx, cy + 1, cz));
            } else {
               double dist = Math.sqrt(Math.pow((double)(originX - cx), (double)2.0F) + Math.pow((double)(originZ - cz), (double)2.0F));
               int minY = originY - depth + 1;
               if (dist > radius || !this.getBlock(pt).isAir()) {
                  continue;
               }

               affected += this.fillY(cx, originY, cz, pattern, minY);
            }

            queue.push(new BlockVector(cx + 1, cy, cz));
            queue.push(new BlockVector(cx - 1, cy, cz));
            queue.push(new BlockVector(cx, cy, cz + 1));
            queue.push(new BlockVector(cx, cy, cz - 1));
         }
      }

      return affected;
   }

   private int fillY(int x, int cy, int z, Pattern pattern, int minY) throws MaxChangedBlocksException {
      int affected = 0;

      for(int y = cy; y >= minY; --y) {
         Vector pt = new Vector(x, y, z);
         if (!this.getBlock(pt).isAir()) {
            break;
         }

         this.setBlock(pt, pattern.next(pt));
         ++affected;
      }

      return affected;
   }

   public int removeAbove(Vector pos, int size, int height) throws MaxChangedBlocksException {
      int maxY = Math.min(this.world.getMaxY(), pos.getBlockY() + height - 1);
      --size;
      int affected = 0;
      int oX = pos.getBlockX();
      int oY = pos.getBlockY();
      int oZ = pos.getBlockZ();

      for(int x = oX - size; x <= oX + size; ++x) {
         for(int z = oZ - size; z <= oZ + size; ++z) {
            for(int y = oY; y <= maxY; ++y) {
               Vector pt = new Vector(x, y, z);
               if (this.getBlockType(pt) != 0) {
                  this.setBlock(pt, new BaseBlock(0));
                  ++affected;
               }
            }
         }
      }

      return affected;
   }

   public int removeBelow(Vector pos, int size, int height) throws MaxChangedBlocksException {
      int minY = Math.max(0, pos.getBlockY() - height);
      --size;
      int affected = 0;
      int oX = pos.getBlockX();
      int oY = pos.getBlockY();
      int oZ = pos.getBlockZ();

      for(int x = oX - size; x <= oX + size; ++x) {
         for(int z = oZ - size; z <= oZ + size; ++z) {
            for(int y = oY; y >= minY; --y) {
               Vector pt = new Vector(x, y, z);
               if (this.getBlockType(pt) != 0) {
                  this.setBlock(pt, new BaseBlock(0));
                  ++affected;
               }
            }
         }
      }

      return affected;
   }

   public int removeNear(Vector pos, int blockType, int size) throws MaxChangedBlocksException {
      int affected = 0;
      BaseBlock air = new BaseBlock(0);
      int minX = pos.getBlockX() - size;
      int maxX = pos.getBlockX() + size;
      int minY = Math.max(0, pos.getBlockY() - size);
      int maxY = Math.min(this.world.getMaxY(), pos.getBlockY() + size);
      int minZ = pos.getBlockZ() - size;
      int maxZ = pos.getBlockZ() + size;

      for(int x = minX; x <= maxX; ++x) {
         for(int y = minY; y <= maxY; ++y) {
            for(int z = minZ; z <= maxZ; ++z) {
               Vector p = new Vector(x, y, z);
               if (this.getBlockType(p) == blockType && this.setBlock(p, air)) {
                  ++affected;
               }
            }
         }
      }

      return affected;
   }

   public int setBlocks(Region region, BaseBlock block) throws MaxChangedBlocksException {
      int affected = 0;
      if (region instanceof CuboidRegion) {
         Vector min = region.getMinimumPoint();
         Vector max = region.getMaximumPoint();
         int minX = min.getBlockX();
         int minY = min.getBlockY();
         int minZ = min.getBlockZ();
         int maxX = max.getBlockX();
         int maxY = max.getBlockY();
         int maxZ = max.getBlockZ();

         for(int x = minX; x <= maxX; ++x) {
            for(int y = minY; y <= maxY; ++y) {
               for(int z = minZ; z <= maxZ; ++z) {
                  Vector pt = new Vector(x, y, z);
                  if (this.setBlock(pt, block)) {
                     ++affected;
                  }
               }
            }
         }
      } else {
         for(Vector pt : region) {
            if (this.setBlock(pt, block)) {
               ++affected;
            }
         }
      }

      return affected;
   }

   public int setBlocks(Region region, Pattern pattern) throws MaxChangedBlocksException {
      int affected = 0;
      if (region instanceof CuboidRegion) {
         Vector min = region.getMinimumPoint();
         Vector max = region.getMaximumPoint();
         int minX = min.getBlockX();
         int minY = min.getBlockY();
         int minZ = min.getBlockZ();
         int maxX = max.getBlockX();
         int maxY = max.getBlockY();
         int maxZ = max.getBlockZ();

         for(int x = minX; x <= maxX; ++x) {
            for(int y = minY; y <= maxY; ++y) {
               for(int z = minZ; z <= maxZ; ++z) {
                  Vector pt = new Vector(x, y, z);
                  if (this.setBlock(pt, pattern.next(pt))) {
                     ++affected;
                  }
               }
            }
         }
      } else {
         for(Vector pt : region) {
            if (this.setBlock(pt, pattern.next(pt))) {
               ++affected;
            }
         }
      }

      return affected;
   }

   public int replaceBlocks(Region region, Set fromBlockTypes, BaseBlock toBlock) throws MaxChangedBlocksException {
      Set<BaseBlock> definiteBlockTypes = new HashSet();
      Set<Integer> fuzzyBlockTypes = new HashSet();
      if (fromBlockTypes != null) {
         for(BaseBlock block : fromBlockTypes) {
            if (block.getData() == -1) {
               fuzzyBlockTypes.add(block.getType());
            } else {
               definiteBlockTypes.add(block);
            }
         }
      }

      int affected = 0;
      if (region instanceof CuboidRegion) {
         Vector min = region.getMinimumPoint();
         Vector max = region.getMaximumPoint();
         int minX = min.getBlockX();
         int minY = min.getBlockY();
         int minZ = min.getBlockZ();
         int maxX = max.getBlockX();
         int maxY = max.getBlockY();
         int maxZ = max.getBlockZ();

         for(int x = minX; x <= maxX; ++x) {
            for(int y = minY; y <= maxY; ++y) {
               for(int z = minZ; z <= maxZ; ++z) {
                  Vector pt = new Vector(x, y, z);
                  BaseBlock curBlockType = this.getBlock(pt);
                  if (fromBlockTypes == null) {
                     if (curBlockType.isAir()) {
                        continue;
                     }
                  } else if (!definiteBlockTypes.contains(curBlockType) && !fuzzyBlockTypes.contains(curBlockType.getType())) {
                     continue;
                  }

                  if (this.setBlock(pt, toBlock)) {
                     ++affected;
                  }
               }
            }
         }
      } else {
         for(Vector pt : region) {
            BaseBlock curBlockType = this.getBlock(pt);
            if (fromBlockTypes == null) {
               if (curBlockType.isAir()) {
                  continue;
               }
            } else if (!definiteBlockTypes.contains(curBlockType) && !fuzzyBlockTypes.contains(curBlockType.getType())) {
               continue;
            }

            if (this.setBlock(pt, toBlock)) {
               ++affected;
            }
         }
      }

      return affected;
   }

   public int replaceBlocks(Region region, Set fromBlockTypes, Pattern pattern) throws MaxChangedBlocksException {
      Set<BaseBlock> definiteBlockTypes = new HashSet();
      Set<Integer> fuzzyBlockTypes = new HashSet();
      if (fromBlockTypes != null) {
         for(BaseBlock block : fromBlockTypes) {
            if (block.getData() == -1) {
               fuzzyBlockTypes.add(block.getType());
            } else {
               definiteBlockTypes.add(block);
            }
         }
      }

      int affected = 0;
      if (region instanceof CuboidRegion) {
         Vector min = region.getMinimumPoint();
         Vector max = region.getMaximumPoint();
         int minX = min.getBlockX();
         int minY = min.getBlockY();
         int minZ = min.getBlockZ();
         int maxX = max.getBlockX();
         int maxY = max.getBlockY();
         int maxZ = max.getBlockZ();

         for(int x = minX; x <= maxX; ++x) {
            for(int y = minY; y <= maxY; ++y) {
               for(int z = minZ; z <= maxZ; ++z) {
                  Vector pt = new Vector(x, y, z);
                  BaseBlock curBlockType = this.getBlock(pt);
                  if (fromBlockTypes == null) {
                     if (curBlockType.isAir()) {
                        continue;
                     }
                  } else if (!definiteBlockTypes.contains(curBlockType) && !fuzzyBlockTypes.contains(curBlockType.getType())) {
                     continue;
                  }

                  if (this.setBlock(pt, pattern.next(pt))) {
                     ++affected;
                  }
               }
            }
         }
      } else {
         for(Vector pt : region) {
            BaseBlock curBlockType = this.getBlock(pt);
            if (fromBlockTypes == null) {
               if (curBlockType.isAir()) {
                  continue;
               }
            } else if (!definiteBlockTypes.contains(curBlockType) && !fuzzyBlockTypes.contains(curBlockType.getType())) {
               continue;
            }

            if (this.setBlock(pt, pattern.next(pt))) {
               ++affected;
            }
         }
      }

      return affected;
   }

   public int center(Region region, Pattern pattern) throws MaxChangedBlocksException {
      Vector center = region.getCenter();
      int x2 = center.getBlockX();
      int y2 = center.getBlockY();
      int z2 = center.getBlockZ();
      int affected = 0;

      for(int x = (int)center.getX(); x <= x2; ++x) {
         for(int y = (int)center.getY(); y <= y2; ++y) {
            for(int z = (int)center.getZ(); z <= z2; ++z) {
               if (this.setBlock(new Vector(x, y, z), pattern)) {
                  ++affected;
               }
            }
         }
      }

      return affected;
   }

   public int makeCuboidFaces(Region region, BaseBlock block) throws MaxChangedBlocksException {
      int affected = 0;
      Vector min = region.getMinimumPoint();
      Vector max = region.getMaximumPoint();
      int minX = min.getBlockX();
      int minY = min.getBlockY();
      int minZ = min.getBlockZ();
      int maxX = max.getBlockX();
      int maxY = max.getBlockY();
      int maxZ = max.getBlockZ();

      for(int x = minX; x <= maxX; ++x) {
         for(int y = minY; y <= maxY; ++y) {
            if (this.setBlock(new Vector(x, y, minZ), block)) {
               ++affected;
            }

            if (this.setBlock(new Vector(x, y, maxZ), block)) {
               ++affected;
            }

            ++affected;
         }
      }

      for(int y = minY; y <= maxY; ++y) {
         for(int z = minZ; z <= maxZ; ++z) {
            if (this.setBlock(new Vector(minX, y, z), block)) {
               ++affected;
            }

            if (this.setBlock(new Vector(maxX, y, z), block)) {
               ++affected;
            }
         }
      }

      for(int z = minZ; z <= maxZ; ++z) {
         for(int x = minX; x <= maxX; ++x) {
            if (this.setBlock(new Vector(x, minY, z), block)) {
               ++affected;
            }

            if (this.setBlock(new Vector(x, maxY, z), block)) {
               ++affected;
            }
         }
      }

      return affected;
   }

   public int makeCuboidFaces(Region region, Pattern pattern) throws MaxChangedBlocksException {
      int affected = 0;
      Vector min = region.getMinimumPoint();
      Vector max = region.getMaximumPoint();
      int minX = min.getBlockX();
      int minY = min.getBlockY();
      int minZ = min.getBlockZ();
      int maxX = max.getBlockX();
      int maxY = max.getBlockY();
      int maxZ = max.getBlockZ();

      for(int x = minX; x <= maxX; ++x) {
         for(int y = minY; y <= maxY; ++y) {
            Vector minV = new Vector(x, y, minZ);
            if (this.setBlock(minV, pattern.next(minV))) {
               ++affected;
            }

            Vector maxV = new Vector(x, y, maxZ);
            if (this.setBlock(maxV, pattern.next(maxV))) {
               ++affected;
            }

            ++affected;
         }
      }

      for(int y = minY; y <= maxY; ++y) {
         for(int z = minZ; z <= maxZ; ++z) {
            Vector minV = new Vector(minX, y, z);
            if (this.setBlock(minV, pattern.next(minV))) {
               ++affected;
            }

            Vector maxV = new Vector(maxX, y, z);
            if (this.setBlock(maxV, pattern.next(maxV))) {
               ++affected;
            }
         }
      }

      for(int z = minZ; z <= maxZ; ++z) {
         for(int x = minX; x <= maxX; ++x) {
            Vector minV = new Vector(x, minY, z);
            if (this.setBlock(minV, pattern.next(minV))) {
               ++affected;
            }

            Vector maxV = new Vector(x, maxY, z);
            if (this.setBlock(maxV, pattern.next(maxV))) {
               ++affected;
            }
         }
      }

      return affected;
   }

   public int makeCuboidWalls(Region region, BaseBlock block) throws MaxChangedBlocksException {
      int affected = 0;
      Vector min = region.getMinimumPoint();
      Vector max = region.getMaximumPoint();
      int minX = min.getBlockX();
      int minY = min.getBlockY();
      int minZ = min.getBlockZ();
      int maxX = max.getBlockX();
      int maxY = max.getBlockY();
      int maxZ = max.getBlockZ();

      for(int x = minX; x <= maxX; ++x) {
         for(int y = minY; y <= maxY; ++y) {
            if (this.setBlock(new Vector(x, y, minZ), block)) {
               ++affected;
            }

            if (this.setBlock(new Vector(x, y, maxZ), block)) {
               ++affected;
            }

            ++affected;
         }
      }

      for(int y = minY; y <= maxY; ++y) {
         for(int z = minZ; z <= maxZ; ++z) {
            if (this.setBlock(new Vector(minX, y, z), block)) {
               ++affected;
            }

            if (this.setBlock(new Vector(maxX, y, z), block)) {
               ++affected;
            }
         }
      }

      return affected;
   }

   public int makeCuboidWalls(Region region, Pattern pattern) throws MaxChangedBlocksException {
      int affected = 0;
      Vector min = region.getMinimumPoint();
      Vector max = region.getMaximumPoint();
      int minX = min.getBlockX();
      int minY = min.getBlockY();
      int minZ = min.getBlockZ();
      int maxX = max.getBlockX();
      int maxY = max.getBlockY();
      int maxZ = max.getBlockZ();

      for(int x = minX; x <= maxX; ++x) {
         for(int y = minY; y <= maxY; ++y) {
            Vector minV = new Vector(x, y, minZ);
            if (this.setBlock(minV, pattern.next(minV))) {
               ++affected;
            }

            Vector maxV = new Vector(x, y, maxZ);
            if (this.setBlock(maxV, pattern.next(maxV))) {
               ++affected;
            }

            ++affected;
         }
      }

      for(int y = minY; y <= maxY; ++y) {
         for(int z = minZ; z <= maxZ; ++z) {
            Vector minV = new Vector(minX, y, z);
            if (this.setBlock(minV, pattern.next(minV))) {
               ++affected;
            }

            Vector maxV = new Vector(maxX, y, z);
            if (this.setBlock(maxV, pattern.next(maxV))) {
               ++affected;
            }
         }
      }

      return affected;
   }

   public int overlayCuboidBlocks(Region region, BaseBlock block) throws MaxChangedBlocksException {
      Vector min = region.getMinimumPoint();
      Vector max = region.getMaximumPoint();
      int upperY = Math.min(this.world.getMaxY(), max.getBlockY() + 1);
      int lowerY = Math.max(0, min.getBlockY() - 1);
      int affected = 0;
      int minX = min.getBlockX();
      int minZ = min.getBlockZ();
      int maxX = max.getBlockX();
      int maxZ = max.getBlockZ();

      for(int x = minX; x <= maxX; ++x) {
         for(int z = minZ; z <= maxZ; ++z) {
            for(int y = upperY; y >= lowerY; --y) {
               Vector above = new Vector(x, y + 1, z);
               if (y + 1 <= this.world.getMaxY() && !this.getBlock(new Vector(x, y, z)).isAir() && this.getBlock(above).isAir()) {
                  if (this.setBlock(above, block)) {
                     ++affected;
                  }
                  break;
               }
            }
         }
      }

      return affected;
   }

   public int overlayCuboidBlocks(Region region, Pattern pattern) throws MaxChangedBlocksException {
      Vector min = region.getMinimumPoint();
      Vector max = region.getMaximumPoint();
      int upperY = Math.min(this.world.getMaxY(), max.getBlockY() + 1);
      int lowerY = Math.max(0, min.getBlockY() - 1);
      int affected = 0;
      int minX = min.getBlockX();
      int minZ = min.getBlockZ();
      int maxX = max.getBlockX();
      int maxZ = max.getBlockZ();

      for(int x = minX; x <= maxX; ++x) {
         for(int z = minZ; z <= maxZ; ++z) {
            for(int y = upperY; y >= lowerY; --y) {
               Vector above = new Vector(x, y + 1, z);
               if (y + 1 <= this.world.getMaxY() && !this.getBlock(new Vector(x, y, z)).isAir() && this.getBlock(above).isAir()) {
                  if (this.setBlock(above, pattern.next(above))) {
                     ++affected;
                  }
                  break;
               }
            }
         }
      }

      return affected;
   }

   public int naturalizeCuboidBlocks(Region region) throws MaxChangedBlocksException {
      Vector min = region.getMinimumPoint();
      Vector max = region.getMaximumPoint();
      int upperY = Math.min(this.world.getMaxY(), max.getBlockY() + 1);
      int lowerY = Math.max(0, min.getBlockY() - 1);
      int affected = 0;
      int minX = min.getBlockX();
      int minZ = min.getBlockZ();
      int maxX = max.getBlockX();
      int maxZ = max.getBlockZ();
      BaseBlock grass = new BaseBlock(2);
      BaseBlock dirt = new BaseBlock(3);
      BaseBlock stone = new BaseBlock(1);

      for(int x = minX; x <= maxX; ++x) {
         for(int z = minZ; z <= maxZ; ++z) {
            int level = -1;

            for(int y = upperY; y >= lowerY; --y) {
               Vector pt = new Vector(x, y, z);
               int blockType = this.getBlockType(pt);
               boolean isTransformable = blockType == 2 || blockType == 3 || blockType == 1;
               if (level == -1) {
                  if (!isTransformable) {
                     continue;
                  }

                  level = 0;
               }

               if (level >= 0) {
                  if (isTransformable) {
                     if (level == 0) {
                        this.setBlock(pt, grass);
                        ++affected;
                     } else if (level <= 2) {
                        this.setBlock(pt, dirt);
                        ++affected;
                     } else {
                        this.setBlock(pt, stone);
                        ++affected;
                     }
                  }

                  ++level;
               }
            }
         }
      }

      return affected;
   }

   public int stackCuboidRegion(Region region, Vector dir, int count, boolean copyAir) throws MaxChangedBlocksException {
      int affected = 0;
      Vector min = region.getMinimumPoint();
      Vector max = region.getMaximumPoint();
      int minX = min.getBlockX();
      int minY = min.getBlockY();
      int minZ = min.getBlockZ();
      int maxX = max.getBlockX();
      int maxY = max.getBlockY();
      int maxZ = max.getBlockZ();
      int xs = region.getWidth();
      int ys = region.getHeight();
      int zs = region.getLength();

      for(int x = minX; x <= maxX; ++x) {
         for(int z = minZ; z <= maxZ; ++z) {
            for(int y = minY; y <= maxY; ++y) {
               BaseBlock block = this.getBlock(new Vector(x, y, z));
               if (!block.isAir() || copyAir) {
                  for(int i = 1; i <= count; ++i) {
                     Vector pos = new Vector(x + xs * dir.getBlockX() * i, y + ys * dir.getBlockY() * i, z + zs * dir.getBlockZ() * i);
                     if (this.setBlock(pos, block)) {
                        ++affected;
                     }
                  }
               }
            }
         }
      }

      return affected;
   }

   public int moveRegion(Region region, Vector dir, int distance, boolean copyAir, BaseBlock replace) throws MaxChangedBlocksException, RegionOperationException {
      int affected = 0;
      Vector shift = dir.multiply(distance);
      Region newRegion = region.clone();
      newRegion.shift(shift);
      Map<Vector, BaseBlock> delayed = new LinkedHashMap();

      for(Vector pos : region) {
         BaseBlock block = this.getBlock(pos);
         if (!block.isAir() || copyAir) {
            Vector newPos = pos.add(shift);
            delayed.put(newPos, this.getBlock(pos));
            if (!newRegion.contains(pos)) {
               this.setBlock(pos, replace);
            }
         }
      }

      for(Map.Entry entry : delayed.entrySet()) {
         this.setBlock((Vector)entry.getKey(), (BaseBlock)entry.getValue());
         ++affected;
      }

      return affected;
   }

   public int moveCuboidRegion(Region region, Vector dir, int distance, boolean copyAir, BaseBlock replace) throws MaxChangedBlocksException {
      int affected = 0;
      Vector shift = dir.multiply(distance);
      Vector min = region.getMinimumPoint();
      Vector max = region.getMaximumPoint();
      int minX = min.getBlockX();
      int minY = min.getBlockY();
      int minZ = min.getBlockZ();
      int maxX = max.getBlockX();
      int maxY = max.getBlockY();
      int maxZ = max.getBlockZ();
      Vector newMin = min.add(shift);
      Vector newMax = min.add(shift);
      Map<Vector, BaseBlock> delayed = new LinkedHashMap();

      for(int x = minX; x <= maxX; ++x) {
         for(int z = minZ; z <= maxZ; ++z) {
            for(int y = minY; y <= maxY; ++y) {
               Vector pos = new Vector(x, y, z);
               BaseBlock block = this.getBlock(pos);
               if (!block.isAir() || copyAir) {
                  Vector newPos = pos.add(shift);
                  delayed.put(newPos, this.getBlock(pos));
                  if (x < newMin.getBlockX() || x > newMax.getBlockX() || y < newMin.getBlockY() || y > newMax.getBlockY() || z < newMin.getBlockZ() || z > newMax.getBlockZ()) {
                     this.setBlock(pos, replace);
                  }
               }
            }
         }
      }

      for(Map.Entry entry : delayed.entrySet()) {
         this.setBlock((Vector)entry.getKey(), (BaseBlock)entry.getValue());
         ++affected;
      }

      return affected;
   }

   public int drainArea(Vector pos, double radius) throws MaxChangedBlocksException {
      int affected = 0;
      HashSet<BlockVector> visited = new HashSet();
      Stack<BlockVector> queue = new Stack();

      for(int x = pos.getBlockX() - 1; x <= pos.getBlockX() + 1; ++x) {
         for(int z = pos.getBlockZ() - 1; z <= pos.getBlockZ() + 1; ++z) {
            for(int y = pos.getBlockY() - 1; y <= pos.getBlockY() + 1; ++y) {
               queue.push(new BlockVector(x, y, z));
            }
         }
      }

      while(!queue.empty()) {
         BlockVector cur = (BlockVector)queue.pop();
         int type = this.getBlockType(cur);
         if ((type == 8 || type == 9 || type == 10 || type == 11) && !visited.contains(cur)) {
            visited.add(cur);
            if (!(pos.distance(cur) > radius)) {
               for(int x = cur.getBlockX() - 1; x <= cur.getBlockX() + 1; ++x) {
                  for(int z = cur.getBlockZ() - 1; z <= cur.getBlockZ() + 1; ++z) {
                     for(int y = cur.getBlockY() - 1; y <= cur.getBlockY() + 1; ++y) {
                        BlockVector newPos = new BlockVector(x, y, z);
                        if (!cur.equals(newPos)) {
                           queue.push(newPos);
                        }
                     }
                  }
               }

               if (this.setBlock(cur, (BaseBlock)(new BaseBlock(0)))) {
                  ++affected;
               }
            }
         }
      }

      return affected;
   }

   public int fixLiquid(Vector pos, double radius, int moving, int stationary) throws MaxChangedBlocksException {
      int affected = 0;
      HashSet<BlockVector> visited = new HashSet();
      Stack<BlockVector> queue = new Stack();

      for(int x = pos.getBlockX() - 1; x <= pos.getBlockX() + 1; ++x) {
         for(int z = pos.getBlockZ() - 1; z <= pos.getBlockZ() + 1; ++z) {
            for(int y = pos.getBlockY() - 1; y <= pos.getBlockY() + 1; ++y) {
               int type = this.getBlock(new Vector(x, y, z)).getType();
               if (type == moving || type == stationary) {
                  queue.push(new BlockVector(x, y, z));
               }
            }
         }
      }

      BaseBlock stationaryBlock = new BaseBlock(stationary);

      while(!queue.empty()) {
         BlockVector cur = (BlockVector)queue.pop();
         int type = this.getBlockType(cur);
         if ((type == moving || type == stationary || type == 0) && !visited.contains(cur)) {
            visited.add(cur);
            if (this.setBlock(cur, (BaseBlock)stationaryBlock)) {
               ++affected;
            }

            if (!(pos.distance(cur) > radius)) {
               queue.push(cur.add(1, 0, 0).toBlockVector());
               queue.push(cur.add(-1, 0, 0).toBlockVector());
               queue.push(cur.add(0, 0, 1).toBlockVector());
               queue.push(cur.add(0, 0, -1).toBlockVector());
            }
         }
      }

      return affected;
   }

   public int makeCylinder(Vector pos, Pattern block, double radius, int height, boolean filled) throws MaxChangedBlocksException {
      return this.makeCylinder(pos, block, radius, radius, height, filled);
   }

   public int makeCylinder(Vector pos, Pattern block, double radiusX, double radiusZ, int height, boolean filled) throws MaxChangedBlocksException {
      int affected = 0;
      radiusX += (double)0.5F;
      radiusZ += (double)0.5F;
      if (height == 0) {
         return 0;
      } else {
         if (height < 0) {
            height = -height;
            pos = pos.subtract(0, height, 0);
         }

         if (pos.getBlockY() < 0) {
            pos = pos.setY(0);
         } else if (pos.getBlockY() + height - 1 > this.world.getMaxY()) {
            height = this.world.getMaxY() - pos.getBlockY() + 1;
         }

         double invRadiusX = (double)1.0F / radiusX;
         double invRadiusZ = (double)1.0F / radiusZ;
         int ceilRadiusX = (int)Math.ceil(radiusX);
         int ceilRadiusZ = (int)Math.ceil(radiusZ);
         double nextXn = (double)0.0F;

         for(int x = 0; x <= ceilRadiusX; ++x) {
            double xn = nextXn;
            nextXn = (double)(x + 1) * invRadiusX;
            double nextZn = (double)0.0F;

            for(int z = 0; z <= ceilRadiusZ; ++z) {
               double zn = nextZn;
               nextZn = (double)(z + 1) * invRadiusZ;
               double distanceSq = lengthSq(xn, zn);
               if (distanceSq > (double)1.0F) {
                  if (z == 0) {
                     return affected;
                  }
                  break;
               }

               if (filled || !(lengthSq(nextXn, zn) <= (double)1.0F) || !(lengthSq(xn, nextZn) <= (double)1.0F)) {
                  for(int y = 0; y < height; ++y) {
                     if (this.setBlock(pos.add(x, y, z), block)) {
                        ++affected;
                     }

                     if (this.setBlock(pos.add(-x, y, z), block)) {
                        ++affected;
                     }

                     if (this.setBlock(pos.add(x, y, -z), block)) {
                        ++affected;
                     }

                     if (this.setBlock(pos.add(-x, y, -z), block)) {
                        ++affected;
                     }
                  }
               }
            }
         }

         return affected;
      }
   }

   public int makeSphere(Vector pos, Pattern block, double radius, boolean filled) throws MaxChangedBlocksException {
      return this.makeSphere(pos, block, radius, radius, radius, filled);
   }

   public int makeSphere(Vector pos, Pattern block, double radiusX, double radiusY, double radiusZ, boolean filled) throws MaxChangedBlocksException {
      int affected = 0;
      radiusX += (double)0.5F;
      radiusY += (double)0.5F;
      radiusZ += (double)0.5F;
      double invRadiusX = (double)1.0F / radiusX;
      double invRadiusY = (double)1.0F / radiusY;
      double invRadiusZ = (double)1.0F / radiusZ;
      int ceilRadiusX = (int)Math.ceil(radiusX);
      int ceilRadiusY = (int)Math.ceil(radiusY);
      int ceilRadiusZ = (int)Math.ceil(radiusZ);
      double nextXn = (double)0.0F;

      label80:
      for(int x = 0; x <= ceilRadiusX; ++x) {
         double xn = nextXn;
         nextXn = (double)(x + 1) * invRadiusX;
         double nextYn = (double)0.0F;

         for(int y = 0; y <= ceilRadiusY; ++y) {
            double yn = nextYn;
            nextYn = (double)(y + 1) * invRadiusY;
            double nextZn = (double)0.0F;

            for(int z = 0; z <= ceilRadiusZ; ++z) {
               double zn = nextZn;
               nextZn = (double)(z + 1) * invRadiusZ;
               double distanceSq = lengthSq(xn, yn, zn);
               if (distanceSq > (double)1.0F) {
                  if (z == 0) {
                     if (y == 0) {
                        return affected;
                     }
                     continue label80;
                  }
                  break;
               }

               if (filled || !(lengthSq(nextXn, yn, zn) <= (double)1.0F) || !(lengthSq(xn, nextYn, zn) <= (double)1.0F) || !(lengthSq(xn, yn, nextZn) <= (double)1.0F)) {
                  if (this.setBlock(pos.add(x, y, z), block)) {
                     ++affected;
                  }

                  if (this.setBlock(pos.add(-x, y, z), block)) {
                     ++affected;
                  }

                  if (this.setBlock(pos.add(x, -y, z), block)) {
                     ++affected;
                  }

                  if (this.setBlock(pos.add(x, y, -z), block)) {
                     ++affected;
                  }

                  if (this.setBlock(pos.add(-x, -y, z), block)) {
                     ++affected;
                  }

                  if (this.setBlock(pos.add(x, -y, -z), block)) {
                     ++affected;
                  }

                  if (this.setBlock(pos.add(-x, y, -z), block)) {
                     ++affected;
                  }

                  if (this.setBlock(pos.add(-x, -y, -z), block)) {
                     ++affected;
                  }
               }
            }
         }
      }

      return affected;
   }

   private static final double lengthSq(double x, double y, double z) {
      return x * x + y * y + z * z;
   }

   private static final double lengthSq(double x, double z) {
      return x * x + z * z;
   }

   public int makePyramid(Vector pos, Pattern block, int size, boolean filled) throws MaxChangedBlocksException {
      int affected = 0;
      int height = size;

      for(int y = 0; y <= height; ++y) {
         --size;

         for(int x = 0; x <= size; ++x) {
            for(int z = 0; z <= size; ++z) {
               if (filled && z <= size && x <= size || z == size || x == size) {
                  if (this.setBlock(pos.add(x, y, z), block)) {
                     ++affected;
                  }

                  if (this.setBlock(pos.add(-x, y, z), block)) {
                     ++affected;
                  }

                  if (this.setBlock(pos.add(x, y, -z), block)) {
                     ++affected;
                  }

                  if (this.setBlock(pos.add(-x, y, -z), block)) {
                     ++affected;
                  }
               }
            }
         }
      }

      return affected;
   }

   public int thaw(Vector pos, double radius) throws MaxChangedBlocksException {
      int affected = 0;
      double radiusSq = radius * radius;
      int ox = pos.getBlockX();
      int oy = pos.getBlockY();
      int oz = pos.getBlockZ();
      BaseBlock air = new BaseBlock(0);
      BaseBlock water = new BaseBlock(9);
      int ceilRadius = (int)Math.ceil(radius);

      for(int x = ox - ceilRadius; x <= ox + ceilRadius; ++x) {
         label41:
         for(int z = oz - ceilRadius; z <= oz + ceilRadius; ++z) {
            if (!((new Vector(x, oy, z)).distanceSq(pos) > radiusSq)) {
               int y = this.world.getMaxY();

               while(y >= 1) {
                  Vector pt = new Vector(x, y, z);
                  int id = this.getBlockType(pt);
                  switch (id) {
                     case 0:
                        --y;
                        break;
                     case 78:
                        if (this.setBlock(pt, air)) {
                           ++affected;
                        }
                        continue label41;
                     case 79:
                        if (this.setBlock(pt, water)) {
                           ++affected;
                        }
                     default:
                        continue label41;
                  }
               }
            }
         }
      }

      return affected;
   }

   public int simulateSnow(Vector pos, double radius) throws MaxChangedBlocksException {
      int affected = 0;
      double radiusSq = radius * radius;
      int ox = pos.getBlockX();
      int oy = pos.getBlockY();
      int oz = pos.getBlockZ();
      BaseBlock ice = new BaseBlock(79);
      BaseBlock snow = new BaseBlock(78);
      int ceilRadius = (int)Math.ceil(radius);

      for(int x = ox - ceilRadius; x <= ox + ceilRadius; ++x) {
         for(int z = oz - ceilRadius; z <= oz + ceilRadius; ++z) {
            if (!((new Vector(x, oy, z)).distanceSq(pos) > radiusSq)) {
               for(int y = this.world.getMaxY(); y >= 1; --y) {
                  Vector pt = new Vector(x, y, z);
                  int id = this.getBlockType(pt);
                  if (id != 0) {
                     if (id != 8 && id != 9) {
                        if (!BlockType.isTranslucent(id) && y != this.world.getMaxY() && this.setBlock(pt.add(0, 1, 0), snow)) {
                           ++affected;
                        }
                        break;
                     }

                     if (this.setBlock(pt, ice)) {
                        ++affected;
                     }
                     break;
                  }
               }
            }
         }
      }

      return affected;
   }

   public int green(Vector pos, double radius) throws MaxChangedBlocksException {
      int affected = 0;
      double radiusSq = radius * radius;
      int ox = pos.getBlockX();
      int oy = pos.getBlockY();
      int oz = pos.getBlockZ();
      BaseBlock grass = new BaseBlock(2);
      int ceilRadius = (int)Math.ceil(radius);

      for(int x = ox - ceilRadius; x <= ox + ceilRadius; ++x) {
         label44:
         for(int z = oz - ceilRadius; z <= oz + ceilRadius; ++z) {
            if (!((new Vector(x, oy, z)).distanceSq(pos) > radiusSq)) {
               for(int y = this.world.getMaxY(); y >= 1; --y) {
                  Vector pt = new Vector(x, y, z);
                  int id = this.getBlockType(pt);
                  switch (id) {
                     case 3:
                        if (this.setBlock(pt, grass)) {
                           ++affected;
                        }
                        continue label44;
                     case 4:
                     case 5:
                     case 6:
                     case 7:
                     default:
                        if (!BlockType.canPassThrough(id)) {
                           continue label44;
                        }
                        break;
                     case 8:
                     case 9:
                     case 10:
                     case 11:
                        continue label44;
                  }
               }
            }
         }
      }

      return affected;
   }

   private void makePumpkinPatch(Vector basePos) throws MaxChangedBlocksException {
      BaseBlock leavesBlock = new BaseBlock(18);
      this.setBlockIfAir(basePos, leavesBlock);
      this.makePumpkinPatchVine(basePos, basePos.add(0, 0, 1));
      this.makePumpkinPatchVine(basePos, basePos.add(0, 0, -1));
      this.makePumpkinPatchVine(basePos, basePos.add(1, 0, 0));
      this.makePumpkinPatchVine(basePos, basePos.add(-1, 0, 0));
   }

   private void makePumpkinPatchVine(Vector basePos, Vector pos) throws MaxChangedBlocksException {
      if (!(pos.distance(basePos) > (double)4.0F)) {
         if (this.getBlockType(pos) == 0) {
            for(int i = -1; i > -3; --i) {
               Vector testPos = pos.add(0, i, 0);
               if (this.getBlockType(testPos) != 0) {
                  break;
               }

               pos = testPos;
            }

            this.setBlockIfAir(pos, new BaseBlock(18));
            int t = prng.nextInt(4);
            int h = prng.nextInt(3) - 1;
            BaseBlock log = new BaseBlock(17);
            switch (t) {
               case 0:
                  if (prng.nextBoolean()) {
                     this.makePumpkinPatchVine(basePos, pos.add(1, 0, 0));
                  }

                  if (prng.nextBoolean()) {
                     this.setBlockIfAir(pos.add(1, h, -1), log);
                  }

                  this.setBlockIfAir(pos.add(0, 0, -1), new BaseBlock(86, prng.nextInt(4)));
                  break;
               case 1:
                  if (prng.nextBoolean()) {
                     this.makePumpkinPatchVine(basePos, pos.add(0, 0, 1));
                  }

                  if (prng.nextBoolean()) {
                     this.setBlockIfAir(pos.add(1, h, 0), log);
                  }

                  this.setBlockIfAir(pos.add(1, 0, 1), new BaseBlock(86, prng.nextInt(4)));
                  break;
               case 2:
                  if (prng.nextBoolean()) {
                     this.makePumpkinPatchVine(basePos, pos.add(0, 0, -1));
                  }

                  if (prng.nextBoolean()) {
                     this.setBlockIfAir(pos.add(-1, h, 0), log);
                  }

                  this.setBlockIfAir(pos.add(-1, 0, 1), new BaseBlock(86, prng.nextInt(4)));
                  break;
               case 3:
                  if (prng.nextBoolean()) {
                     this.makePumpkinPatchVine(basePos, pos.add(-1, 0, 0));
                  }

                  if (prng.nextBoolean()) {
                     this.setBlockIfAir(pos.add(-1, h, -1), log);
                  }

                  this.setBlockIfAir(pos.add(-1, 0, -1), new BaseBlock(86, prng.nextInt(4)));
            }

         }
      }
   }

   public int makePumpkinPatches(Vector basePos, int size) throws MaxChangedBlocksException {
      int affected = 0;

      for(int x = basePos.getBlockX() - size; x <= basePos.getBlockX() + size; ++x) {
         for(int z = basePos.getBlockZ() - size; z <= basePos.getBlockZ() + size; ++z) {
            if (this.getBlock(new Vector(x, basePos.getBlockY(), z)).isAir() && !(Math.random() < 0.98)) {
               for(int y = basePos.getBlockY(); y >= basePos.getBlockY() - 10; --y) {
                  int t = this.getBlock(new Vector(x, y, z)).getType();
                  if (t == 2 || t == 3) {
                     this.makePumpkinPatch(new Vector(x, y + 1, z));
                     ++affected;
                     break;
                  }

                  if (t != 0) {
                     break;
                  }
               }
            }
         }
      }

      return affected;
   }

   public int makeForest(Vector basePos, int size, double density, TreeGenerator treeGenerator) throws MaxChangedBlocksException {
      int affected = 0;

      for(int x = basePos.getBlockX() - size; x <= basePos.getBlockX() + size; ++x) {
         for(int z = basePos.getBlockZ() - size; z <= basePos.getBlockZ() + size; ++z) {
            if (this.getBlock(new Vector(x, basePos.getBlockY(), z)).isAir() && !(Math.random() >= density)) {
               for(int y = basePos.getBlockY(); y >= basePos.getBlockY() - 10; --y) {
                  int t = this.getBlock(new Vector(x, y, z)).getType();
                  if (t == 2 || t == 3) {
                     treeGenerator.generate(this, new Vector(x, y + 1, z));
                     ++affected;
                     break;
                  }

                  if (t != 0) {
                     break;
                  }
               }
            }
         }
      }

      return affected;
   }

   public List getBlockDistribution(Region region) {
      List<Countable<Integer>> distribution = new ArrayList();
      Map<Integer, Countable<Integer>> map = new HashMap();
      if (region instanceof CuboidRegion) {
         Vector min = region.getMinimumPoint();
         Vector max = region.getMaximumPoint();
         int minX = min.getBlockX();
         int minY = min.getBlockY();
         int minZ = min.getBlockZ();
         int maxX = max.getBlockX();
         int maxY = max.getBlockY();
         int maxZ = max.getBlockZ();

         for(int x = minX; x <= maxX; ++x) {
            for(int y = minY; y <= maxY; ++y) {
               for(int z = minZ; z <= maxZ; ++z) {
                  Vector pt = new Vector(x, y, z);
                  int id = this.getBlockType(pt);
                  if (map.containsKey(id)) {
                     ((Countable)map.get(id)).increment();
                  } else {
                     Countable<Integer> c = new Countable(id, 1);
                     map.put(id, c);
                     distribution.add(c);
                  }
               }
            }
         }
      } else {
         for(Vector pt : region) {
            int id = this.getBlockType(pt);
            if (map.containsKey(id)) {
               ((Countable)map.get(id)).increment();
            } else {
               Countable<Integer> c = new Countable(id, 1);
               map.put(id, c);
            }
         }
      }

      Collections.sort(distribution);
      return distribution;
   }

   public List getBlockDistributionWithData(Region region) {
      List<Countable<BaseBlock>> distribution = new ArrayList();
      Map<BaseBlock, Countable<BaseBlock>> map = new HashMap();
      if (region instanceof CuboidRegion) {
         Vector min = region.getMinimumPoint();
         Vector max = region.getMaximumPoint();
         int minX = min.getBlockX();
         int minY = min.getBlockY();
         int minZ = min.getBlockZ();
         int maxX = max.getBlockX();
         int maxY = max.getBlockY();
         int maxZ = max.getBlockZ();

         for(int x = minX; x <= maxX; ++x) {
            for(int y = minY; y <= maxY; ++y) {
               for(int z = minZ; z <= maxZ; ++z) {
                  Vector pt = new Vector(x, y, z);
                  BaseBlock blk = new BaseBlock(this.getBlockType(pt), this.getBlockData(pt));
                  if (map.containsKey(blk)) {
                     ((Countable)map.get(blk)).increment();
                  } else {
                     Countable<BaseBlock> c = new Countable(blk, 1);
                     map.put(blk, c);
                     distribution.add(c);
                  }
               }
            }
         }
      } else {
         for(Vector pt : region) {
            BaseBlock blk = new BaseBlock(this.getBlockType(pt), this.getBlockData(pt));
            if (map.containsKey(blk)) {
               ((Countable)map.get(blk)).increment();
            } else {
               Countable<BaseBlock> c = new Countable(blk, 1);
               map.put(blk, c);
            }
         }
      }

      Collections.sort(distribution);
      return distribution;
   }

   public int makeShape(Region region, final Vector zero, final Vector unit, Pattern pattern, String expressionString, boolean hollow) throws ExpressionException, MaxChangedBlocksException {
      final Expression expression = Expression.compile(expressionString, "x", "y", "z", "type", "data");
      expression.optimize();
      final RValue typeVariable = expression.getVariable("type", false);
      final RValue dataVariable = expression.getVariable("data", false);
      ArbitraryShape shape = new ArbitraryShape(region) {
         protected BaseBlock getMaterial(int x, int y, int z, BaseBlock defaultMaterial) {
            Vector scaled = (new Vector(x, y, z)).subtract(zero).divide(unit);

            try {
               return expression.evaluate(scaled.getX(), scaled.getY(), scaled.getZ(), (double)defaultMaterial.getType(), (double)defaultMaterial.getData()) <= (double)0.0F ? null : new BaseBlock((int)typeVariable.getValue(), (int)dataVariable.getValue());
            } catch (Exception e) {
               e.printStackTrace();
               return null;
            }
         }
      };
      return shape.generate(this, pattern, hollow);
   }

   public int deformRegion(Region region, Vector zero, Vector unit, String expressionString) throws ExpressionException, MaxChangedBlocksException {
      Expression expression = Expression.compile(expressionString, "x", "y", "z");
      expression.optimize();
      RValue x = expression.getVariable("x", false);
      RValue y = expression.getVariable("y", false);
      RValue z = expression.getVariable("z", false);
      Vector zero2 = zero.add((double)0.5F, (double)0.5F, (double)0.5F);
      DoubleArrayList<BlockVector, BaseBlock> queue = new DoubleArrayList(false);

      for(BlockVector position : region) {
         Vector scaled = position.subtract(zero).divide(unit);
         expression.evaluate(scaled.getX(), scaled.getY(), scaled.getZ());
         Vector sourceScaled = new Vector(x.getValue(), y.getValue(), z.getValue());
         BlockVector sourcePosition = sourceScaled.multiply(unit).add(zero2).toBlockPoint();
         BaseBlock material = new BaseBlock(this.world.getBlockType(sourcePosition), this.world.getBlockData(sourcePosition));
         queue.put(position, material);
      }

      int affected = 0;

      for(Map.Entry entry : queue) {
         BlockVector position = (BlockVector)entry.getKey();
         BaseBlock material = (BaseBlock)entry.getValue();
         if (this.setBlock(position, (BaseBlock)material)) {
            ++affected;
         }
      }

      return affected;
   }

   public int hollowOutRegion(Region region, int thickness, Pattern pattern) throws MaxChangedBlocksException {
      int affected = 0;
      Set<BlockVector> outside = new HashSet();
      Vector min = region.getMinimumPoint();
      Vector max = region.getMaximumPoint();
      int minX = min.getBlockX();
      int minY = min.getBlockY();
      int minZ = min.getBlockZ();
      int maxX = max.getBlockX();
      int maxY = max.getBlockY();
      int maxZ = max.getBlockZ();

      for(int x = minX; x <= maxX; ++x) {
         for(int y = minY; y <= maxY; ++y) {
            this.recurseHollow(region, new BlockVector(x, y, minZ), outside);
            this.recurseHollow(region, new BlockVector(x, y, maxZ), outside);
         }
      }

      for(int y = minY; y <= maxY; ++y) {
         for(int z = minZ; z <= maxZ; ++z) {
            this.recurseHollow(region, new BlockVector(minX, y, z), outside);
            this.recurseHollow(region, new BlockVector(maxX, y, z), outside);
         }
      }

      for(int z = minZ; z <= maxZ; ++z) {
         for(int x = minX; x <= maxX; ++x) {
            this.recurseHollow(region, new BlockVector(x, minY, z), outside);
            this.recurseHollow(region, new BlockVector(x, maxY, z), outside);
         }
      }

      for(int i = 1; i < thickness; ++i) {
         Set<BlockVector> newOutside = new HashSet();

         for(BlockVector position : region) {
            for(Vector recurseDirection : this.recurseDirections) {
               BlockVector neighbor = position.add(recurseDirection).toBlockVector();
               if (outside.contains(neighbor)) {
                  newOutside.add(position);
                  break;
               }
            }
         }

         outside.addAll(newOutside);
      }

      label56:
      for(BlockVector position : region) {
         for(Vector recurseDirection : this.recurseDirections) {
            BlockVector neighbor = position.add(recurseDirection).toBlockVector();
            if (outside.contains(neighbor)) {
               continue label56;
            }
         }

         if (this.setBlock(position, (BaseBlock)pattern.next(position))) {
            ++affected;
         }
      }

      return affected;
   }

   private void recurseHollow(Region region, BlockVector origin, Set outside) {
      LinkedList<BlockVector> queue = new LinkedList();
      queue.addLast(origin);

      while(!queue.isEmpty()) {
         BlockVector current = (BlockVector)queue.removeFirst();
         if (BlockType.canPassThrough(this.getBlockType(current)) && outside.add(current) && region.contains(current)) {
            for(Vector recurseDirection : this.recurseDirections) {
               queue.addLast(current.add(recurseDirection).toBlockVector());
            }
         }
      }

   }
}
