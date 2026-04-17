package com.sk89q.worldedit.bukkit.selections;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import org.bukkit.Location;
import org.bukkit.World;

public abstract class RegionSelection implements Selection {
   private World world;
   private RegionSelector selector;
   private Region region;

   public RegionSelection(World world) {
      this.world = world;
   }

   public RegionSelection(World world, RegionSelector selector, Region region) {
      this.world = world;
      this.region = region;
      this.selector = selector;
   }

   protected Region getRegion() {
      return this.region;
   }

   protected void setRegion(Region region) {
      this.region = region;
   }

   public RegionSelector getRegionSelector() {
      return this.selector;
   }

   protected void setRegionSelector(RegionSelector selector) {
      this.selector = selector;
   }

   public Location getMinimumPoint() {
      return BukkitUtil.toLocation(this.world, this.region.getMinimumPoint());
   }

   public Vector getNativeMinimumPoint() {
      return this.region.getMinimumPoint();
   }

   public Location getMaximumPoint() {
      return BukkitUtil.toLocation(this.world, this.region.getMaximumPoint());
   }

   public Vector getNativeMaximumPoint() {
      return this.region.getMaximumPoint();
   }

   public World getWorld() {
      return this.world;
   }

   public int getArea() {
      return this.region.getArea();
   }

   public int getWidth() {
      return this.region.getWidth();
   }

   public int getHeight() {
      return this.region.getHeight();
   }

   public int getLength() {
      return this.region.getLength();
   }

   public boolean contains(Location pt) {
      return !pt.getWorld().equals(this.world) ? false : this.region.contains(BukkitUtil.toVector(pt));
   }
}
