package net.milkbowl.vault.permission.plugins;

import com.platymuus.bukkit.permissions.Group;
import com.platymuus.bukkit.permissions.PermissionInfo;
import com.platymuus.bukkit.permissions.PermissionsPlugin;
import java.util.ArrayList;
import java.util.List;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

public class Permission_PermissionsBukkit extends Permission {
   private final String name = "PermissionsBukkit";
   private PermissionsPlugin perms = null;

   public Permission_PermissionsBukkit(Plugin plugin) {
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new PermissionServerListener(this), plugin);
      if (this.perms == null) {
         Plugin perms = plugin.getServer().getPluginManager().getPlugin("PermissionsBukkit");
         if (perms != null) {
            this.perms = (PermissionsPlugin)perms;
            log.info(String.format("[%s][Permission] %s hooked.", plugin.getDescription().getName(), "PermissionsBukkit"));
         }
      }

   }

   public String getName() {
      return "PermissionsBukkit";
   }

   public boolean isEnabled() {
      return this.perms == null ? false : this.perms.isEnabled();
   }

   public boolean playerHas(String world, String player, String permission) {
      return Bukkit.getPlayer(player) != null ? Bukkit.getPlayer(player).hasPermission(permission) : false;
   }

   public boolean playerAdd(String world, String player, String permission) {
      if (world != null) {
         permission = world + ":" + permission;
      }

      return this.plugin.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "permissions player setperm " + player + " " + permission + " true");
   }

   public boolean playerRemove(String world, String player, String permission) {
      if (world != null) {
         permission = world + ":" + permission;
      }

      return this.plugin.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "permissions player unsetperm " + player + " " + permission);
   }

   public boolean groupHas(String world, String group, String permission) {
      if (world != null && !world.isEmpty()) {
         return this.perms.getGroup(group).getInfo().getWorldPermissions(world).get(permission) == null ? false : (Boolean)this.perms.getGroup(group).getInfo().getWorldPermissions(world).get(permission);
      } else if (this.perms.getGroup(group) == null) {
         return false;
      } else if (this.perms.getGroup(group).getInfo() == null) {
         return false;
      } else {
         return this.perms.getGroup(group).getInfo().getPermissions() == null ? false : (Boolean)this.perms.getGroup(group).getInfo().getPermissions().get(permission);
      }
   }

   public boolean groupAdd(String world, String group, String permission) {
      if (world != null) {
         permission = world + ":" + permission;
      }

      return this.plugin.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "permissions group setperm " + group + " " + permission + " true");
   }

   public boolean groupRemove(String world, String group, String permission) {
      if (world != null) {
         permission = world + ":" + permission;
      }

      return this.plugin.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "permissions group unsetperm " + group + " " + permission);
   }

   public boolean playerInGroup(String world, String player, String group) {
      if (world != null) {
         for(Group g : this.perms.getPlayerInfo(player).getGroups()) {
            if (g.getName().equals(group)) {
               return g.getInfo().getWorlds().contains(world);
            }
         }

         return false;
      } else {
         Group g = this.perms.getGroup(group);
         return g == null ? false : g.getPlayers().contains(player);
      }
   }

   public boolean playerAddGroup(String world, String player, String group) {
      return world != null ? false : this.plugin.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "permissions player addgroup " + player + " " + group);
   }

   public boolean playerRemoveGroup(String world, String player, String group) {
      return world != null ? false : this.plugin.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "permissions player removegroup " + player + " " + group);
   }

   public String[] getPlayerGroups(String world, String player) {
      List<String> groupList = new ArrayList();
      PermissionInfo info = this.perms.getPlayerInfo(player);
      if (world != null && info != null) {
         for(Group group : this.perms.getPlayerInfo(player).getGroups()) {
            if (group.getInfo().getWorlds().contains(world)) {
               groupList.add(group.getName());
            }
         }

         return (String[])groupList.toArray(new String[0]);
      } else {
         if (info != null) {
            for(Group group : info.getGroups()) {
               groupList.add(group.getName());
            }
         }

         return (String[])groupList.toArray(new String[0]);
      }
   }

   public String getPrimaryGroup(String world, String player) {
      if (this.perms.getPlayerInfo(player) == null) {
         return null;
      } else {
         return this.perms.getPlayerInfo(player).getGroups() != null && !this.perms.getPlayerInfo(player).getGroups().isEmpty() ? ((Group)this.perms.getPlayerInfo(player).getGroups().get(0)).getName() : null;
      }
   }

   public String[] getGroups() {
      List<String> groupNames = new ArrayList();

      for(Group group : this.perms.getAllGroups()) {
         groupNames.add(group.getName());
      }

      return (String[])groupNames.toArray(new String[0]);
   }

   public boolean hasSuperPermsCompat() {
      return true;
   }

   public class PermissionServerListener implements Listener {
      Permission_PermissionsBukkit permission = null;

      public PermissionServerListener(Permission_PermissionsBukkit permission) {
         this.permission = permission;
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (this.permission.perms == null) {
            Plugin perms = Permission_PermissionsBukkit.this.plugin.getServer().getPluginManager().getPlugin("PermissionsBukkit");
            if (perms != null && perms.isEnabled()) {
               this.permission.perms = (PermissionsPlugin)perms;
               Permission_PermissionsBukkit.log.info(String.format("[%s][Permission] %s hooked.", Permission_PermissionsBukkit.this.plugin.getDescription().getName(), "PermissionsBukkit"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (this.permission.perms != null && event.getPlugin().getDescription().getName().equals("PermissionsBukkit")) {
            this.permission.perms = null;
            Permission_PermissionsBukkit.log.info(String.format("[%s][Permission] %s un-hooked.", Permission_PermissionsBukkit.this.plugin.getDescription().getName(), "PermissionsBukkit"));
         }

      }
   }
}
