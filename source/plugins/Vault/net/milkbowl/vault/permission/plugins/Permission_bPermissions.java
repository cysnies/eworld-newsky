package net.milkbowl.vault.permission.plugins;

import de.bananaco.permissions.Permissions;
import de.bananaco.permissions.interfaces.PermissionSet;
import de.bananaco.permissions.worlds.HasPermission;
import de.bananaco.permissions.worlds.WorldPermissionsManager;
import java.util.List;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

public class Permission_bPermissions extends Permission {
   private final String name = "bPermissions";
   private WorldPermissionsManager perms;

   public Permission_bPermissions(Plugin plugin) {
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new PermissionServerListener(), plugin);
      if (this.perms == null) {
         Plugin p = plugin.getServer().getPluginManager().getPlugin("bPermissions");
         if (p != null) {
            this.perms = Permissions.getWorldPermissionsManager();
            log.info(String.format("[%s][Permission] %s hooked.", plugin.getDescription().getName(), "bPermissions"));
         }
      }

   }

   public String getName() {
      return "bPermissions";
   }

   public boolean isEnabled() {
      return this.perms != null;
   }

   public boolean playerHas(String world, String player, String permission) {
      return HasPermission.has(player, world, permission);
   }

   public boolean playerAdd(String world, String player, String permission) {
      if (world == null) {
         return false;
      } else {
         PermissionSet set = this.perms.getPermissionSet(world);
         if (set == null) {
            return false;
         } else {
            set.addPlayerNode(permission, player);
            return true;
         }
      }
   }

   public boolean playerRemove(String world, String player, String permission) {
      if (world == null) {
         return false;
      } else {
         PermissionSet set = this.perms.getPermissionSet(world);
         if (set == null) {
            return false;
         } else {
            set.removePlayerNode(permission, player);
            return true;
         }
      }
   }

   public boolean groupHas(String world, String group, String permission) {
      if (world == null) {
         return false;
      } else {
         PermissionSet set = this.perms.getPermissionSet(world);
         if (set == null) {
            return false;
         } else {
            return set.getGroupNodes(group) == null ? false : set.getGroupNodes(group).contains(permission);
         }
      }
   }

   public boolean groupAdd(String world, String group, String permission) {
      if (world == null) {
         return false;
      } else {
         PermissionSet set = this.perms.getPermissionSet(world);
         if (set == null) {
            return false;
         } else if (set.getGroupNodes(group) == null) {
            return false;
         } else {
            set.addNode(permission, group);
            return true;
         }
      }
   }

   public boolean groupRemove(String world, String group, String permission) {
      if (world == null) {
         return false;
      } else {
         PermissionSet set = this.perms.getPermissionSet(world);
         if (set == null) {
            return false;
         } else if (set.getGroupNodes(group) == null) {
            return false;
         } else {
            set.removeNode(permission, group);
            return true;
         }
      }
   }

   public boolean playerInGroup(String world, String player, String group) {
      if (world == null) {
         return false;
      } else {
         PermissionSet set = this.perms.getPermissionSet(world);
         if (set == null) {
            return false;
         } else {
            return set.getGroups(player) == null ? false : set.getGroups(player).contains(group);
         }
      }
   }

   public boolean playerAddGroup(String world, String player, String group) {
      if (world == null) {
         return false;
      } else {
         PermissionSet set = this.perms.getPermissionSet(world);
         if (set == null) {
            return false;
         } else if (set.getGroupNodes(group) == null) {
            return false;
         } else {
            set.addGroup(player, group);
            return true;
         }
      }
   }

   public boolean playerRemoveGroup(String world, String player, String group) {
      if (world == null) {
         return false;
      } else {
         PermissionSet set = this.perms.getPermissionSet(world);
         if (set == null) {
            return false;
         } else {
            set.removeGroup(player, group);
            return true;
         }
      }
   }

   public String[] getPlayerGroups(String world, String player) {
      if (world == null) {
         return null;
      } else {
         PermissionSet set = this.perms.getPermissionSet(world);
         if (set == null) {
            return null;
         } else {
            List<String> groups = set.getGroups(player);
            return groups == null ? null : (String[])groups.toArray(new String[0]);
         }
      }
   }

   public String getPrimaryGroup(String world, String player) {
      if (world == null) {
         return null;
      } else {
         PermissionSet set = this.perms.getPermissionSet(world);
         if (set == null) {
            return null;
         } else {
            List<String> groups = set.getGroups(player);
            return groups != null && !groups.isEmpty() ? (String)groups.get(0) : null;
         }
      }
   }

   public String[] getGroups() {
      throw new UnsupportedOperationException("bPermissions does not support server-wide groups");
   }

   public boolean hasSuperPermsCompat() {
      return true;
   }

   public class PermissionServerListener implements Listener {
      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (Permission_bPermissions.this.perms == null) {
            Plugin p = event.getPlugin();
            if (p.getDescription().getName().equals("bPermissions") && p.isEnabled()) {
               Permission_bPermissions.this.perms = Permissions.getWorldPermissionsManager();
               Permission_bPermissions.log.info(String.format("[%s][Permission] %s hooked.", Permission_bPermissions.this.plugin.getDescription().getName(), "bPermissions"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (Permission_bPermissions.this.perms != null && event.getPlugin().getDescription().getName().equals("bPermissions")) {
            Permission_bPermissions.this.perms = null;
            Permission_bPermissions.log.info(String.format("[%s][Permission] %s un-hooked.", Permission_bPermissions.this.plugin.getDescription().getName(), "bPermissions"));
         }

      }
   }
}
