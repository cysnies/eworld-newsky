package com.sk89q.worldedit.data;

import com.sk89q.worldedit.CuboidClipboard;

/** @deprecated */
@Deprecated
public final class BlockData {
   /** @deprecated */
   @Deprecated
   public static int rotate90(int type, int data) {
      return com.sk89q.worldedit.blocks.BlockData.rotate90(type, data);
   }

   /** @deprecated */
   @Deprecated
   public static int rotate90Reverse(int type, int data) {
      return com.sk89q.worldedit.blocks.BlockData.rotate90Reverse(type, data);
   }

   /** @deprecated */
   @Deprecated
   public static int flip(int type, int data) {
      return rotate90(type, rotate90(type, data));
   }

   /** @deprecated */
   @Deprecated
   public static int flip(int type, int data, CuboidClipboard.FlipDirection direction) {
      return com.sk89q.worldedit.blocks.BlockData.flip(type, data, direction);
   }

   /** @deprecated */
   @Deprecated
   public static int cycle(int type, int data, int increment) {
      return com.sk89q.worldedit.blocks.BlockData.cycle(type, data, increment);
   }

   /** @deprecated */
   @Deprecated
   public static int nextClothColor(int data) {
      return com.sk89q.worldedit.blocks.BlockData.nextClothColor(data);
   }

   /** @deprecated */
   @Deprecated
   public static int prevClothColor(int data) {
      return com.sk89q.worldedit.blocks.BlockData.prevClothColor(data);
   }
}
