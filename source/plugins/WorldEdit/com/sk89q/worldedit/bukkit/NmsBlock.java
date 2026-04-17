package com.sk89q.worldedit.bukkit;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.foundation.Block;
import org.bukkit.World;

public abstract class NmsBlock extends BaseBlock {
   protected NmsBlock(int type) {
      super(type);
   }

   protected NmsBlock(int type, int data) {
      super(type, data);
   }

   public static boolean verify() {
      return false;
   }

   public static NmsBlock get(World world, Vector vector, int type, int data) {
      return null;
   }

   public static boolean set(World world, Vector vector, Block block) {
      return false;
   }

   public static boolean setSafely(World world, Vector vector, Block block, boolean notify) {
      return false;
   }

   public static boolean hasTileEntity(int type) {
      return false;
   }

   public static boolean isValidBlockType(int type) {
      return false;
   }
}
