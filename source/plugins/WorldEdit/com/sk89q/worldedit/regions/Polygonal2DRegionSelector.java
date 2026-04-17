package com.sk89q.worldedit.regions;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.cui.CUIRegion;
import com.sk89q.worldedit.cui.SelectionMinMaxEvent;
import com.sk89q.worldedit.cui.SelectionPoint2DEvent;
import com.sk89q.worldedit.cui.SelectionShapeEvent;
import java.util.Collections;
import java.util.List;

public class Polygonal2DRegionSelector implements RegionSelector, CUIRegion {
   private int maxPoints;
   private BlockVector pos1;
   private Polygonal2DRegion region;

   /** @deprecated */
   @Deprecated
   public Polygonal2DRegionSelector(LocalWorld world) {
      this((LocalWorld)world, 50);
   }

   public Polygonal2DRegionSelector(LocalWorld world, int maxPoints) {
      this.maxPoints = maxPoints;
      this.region = new Polygonal2DRegion(world);
   }

   /** @deprecated */
   @Deprecated
   public Polygonal2DRegionSelector(RegionSelector oldSelector) {
      this((RegionSelector)oldSelector, 50);
   }

   public Polygonal2DRegionSelector(RegionSelector oldSelector, int maxPoints) {
      this(oldSelector.getIncompleteRegion().getWorld(), maxPoints);
      if (oldSelector instanceof Polygonal2DRegionSelector) {
         Polygonal2DRegionSelector polygonal2DRegionSelector = (Polygonal2DRegionSelector)oldSelector;
         this.pos1 = polygonal2DRegionSelector.pos1;
         this.region = new Polygonal2DRegion(polygonal2DRegionSelector.region);
      } else {
         Region oldRegion;
         try {
            oldRegion = oldSelector.getRegion();
         } catch (IncompleteRegionException var7) {
            return;
         }

         int minY = oldRegion.getMinimumPoint().getBlockY();
         int maxY = oldRegion.getMaximumPoint().getBlockY();
         List<BlockVector2D> points = oldRegion.polygonize(maxPoints);
         this.pos1 = ((BlockVector2D)points.get(0)).toVector((double)minY).toBlockVector();
         this.region = new Polygonal2DRegion(oldRegion.getWorld(), points, minY, maxY);
      }

   }

   public Polygonal2DRegionSelector(LocalWorld world, List points, int minY, int maxY) {
      BlockVector2D pos2D = (BlockVector2D)points.get(0);
      this.pos1 = new BlockVector(pos2D.getX(), (double)minY, pos2D.getZ());
      this.region = new Polygonal2DRegion(world, points, minY, maxY);
   }

   public boolean selectPrimary(Vector pos) {
      if (pos.equals(this.pos1)) {
         return false;
      } else {
         this.pos1 = pos.toBlockVector();
         this.region = new Polygonal2DRegion(this.region.getWorld());
         this.region.addPoint(pos);
         this.region.expandY(pos.getBlockY());
         return true;
      }
   }

   public boolean selectSecondary(Vector pos) {
      if (this.region.size() > 0) {
         List<BlockVector2D> points = this.region.getPoints();
         BlockVector2D lastPoint = (BlockVector2D)points.get(this.region.size() - 1);
         if (lastPoint.getBlockX() == pos.getBlockX() && lastPoint.getBlockZ() == pos.getBlockZ()) {
            return false;
         }

         if (this.maxPoints >= 0 && points.size() > this.maxPoints) {
            return false;
         }
      }

      this.region.addPoint(pos);
      this.region.expandY(pos.getBlockY());
      return true;
   }

   public void explainPrimarySelection(LocalPlayer player, LocalSession session, Vector pos) {
      player.print("Starting a new polygon at " + pos + ".");
      session.dispatchCUIEvent(player, new SelectionShapeEvent(this.getTypeID()));
      session.dispatchCUIEvent(player, new SelectionPoint2DEvent(0, pos, this.getArea()));
      session.dispatchCUIEvent(player, new SelectionMinMaxEvent(this.region.getMinimumY(), this.region.getMaximumY()));
   }

   public void explainSecondarySelection(LocalPlayer player, LocalSession session, Vector pos) {
      player.print("Added point #" + this.region.size() + " at " + pos + ".");
      session.dispatchCUIEvent(player, new SelectionPoint2DEvent(this.region.size() - 1, pos, this.getArea()));
      session.dispatchCUIEvent(player, new SelectionMinMaxEvent(this.region.getMinimumY(), this.region.getMaximumY()));
   }

   public void explainRegionAdjust(LocalPlayer player, LocalSession session) {
      session.dispatchCUIEvent(player, new SelectionShapeEvent(this.getTypeID()));
      this.describeCUI(session, player);
   }

   public BlockVector getPrimaryPosition() throws IncompleteRegionException {
      if (this.pos1 == null) {
         throw new IncompleteRegionException();
      } else {
         return this.pos1;
      }
   }

   public Polygonal2DRegion getRegion() throws IncompleteRegionException {
      if (!this.isDefined()) {
         throw new IncompleteRegionException();
      } else {
         return this.region;
      }
   }

   public Polygonal2DRegion getIncompleteRegion() {
      return this.region;
   }

   public boolean isDefined() {
      return this.region.size() > 2;
   }

   public void learnChanges() {
      BlockVector2D pt = (BlockVector2D)this.region.getPoints().get(0);
      this.pos1 = new BlockVector(pt.getBlockX(), this.region.getMinimumPoint().getBlockY(), pt.getBlockZ());
   }

   public void clear() {
      this.pos1 = null;
      this.region = new Polygonal2DRegion(this.region.getWorld());
   }

   public String getTypeName() {
      return "2Dx1D polygon";
   }

   public List getInformationLines() {
      return Collections.singletonList("# points: " + this.region.size());
   }

   public int getArea() {
      return this.region.getArea();
   }

   public int getPointCount() {
      return this.region.getPoints().size();
   }

   public void describeCUI(LocalSession session, LocalPlayer player) {
      List<BlockVector2D> points = this.region.getPoints();

      for(int id = 0; id < points.size(); ++id) {
         session.dispatchCUIEvent(player, new SelectionPoint2DEvent(id, (Vector2D)points.get(id), this.getArea()));
      }

      session.dispatchCUIEvent(player, new SelectionMinMaxEvent(this.region.getMinimumY(), this.region.getMaximumY()));
   }

   public void describeLegacyCUI(LocalSession session, LocalPlayer player) {
      this.describeCUI(session, player);
   }

   public int getProtocolVersion() {
      return 0;
   }

   public String getTypeID() {
      return "polygon2d";
   }

   public String getLegacyTypeID() {
      return "polygon2d";
   }
}
