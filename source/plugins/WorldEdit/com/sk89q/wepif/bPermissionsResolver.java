package com.sk89q.wepif;

import com.sk89q.util.yaml.YAMLProcessor;
import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.util.CalculableType;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class bPermissionsResolver implements PermissionsResolver {
   private final Server server;

   public static PermissionsResolver factory(Server server, YAMLProcessor config) {
      try {
         Class.forName("de.bananaco.bpermissions.api.ApiLayer");
      } catch (ClassNotFoundException var3) {
         return null;
      }

      return new bPermissionsResolver(server);
   }

   public bPermissionsResolver(Server server) {
      this.server = server;
   }

   public void load() {
   }

   public String getDetectionMessage() {
      return "bPermissions detected! Using bPermissions for permissions";
   }

   public boolean hasPermission(String name, String permission) {
      return this.hasPermission(this.server.getOfflinePlayer(name), permission);
   }

   public boolean hasPermission(String worldName, String name, String permission) {
      return ApiLayer.hasPermission(worldName, CalculableType.USER, name, permission);
   }

   public boolean inGroup(String player, String group) {
      return this.inGroup(this.server.getOfflinePlayer(player), group);
   }

   public String[] getGroups(String player) {
      return this.getGroups(this.server.getOfflinePlayer(player));
   }

   public boolean hasPermission(OfflinePlayer player, String permission) {
      Player onlinePlayer = player.getPlayer();
      return onlinePlayer == null ? ApiLayer.hasPermission((String)null, CalculableType.USER, player.getName(), permission) : ApiLayer.hasPermission(onlinePlayer.getWorld().getName(), CalculableType.USER, player.getName(), permission);
   }

   public boolean hasPermission(String worldName, OfflinePlayer player, String permission) {
      return this.hasPermission(worldName, player.getName(), permission);
   }

   public boolean inGroup(OfflinePlayer player, String group) {
      Player onlinePlayer = player.getPlayer();
      return onlinePlayer == null ? ApiLayer.hasGroupRecursive((String)null, CalculableType.USER, player.getName(), group) : ApiLayer.hasGroupRecursive(onlinePlayer.getWorld().getName(), CalculableType.USER, player.getName(), group);
   }

   public String[] getGroups(OfflinePlayer player) {
      Player onlinePlayer = player.getPlayer();
      return onlinePlayer == null ? ApiLayer.getGroups((String)null, CalculableType.USER, player.getName()) : ApiLayer.getGroups(onlinePlayer.getWorld().getName(), CalculableType.USER, player.getName());
   }
}
