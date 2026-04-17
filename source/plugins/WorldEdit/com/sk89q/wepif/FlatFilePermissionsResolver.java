package com.sk89q.wepif;

import com.sk89q.util.yaml.YAMLProcessor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;

public class FlatFilePermissionsResolver implements PermissionsResolver {
   private Map userPermissionsCache;
   private Set defaultPermissionsCache;
   private Map userGroups;
   protected File groupFile;
   protected File userFile;

   public static PermissionsResolver factory(Server server, YAMLProcessor config) {
      File groups = new File("perms_groups.txt");
      File users = new File("perms_users.txt");
      return groups.exists() && users.exists() ? new FlatFilePermissionsResolver(groups, users) : null;
   }

   public FlatFilePermissionsResolver() {
      this(new File("perms_groups.txt"), new File("perms_users.txt"));
   }

   public FlatFilePermissionsResolver(File groupFile, File userFile) {
      this.groupFile = groupFile;
      this.userFile = userFile;
   }

   /** @deprecated */
   @Deprecated
   public static boolean filesExists() {
      return (new File("perms_groups.txt")).exists() && (new File("perms_users.txt")).exists();
   }

   public Map loadGroupPermissions() {
      Map<String, Set<String>> userGroupPermissions = new HashMap();
      BufferedReader buff = null;

      try {
         FileReader input = new FileReader(this.groupFile);
         buff = new BufferedReader(input);

         String line;
         while((line = buff.readLine()) != null) {
            line = line.trim();
            if (line.length() != 0 && line.charAt(0) != ';' && line.charAt(0) != '#') {
               String[] parts = line.split(":");
               String key = parts[0];
               if (parts.length > 1) {
                  String[] perms = parts[1].split(",");
                  Set<String> groupPerms = new HashSet(Arrays.asList(perms));
                  userGroupPermissions.put(key, groupPerms);
               }
            }
         }
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         try {
            if (buff != null) {
               buff.close();
            }
         } catch (IOException var16) {
         }

      }

      return userGroupPermissions;
   }

   public void load() {
      this.userGroups = new HashMap();
      this.userPermissionsCache = new HashMap();
      this.defaultPermissionsCache = new HashSet();
      Map<String, Set<String>> userGroupPermissions = this.loadGroupPermissions();
      if (userGroupPermissions.containsKey("default")) {
         this.defaultPermissionsCache = (Set)userGroupPermissions.get("default");
      }

      BufferedReader buff = null;

      try {
         FileReader input = new FileReader(this.userFile);
         buff = new BufferedReader(input);

         String line;
         while((line = buff.readLine()) != null) {
            Set<String> permsCache = new HashSet();
            line = line.trim();
            if (line.length() != 0 && line.charAt(0) != ';' && line.charAt(0) != '#') {
               String[] parts = line.split(":");
               String key = parts[0];
               if (parts.length > 1) {
                  String[] groups = (parts[1] + ",default").split(",");
                  String[] perms = parts.length > 2 ? parts[2].split(",") : new String[0];
                  permsCache.addAll(Arrays.asList(perms));

                  for(String group : groups) {
                     Set<String> groupPerms = (Set)userGroupPermissions.get(group);
                     if (groupPerms != null) {
                        permsCache.addAll(groupPerms);
                     }
                  }

                  this.userPermissionsCache.put(key.toLowerCase(), permsCache);
                  this.userGroups.put(key.toLowerCase(), new HashSet(Arrays.asList(groups)));
               }
            }
         }
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         try {
            if (buff != null) {
               buff.close();
            }
         } catch (IOException var22) {
         }

      }

   }

   public boolean hasPermission(String player, String permission) {
      int dotPos = permission.lastIndexOf(".");
      if (dotPos > -1 && this.hasPermission(player, permission.substring(0, dotPos))) {
         return true;
      } else {
         Set<String> perms = (Set)this.userPermissionsCache.get(player.toLowerCase());
         if (perms == null) {
            return this.defaultPermissionsCache.contains(permission) || this.defaultPermissionsCache.contains("*");
         } else {
            return perms.contains("*") || perms.contains(permission);
         }
      }
   }

   public boolean hasPermission(String worldName, String player, String permission) {
      return this.hasPermission(player, "worlds." + worldName + "." + permission) || this.hasPermission(player, permission);
   }

   public boolean inGroup(String player, String group) {
      Set<String> groups = (Set)this.userGroups.get(player.toLowerCase());
      return groups == null ? false : groups.contains(group);
   }

   public String[] getGroups(String player) {
      Set<String> groups = (Set)this.userGroups.get(player.toLowerCase());
      return groups == null ? new String[0] : (String[])groups.toArray(new String[groups.size()]);
   }

   public boolean hasPermission(OfflinePlayer player, String permission) {
      return this.hasPermission(player.getName(), permission);
   }

   public boolean hasPermission(String worldName, OfflinePlayer player, String permission) {
      return this.hasPermission(worldName, player.getName(), permission);
   }

   public boolean inGroup(OfflinePlayer player, String group) {
      return this.inGroup(player.getName(), group);
   }

   public String[] getGroups(OfflinePlayer player) {
      return this.getGroups(player.getName());
   }

   public String getDetectionMessage() {
      return "perms_groups.txt and perms_users.txt detected! Using flat file permissions.";
   }
}
