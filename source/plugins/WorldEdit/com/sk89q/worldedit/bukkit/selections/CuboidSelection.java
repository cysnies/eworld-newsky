package com.sk89q.worldedit.bukkit.selections;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.CuboidRegionSelector;
import com.sk89q.worldedit.regions.RegionSelector;
import org.bukkit.Location;
import org.bukkit.World;

public class CuboidSelection extends RegionSelection {
   protected CuboidRegion cuboid;

   public CuboidSelection(World world, Location pt1, Location pt2) {
      this(world, BukkitUtil.toVector(pt1), BukkitUtil.toVector(pt2));
   }

   public CuboidSelection(World world, Vector pt1, Vector pt2) {
      super(world);
      if (pt1 == null) {
         throw new IllegalArgumentException("Null point 1 not permitted");
      } else if (pt2 == null) {
         throw new IllegalArgumentException("Null point 2 not permitted");
      } else {
         CuboidRegionSelector sel = new CuboidRegionSelector(BukkitUtil.getLocalWorld(world));
         sel.selectPrimary(pt1);
         sel.selectSecondary(pt2);
         this.cuboid = sel.getIncompleteRegion();
         this.setRegionSelector(sel);
         this.setRegion(this.cuboid);
      }
   }

   public CuboidSelection(World world, RegionSelector sel, CuboidRegion region) {
      super(world, sel, region);
      this.cuboid = region;
   }
}
