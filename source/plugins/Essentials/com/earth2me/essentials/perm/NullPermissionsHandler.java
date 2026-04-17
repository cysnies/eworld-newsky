package com.earth2me.essentials.perm;

import java.util.Collections;
import java.util.List;
import org.bukkit.entity.Player;

public class NullPermissionsHandler implements IPermissionsHandler {
   public String getGroup(Player base) {
      return null;
   }

   public List getGroups(Player base) {
      return Collections.emptyList();
   }

   public boolean canBuild(Player base, String group) {
      return false;
   }

   public boolean inGroup(Player base, String group) {
      return false;
   }

   public boolean hasPermission(Player base, String node) {
      return false;
   }

   public String getPrefix(Player base) {
      return null;
   }

   public String getSuffix(Player base) {
      return null;
   }
}
