package com.sk89q.worldedit.snapshots;

import com.sk89q.worldedit.data.ChunkStore;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.data.FileLegacyChunkStore;
import com.sk89q.worldedit.data.FileMcRegionChunkStore;
import com.sk89q.worldedit.data.TrueZipLegacyChunkStore;
import com.sk89q.worldedit.data.TrueZipMcRegionChunkStore;
import com.sk89q.worldedit.data.ZippedLegacyChunkStore;
import com.sk89q.worldedit.data.ZippedMcRegionChunkStore;
import de.schlichtherle.util.zip.ZipFile;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Logger;

public class Snapshot implements Comparable {
   protected static Logger logger = Logger.getLogger("Minecraft.WorldEdit");
   protected File file;
   protected String name;
   protected Calendar date;

   public Snapshot(SnapshotRepository repo, String snapshot) {
      this.file = new File(repo.getDirectory(), snapshot);
      this.name = snapshot;
   }

   public ChunkStore getChunkStore() throws IOException, DataException {
      ChunkStore chunkStore = this._getChunkStore();
      logger.info("WorldEdit: Using " + chunkStore.getClass().getCanonicalName() + " for loading snapshot '" + this.file.getAbsolutePath() + "'");
      return chunkStore;
   }

   public ChunkStore _getChunkStore() throws IOException, DataException {
      if (this.file.getName().toLowerCase().endsWith(".zip")) {
         try {
            ChunkStore chunkStore = new TrueZipMcRegionChunkStore(this.file);
            return (ChunkStore)(!chunkStore.isValid() ? new TrueZipLegacyChunkStore(this.file) : chunkStore);
         } catch (NoClassDefFoundError var4) {
            ChunkStore chunkStore = new ZippedMcRegionChunkStore(this.file);
            return (ChunkStore)(!chunkStore.isValid() ? new ZippedLegacyChunkStore(this.file) : chunkStore);
         }
      } else if (!this.file.getName().toLowerCase().endsWith(".tar.bz2") && !this.file.getName().toLowerCase().endsWith(".tar.gz") && !this.file.getName().toLowerCase().endsWith(".tar")) {
         ChunkStore chunkStore = new FileMcRegionChunkStore(this.file);
         return (ChunkStore)(!chunkStore.isValid() ? new FileLegacyChunkStore(this.file) : chunkStore);
      } else {
         try {
            ChunkStore chunkStore = new TrueZipMcRegionChunkStore(this.file);
            return (ChunkStore)(!chunkStore.isValid() ? new TrueZipLegacyChunkStore(this.file) : chunkStore);
         } catch (NoClassDefFoundError var3) {
            throw new DataException("TrueZIP is required for .tar support");
         }
      }
   }

   public boolean containsWorld(String worldname) {
      try {
         if (!this.file.getName().toLowerCase().endsWith(".zip")) {
            if (!this.file.getName().toLowerCase().endsWith(".tar.bz2") && !this.file.getName().toLowerCase().endsWith(".tar.gz") && !this.file.getName().toLowerCase().endsWith(".tar")) {
               return this.file.getName().equalsIgnoreCase(worldname);
            }

            try {
               ZipFile entry = new ZipFile(this.file);
               return entry.getEntry(worldname) != null;
            } catch (NoClassDefFoundError var3) {
               throw new DataException("TrueZIP is required for .tar support");
            }
         }

         java.util.zip.ZipFile entry = new java.util.zip.ZipFile(this.file);
         return entry.getEntry(worldname) != null || entry.getEntry(worldname + "/level.dat") != null;
      } catch (IOException var4) {
         logger.info("Could not load snapshot: " + this.file.getPath());
      } catch (DataException var5) {
      }

      return false;
   }

   public String getName() {
      return this.name;
   }

   public File getFile() {
      return this.file;
   }

   public Calendar getDate() {
      return this.date;
   }

   public void setDate(Calendar date) {
      this.date = date;
   }

   public int compareTo(Snapshot o) {
      if (o.date != null && this.date != null) {
         return this.date.compareTo(o.date);
      } else {
         int i = this.name.indexOf("/");
         int j = o.name.indexOf("/");
         return this.name.substring(i > 0 ? 0 : i).compareTo(o.name.substring(j > 0 ? 0 : j));
      }
   }

   public boolean equals(Object o) {
      return o instanceof Snapshot ? this.file.equals(((Snapshot)o).file) : false;
   }
}
