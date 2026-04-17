package com.earth2me.essentials.perm;

import com.platymuus.bukkit.permissions.Group;
import com.platymuus.bukkit.permissions.PermissionInfo;
import com.platymuus.bukkit.permissions.PermissionsPlugin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PermissionsBukkitHandler extends SuperpermsHandler {
   private final transient PermissionsPlugin plugin;

   public PermissionsBukkitHandler(Plugin plugin) {
      this.plugin = (PermissionsPlugin)plugin;
   }

   public String getGroup(Player base) {
      List<Group> groups = this.getPBGroups(base);
      return groups != null && !groups.isEmpty() ? ((Group)groups.get(0)).getName() : null;
   }

   public List getGroups(Player base) {
      List<Group> groups = this.getPBGroups(base);
      if (groups.size() == 1) {
         return Collections.singletonList(((Group)groups.get(0)).getName());
      } else {
         List<String> groupNames = new ArrayList(groups.size());

         for(Group group : groups) {
            groupNames.add(group.getName());
         }

         return groupNames;
      }
   }

   private List getPBGroups(Player base) {
      PermissionInfo info = this.plugin.getPlayerInfo(base.getName());
      if (info == null) {
         return Collections.emptyList();
      } else {
         List<Group> groups = info.getGroups();
         return groups != null && !groups.isEmpty() ? groups : Collections.emptyList();
      }
   }

   public boolean inGroup(Player base, String group) {
      for(Group group1 : this.getPBGroups(base)) {
         if (group1.getName().equalsIgnoreCase(group)) {
            return true;
         }
      }

      return false;
   }

   public boolean canBuild(Player base, String group) {
      return false;
   }
}
