package com.sk89q.worldedit.util;

import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldVectorFace;
import com.sk89q.worldedit.blocks.BlockType;

public class TargetBlock {
   private LocalWorld world;
   private int maxDistance;
   private double checkDistance;
   private double curDistance;
   private Vector targetPos = new Vector();
   private Vector targetPosDouble = new Vector();
   private Vector prevPos = new Vector();
   private Vector offset = new Vector();

   public TargetBlock(LocalPlayer player) {
      this.world = player.getWorld();
      this.setValues(player.getPosition(), player.getYaw(), player.getPitch(), 300, 1.65, 0.2);
   }

   public TargetBlock(LocalPlayer player, int maxDistance, double checkDistance) {
      this.world = player.getWorld();
      this.setValues(player.getPosition(), player.getYaw(), player.getPitch(), maxDistance, 1.65, checkDistance);
   }

   private void setValues(Vector loc, double xRotation, double yRotation, int maxDistance, double viewHeight, double checkDistance) {
      this.maxDistance = maxDistance;
      this.checkDistance = checkDistance;
      this.curDistance = (double)0.0F;
      xRotation = (xRotation + (double)90.0F) % (double)360.0F;
      yRotation *= (double)-1.0F;
      double h = checkDistance * Math.cos(Math.toRadians(yRotation));
      this.offset = new Vector(h * Math.cos(Math.toRadians(xRotation)), checkDistance * Math.sin(Math.toRadians(yRotation)), h * Math.sin(Math.toRadians(xRotation)));
      this.targetPosDouble = loc.add((double)0.0F, viewHeight, (double)0.0F);
      this.targetPos = this.targetPosDouble.toBlockPoint();
      this.prevPos = this.targetPos;
   }

   public BlockWorldVector getAnyTargetBlock() {
      boolean searchForLastBlock = true;
      BlockWorldVector lastBlock = null;

      while(this.getNextBlock() != null && this.world.getBlockType(this.getCurrentBlock()) == 0) {
         if (searchForLastBlock) {
            lastBlock = this.getCurrentBlock();
            if (lastBlock.getBlockY() <= 0 || lastBlock.getBlockY() >= this.world.getMaxY()) {
               searchForLastBlock = false;
            }
         }
      }

      BlockWorldVector currentBlock = this.getCurrentBlock();
      return currentBlock != null ? currentBlock : lastBlock;
   }

   public BlockWorldVector getTargetBlock() {
      while(this.getNextBlock() != null && this.world.getBlockType(this.getCurrentBlock()) == 0) {
      }

      return this.getCurrentBlock();
   }

   public BlockWorldVector getSolidTargetBlock() {
      while(this.getNextBlock() != null && BlockType.canPassThrough(this.world.getBlockType(this.getCurrentBlock()))) {
      }

      return this.getCurrentBlock();
   }

   public BlockWorldVector getNextBlock() {
      this.prevPos = this.targetPos;

      do {
         this.curDistance += this.checkDistance;
         this.targetPosDouble = this.offset.add(this.targetPosDouble.getX(), this.targetPosDouble.getY(), this.targetPosDouble.getZ());
         this.targetPos = this.targetPosDouble.toBlockPoint();
      } while(this.curDistance <= (double)this.maxDistance && this.targetPos.getBlockX() == this.prevPos.getBlockX() && this.targetPos.getBlockY() == this.prevPos.getBlockY() && this.targetPos.getBlockZ() == this.prevPos.getBlockZ());

      return this.curDistance > (double)this.maxDistance ? null : new BlockWorldVector(this.world, this.targetPos);
   }

   public BlockWorldVector getCurrentBlock() {
      return this.curDistance > (double)this.maxDistance ? null : new BlockWorldVector(this.world, this.targetPos);
   }

   public BlockWorldVector getPreviousBlock() {
      return new BlockWorldVector(this.world, this.prevPos);
   }

   public WorldVectorFace getAnyTargetBlockFace() {
      this.getAnyTargetBlock();
      return WorldVectorFace.getWorldVectorFace(this.world, this.getCurrentBlock(), this.getPreviousBlock());
   }

   public WorldVectorFace getTargetBlockFace() {
      this.getAnyTargetBlock();
      return WorldVectorFace.getWorldVectorFace(this.world, this.getCurrentBlock(), this.getPreviousBlock());
   }
}
