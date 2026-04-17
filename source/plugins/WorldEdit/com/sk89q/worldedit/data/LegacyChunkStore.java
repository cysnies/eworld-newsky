package com.sk89q.worldedit.data;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public abstract class LegacyChunkStore extends ChunkStore {
   public static String getFilename(Vector2D pos, String separator) {
      int x = pos.getBlockX();
      int z = pos.getBlockZ();
      String folder1 = Integer.toString(divisorMod(x, 64), 36);
      String folder2 = Integer.toString(divisorMod(z, 64), 36);
      String filename = "c." + Integer.toString(x, 36) + "." + Integer.toString(z, 36) + ".dat";
      return folder1 + separator + folder2 + separator + filename;
   }

   public static String getFilename(Vector2D pos) {
      return getFilename(pos, File.separator);
   }

   public CompoundTag getChunkTag(Vector2D pos, LocalWorld world) throws DataException, IOException {
      int x = pos.getBlockX();
      int z = pos.getBlockZ();
      String folder1 = Integer.toString(divisorMod(x, 64), 36);
      String folder2 = Integer.toString(divisorMod(z, 64), 36);
      String filename = "c." + Integer.toString(x, 36) + "." + Integer.toString(z, 36) + ".dat";
      InputStream stream = this.getInputStream(folder1, folder2, filename);
      NBTInputStream nbt = new NBTInputStream(new GZIPInputStream(stream));

      CompoundTag var18;
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

         var18 = rootTag;
      } finally {
         nbt.close();
      }

      return var18;
   }

   private static int divisorMod(int a, int n) {
      return (int)((double)a - (double)n * Math.floor(Math.floor((double)a) / (double)n));
   }

   protected abstract InputStream getInputStream(String var1, String var2, String var3) throws IOException, DataException;
}
