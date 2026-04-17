package com.sk89q.wepif;

import com.sk89q.util.yaml.YAMLProcessor;
import org.anjocaido.groupmanager.dataholder.worlds.WorldsHolder;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class GroupManagerResolver implements PermissionsResolver {
   private final WorldsHolder worldsHolder;
   private final Server server;

   public static PermissionsResolver factory(Server server, YAMLProcessor config) {
      try {
         WorldsHolder worldsHolder = (WorldsHolder)server.getServicesManager().load(WorldsHolder.class);
         return worldsHolder == null ? null : new GroupManagerResolver(server, worldsHolder);
      } catch (Throwable var3) {
         return null;
      }
   }

   public GroupManagerResolver(Server server, WorldsHolder worldsHolder) {
      this.server = server;
      this.worldsHolder = worldsHolder;
   }

   public void load() {
   }

   private AnjoPermissionsHandler getPermissionHandler(String name, String worldName) {
      if (name != null && !name.isEmpty()) {
         if (worldName != null && !worldName.isEmpty()) {
            return this.worldsHolder.getWorldPermissions(worldName);
         } else {
            Player player = this.server.getPlayerExact(name);
            if (player == null) {
               return null;
            } else {
               World world = player.getWorld();
               return world == null ? this.worldsHolder.getDefaultWorld().getPermissionsHandler() : this.worldsHolder.getWorldPermissions(world.getName());
            }
         }
      } else {
         return null;
      }
   }

   public boolean hasPermission(String name, String permission) {
      return this.hasPermission((String)null, (String)name, permission);
   }

   public boolean hasPermission(String worldName, String name, String permission) {
      if (permission != null && !permission.isEmpty()) {
         AnjoPermissionsHandler permissionHandler = this.getPermissionHandler(name, worldName);
         return permissionHandler == null ? false : permissionHandler.permission(name, permission);
      } else {
         return false;
      }
   }

   public boolean inGroup(String name, String group) {
      if (group != null && !group.isEmpty()) {
         AnjoPermissionsHandler permissionHandler = this.getPermissionHandler(name, (String)null);
         return permissionHandler == null ? false : permissionHandler.inGroup(name, group);
      } else {
         return false;
      }
   }

   public String[] getGroups(String name) {
      AnjoPermissionsHandler permissionHandler = this.getPermissionHandler(name, (String)null);
      return permissionHandler == null ? new String[0] : permissionHandler.getGroups(name);
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
      return "GroupManager detected! Using GroupManager for permissions.";
   }
}
