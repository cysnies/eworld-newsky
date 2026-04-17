package com.sk89q.worldedit.regions.polyhedron;

import com.sk89q.worldedit.Vector;

public class Triangle {
   private final Vector[] vertices;
   private final Vector normal;
   private final double b;
   String tag = "Triangle";

   public Triangle(Vector v0, Vector v1, Vector v2) {
      this.vertices = new Vector[]{v0, v1, v2};
      this.normal = v1.subtract(v0).cross(v2.subtract(v0)).normalize();
      this.b = Math.max(Math.max(this.normal.dot(v0), this.normal.dot(v1)), this.normal.dot(v2));
   }

   public Vector getVertex(int index) {
      return this.vertices[index];
   }

   public Edge getEdge(int index) {
      return index == this.vertices.length - 1 ? new Edge(this.vertices[index], this.vertices[0]) : new Edge(this.vertices[index], this.vertices[index + 1]);
   }

   public boolean below(Vector pt) {
      return this.normal.dot(pt) < this.b;
   }

   public boolean above(Vector pt) {
      return this.normal.dot(pt) > this.b;
   }

   public String toString() {
      return this.tag + "(" + this.vertices[0] + "," + this.vertices[1] + "," + this.vertices[2] + ")";
   }

   public Triangle tag(String tag) {
      this.tag = tag;
      return this;
   }
}
