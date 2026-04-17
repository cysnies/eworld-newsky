package com.sk89q.worldedit.data;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public abstract class McRegionChunkStore extends ChunkStore {
   protected String curFilename = null;
   protected McRegionReader cachedReader = null;

   public static String getFilename(Vector2D pos) {
      int x = pos.getBlockX();
      int z = pos.getBlockZ();
      String filename = "r." + (x >> 5) + "." + (z >> 5) + ".mca";
      return filename;
   }

   protected McRegionReader getReader(Vector2D pos, String worldname) throws DataException, IOException {
      String filename = getFilename(pos);
      if (this.curFilename != null) {
         if (this.curFilename.equals(filename)) {
            return this.cachedReader;
         }

         try {
            this.cachedReader.close();
         } catch (IOException var5) {
         }
      }

      InputStream stream = this.getInputStream(filename, worldname);
      this.cachedReader = new McRegionReader(stream);
      return this.cachedReader;
   }

   public CompoundTag getChunkTag(Vector2D pos, LocalWorld world) throws DataException, IOException {
      McRegionReader reader = this.getReader(pos, world.getName());
      InputStream stream = reader.getChunkInputStream(pos);
      NBTInputStream nbt = new NBTInputStream(stream);

      CompoundTag var14;
      try {
         Tag tag = nbt.readTag();
         if (!(tag instanceof CompoundTag)) {
            throw new ChunkStoreException("CompoundTag expected for chunk; got " + tag.getClass().getName());
         }

         Map<String, Tag> children = ((CompoundTag)tag).getValue();
         CompoundTag rootTag = null;

         for(Map.Entry entry : children.entrySet()) {
            if (((String)entry.getKey()).equals("Level")) {
               if (!(entry.getValue() instanceof CompoundTag)) {
                  throw new ChunkStoreException("CompoundTag expected for 'Level'; got " + ((Tag)entry.getValue()).getClass().getName());
               }

               rootTag = (CompoundTag)entry.getValue();
               break;
            }
         }

         if (rootTag == null) {
            throw new ChunkStoreException("Missing root 'Level' tag");
         }

         var14 = rootTag;
      } finally {
         nbt.close();
      }

      return var14;
   }

   protected abstract InputStream getInputStream(String var1, String var2) throws IOException, DataException;

   public void close() throws IOException {
      if (this.cachedReader != null) {
         this.cachedReader.close();
      }

   }
}
