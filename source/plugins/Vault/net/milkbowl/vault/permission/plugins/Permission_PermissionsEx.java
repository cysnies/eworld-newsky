package net.milkbowl.vault.permission.plugins;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class Permission_PermissionsEx extends Permission {
   private final String name = "PermissionsEx";
   private PermissionsEx permission = null;

   public Permission_PermissionsEx(Plugin plugin) {
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new PermissionServerListener(this), plugin);
      if (this.permission == null) {
         Plugin perms = plugin.getServer().getPluginManager().getPlugin("PermissionsEx");
         if (perms != null && perms.isEnabled()) {
            try {
               if (Double.valueOf(perms.getDescription().getVersion()) < 1.16) {
                  log.info(String.format("[%s][Permission] %s below 1.16 is not compatible with Vault! Falling back to SuperPerms only mode. PLEASE UPDATE!", plugin.getDescription().getName(), "PermissionsEx"));
               }
            } catch (NumberFormatException var4) {
            }

            this.permission = (PermissionsEx)perms;
            log.info(String.format("[%s][Permission] %s hooked.", plugin.getDescription().getName(), "PermissionsEx"));
         }
      }

   }

   public boolean isEnabled() {
      return this.permission == null ? false : this.permission.isEnabled();
   }

   public boolean playerInGroup(String worldName, String playerName, String groupName) {
      return PermissionsEx.getPermissionManager().getUser(playerName).inGroup(groupName);
   }

   public String getName() {
      return "PermissionsEx";
   }

   public boolean playerAddGroup(String worldName, String playerName, String groupName) {
      PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(groupName);
      PermissionUser user = PermissionsEx.getPermissionManager().getUser(playerName);
      if (group != null && user != null) {
         user.addGroup(group);
         return true;
      } else {
         return false;
      }
   }

   public boolean playerRemoveGroup(String worldName, String playerName, String groupName) {
      PermissionsEx.getPermissionManager().getUser(playerName).removeGroup(groupName);
      return true;
   }

   public boolean playerAdd(String worldName, String playerName, String permission) {
      PermissionUser user = PermissionsEx.getPermissionManager().getUser(playerName);
      if (user == null) {
         return false;
      } else {
         user.addPermission(permission, worldName);
         return true;
      }
   }

   public boolean playerRemove(String worldName, String playerName, String permission) {
      PermissionUser user = PermissionsEx.getPermissionManager().getUser(playerName);
      if (user == null) {
         return false;
      } else {
         user.removePermission(permission, worldName);
         return true;
      }
   }

   public boolean groupAdd(String worldName, String groupName, String permission) {
      PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(groupName);
      if (group == null) {
         return false;
      } else {
         group.addPermission(permission, worldName);
         return true;
      }
   }

   public boolean groupRemove(String worldName, String groupName, String permission) {
      PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(groupName);
      if (group == null) {
         return false;
      } else {
         group.removePermission(permission, worldName);
         return true;
      }
   }

   public boolean groupHas(String worldName, String groupName, String permission) {
      PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(groupName);
      return group == null ? false : group.has(permission, worldName);
   }

   public String[] getPlayerGroups(String world, String playerName) {
      return PermissionsEx.getPermissionManager().getUser(playerName).getGroupsNames();
   }

   public String getPrimaryGroup(String world, String playerName) {
      PermissionUser user = PermissionsEx.getPermissionManager().getUser(playerName);
      if (user == null) {
         return null;
      } else {
         return user.getGroupsNames(world).length > 0 ? user.getGroupsNames(world)[0] : null;
      }
   }

   public boolean playerHas(String worldName, String playerName, String permission) {
      PermissionUser user = PermissionsEx.getPermissionManager().getUser(playerName);
      return user != null ? user.has(permission, worldName) : false;
   }

   public boolean playerAddTransient(String worldName, String player, String permission) {
      PermissionUser pPlayer = PermissionsEx.getPermissionManager().getUser(player);
      if (pPlayer != null) {
         pPlayer.addTimedPermission(permission, worldName, 0);
         return true;
      } else {
         return false;
      }
   }

   public boolean playerAddTransient(String worldName, Player player, String permission) {
      return this.playerAddTransient(worldName, player.getName(), permission);
   }

   public boolean playerAddTransient(String player, String permission) {
      return this.playerAddTransient((String)null, (String)player, permission);
   }

   public boolean playerAddTransient(Player player, String permission) {
      return this.playerAddTransient((String)null, (String)player.getName(), permission);
   }

   public boolean playerRemoveTransient(String worldName, String player, String permission) {
      PermissionUser pPlayer = PermissionsEx.getPermissionManager().getUser(player);
      if (pPlayer != null) {
         pPlayer.removeTimedPermission(permission, worldName);
         return true;
      } else {
         return false;
      }
   }

   public boolean playerRemoveTransient(Player player, String permission) {
      return this.playerRemoveTransient((String)null, (String)player.getName(), permission);
   }

   public boolean playerRemoveTransient(String worldName, Player player, String permission) {
      return this.playerRemoveTransient(worldName, player.getName(), permission);
   }

   public boolean playerRemoveTransient(String player, String permission) {
      return this.playerRemoveTransient((String)null, (String)player, permission);
   }

   public String[] getGroups() {
      PermissionGroup[] groups = PermissionsEx.getPermissionManager().getGroups();
      if (groups != null && groups.length != 0) {
         String[] groupNames = new String[groups.length];

         for(int i = 0; i < groups.length; ++i) {
            groupNames[i] = groups[i].getName();
         }

         return groupNames;
      } else {
         return null;
      }
   }

   public boolean hasSuperPermsCompat() {
      return true;
   }

   public class PermissionServerListener implements Listener {
      Permission_PermissionsEx permission = null;

      public PermissionServerListener(Permission_PermissionsEx permission) {
         this.permission = permission;
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (this.permission.permission == null) {
            Plugin perms = Permission_PermissionsEx.this.plugin.getServer().getPluginManager().getPlugin("PermissionsEx");
            if (perms != null) {
               try {
                  if (Double.valueOf(perms.getDescription().getVersion()) < 1.16) {
                     Permission_PermissionsEx.log.info(String.format("[%s][Permission] %s below 1.16 is not compatible with Vault! Falling back to SuperPerms only mode. PLEASE UPDATE!", Permission_PermissionsEx.this.plugin.getDescription().getName(), "PermissionsEx"));
                     return;
                  }
               } catch (NumberFormatException var4) {
               }

               this.permission.permission = (PermissionsEx)perms;
               Permission_PermissionsEx.log.info(String.format("[%s][Permission] %s hooked.", Permission_PermissionsEx.this.plugin.getDescription().getName(), "PermissionsEx"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (this.permission.permission != null && event.getPlugin().getDescription().getName().equals("PermissionsEx")) {
            this.permission.permission = null;
            Permission_PermissionsEx.log.info(String.format("[%s][Permission] %s un-hooked.", Permission_PermissionsEx.this.plugin.getDescription().getName(), "PermissionsEx"));
         }

      }
   }
}
