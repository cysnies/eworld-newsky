package com.sk89q.worldedit.regions;

public interface FlatRegion extends Region {
   int getMinimumY();

   int getMaximumY();

   Iterable asFlatRegion();
}
