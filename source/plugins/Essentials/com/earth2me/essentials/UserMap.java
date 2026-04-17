package com.earth2me.essentials;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.util.concurrent.UncheckedExecutionException;
import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import org.bukkit.entity.Player;

public class UserMap extends CacheLoader implements IConf {
   private final transient IEssentials ess;
   private final transient Cache users = CacheBuilder.newBuilder().softValues().build(this);
   private final transient ConcurrentSkipListSet keys = new ConcurrentSkipListSet();

   public UserMap(IEssentials ess) {
      this.ess = ess;
      this.loadAllUsersAsync(ess);
   }

   private void loadAllUsersAsync(final IEssentials ess) {
      ess.runTaskAsynchronously(new Runnable() {
         public void run() {
            File userdir = new File(ess.getDataFolder(), "userdata");
            if (userdir.exists()) {
               UserMap.this.keys.clear();
               UserMap.this.users.invalidateAll();

               for(String string : userdir.list()) {
                  if (string.endsWith(".yml")) {
                     String name = string.substring(0, string.length() - 4);
                     UserMap.this.keys.add(Util.sanitizeFileName(name));
                  }
               }

            }
         }
      });
   }

   public boolean userExists(String name) {
      return this.keys.contains(Util.sanitizeFileName(name));
   }

   public User getUser(String name) {
      try {
         return (User)this.users.get(name);
      } catch (ExecutionException var3) {
         return null;
      } catch (UncheckedExecutionException var4) {
         return null;
      }
   }

   public User load(String name) throws Exception {
      String sanitizedName = Util.sanitizeFileName(name);
      if (!sanitizedName.equals(name)) {
         User user = this.getUser(sanitizedName);
         if (user == null) {
            throw new Exception("User not found!");
         } else {
            return user;
         }
      } else {
         for(Player player : this.ess.getServer().getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(name)) {
               this.keys.add(sanitizedName);
               return new User(player, this.ess);
            }
         }

         File userFile = this.getUserFile2(sanitizedName);
         if (userFile.exists()) {
            this.keys.add(sanitizedName);
            return new User(new OfflinePlayer(name, this.ess), this.ess);
         } else {
            throw new Exception("User not found!");
         }
      }
   }

   public void reloadConfig() {
      this.loadAllUsersAsync(this.ess);
   }

   public void removeUser(String name) {
      this.keys.remove(Util.sanitizeFileName(name));
      this.users.invalidate(Util.sanitizeFileName(name));
      this.users.invalidate(name);
   }

   public Set getAllUniqueUsers() {
      return Collections.unmodifiableSet(this.keys);
   }

   public int getUniqueUsers() {
      return this.keys.size();
   }

   public File getUserFile(String name) {
      return this.getUserFile2(Util.sanitizeFileName(name));
   }

   private File getUserFile2(String name) {
      File userFolder = new File(this.ess.getDataFolder(), "userdata");
      return new File(userFolder, name + ".yml");
   }
}
