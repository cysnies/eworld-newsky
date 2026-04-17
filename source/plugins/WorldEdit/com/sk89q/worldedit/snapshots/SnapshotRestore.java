package com.sk89q.worldedit.snapshots;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.data.Chunk;
import com.sk89q.worldedit.data.ChunkStore;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.data.MissingChunkException;
import com.sk89q.worldedit.data.MissingWorldException;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SnapshotRestore {
   private Map neededChunks = new LinkedHashMap();
   private ChunkStore chunkStore;
   private ArrayList missingChunks;
   private ArrayList errorChunks;
   private String lastErrorMessage;

   public SnapshotRestore(ChunkStore chunkStore, Region region) {
      this.chunkStore = chunkStore;
      if (region instanceof CuboidRegion) {
         this.findNeededCuboidChunks(region);
      } else {
         this.findNeededChunks(region);
      }

   }

   private void findNeededCuboidChunks(Region region) {
      Vector min = region.getMinimumPoint();
      Vector max = region.getMaximumPoint();

      for(int x = min.getBlockX(); x <= max.getBlockX(); ++x) {
         for(int y = min.getBlockY(); y <= max.getBlockY(); ++y) {
            for(int z = min.getBlockZ(); z <= max.getBlockZ(); ++z) {
               Vector pos = new Vector(x, y, z);
               BlockVector2D chunkPos = ChunkStore.toChunk(pos);
               if (!this.neededChunks.containsKey(chunkPos)) {
                  this.neededChunks.put(chunkPos, new ArrayList());
               }

               ((ArrayList)this.neededChunks.get(chunkPos)).add(pos);
            }
         }
      }

   }

   private void findNeededChunks(Region region) {
      for(Vector pos : region) {
         BlockVector2D chunkPos = ChunkStore.toChunk(pos);
         if (!this.neededChunks.containsKey(chunkPos)) {
            this.neededChunks.put(chunkPos, new ArrayList());
         }

         ((ArrayList)this.neededChunks.get(chunkPos)).add(pos);
      }

   }

   public int getChunksAffected() {
      return this.neededChunks.size();
   }

   public void restore(EditSession editSession) throws MaxChangedBlocksException {
      this.missingChunks = new ArrayList();
      this.errorChunks = new ArrayList();

      for(Map.Entry entry : this.neededChunks.entrySet()) {
         BlockVector2D chunkPos = (BlockVector2D)entry.getKey();

         try {
            Chunk chunk = this.chunkStore.getChunk(chunkPos, editSession.getWorld());

            for(Vector pos : (ArrayList)entry.getValue()) {
               try {
                  BaseBlock block = chunk.getBlock(pos);
                  editSession.rawSetBlock(pos, block);
               } catch (DataException var9) {
               }
            }
         } catch (MissingChunkException var10) {
            this.missingChunks.add(chunkPos);
         } catch (MissingWorldException me) {
            this.errorChunks.add(chunkPos);
            this.lastErrorMessage = me.getMessage();
         } catch (DataException de) {
            this.errorChunks.add(chunkPos);
            this.lastErrorMessage = de.getMessage();
         } catch (IOException ioe) {
            this.errorChunks.add(chunkPos);
            this.lastErrorMessage = ioe.getMessage();
         }
      }

   }

   public List getMissingChunks() {
      return this.missingChunks;
   }

   public List getErrorChunks() {
      return this.errorChunks;
   }

   public boolean hadTotalFailure() {
      return this.missingChunks.size() + this.errorChunks.size() == this.getChunksAffected();
   }

   public String getLastErrorMessage() {
      return this.lastErrorMessage;
   }
}
