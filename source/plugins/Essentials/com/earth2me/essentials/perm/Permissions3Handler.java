package com.earth2me.essentials.perm;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import java.util.Arrays;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Permissions3Handler implements IPermissionsHandler {
   private final transient PermissionHandler permissionHandler;

   public Permissions3Handler(Plugin permissionsPlugin) {
      this.permissionHandler = ((Permissions)permissionsPlugin).getHandler();
   }

   public String getGroup(Player base) {
      return this.permissionHandler.getPrimaryGroup(base.getWorld().getName(), base.getName());
   }

   public List getGroups(Player base) {
      return Arrays.asList(this.permissionHandler.getGroups(base.getWorld().getName(), base.getName()));
   }

   public boolean canBuild(Player base, String group) {
      return this.permissionHandler.canUserBuild(base.getWorld().getName(), base.getName());
   }

   public boolean inGroup(Player base, String group) {
      return this.permissionHandler.inGroup(base.getWorld().getName(), base.getName(), group);
   }

   public boolean hasPermission(Player base, String node) {
      return this.permissionHandler.has(base, node);
   }

   public String getPrefix(Player base) {
      return this.permissionHandler.getUserPrefix(base.getWorld().getName(), base.getName());
   }

   public String getSuffix(Player base) {
      return this.permissionHandler.getUserSuffix(base.getWorld().getName(), base.getName());
   }
}
