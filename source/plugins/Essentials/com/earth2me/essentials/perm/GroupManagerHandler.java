package com.earth2me.essentials.perm;

import java.util.Arrays;
import java.util.List;
import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.dataholder.worlds.WorldsHolder;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class GroupManagerHandler implements IPermissionsHandler {
   private final transient GroupManager groupManager;

   public GroupManagerHandler(Plugin permissionsPlugin) {
      this.groupManager = (GroupManager)permissionsPlugin;
   }

   public String getGroup(Player base) {
      AnjoPermissionsHandler handler = this.getHandler(base);
      return handler == null ? null : handler.getGroup(base.getName());
   }

   public List getGroups(Player base) {
      AnjoPermissionsHandler handler = this.getHandler(base);
      return handler == null ? null : Arrays.asList(handler.getGroups(base.getName()));
   }

   public boolean canBuild(Player base, String group) {
      AnjoPermissionsHandler handler = this.getHandler(base);
      return handler == null ? false : handler.canUserBuild(base.getName());
   }

   public boolean inGroup(Player base, String group) {
      AnjoPermissionsHandler handler = this.getHandler(base);
      return handler == null ? false : handler.inGroup(base.getName(), group);
   }

   public boolean hasPermission(Player base, String node) {
      AnjoPermissionsHandler handler = this.getHandler(base);
      return handler == null ? false : handler.has(base, node);
   }

   public String getPrefix(Player base) {
      AnjoPermissionsHandler handler = this.getHandler(base);
      return handler == null ? null : handler.getUserPrefix(base.getName());
   }

   public String getSuffix(Player base) {
      AnjoPermissionsHandler handler = this.getHandler(base);
      return handler == null ? null : handler.getUserSuffix(base.getName());
   }

   private AnjoPermissionsHandler getHandler(Player base) {
      WorldsHolder holder = this.groupManager.getWorldsHolder();
      return holder == null ? null : holder.getWorldPermissions(base);
   }
}
