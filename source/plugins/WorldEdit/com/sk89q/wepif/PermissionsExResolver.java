package com.sk89q.wepif;

import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.permissions.Permissible;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;

public class PermissionsExResolver extends DinnerPermsResolver {
   private final PermissionManager manager;

   public static PermissionsResolver factory(Server server, YAMLProcessor config) {
      try {
         PermissionManager manager = (PermissionManager)server.getServicesManager().load(PermissionManager.class);
         return manager == null ? null : new PermissionsExResolver(server, manager);
      } catch (Throwable var3) {
         return null;
      }
   }

   public PermissionsExResolver(Server server, PermissionManager manager) {
      super(server);
      this.manager = manager;
   }

   public boolean hasPermission(String worldName, String name, String permission) {
      return this.manager.has(name, permission, worldName);
   }

   public boolean hasPermission(OfflinePlayer player, String permission) {
      Permissible permissible = this.getPermissible(player);
      return permissible == null ? this.manager.has(player.getName(), permission, (String)null) : permissible.hasPermission(permission);
   }

   public boolean hasPermission(String worldName, OfflinePlayer player, String permission) {
      return this.hasPermission(worldName, player.getName(), permission);
   }

   public boolean inGroup(OfflinePlayer player, String group) {
      return this.getPermissible(player) == null ? this.manager.getUser(player.getName()).inGroup(group) : this.hasPermission(player, "group." + group);
   }

   public String[] getGroups(OfflinePlayer player) {
      if (this.getPermissible(player) == null) {
         PermissionUser user = this.manager.getUser(player.getName());
         return user == null ? new String[0] : user.getGroupsNames();
      } else {
         return super.getGroups(player);
      }
   }

   public String getDetectionMessage() {
      return "PermissionsEx detected! Using PermissionsEx for permissions.";
   }
}
