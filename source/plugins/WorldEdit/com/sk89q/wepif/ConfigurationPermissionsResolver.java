package com.sk89q.wepif;

import com.sk89q.util.yaml.YAMLNode;
import com.sk89q.util.yaml.YAMLProcessor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.OfflinePlayer;

public class ConfigurationPermissionsResolver implements PermissionsResolver {
   private YAMLProcessor config;
   private Map userPermissionsCache;
   private Set defaultPermissionsCache;
   private Map userGroups;

   public ConfigurationPermissionsResolver(YAMLProcessor config) {
      this.config = config;
   }

   public static YAMLNode generateDefaultPerms(YAMLNode section) {
      section.setProperty("groups.default.permissions", new String[]{"worldedit.reload", "worldedit.selection", "worlds.creative.worldedit.region"});
      section.setProperty("groups.admins.permissions", new String[]{"*"});
      section.setProperty("users.sk89q.permissions", new String[]{"worldedit"});
      section.setProperty("users.sk89q.groups", new String[]{"admins"});
      return section;
   }

   public void load() {
      this.userGroups = new HashMap();
      this.userPermissionsCache = new HashMap();
      this.defaultPermissionsCache = new HashSet();
      Map<String, Set<String>> userGroupPermissions = new HashMap();
      List<String> groupKeys = this.config.getStringList("permissions.groups", (List)null);
      if (groupKeys != null) {
         for(String key : groupKeys) {
            List<String> permissions = this.config.getStringList("permissions.groups." + key + ".permissions", (List)null);
            if (permissions.size() > 0) {
               Set<String> groupPerms = new HashSet(permissions);
               userGroupPermissions.put(key, groupPerms);
               if (key.equals("default")) {
                  this.defaultPermissionsCache.addAll(permissions);
               }
            }
         }
      }

      List<String> userKeys = this.config.getStringList("permissions.users", (List)null);
      if (userKeys != null) {
         for(String key : userKeys) {
            Set<String> permsCache = new HashSet();
            List<String> permissions = this.config.getStringList("permissions.users." + key + ".permissions", (List)null);
            if (permissions.size() > 0) {
               permsCache.addAll(permissions);
            }

            List<String> groups = this.config.getStringList("permissions.users." + key + ".groups", (List)null);
            groups.add("default");
            if (groups.size() > 0) {
               for(String group : groups) {
                  Set<String> groupPerms = (Set)userGroupPermissions.get(group);
                  if (groupPerms != null) {
                     permsCache.addAll(groupPerms);
                  }
               }
            }

            this.userPermissionsCache.put(key.toLowerCase(), permsCache);
            this.userGroups.put(key.toLowerCase(), new HashSet(groups));
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
      return "No known permissions plugin detected. Using configuration file for permissions.";
   }
}
