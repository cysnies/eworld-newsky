package com.earth2me.essentials.spawn;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.IEssentialsModule;
import com.earth2me.essentials.settings.Spawns;
import com.earth2me.essentials.storage.AsyncStorageObjectHolder;
import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;

public class SpawnStorage extends AsyncStorageObjectHolder implements IEssentialsModule {
   public SpawnStorage(IEssentials ess) {
      super(ess, Spawns.class);
      this.reloadConfig();
   }

   public File getStorageFile() {
      return new File(this.ess.getDataFolder(), "spawn.yml");
   }

   public void finishRead() {
   }

   public void finishWrite() {
   }

   public void setSpawn(Location loc, String group) {
      this.acquireWriteLock();

      try {
         if (((Spawns)this.getData()).getSpawns() == null) {
            ((Spawns)this.getData()).setSpawns(new HashMap());
         }

         ((Spawns)this.getData()).getSpawns().put(group.toLowerCase(Locale.ENGLISH), loc);
      } finally {
         this.unlock();
      }

      if ("default".equalsIgnoreCase(group)) {
         loc.getWorld().setSpawnLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
      }

   }

   public Location getSpawn(String group) {
      this.acquireReadLock();

      Location var2;
      try {
         if (((Spawns)this.getData()).getSpawns() != null && group != null) {
            Map<String, Location> spawnMap = ((Spawns)this.getData()).getSpawns();
            String groupName = group.toLowerCase(Locale.ENGLISH);
            if (!spawnMap.containsKey(groupName)) {
               groupName = "default";
            }

            if (!spawnMap.containsKey(groupName)) {
               Location var9 = this.getWorldSpawn();
               return var9;
            }

            Location var4 = (Location)spawnMap.get(groupName);
            return var4;
         }

         var2 = this.getWorldSpawn();
      } finally {
         this.unlock();
      }

      return var2;
   }

   private Location getWorldSpawn() {
      for(World world : this.ess.getServer().getWorlds()) {
         if (world.getEnvironment() == Environment.NORMAL) {
            return world.getSpawnLocation();
         }
      }

      return ((World)this.ess.getServer().getWorlds().get(0)).getSpawnLocation();
   }
}
