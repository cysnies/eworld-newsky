package com.earth2me.essentials.perm;

import de.bananaco.permissions.Permissions;
import de.bananaco.permissions.info.InfoReader;
import de.bananaco.permissions.interfaces.PermissionSet;
import de.bananaco.permissions.worlds.WorldPermissionsManager;
import java.util.List;
import org.bukkit.entity.Player;

public class BPermissionsHandler extends SuperpermsHandler {
   private final transient WorldPermissionsManager wpm = Permissions.getWorldPermissionsManager();
   private final transient InfoReader info = new InfoReader();

   public BPermissionsHandler() {
      this.info.instantiate();
   }

   public String getGroup(Player base) {
      List<String> groups = this.getGroups(base);
      return groups != null && !groups.isEmpty() ? (String)groups.get(0) : null;
   }

   public List getGroups(Player base) {
      PermissionSet pset = this.wpm.getPermissionSet(base.getWorld());
      return pset == null ? null : pset.getGroups(base);
   }

   public boolean inGroup(Player base, String group) {
      List<String> groups = this.getGroups(base);
      return groups != null && !groups.isEmpty() ? groups.contains(group) : false;
   }

   public boolean canBuild(Player base, String group) {
      return this.hasPermission(base, "bPermissions.build");
   }

   public String getPrefix(Player base) {
      return this.info.getPrefix(base);
   }

   public String getSuffix(Player base) {
      return this.info.getSuffix(base);
   }
}
