package com.wimbli.WorldBorder;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WorldFillTask implements Runnable {
   private transient Server server = null;
   private transient World world = null;
   private transient BorderData border = null;
   private transient WorldFileData worldData = null;
   private transient boolean readyToGo = false;
   private transient boolean paused = false;
   private transient boolean pausedForMemory = false;
   private transient int taskID = -1;
   private transient Player notifyPlayer = null;
   private transient int chunksPerRun = 1;
   private transient boolean continueNotice = false;
   private transient int fillDistance = 208;
   private transient int tickFrequency = 1;
   private transient int refX = 0;
   private transient int lastLegX = 0;
   private transient int refZ = 0;
   private transient int lastLegZ = 0;
   private transient int refLength = -1;
   private transient int refTotal = 0;
   private transient int lastLegTotal = 0;
   private transient int x = 0;
   private transient int z = 0;
   private transient boolean isZLeg = false;
   private transient boolean isNeg = false;
   private transient int length = -1;
   private transient int current = 0;
   private transient boolean insideBorder = true;
   private List storedChunks = new LinkedList();
   private Set originalChunks = new HashSet();
   private transient CoordXZ lastChunk = new CoordXZ(0, 0);
   private transient long lastReport = Config.Now();
   private transient int reportTarget = 0;
   private transient int reportTotal = 0;
   private transient int reportNum = 0;
   private int reportCounter = 0;

   public WorldFillTask(Server theServer, Player player, String worldName, int fillDistance, int chunksPerRun, int tickFrequency) {
      this.server = theServer;
      this.notifyPlayer = player;
      this.fillDistance = fillDistance;
      this.tickFrequency = tickFrequency;
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
            this.worldData = WorldFileData.create(this.world, this.notifyPlayer);
            if (this.worldData == null) {
               this.stop();
            } else {
               this.border.setRadiusX(this.border.getRadiusX() + fillDistance);
               this.border.setRadiusZ(this.border.getRadiusZ() + fillDistance);
               this.x = CoordXZ.blockToChunk((int)this.border.getX());
               this.z = CoordXZ.blockToChunk((int)this.border.getZ());
               int chunkWidthX = (int)Math.ceil((double)((this.border.getRadiusX() + 16) * 2) / (double)16.0F);
               int chunkWidthZ = (int)Math.ceil((double)((this.border.getRadiusZ() + 16) * 2) / (double)16.0F);
               int biggerWidth = chunkWidthX > chunkWidthZ ? chunkWidthX : chunkWidthZ;
               this.reportTarget = biggerWidth * biggerWidth + biggerWidth + 1;
               Chunk[] originals = this.world.getLoadedChunks();

               for(Chunk original : originals) {
                  this.originalChunks.add(new CoordXZ(original.getX(), original.getZ()));
               }

               this.readyToGo = true;
            }
         }
      }
   }

   public void setTaskID(int ID) {
      if (ID == -1) {
         this.stop();
      }

      this.taskID = ID;
   }

   public void run() {
      if (this.continueNotice) {
         this.continueNotice = false;
         this.sendMessage("World map generation task automatically continuing.");
         this.sendMessage("Reminder: you can cancel at any time with \"wb fill cancel\", or pause/unpause with \"wb fill pause\".");
      }

      if (this.pausedForMemory) {
         if (Config.AvailableMemory() < 500) {
            return;
         }

         this.pausedForMemory = false;
         this.readyToGo = true;
         this.sendMessage("Available memory is sufficient, automatically continuing.");
      }

      if (this.server != null && this.readyToGo && !this.paused) {
         this.readyToGo = false;
         long loopStartTime = Config.Now();

         for(int loop = 0; loop < this.chunksPerRun; ++loop) {
            if (this.paused || this.pausedForMemory) {
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

            while(!this.border.insideBorder((double)(CoordXZ.chunkToBlock(this.x) + 8), (double)(CoordXZ.chunkToBlock(this.z) + 8))) {
               if (!this.moveToNext()) {
                  return;
               }
            }

            this.insideBorder = true;

            while(this.worldData.isChunkFullyGenerated(this.x, this.z)) {
               this.insideBorder = true;
               if (!this.moveToNext()) {
                  return;
               }
            }

            this.world.loadChunk(this.x, this.z, true);
            this.worldData.chunkExistsNow(this.x, this.z);
            int popX = !this.isZLeg ? this.x : this.x + (this.isNeg ? -1 : 1);
            int popZ = this.isZLeg ? this.z : this.z + (!this.isNeg ? -1 : 1);
            this.world.loadChunk(popX, popZ, false);
            if (!this.storedChunks.contains(this.lastChunk) && !this.originalChunks.contains(this.lastChunk)) {
               this.world.loadChunk(this.lastChunk.x, this.lastChunk.z, false);
               this.storedChunks.add(new CoordXZ(this.lastChunk.x, this.lastChunk.z));
            }

            this.storedChunks.add(new CoordXZ(popX, popZ));
            this.storedChunks.add(new CoordXZ(this.x, this.z));

            while(this.storedChunks.size() > 8) {
               CoordXZ coord = (CoordXZ)this.storedChunks.remove(0);
               if (!this.originalChunks.contains(coord)) {
                  this.world.unloadChunkRequest(coord.x, coord.z);
               }
            }

            if (!this.moveToNext()) {
               return;
            }
         }

         this.readyToGo = true;
      }
   }

   public boolean moveToNext() {
      if (!this.paused && !this.pausedForMemory) {
         ++this.reportNum;
         if (!this.isNeg && this.current == 0 && this.length > 3) {
            if (!this.isZLeg) {
               this.lastLegX = this.x;
               this.lastLegZ = this.z;
               this.lastLegTotal = this.reportTotal + this.reportNum;
            } else {
               this.refX = this.lastLegX;
               this.refZ = this.lastLegZ;
               this.refTotal = this.lastLegTotal;
               this.refLength = this.length - 1;
            }
         }

         if (this.current < this.length) {
            ++this.current;
         } else {
            this.current = 0;
            this.isZLeg ^= true;
            if (this.isZLeg) {
               this.isNeg ^= true;
               ++this.length;
            }
         }

         this.lastChunk.x = this.x;
         this.lastChunk.z = this.z;
         if (this.isZLeg) {
            this.z += this.isNeg ? -1 : 1;
         } else {
            this.x += this.isNeg ? -1 : 1;
         }

         if (this.isZLeg && this.isNeg && this.current == 0) {
            if (!this.insideBorder) {
               this.finish();
               return false;
            }

            this.insideBorder = false;
         }

         return true;
      } else {
         return false;
      }
   }

   public void finish() {
      this.paused = true;
      this.reportProgress();
      this.world.save();
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

         while(!this.storedChunks.isEmpty()) {
            CoordXZ coord = (CoordXZ)this.storedChunks.remove(0);
            if (!this.originalChunks.contains(coord)) {
               this.world.unloadChunkRequest(coord.x, coord.z);
            }
         }

      }
   }

   public boolean valid() {
      return this.server != null;
   }

   public void pause() {
      if (this.pausedForMemory) {
         this.pause(false);
      } else {
         this.pause(!this.paused);
      }

   }

   public void pause(boolean pause) {
      if (this.pausedForMemory && !pause) {
         this.pausedForMemory = false;
      } else {
         this.paused = pause;
      }

      if (this.paused) {
         Config.StoreFillTask();
         this.reportProgress();
      } else {
         Config.UnStoreFillTask();
      }

   }

   public boolean isPaused() {
      return this.paused || this.pausedForMemory;
   }

   private void reportProgress() {
      this.lastReport = Config.Now();
      double perc = (double)(this.reportTotal + this.reportNum) / (double)this.reportTarget * (double)100.0F;
      if (perc > (double)100.0F) {
         perc = (double)100.0F;
      }

      this.sendMessage(this.reportNum + " more chunks processed (" + (this.reportTotal + this.reportNum) + " total, ~" + Config.coord.format(perc) + "%" + ")");
      this.reportTotal += this.reportNum;
      this.reportNum = 0;
      ++this.reportCounter;
      if (this.reportCounter >= 6) {
         this.reportCounter = 0;
         this.sendMessage("Saving the world to disk, just to be on the safe side.");
         this.world.save();
      }

   }

   private void sendMessage(String text) {
      int availMem = Config.AvailableMemory();
      Config.Log("[Fill] " + text + " (free mem: " + availMem + " MB)");
      if (this.notifyPlayer != null) {
         this.notifyPlayer.sendMessage("[Fill] " + text);
      }

      if (availMem < 200) {
         this.pausedForMemory = true;
         Config.StoreFillTask();
         text = "Available memory is very low, task is pausing. A cleanup will be attempted now, and the task will automatically continue if/when sufficient memory is freed up.\n Alternatively, if you restart the server, this task will automatically continue once the server is back up.";
         Config.Log("[Fill] " + text);
         if (this.notifyPlayer != null) {
            this.notifyPlayer.sendMessage("[Fill] " + text);
         }

         System.gc();
      }

   }

   public void continueProgress(int x, int z, int length, int totalDone) {
      this.x = x;
      this.z = z;
      this.length = length;
      this.reportTotal = totalDone;
      this.continueNotice = true;
   }

   public int refX() {
      return this.refX;
   }

   public int refZ() {
      return this.refZ;
   }

   public int refLength() {
      return this.refLength;
   }

   public int refTotal() {
      return this.refTotal;
   }

   public int refFillDistance() {
      return this.fillDistance;
   }

   public int refTickFrequency() {
      return this.tickFrequency;
   }

   public int refChunksPerRun() {
      return this.chunksPerRun;
   }

   public String refWorld() {
      return this.world.getName();
   }
}
