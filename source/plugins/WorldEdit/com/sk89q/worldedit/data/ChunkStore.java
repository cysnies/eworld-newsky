package com.sk89q.worldedit.data;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import java.io.IOException;
import java.util.Map;

public abstract class ChunkStore {
   public static final int CHUNK_SHIFTS = 4;

   public static BlockVector2D toChunk(Vector pos) {
      int chunkX = (int)Math.floor((double)pos.getBlockX() / (double)16.0F);
      int chunkZ = (int)Math.floor((double)pos.getBlockZ() / (double)16.0F);
      return new BlockVector2D(chunkX, chunkZ);
   }

   public abstract CompoundTag getChunkTag(Vector2D var1, LocalWorld var2) throws DataException, IOException;

   public Chunk getChunk(Vector2D pos, LocalWorld world) throws DataException, IOException {
      CompoundTag tag = this.getChunkTag(pos, world);
      Map<String, Tag> tags = tag.getValue();
      return (Chunk)(tags.containsKey("Sections") ? new AnvilChunk(world, tag) : new OldChunk(world, tag));
   }

   public void close() throws IOException {
   }

   public abstract boolean isValid();
}
