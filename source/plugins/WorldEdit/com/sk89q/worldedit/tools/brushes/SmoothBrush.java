package com.sk89q.worldedit.tools.brushes;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.HeightMap;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.filtering.GaussianKernel;
import com.sk89q.worldedit.filtering.HeightMapFilter;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;

public class SmoothBrush implements Brush {
   private int iterations;
   private boolean naturalOnly;

   public SmoothBrush(int iterations) {
      this(iterations, false);
   }

   public SmoothBrush(int iterations, boolean naturalOnly) {
      this.iterations = iterations;
      this.naturalOnly = naturalOnly;
   }

   public void build(EditSession editSession, Vector pos, Pattern mat, double size) throws MaxChangedBlocksException {
      WorldVector min = new WorldVector(editSession.getWorld(), pos.subtract(size, size, size));
      Vector max = pos.add(size, size + (double)10.0F, size);
      Region region = new CuboidRegion(editSession.getWorld(), min, max);
      HeightMap heightMap = new HeightMap(editSession, region, this.naturalOnly);
      HeightMapFilter filter = new HeightMapFilter(new GaussianKernel(5, (double)1.0F));
      heightMap.applyFilter(filter, this.iterations);
   }
}
