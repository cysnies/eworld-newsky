package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.base.Objects;
import java.lang.reflect.Constructor;
import org.bukkit.util.Vector;

public class ChunkPosition {
   public static ChunkPosition ORIGIN = new ChunkPosition(0, 0, 0);
   private static Constructor chunkPositionConstructor;
   protected final int x;
   protected final int y;
   protected final int z;
   private static StructureModifier intModifier;

   public ChunkPosition(int x, int y, int z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public ChunkPosition(Vector vector) {
      if (vector == null) {
         throw new IllegalArgumentException("Vector cannot be NULL.");
      } else {
         this.x = vector.getBlockX();
         this.y = vector.getBlockY();
         this.z = vector.getBlockZ();
      }
   }

   public Vector toVector() {
      return new Vector(this.x, this.y, this.z);
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public int getZ() {
      return this.z;
   }

   public ChunkPosition add(ChunkPosition other) {
      if (other == null) {
         throw new IllegalArgumentException("other cannot be NULL");
      } else {
         return new ChunkPosition(this.x + other.x, this.y + other.y, this.z + other.z);
      }
   }

   public ChunkPosition subtract(ChunkPosition other) {
      if (other == null) {
         throw new IllegalArgumentException("other cannot be NULL");
      } else {
         return new ChunkPosition(this.x - other.x, this.y - other.y, this.z - other.z);
      }
   }

   public ChunkPosition multiply(int factor) {
      return new ChunkPosition(this.x * factor, this.y * factor, this.z * factor);
   }

   public ChunkPosition divide(int divisor) {
      if (divisor == 0) {
         throw new IllegalArgumentException("Cannot divide by null.");
      } else {
         return new ChunkPosition(this.x / divisor, this.y / divisor, this.z / divisor);
      }
   }

   public static EquivalentConverter getConverter() {
      return new EquivalentConverter() {
         public Object getGeneric(Class genericType, ChunkPosition specific) {
            if (ChunkPosition.chunkPositionConstructor == null) {
               try {
                  ChunkPosition.chunkPositionConstructor = MinecraftReflection.getChunkPositionClass().getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE);
               } catch (Exception e) {
                  throw new RuntimeException("Cannot find chunk position constructor.", e);
               }
            }

            try {
               return ChunkPosition.chunkPositionConstructor.newInstance(specific.x, specific.y, specific.z);
            } catch (Exception e) {
               throw new RuntimeException("Cannot construct ChunkPosition.", e);
            }
         }

         public ChunkPosition getSpecific(Object generic) {
            if (MinecraftReflection.isChunkPosition(generic)) {
               ChunkPosition.intModifier = (new StructureModifier(generic.getClass(), (Class)null, false)).withType(Integer.TYPE);
               if (ChunkPosition.intModifier.size() < 3) {
                  throw new IllegalStateException("Cannot read class " + generic.getClass() + " for its integer fields.");
               }

               if (ChunkPosition.intModifier.size() >= 3) {
                  try {
                     StructureModifier<Integer> instance = ChunkPosition.intModifier.withTarget(generic);
                     return new ChunkPosition((Integer)instance.read(0), (Integer)instance.read(1), (Integer)instance.read(2));
                  } catch (FieldAccessException e) {
                     throw new RuntimeException("Field access error.", e);
                  }
               }
            }

            return null;
         }

         public Class getSpecificType() {
            return ChunkPosition.class;
         }
      };
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (!(obj instanceof ChunkPosition)) {
         return false;
      } else {
         ChunkPosition other = (ChunkPosition)obj;
         return this.x == other.x && this.y == other.y && this.z == other.z;
      }
   }

   public int hashCode() {
      return Objects.hashCode(new Object[]{this.x, this.y, this.z});
   }

   public String toString() {
      return "ChunkPosition [x=" + this.x + ", y=" + this.y + ", z=" + this.z + "]";
   }
}
