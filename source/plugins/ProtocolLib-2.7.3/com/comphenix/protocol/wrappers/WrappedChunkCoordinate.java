package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.base.Objects;

public class WrappedChunkCoordinate implements Comparable {
   private static final boolean LARGER_THAN_NULL = true;
   protected Comparable handle;
   private static StructureModifier intModifier;

   public WrappedChunkCoordinate() {
      try {
         this.handle = (Comparable)MinecraftReflection.getChunkCoordinatesClass().newInstance();
         this.initializeModifier();
      } catch (Exception var2) {
         throw new RuntimeException("Cannot construct chunk coordinate.");
      }
   }

   public WrappedChunkCoordinate(Comparable handle) {
      if (handle == null) {
         throw new IllegalArgumentException("handle cannot be NULL");
      } else {
         this.handle = handle;
         this.initializeModifier();
      }
   }

   private void initializeModifier() {
      if (intModifier == null) {
         intModifier = (new StructureModifier(this.handle.getClass(), (Class)null, false)).withType(Integer.TYPE);
      }

   }

   public WrappedChunkCoordinate(int x, int y, int z) {
      this();
      this.setX(x);
      this.setY(y);
      this.setZ(z);
   }

   public WrappedChunkCoordinate(ChunkPosition position) {
      this(position.getX(), position.getY(), position.getZ());
   }

   public Object getHandle() {
      return this.handle;
   }

   public int getX() {
      return (Integer)intModifier.read(0);
   }

   public void setX(int newX) {
      intModifier.write(0, newX);
   }

   public int getY() {
      return (Integer)intModifier.read(1);
   }

   public void setY(int newY) {
      intModifier.write(1, newY);
   }

   public int getZ() {
      return (Integer)intModifier.read(2);
   }

   public void setZ(int newZ) {
      intModifier.write(2, newZ);
   }

   public ChunkPosition toPosition() {
      return new ChunkPosition(this.getX(), this.getY(), this.getZ());
   }

   public int compareTo(WrappedChunkCoordinate other) {
      return other.handle == null ? -1 : this.handle.compareTo(other.handle);
   }

   public boolean equals(Object other) {
      if (other instanceof WrappedChunkCoordinate) {
         WrappedChunkCoordinate wrapper = (WrappedChunkCoordinate)other;
         return Objects.equal(this.handle, wrapper.handle);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.handle.hashCode();
   }

   public String toString() {
      return String.format("ChunkCoordinate [x: %s, y: %s, z: %s]", this.getX(), this.getY(), this.getZ());
   }
}
