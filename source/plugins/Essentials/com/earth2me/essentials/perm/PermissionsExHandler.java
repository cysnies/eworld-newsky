package com.earth2me.essentials.perm;

import java.util.Arrays;
import java.util.List;
import org.bukkit.entity.Player;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PermissionsExHandler extends SuperpermsHandler {
   private final transient PermissionManager manager = PermissionsEx.getPermissionManager();

   public String getGroup(Player base) {
      PermissionUser user = this.manager.getUser(base.getName());
      return user == null ? null : user.getGroupsNames()[0];
   }

   public List getGroups(Player base) {
      PermissionUser user = this.manager.getUser(base.getName());
      return user == null ? null : Arrays.asList(user.getGroupsNames());
   }

   public boolean canBuild(Player base, String group) {
      PermissionUser user = this.manager.getUser(base.getName());
      return user == null ? false : user.getOptionBoolean("build", base.getWorld().getName(), false);
   }

   public boolean inGroup(Player base, String group) {
      PermissionUser user = this.manager.getUser(base.getName());
      return user == null ? false : user.inGroup(group);
   }

   public boolean hasPermission(Player base, String node) {
      return super.hasPermission(base, node);
   }

   public String getPrefix(Player base) {
      PermissionUser user = this.manager.getUser(base.getName());
      return user == null ? null : user.getPrefix(base.getWorld().getName());
   }

   public String getSuffix(Player base) {
      PermissionUser user = this.manager.getUser(base.getName());
      return user == null ? null : user.getSuffix(base.getWorld().getName());
   }
}
