package com.sk89q.worldedit.regions;

import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.polyhedron.Edge;
import com.sk89q.worldedit.regions.polyhedron.Triangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ConvexPolyhedralRegion extends AbstractRegion {
   private final Set vertices;
   private final List triangles;
   private final Set vertexBacklog;
   private Vector minimumPoint;
   private Vector maximumPoint;
   private Vector centerAccum;
   private Triangle lastTriangle;

   public ConvexPolyhedralRegion(LocalWorld world) {
      super(world);
      this.vertices = new LinkedHashSet();
      this.triangles = new ArrayList();
      this.vertexBacklog = new LinkedHashSet();
      this.centerAccum = Vector.ZERO;
   }

   public ConvexPolyhedralRegion(ConvexPolyhedralRegion region) {
      this(region.world);
      this.vertices.addAll(region.vertices);
      this.triangles.addAll(region.triangles);
      this.vertexBacklog.addAll(region.vertexBacklog);
      this.minimumPoint = region.minimumPoint;
      this.maximumPoint = region.maximumPoint;
      this.centerAccum = region.centerAccum;
      this.lastTriangle = region.lastTriangle;
   }

   public void clear() {
      this.vertices.clear();
      this.triangles.clear();
      this.vertexBacklog.clear();
      this.minimumPoint = null;
      this.maximumPoint = null;
      this.centerAccum = Vector.ZERO;
      this.lastTriangle = null;
   }

   public boolean addVertex(Vector vertex) {
      this.lastTriangle = null;
      if (this.vertices.contains(vertex)) {
         return false;
      } else {
         if (this.vertices.size() == 3) {
            if (this.vertexBacklog.contains(vertex)) {
               return false;
            }

            if (this.containsRaw(vertex)) {
               return this.vertexBacklog.add(vertex);
            }
         }

         this.vertices.add(vertex);
         this.centerAccum = this.centerAccum.add(vertex);
         if (this.minimumPoint == null) {
            this.minimumPoint = this.maximumPoint = vertex;
         } else {
            this.minimumPoint = Vector.getMinimum(this.minimumPoint, vertex);
            this.maximumPoint = Vector.getMaximum(this.maximumPoint, vertex);
         }

         switch (this.vertices.size()) {
            case 0:
            case 1:
            case 2:
               return true;
            case 3:
               Vector[] v = (Vector[])this.vertices.toArray(new Vector[this.vertices.size()]);
               this.triangles.add(new Triangle(v[0], v[1], v[2]));
               this.triangles.add(new Triangle(v[0], v[2], v[1]));
               return true;
            default:
               Set<Edge> borderEdges = new LinkedHashSet();
               Iterator<Triangle> it = this.triangles.iterator();

               while(it.hasNext()) {
                  Triangle triangle = (Triangle)it.next();
                  if (triangle.above(vertex)) {
                     it.remove();

                     for(int i = 0; i < 3; ++i) {
                        Edge edge = triangle.getEdge(i);
                        if (!borderEdges.remove(edge)) {
                           borderEdges.add(edge);
                        }
                     }
                  }
               }

               for(Edge edge : borderEdges) {
                  this.triangles.add(edge.createTriangle(vertex));
               }

               if (!this.vertexBacklog.isEmpty()) {
                  this.vertices.remove(vertex);
                  List<Vector> vertexBacklog2 = new ArrayList(this.vertexBacklog);
                  this.vertexBacklog.clear();

                  for(Vector vertex2 : vertexBacklog2) {
                     this.addVertex(vertex2);
                  }

                  this.vertices.add(vertex);
               }

               return true;
         }
      }
   }

   public boolean isDefined() {
      return !this.triangles.isEmpty();
   }

   public Vector getMinimumPoint() {
      return this.minimumPoint;
   }

   public Vector getMaximumPoint() {
      return this.maximumPoint;
   }

   public Vector getCenter() {
      return this.centerAccum.divide(this.vertices.size());
   }

   public void expand(Vector... changes) throws RegionOperationException {
   }

   public void contract(Vector... changes) throws RegionOperationException {
   }

   public void shift(Vector change) throws RegionOperationException {
      shiftCollection(this.vertices, change);
      shiftCollection(this.vertexBacklog, change);

      for(int i = 0; i < this.triangles.size(); ++i) {
         Triangle triangle = (Triangle)this.triangles.get(i);
         Vector v0 = change.add(triangle.getVertex(0));
         Vector v1 = change.add(triangle.getVertex(1));
         Vector v2 = change.add(triangle.getVertex(2));
         this.triangles.set(i, new Triangle(v0, v1, v2));
      }

      this.minimumPoint = change.add(this.minimumPoint);
      this.maximumPoint = change.add(this.maximumPoint);
      this.centerAccum = change.multiply(this.vertices.size()).add(this.centerAccum);
      this.lastTriangle = null;
   }

   private static void shiftCollection(Collection collection, Vector change) {
      List<Vector> tmp = new ArrayList(collection);
      collection.clear();

      for(Vector vertex : tmp) {
         collection.add(change.add(vertex));
      }

   }

   public boolean contains(Vector pt) {
      if (!this.isDefined()) {
         return false;
      } else {
         int x = pt.getBlockX();
         int y = pt.getBlockY();
         int z = pt.getBlockZ();
         Vector min = this.getMinimumPoint();
         Vector max = this.getMaximumPoint();
         if (x < min.getBlockX()) {
            return false;
         } else if (x > max.getBlockX()) {
            return false;
         } else if (y < min.getBlockY()) {
            return false;
         } else if (y > max.getBlockY()) {
            return false;
         } else if (z < min.getBlockZ()) {
            return false;
         } else {
            return z > max.getBlockZ() ? false : this.containsRaw(pt);
         }
      }
   }

   private boolean containsRaw(Vector pt) {
      if (this.lastTriangle != null && this.lastTriangle.above(pt)) {
         return false;
      } else {
         for(Triangle triangle : this.triangles) {
            if (this.lastTriangle != triangle && triangle.above(pt)) {
               this.lastTriangle = triangle;
               return false;
            }
         }

         return true;
      }
   }

   public Collection getVertices() {
      if (this.vertexBacklog.isEmpty()) {
         return this.vertices;
      } else {
         List<Vector> ret = new ArrayList(this.vertices);
         ret.addAll(this.vertexBacklog);
         return ret;
      }
   }

   public Collection getTriangles() {
      return this.triangles;
   }

   public AbstractRegion clone() {
      return new ConvexPolyhedralRegion(this);
   }
}
