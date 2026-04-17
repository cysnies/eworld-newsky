package com.earth2me.essentials.perm;

import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.util.CalculableType;
import java.util.Arrays;
import java.util.List;
import org.bukkit.entity.Player;

public class BPermissions2Handler extends SuperpermsHandler {
   public String getGroup(Player base) {
      List<String> groups = this.getGroups(base);
      return groups != null && !groups.isEmpty() ? (String)groups.get(0) : null;
   }

   public List getGroups(Player base) {
      String[] groups = ApiLayer.getGroups(base.getWorld().getName(), CalculableType.USER, base.getName());
      return Arrays.asList(groups);
   }

   public boolean inGroup(Player base, String group) {
      return ApiLayer.hasGroupRecursive(base.getWorld().getName(), CalculableType.USER, base.getName(), group);
   }

   public boolean canBuild(Player base, String group) {
      return this.hasPermission(base, "bPermissions.build");
   }

   public String getPrefix(Player base) {
      return ApiLayer.getValue(base.getWorld().getName(), CalculableType.USER, base.getName(), "prefix");
   }

   public String getSuffix(Player base) {
      return ApiLayer.getValue(base.getWorld().getName(), CalculableType.USER, base.getName(), "suffix");
   }
}
