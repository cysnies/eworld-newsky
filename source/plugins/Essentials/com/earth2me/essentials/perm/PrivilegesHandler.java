package com.earth2me.essentials.perm;

import java.util.ArrayList;
import java.util.List;
import net.krinsoft.privileges.Privileges;
import net.krinsoft.privileges.groups.Group;
import net.krinsoft.privileges.groups.GroupManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PrivilegesHandler extends SuperpermsHandler {
   private final transient Privileges plugin;
   private final GroupManager manager;

   public PrivilegesHandler(Plugin plugin) {
      this.plugin = (Privileges)plugin;
      this.manager = this.plugin.getGroupManager();
   }

   public String getGroup(Player base) {
      Group group = this.manager.getGroup(base);
      return group == null ? null : group.getName();
   }

   public List getGroups(Player base) {
      Group group = this.manager.getGroup(base);
      return (List)(group == null ? new ArrayList() : group.getGroupTree());
   }

   public boolean inGroup(Player base, String group) {
      Group pGroup = this.manager.getGroup(base);
      return pGroup == null ? false : pGroup.isMemberOf(group);
   }

   public boolean canBuild(Player base, String group) {
      return this.hasPermission(base, "privileges.build");
   }
}
