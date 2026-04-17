package com.wimbli.WorldBorder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WorldTrimTask implements Runnable {
   private transient Server server = null;
   private transient World world = null;
   private transient WorldFileData worldData = null;
   private transient BorderData border = null;
   private transient boolean readyToGo = false;
   private transient boolean paused = false;
   private transient int taskID = -1;
   private transient Player notifyPlayer = null;
   private transient int chunksPerRun = 1;
   private transient int currentRegion = -1;
   private transient int regionX = 0;
   private transient int regionZ = 0;
   private transient int currentChunk = 0;
   private transient List regionChunks = new ArrayList(1024);
   private transient List trimChunks = new ArrayList(1024);
   private transient int counter = 0;
   private transient long lastReport = Config.Now();
   private transient int reportTarget = 0;
   private transient int reportTotal = 0;
   private transient int reportTrimmedRegions = 0;
   private transient int reportTrimmedChunks = 0;

   public WorldTrimTask(Server theServer, Player player, String worldName, int trimDistance, int chunksPerRun) {
      this.server = theServer;
      this.notifyPlayer = player;
      this.chunksPerRun = chunksPerRun;
      this.world = this.server.getWorld(worldName);
      if (this.world == null) {
         if (worldName.isEmpty()) {
            this.sendMessage("You must specify a world!");
         } else {
            this.sendMessage("World \"" + worldName + "\" not found!");
         }

         this.stop();
      } else {
         this.border = Config.Border(worldName) == null ? null : Config.Border(worldName).copy();
         if (this.border == null) {
            this.sendMessage("No border found for world \"" + worldName + "\"!");
            this.stop();
         } else {
            this.border.setRadiusX(this.border.getRadiusX() + trimDistance);
            this.border.setRadiusZ(this.border.getRadiusZ() + trimDistance);
            this.worldData = WorldFileData.create(this.world, this.notifyPlayer);
            if (this.worldData == null) {
               this.stop();
            } else {
               this.reportTarget = this.worldData.regionFileCount() * 3072;
               if (this.nextFile()) {
                  this.readyToGo = true;
               }
            }
         }
      }
   }

   public void setTaskID(int ID) {
      this.taskID = ID;
   }

   public void run() {
      if (this.server != null && this.readyToGo && !this.paused) {
         this.readyToGo = false;
         long loopStartTime = Config.Now();
         this.counter = 0;

         while(this.counter <= this.chunksPerRun) {
            if (this.paused) {
               return;
            }

            long now = Config.Now();
            if (now > this.lastReport + 5000L) {
               this.reportProgress();
            }

            if (now > loopStartTime + 45L) {
               this.readyToGo = true;
               return;
            }

            if (this.regionChunks.isEmpty()) {
               this.addCornerChunks();
            } else if (this.currentChunk == 4) {
               if (this.trimChunks.isEmpty()) {
                  this.counter += 4;
                  this.nextFile();
                  continue;
               }

               this.addEdgeChunks();
               this.addInnerChunks();
            } else {
               if (this.currentChunk == 124 && this.trimChunks.size() == 124) {
                  this.counter += 16;
                  this.trimChunks = this.regionChunks;
                  this.unloadChunks();
                  ++this.reportTrimmedRegions;
                  File regionFile = this.worldData.regionFile(this.currentRegion);
                  if (!regionFile.delete()) {
                     this.sendMessage("Error! Region file which is outside the border could not be deleted: " + regionFile.getName());
                     this.wipeChunks();
                  }

                  this.nextFile();
                  continue;
               }

               if (this.currentChunk == 1024) {
                  this.counter += 32;
                  this.unloadChunks();
                  this.wipeChunks();
                  this.nextFile();
                  continue;
               }
            }

            CoordXZ chunk = (CoordXZ)this.regionChunks.get(this.currentChunk);
            if (!this.isChunkInsideBorder(chunk)) {
               this.trimChunks.add(chunk);
            }

            ++this.currentChunk;
            ++this.counter;
         }

         this.reportTotal += this.counter;
         this.readyToGo = true;
      }
   }

   private boolean nextFile() {
      this.reportTotal = this.currentRegion * 3072;
      ++this.currentRegion;
      this.regionX = this.regionZ = this.currentChunk = 0;
      this.regionChunks = new ArrayList(1024);
      this.trimChunks = new ArrayList(1024);
      if (this.currentRegion >= this.worldData.regionFileCount()) {
         this.paused = true;
         this.readyToGo = false;
         this.finish();
         return false;
      } else {
         this.counter += 16;
         CoordXZ coord = this.worldData.regionFileCoordinates(this.currentRegion);
         if (coord == null) {
            return false;
         } else {
            this.regionX = coord.x;
            this.regionZ = coord.z;
            return true;
         }
      }
   }

   private void addCornerChunks() {
      this.regionChunks.add(new CoordXZ(CoordXZ.regionToChunk(this.regionX), CoordXZ.regionToChunk(this.regionZ)));
      this.regionChunks.add(new CoordXZ(CoordXZ.regionToChunk(this.regionX) + 31, CoordXZ.regionToChunk(this.regionZ)));
      this.regionChunks.add(new CoordXZ(CoordXZ.regionToChunk(this.regionX), CoordXZ.regionToChunk(this.regionZ) + 31));
      this.regionChunks.add(new CoordXZ(CoordXZ.regionToChunk(this.regionX) + 31, CoordXZ.regionToChunk(this.regionZ) + 31));
   }

   private void addEdgeChunks() {
      int chunkX = 0;

      for(int chunkZ = 1; chunkZ < 31; ++chunkZ) {
         this.regionChunks.add(new CoordXZ(CoordXZ.regionToChunk(this.regionX) + chunkX, CoordXZ.regionToChunk(this.regionZ) + chunkZ));
      }

      chunkX = 31;

      for(int var6 = 1; var6 < 31; ++var6) {
         this.regionChunks.add(new CoordXZ(CoordXZ.regionToChunk(this.regionX) + chunkX, CoordXZ.regionToChunk(this.regionZ) + var6));
      }

      int var7 = 0;

      for(int var4 = 1; var4 < 31; ++var4) {
         this.regionChunks.add(new CoordXZ(CoordXZ.regionToChunk(this.regionX) + var4, CoordXZ.regionToChunk(this.regionZ) + var7));
      }

      var7 = 31;

      for(int var5 = 1; var5 < 31; ++var5) {
         this.regionChunks.add(new CoordXZ(CoordXZ.regionToChunk(this.regionX) + var5, CoordXZ.regionToChunk(this.regionZ) + var7));
      }

      this.counter += 4;
   }

   private void addInnerChunks() {
      for(int chunkX = 1; chunkX < 31; ++chunkX) {
         for(int chunkZ = 1; chunkZ < 31; ++chunkZ) {
            this.regionChunks.add(new CoordXZ(CoordXZ.regionToChunk(this.regionX) + chunkX, CoordXZ.regionToChunk(this.regionZ) + chunkZ));
         }
      }

      this.counter += 32;
   }

   private void unloadChunks() {
      for(CoordXZ unload : this.trimChunks) {
         if (this.world.isChunkLoaded(unload.x, unload.z)) {
            this.world.unloadChunk(unload.x, unload.z, false, false);
         }
      }

      this.counter += this.trimChunks.size();
   }

   private void wipeChunks() {
      File regionFile = this.worldData.regionFile(this.currentRegion);
      if (!regionFile.canWrite()) {
         regionFile.setWritable(true);
         if (!regionFile.canWrite()) {
            this.sendMessage("Error! region file is locked and can't be trimmed: " + regionFile.getName());
            return;
         }
      }

      int offsetX = CoordXZ.regionToChunk(this.regionX);
      int offsetZ = CoordXZ.regionToChunk(this.regionZ);
      long wipePos = 0L;
      int chunkCount = 0;

      try {
         RandomAccessFile unChunk = new RandomAccessFile(regionFile, "rwd");

         for(CoordXZ wipe : this.trimChunks) {
            if (this.worldData.doesChunkExist(wipe.x, wipe.z)) {
               wipePos = (long)(4 * (wipe.x - offsetX + (wipe.z - offsetZ) * 32));
               unChunk.seek(wipePos);
               unChunk.writeInt(0);
               ++chunkCount;
            }
         }

         unChunk.close();
         this.reportTrimmedChunks += chunkCount;
      } catch (FileNotFoundException var10) {
         this.sendMessage("Error! Could not open region file to wipe individual chunks: " + regionFile.getName());
      } catch (IOException var11) {
         this.sendMessage("Error! Could not modify region file to wipe individual chunks: " + regionFile.getName());
      }

      this.counter += this.trimChunks.size();
   }

   private boolean isChunkInsideBorder(CoordXZ chunk) {
      return this.border.insideBorder((double)(CoordXZ.chunkToBlock(chunk.x) + 8), (double)(CoordXZ.chunkToBlock(chunk.z) + 8));
   }

   public void finish() {
      this.reportTotal = this.reportTarget;
      this.reportProgress();
      this.sendMessage("task successfully completed!");
      this.stop();
   }

   public void cancel() {
      this.stop();
   }

   private void stop() {
      if (this.server != null) {
         this.readyToGo = false;
         if (this.taskID != -1) {
            this.server.getScheduler().cancelTask(this.taskID);
         }

         this.server = null;
      }
   }

   public boolean valid() {
      return this.server != null;
   }

   public void pause() {
      this.pause(!this.paused);
   }

   public void pause(boolean pause) {
      this.paused = pause;
      if (pause) {
         this.reportProgress();
      }

   }

   public boolean isPaused() {
      return this.paused;
   }

   private void reportProgress() {
      this.lastReport = Config.Now();
      double perc = (double)this.reportTotal / (double)this.reportTarget * (double)100.0F;
      this.sendMessage(this.reportTrimmedRegions + " entire region(s) and " + this.reportTrimmedChunks + " individual chunk(s) trimmed so far (" + Config.coord.format(perc) + "% done" + ")");
   }

   private void sendMessage(String text) {
      Config.Log("[Trim] " + text);
      if (this.notifyPlayer != null) {
         this.notifyPlayer.sendMessage("[Trim] " + text);
      }

   }
}
