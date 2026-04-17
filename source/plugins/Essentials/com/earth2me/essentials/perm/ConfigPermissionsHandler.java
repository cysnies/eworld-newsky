package com.earth2me.essentials.perm;

import com.earth2me.essentials.IEssentials;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ConfigPermissionsHandler extends SuperpermsHandler {
   private final transient IEssentials ess;

   public ConfigPermissionsHandler(Plugin ess) {
      this.ess = (IEssentials)ess;
   }

   public boolean canBuild(Player base, String group) {
      return true;
   }

   public boolean hasPermission(Player base, String node) {
      String[] cmds = node.split("\\.", 2);
      return this.ess.getSettings().isPlayerCommand(cmds[cmds.length - 1]) || super.hasPermission(base, node);
   }
}
