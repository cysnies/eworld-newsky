package com.sk89q.worldedit.regions;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.cui.CUIRegion;
import com.sk89q.worldedit.cui.SelectionEllipsoidPointEvent;
import com.sk89q.worldedit.cui.SelectionPointEvent;
import java.util.ArrayList;
import java.util.List;

public class EllipsoidRegionSelector implements RegionSelector, CUIRegion {
   protected EllipsoidRegion region;

   public EllipsoidRegionSelector(LocalWorld world) {
      this.region = new EllipsoidRegion(world, new Vector(), new Vector());
   }

   public EllipsoidRegionSelector() {
      this((LocalWorld)null);
   }

   public EllipsoidRegionSelector(RegionSelector oldSelector) {
      this(oldSelector.getIncompleteRegion().getWorld());
      if (oldSelector instanceof EllipsoidRegionSelector) {
         EllipsoidRegionSelector ellipsoidRegionSelector = (EllipsoidRegionSelector)oldSelector;
         this.region = new EllipsoidRegion(ellipsoidRegionSelector.getIncompleteRegion());
      } else {
         Region oldRegion = null;

         try {
            oldRegion = oldSelector.getRegion();
         } catch (IncompleteRegionException var6) {
            return;
         }

         BlockVector pos1 = oldRegion.getMinimumPoint().toBlockVector();
         BlockVector pos2 = oldRegion.getMaximumPoint().toBlockVector();
         Vector center = pos1.add(pos2).divide(2).floor();
         this.region.setCenter(center);
         this.region.setRadius(pos2.subtract(center));
      }

   }

   public EllipsoidRegionSelector(LocalWorld world, Vector center, Vector radius) {
      this(world);
      this.region.setCenter(center);
      this.region.setRadius(radius);
   }

   public boolean selectPrimary(Vector pos) {
      if (pos.equals(this.region.getCenter()) && this.region.getRadius().lengthSq() == (double)0.0F) {
         return false;
      } else {
         this.region.setCenter(pos.toBlockVector());
         this.region.setRadius(new Vector());
         return true;
      }
   }

   public boolean selectSecondary(Vector pos) {
      Vector diff = pos.subtract(this.region.getCenter());
      Vector minRadius = Vector.getMaximum(diff, diff.multiply((double)-1.0F));
      this.region.extendRadius(minRadius);
      return true;
   }

   public void explainPrimarySelection(LocalPlayer player, LocalSession session, Vector pos) {
      if (this.isDefined()) {
         player.print("Center position set to " + this.region.getCenter() + " (" + this.region.getArea() + ").");
      } else {
         player.print("Center position set to " + this.region.getCenter() + ".");
      }

      session.describeCUI(player);
   }

   public void explainSecondarySelection(LocalPlayer player, LocalSession session, Vector pos) {
      if (this.isDefined()) {
         player.print("Radius set to " + this.region.getRadius() + " (" + this.region.getArea() + ").");
      } else {
         player.print("Radius set to " + this.region.getRadius() + ".");
      }

      session.describeCUI(player);
   }

   public void explainRegionAdjust(LocalPlayer player, LocalSession session) {
      session.describeCUI(player);
   }

   public boolean isDefined() {
      return this.region.getRadius().lengthSq() > (double)0.0F;
   }

   public EllipsoidRegion getRegion() throws IncompleteRegionException {
      if (!this.isDefined()) {
         throw new IncompleteRegionException();
      } else {
         return this.region;
      }
   }

   public EllipsoidRegion getIncompleteRegion() {
      return this.region;
   }

   public void learnChanges() {
   }

   public void clear() {
      this.region.setCenter(new Vector());
      this.region.setRadius(new Vector());
   }

   public String getTypeName() {
      return "ellipsoid";
   }

   public List getInformationLines() {
      List<String> lines = new ArrayList();
      Vector center = this.region.getCenter();
      if (center.lengthSq() > (double)0.0F) {
         lines.add("Center: " + center);
      }

      Vector radius = this.region.getRadius();
      if (radius.lengthSq() > (double)0.0F) {
         lines.add("X/Y/Z radius: " + radius);
      }

      return lines;
   }

   public int getArea() {
      return this.region.getArea();
   }

   public void describeCUI(LocalSession session, LocalPlayer player) {
      session.dispatchCUIEvent(player, new SelectionEllipsoidPointEvent(0, this.region.getCenter()));
      session.dispatchCUIEvent(player, new SelectionEllipsoidPointEvent(1, this.region.getRadius()));
   }

   public void describeLegacyCUI(LocalSession session, LocalPlayer player) {
      session.dispatchCUIEvent(player, new SelectionPointEvent(0, this.region.getMinimumPoint(), this.getArea()));
      session.dispatchCUIEvent(player, new SelectionPointEvent(1, this.region.getMaximumPoint(), this.getArea()));
   }

   public String getLegacyTypeID() {
      return "cuboid";
   }

   public int getProtocolVersion() {
      return 1;
   }

   public String getTypeID() {
      return "ellipsoid";
   }

   public BlockVector getPrimaryPosition() throws IncompleteRegionException {
      return this.region.getCenter().toBlockVector();
   }
}
