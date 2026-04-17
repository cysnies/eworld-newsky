package com.sk89q.worldedit.regions;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.cui.CUIRegion;
import com.sk89q.worldedit.cui.SelectionPointEvent;
import com.sk89q.worldedit.cui.SelectionPolygonEvent;
import com.sk89q.worldedit.cui.SelectionShapeEvent;
import com.sk89q.worldedit.regions.polyhedron.Triangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConvexPolyhedralRegionSelector implements RegionSelector, CUIRegion {
   private int maxVertices;
   private final ConvexPolyhedralRegion region;
   private BlockVector pos1;

   public ConvexPolyhedralRegionSelector(LocalWorld world, int maxVertices) {
      this.maxVertices = maxVertices;
      this.region = new ConvexPolyhedralRegion(world);
   }

   public ConvexPolyhedralRegionSelector(RegionSelector oldSelector, int maxVertices) {
      this.maxVertices = maxVertices;
      if (oldSelector instanceof ConvexPolyhedralRegionSelector) {
         ConvexPolyhedralRegionSelector convexPolyhedralRegionSelector = (ConvexPolyhedralRegionSelector)oldSelector;
         this.pos1 = convexPolyhedralRegionSelector.pos1;
         this.region = new ConvexPolyhedralRegion(convexPolyhedralRegionSelector.region);
      } else {
         Region oldRegion;
         try {
            oldRegion = oldSelector.getRegion();
         } catch (IncompleteRegionException var8) {
            this.region = new ConvexPolyhedralRegion(oldSelector.getIncompleteRegion().getWorld());
            return;
         }

         int minY = oldRegion.getMinimumPoint().getBlockY();
         int maxY = oldRegion.getMaximumPoint().getBlockY();
         this.region = new ConvexPolyhedralRegion(oldRegion.getWorld());

         for(BlockVector2D pt : new ArrayList(oldRegion.polygonize(maxVertices < 0 ? maxVertices : maxVertices / 2))) {
            this.region.addVertex(pt.toVector((double)minY));
            this.region.addVertex(pt.toVector((double)maxY));
         }

         this.learnChanges();
      }

   }

   public boolean selectPrimary(Vector pos) {
      this.clear();
      this.pos1 = pos.toBlockVector();
      return this.region.addVertex(pos);
   }

   public boolean selectSecondary(Vector pos) {
      return this.maxVertices >= 0 && this.region.getVertices().size() > this.maxVertices ? false : this.region.addVertex(pos);
   }

   public BlockVector getPrimaryPosition() throws IncompleteRegionException {
      return this.pos1;
   }

   public Region getRegion() throws IncompleteRegionException {
      if (!this.region.isDefined()) {
         throw new IncompleteRegionException();
      } else {
         return this.region;
      }
   }

   public Region getIncompleteRegion() {
      return this.region;
   }

   public boolean isDefined() {
      return this.region.isDefined();
   }

   public int getArea() {
      return this.region.getArea();
   }

   public void learnChanges() {
      this.pos1 = ((Vector)this.region.getVertices().iterator().next()).toBlockVector();
   }

   public void clear() {
      this.region.clear();
   }

   public String getTypeName() {
      return "Convex Polyhedron";
   }

   public List getInformationLines() {
      List<String> ret = new ArrayList();
      ret.add("Vertices: " + this.region.getVertices().size());
      ret.add("Triangles: " + this.region.getTriangles().size());
      return ret;
   }

   public void explainPrimarySelection(LocalPlayer player, LocalSession session, Vector pos) {
      session.describeCUI(player);
      player.print("Started new selection with vertex " + pos + ".");
   }

   public void explainSecondarySelection(LocalPlayer player, LocalSession session, Vector pos) {
      session.describeCUI(player);
      player.print("Added vertex " + pos + " to the selection.");
   }

   public void explainRegionAdjust(LocalPlayer player, LocalSession session) {
      session.describeCUI(player);
   }

   public int getProtocolVersion() {
      return 3;
   }

   public String getTypeID() {
      return "polyhedron";
   }

   public void describeCUI(LocalSession session, LocalPlayer player) {
      Collection<Vector> vertices = this.region.getVertices();
      Collection<Triangle> triangles = this.region.getTriangles();
      player.dispatchCUIEvent(new SelectionShapeEvent(this.getTypeID()));
      Map<Vector, Integer> vertexIds = new HashMap(vertices.size());
      int lastVertexId = -1;

      for(Vector vertex : vertices) {
         ++lastVertexId;
         vertexIds.put(vertex, lastVertexId);
         session.dispatchCUIEvent(player, new SelectionPointEvent(lastVertexId, vertex, this.getArea()));
      }

      for(Triangle triangle : triangles) {
         int[] v = new int[3];

         for(int i = 0; i < 3; ++i) {
            v[i] = (Integer)vertexIds.get(triangle.getVertex(i));
         }

         session.dispatchCUIEvent(player, new SelectionPolygonEvent(v));
      }

   }

   public String getLegacyTypeID() {
      return "cuboid";
   }

   public void describeLegacyCUI(LocalSession session, LocalPlayer player) {
      if (this.isDefined()) {
         session.dispatchCUIEvent(player, new SelectionPointEvent(0, this.region.getMinimumPoint(), this.getArea()));
         session.dispatchCUIEvent(player, new SelectionPointEvent(1, this.region.getMaximumPoint(), this.getArea()));
      } else {
         session.dispatchCUIEvent(player, new SelectionShapeEvent(this.getLegacyTypeID()));
      }

   }
}
