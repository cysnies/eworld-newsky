package com.sk89q.worldedit;

public interface BiomeType {
   BiomeType UNKNOWN = new BiomeType() {
      public String getName() {
         return "Unknown";
      }
   };

   String getName();
}
