package com.earth2me.essentials.perm;

import java.util.List;
import org.bukkit.entity.Player;

public class SuperpermsHandler implements IPermissionsHandler {
   public String getGroup(Player base) {
      return null;
   }

   public List getGroups(Player base) {
      return null;
   }

   public boolean canBuild(Player base, String group) {
      return false;
   }

   public boolean inGroup(Player base, String group) {
      return this.hasPermission(base, "group." + group);
   }

   public boolean hasPermission(Player base, String node) {
      String permCheck;
      for(permCheck = node; !base.isPermissionSet(permCheck); permCheck = node + ".*") {
         int index = node.lastIndexOf(46);
         if (index < 1) {
            return base.hasPermission("*");
         }

         node = node.substring(0, index);
      }

      return base.hasPermission(permCheck);
   }

   public String getPrefix(Player base) {
      return null;
   }

   public String getSuffix(Player base) {
      return null;
   }
}
