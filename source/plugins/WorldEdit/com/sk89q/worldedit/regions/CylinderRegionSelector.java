package com.sk89q.worldedit.regions;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.cui.CUIRegion;
import com.sk89q.worldedit.cui.SelectionCylinderEvent;
import com.sk89q.worldedit.cui.SelectionMinMaxEvent;
import com.sk89q.worldedit.cui.SelectionPointEvent;
import com.sk89q.worldedit.cui.SelectionShapeEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class CylinderRegionSelector implements RegionSelector, CUIRegion {
   protected CylinderRegion region;
   protected static final NumberFormat format = (NumberFormat)NumberFormat.getInstance().clone();

   public CylinderRegionSelector(LocalWorld world) {
      this.region = new CylinderRegion(world);
   }

   public CylinderRegionSelector(RegionSelector oldSelector) {
      this(oldSelector.getIncompleteRegion().getWorld());
      if (oldSelector instanceof CylinderRegionSelector) {
         CylinderRegionSelector cylSelector = (CylinderRegionSelector)oldSelector;
         this.region = new CylinderRegion(cylSelector.region);
      } else {
         Region oldRegion;
         try {
            oldRegion = oldSelector.getRegion();
         } catch (IncompleteRegionException var6) {
            return;
         }

         Vector pos1 = oldRegion.getMinimumPoint();
         Vector pos2 = oldRegion.getMaximumPoint();
         Vector center = pos1.add(pos2).divide(2).floor();
         this.region.setCenter(center.toVector2D());
         this.region.setRadius(pos2.toVector2D().subtract(center.toVector2D()));
         this.region.setMaximumY(Math.max(pos1.getBlockY(), pos2.getBlockY()));
         this.region.setMinimumY(Math.min(pos1.getBlockY(), pos2.getBlockY()));
      }

   }

   public CylinderRegionSelector(LocalWorld world, Vector2D center, Vector2D radius, int minY, int maxY) {
      this(world);
      this.region.setCenter(center);
      this.region.setRadius(radius);
      this.region.setMinimumY(Math.min(minY, maxY));
      this.region.setMaximumY(Math.max(minY, maxY));
   }

   public boolean selectPrimary(Vector pos) {
      if (!this.region.getCenter().equals(Vector.ZERO) && pos.compareTo(this.region.getCenter()) == 0) {
         return false;
      } else {
         this.region = new CylinderRegion(this.region.getWorld());
         this.region.setCenter(pos.toVector2D());
         this.region.setY(pos.getBlockY());
         return true;
      }
   }

   public boolean selectSecondary(Vector pos) {
      Vector center = this.region.getCenter();
      if (center.compareTo(Vector.ZERO) == 0) {
         return true;
      } else {
         Vector2D diff = pos.subtract(center).toVector2D();
         Vector2D minRadius = Vector2D.getMaximum(diff, diff.multiply((double)-1.0F));
         this.region.extendRadius(minRadius);
         this.region.setY(pos.getBlockY());
         return true;
      }
   }

   public void explainPrimarySelection(LocalPlayer player, LocalSession session, Vector pos) {
      player.print("Starting a new cylindrical selection at " + pos + ".");
      session.describeCUI(player);
   }

   public void explainSecondarySelection(LocalPlayer player, LocalSession session, Vector pos) {
      Vector center = this.region.getCenter();
      if (!center.equals(Vector.ZERO)) {
         player.print("Radius set to " + format.format(this.region.getRadius().getX()) + "/" + format.format(this.region.getRadius().getZ()) + " blocks. (" + this.region.getArea() + ").");
         session.describeCUI(player);
      } else {
         player.printError("You must select the center point before setting the radius.");
      }
   }

   public void explainRegionAdjust(LocalPlayer player, LocalSession session) {
      session.describeCUI(player);
   }

   public BlockVector getPrimaryPosition() throws IncompleteRegionException {
      if (!this.isDefined()) {
         throw new IncompleteRegionException();
      } else {
         return this.region.getCenter().toBlockVector();
      }
   }

   public CylinderRegion getRegion() throws IncompleteRegionException {
      if (!this.isDefined()) {
         throw new IncompleteRegionException();
      } else {
         return this.region;
      }
   }

   public CylinderRegion getIncompleteRegion() {
      return this.region;
   }

   public boolean isDefined() {
      return !this.region.getRadius().equals(Vector2D.ZERO);
   }

   public void learnChanges() {
   }

   public void clear() {
      this.region = new CylinderRegion(this.region.getWorld());
   }

   public String getTypeName() {
      return "Cylinder";
   }

   public List getInformationLines() {
      List<String> lines = new ArrayList();
      if (!this.region.getCenter().equals(Vector.ZERO)) {
         lines.add("Center: " + this.region.getCenter());
      }

      if (!this.region.getRadius().equals(Vector2D.ZERO)) {
         lines.add("Radius: " + this.region.getRadius());
      }

      return lines;
   }

   public int getArea() {
      return this.region.getArea();
   }

   public void describeCUI(LocalSession session, LocalPlayer player) {
      session.dispatchCUIEvent(player, new SelectionCylinderEvent(this.region.getCenter(), this.region.getRadius()));
      session.dispatchCUIEvent(player, new SelectionMinMaxEvent(this.region.getMinimumY(), this.region.getMaximumY()));
   }

   public void describeLegacyCUI(LocalSession session, LocalPlayer player) {
      if (this.isDefined()) {
         session.dispatchCUIEvent(player, new SelectionPointEvent(0, this.region.getMinimumPoint(), this.getArea()));
         session.dispatchCUIEvent(player, new SelectionPointEvent(1, this.region.getMaximumPoint(), this.getArea()));
      } else {
         session.dispatchCUIEvent(player, new SelectionShapeEvent(this.getLegacyTypeID()));
      }

   }

   public int getProtocolVersion() {
      return 1;
   }

   public String getTypeID() {
      return "cylinder";
   }

   public String getLegacyTypeID() {
      return "cuboid";
   }

   static {
      format.setMaximumFractionDigits(3);
   }
}
