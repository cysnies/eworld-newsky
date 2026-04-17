package net.milkbowl.vault.permission.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.crystalyx.bukkit.simplyperms.SimplyAPI;
import net.crystalyx.bukkit.simplyperms.SimplyPlugin;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

public class Permission_SimplyPerms extends Permission {
   private final String name = "SimplyPerms";
   private SimplyAPI perms;

   public Permission_SimplyPerms(Plugin plugin) {
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new PermissionServerListener(this), plugin);
      if (this.perms == null) {
         Plugin perms = plugin.getServer().getPluginManager().getPlugin("SimplyPerms");
         if (perms != null && perms.isEnabled()) {
            this.perms = ((SimplyPlugin)perms).getAPI();
            log.info(String.format("[%s][Permission] %s hooked.", plugin.getDescription().getName(), "SimplyPerms"));
         }
      }

   }

   public String getName() {
      return "SimplyPerms";
   }

   public boolean isEnabled() {
      return this.perms != null;
   }

   public boolean hasSuperPermsCompat() {
      return true;
   }

   public boolean playerHas(String world, String player, String permission) {
      permission = permission.toLowerCase();
      Map<String, Boolean> playerPermissions = this.perms.getPlayerPermissions(player, world);
      return playerPermissions.containsKey(permission) && (Boolean)playerPermissions.get(permission);
   }

   public boolean playerAdd(String world, String player, String permission) {
      permission = permission.toLowerCase();
      if (world != null) {
         this.perms.addPlayerPermission(player, world, permission, true);
      } else {
         this.perms.addPlayerPermission(player, permission, true);
      }

      return true;
   }

   public boolean playerRemove(String world, String player, String permission) {
      permission = permission.toLowerCase();
      if (world != null) {
         this.perms.removePlayerPermission(player, world, permission);
      } else {
         this.perms.removePlayerPermission(player, permission);
      }

      return true;
   }

   public boolean groupHas(String world, String group, String permission) {
      permission = permission.toLowerCase();
      Map<String, Boolean> groupPermissions = this.perms.getGroupPermissions(group, world);
      return groupPermissions.containsKey(permission) && (Boolean)groupPermissions.get(permission);
   }

   public boolean groupAdd(String world, String group, String permission) {
      permission = permission.toLowerCase();
      if (world != null) {
         this.perms.addGroupPermission(group, world, permission, true);
      } else {
         this.perms.addGroupPermission(group, permission, true);
      }

      return true;
   }

   public boolean groupRemove(String world, String group, String permission) {
      permission = permission.toLowerCase();
      if (world != null) {
         permission = world + ":" + permission;
         this.perms.removeGroupPermission(group, world, permission);
      } else {
         this.perms.removeGroupPermission(group, permission);
      }

      return true;
   }

   public boolean playerInGroup(String world, String player, String group) {
      if (world != null) {
         for(String g : this.perms.getPlayerGroups(player)) {
            if (g.equals(group)) {
               return this.perms.getGroupWorlds(group).contains(world);
            }
         }

         return false;
      } else {
         return !this.perms.getAllGroups().contains(group) ? false : this.perms.getPlayerGroups(player).contains(group);
      }
   }

   public boolean playerAddGroup(String world, String player, String group) {
      group = group.toLowerCase();
      this.perms.addPlayerGroup(player, group);
      return true;
   }

   public boolean playerRemoveGroup(String world, String player, String group) {
      group = group.toLowerCase();
      this.perms.removePlayerGroup(player, group);
      return true;
   }

   public String[] getPlayerGroups(String world, String player) {
      List<String> groupList = new ArrayList();
      if (world != null && this.perms.isPlayerInDB(player)) {
         for(String group : this.perms.getPlayerGroups(player)) {
            if (this.perms.getGroupWorlds(group).contains(world)) {
               groupList.add(group);
            }
         }

         return (String[])groupList.toArray(new String[0]);
      } else {
         if (this.perms.isPlayerInDB(player)) {
            for(String group : this.perms.getPlayerGroups(player)) {
               groupList.add(group);
            }
         }

         return (String[])groupList.toArray(new String[0]);
      }
   }

   public String getPrimaryGroup(String world, String player) {
      if (!this.perms.isPlayerInDB(player)) {
         return null;
      } else {
         return this.perms.getPlayerGroups(player) != null && !this.perms.getPlayerGroups(player).isEmpty() ? (String)this.perms.getPlayerGroups(player).get(0) : null;
      }
   }

   public String[] getGroups() {
      return (String[])this.perms.getAllGroups().toArray(new String[0]);
   }

   public class PermissionServerListener implements Listener {
      Permission_SimplyPerms permission = null;

      public PermissionServerListener(Permission_SimplyPerms permission) {
         this.permission = permission;
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (this.permission.perms == null) {
            Plugin perms = Permission_SimplyPerms.this.plugin.getServer().getPluginManager().getPlugin("SimplyPerms");
            if (perms != null) {
               this.permission.perms = ((SimplyPlugin)perms).getAPI();
               Permission_SimplyPerms.log.info(String.format("[%s][Permission] %s hooked.", Permission_SimplyPerms.this.plugin.getDescription().getName(), "SimplyPerms"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (this.permission.perms != null && event.getPlugin().getDescription().getName().equals("SimplyPerms")) {
            this.permission.perms = null;
            Permission_SimplyPerms.log.info(String.format("[%s][Permission] %s un-hooked.", Permission_SimplyPerms.this.plugin.getDescription().getName(), "SimplyPerms"));
         }

      }
   }
}
