package com.earth2me.essentials.perm;

import java.util.List;
import net.crystalyx.bukkit.simplyperms.SimplyAPI;
import net.crystalyx.bukkit.simplyperms.SimplyPlugin;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class SimplyPermsHandler extends SuperpermsHandler {
   private final transient SimplyAPI api;

   public SimplyPermsHandler(Plugin plugin) {
      this.api = ((SimplyPlugin)plugin).getAPI();
   }

   public String getGroup(Player base) {
      List<String> groups = this.api.getPlayerGroups(base.getName());
      return groups != null && !groups.isEmpty() ? (String)groups.get(0) : null;
   }

   public List getGroups(Player base) {
      return this.api.getPlayerGroups(base.getName());
   }

   public boolean inGroup(Player base, String group) {
      for(String group1 : this.api.getPlayerGroups(base.getName())) {
         if (group1.equalsIgnoreCase(group)) {
            return true;
         }
      }

      return false;
   }

   public boolean canBuild(Player base, String group) {
      return this.hasPermission(base, "essentials.build") || this.hasPermission(base, "permissions.allow.build");
   }
}
