package com.sk89q.wepif;

import com.sk89q.util.yaml.YAMLProcessor;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class DinnerPermsResolver implements PermissionsResolver {
   public static final String GROUP_PREFIX = "group.";
   protected final Server server;

   public DinnerPermsResolver(Server server) {
      this.server = server;
   }

   public static PermissionsResolver factory(Server server, YAMLProcessor config) {
      return new DinnerPermsResolver(server);
   }

   public void load() {
   }

   public boolean hasPermission(String name, String permission) {
      return this.hasPermission(this.server.getOfflinePlayer(name), permission);
   }

   public boolean hasPermission(String worldName, String name, String permission) {
      return this.hasPermission(worldName, this.server.getOfflinePlayer(name), permission);
   }

   public boolean inGroup(String name, String group) {
      return this.inGroup(this.server.getOfflinePlayer(name), group);
   }

   public String[] getGroups(String name) {
      return this.getGroups(this.server.getOfflinePlayer(name));
   }

   public boolean hasPermission(OfflinePlayer param1, String param2) {
      // $FF: Couldn't be decompiled
   }

   public boolean hasPermission(String worldName, OfflinePlayer player, String permission) {
      return this.hasPermission(player, permission);
   }

   public boolean inGroup(OfflinePlayer player, String group) {
      Permissible perms = this.getPermissible(player);
      if (perms == null) {
         return false;
      } else {
         String perm = "group." + group;
         return perms.isPermissionSet(perm) && perms.hasPermission(perm);
      }
   }

   public String[] getGroups(OfflinePlayer player) {
      Permissible perms = this.getPermissible(player);
      if (perms == null) {
         return new String[0];
      } else {
         List<String> groupNames = new ArrayList();

         for(PermissionAttachmentInfo permAttach : perms.getEffectivePermissions()) {
            String perm = permAttach.getPermission();
            if (perm.startsWith("group.") && permAttach.getValue()) {
               groupNames.add(perm.substring("group.".length(), perm.length()));
            }
         }

         return (String[])groupNames.toArray(new String[groupNames.size()]);
      }
   }

   public Permissible getPermissible(OfflinePlayer offline) {
      if (offline == null) {
         return null;
      } else {
         Permissible perm = null;
         if (offline instanceof Permissible) {
            perm = (Permissible)offline;
         } else {
            Player player = offline.getPlayer();
            if (player != null) {
               perm = player;
            }
         }

         return perm;
      }
   }

   public int internalHasPermission(Permissible perms, String permission) {
      if (perms.isPermissionSet(permission)) {
         return perms.hasPermission(permission) ? 1 : -1;
      } else {
         Permission perm = this.server.getPluginManager().getPermission(permission);
         if (perm != null) {
            return perm.getDefault().getValue(perms.isOp()) ? 1 : 0;
         } else {
            return 0;
         }
      }
   }

   public String getDetectionMessage() {
      return "Using the Bukkit Permissions API.";
   }
}
