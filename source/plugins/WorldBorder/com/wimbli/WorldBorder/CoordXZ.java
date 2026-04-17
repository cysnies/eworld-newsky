package com.wimbli.WorldBorder;

public class CoordXZ {
   public int x;
   public int z;

   public CoordXZ(int x, int z) {
      this.x = x;
      this.z = z;
   }

   public static int blockToChunk(int blockVal) {
      return blockVal >> 4;
   }

   public static int blockToRegion(int blockVal) {
      return blockVal >> 9;
   }

   public static int chunkToRegion(int chunkVal) {
      return chunkVal >> 5;
   }

   public static int chunkToBlock(int chunkVal) {
      return chunkVal << 4;
   }

   public static int regionToBlock(int regionVal) {
      return regionVal << 9;
   }

   public static int regionToChunk(int regionVal) {
      return regionVal << 5;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && obj.getClass() == this.getClass()) {
         CoordXZ test = (CoordXZ)obj;
         return test.x == this.x && test.z == this.z;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return (this.x << 9) + this.z;
   }
}
