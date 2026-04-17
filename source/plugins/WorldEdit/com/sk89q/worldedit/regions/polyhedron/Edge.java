package com.sk89q.worldedit.regions.polyhedron;

import com.sk89q.worldedit.Vector;

public class Edge {
   private final Vector start;
   private final Vector end;

   public Edge(Vector start, Vector end) {
      this.start = start;
      this.end = end;
   }

   public boolean equals(Object other) {
      if (!(other instanceof Edge)) {
         return false;
      } else {
         Edge otherEdge = (Edge)other;
         if (this.start == otherEdge.end && this.end == otherEdge.start) {
            return true;
         } else {
            return this.end == otherEdge.end && this.start == otherEdge.start;
         }
      }
   }

   public int hashCode() {
      return this.start.hashCode() ^ this.end.hashCode();
   }

   public String toString() {
      return "(" + this.start + "," + this.end + ")";
   }

   public Triangle createTriangle(Vector vertex) {
      return new Triangle(this.start, this.end, vertex);
   }

   public Triangle createTriangle2(Vector vertex) {
      return new Triangle(this.start, vertex, this.end);
   }
}
