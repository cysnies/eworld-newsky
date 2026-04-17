package com.sk89q.worldedit.regions;

import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import java.util.List;
import java.util.Set;

public interface Region extends Iterable, Cloneable {
   Vector getMinimumPoint();

   Vector getMaximumPoint();

   Vector getCenter();

   int getArea();

   int getWidth();

   int getHeight();

   int getLength();

   void expand(Vector... var1) throws RegionOperationException;

   void contract(Vector... var1) throws RegionOperationException;

   void shift(Vector var1) throws RegionOperationException;

   boolean contains(Vector var1);

   Set getChunks();

   Set getChunkCubes();

   LocalWorld getWorld();

   void setWorld(LocalWorld var1);

   Region clone();

   List polygonize(int var1);
}
