package net.milkbowl.vault.permission.plugins;

import com.nijiko.permissions.Group;
import com.nijiko.permissions.ModularControl;
import com.nijikokun.bukkit.Permissions.Permissions;
import java.util.HashSet;
import java.util.Set;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

public class Permission_Permissions3 extends Permission {
   private String name = "Permissions3";
   private ModularControl perms;
   private Permissions permission = null;

   public Permission_Permissions3(Plugin plugin) {
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new PermissionServerListener(), plugin);
      if (this.permission == null) {
         Plugin perms = plugin.getServer().getPluginManager().getPlugin("Permissions");
         if (perms == null) {
            plugin.getServer().getPluginManager().getPlugin("vPerms");
            this.name = "vPerms";
         }

         if (perms != null && perms.isEnabled() && perms.getDescription().getVersion().startsWith("3")) {
            this.permission = (Permissions)perms;
            this.perms = (ModularControl)this.permission.getHandler();
            log.severe("Your permission system is outdated and no longer fully supported! It is highly advised to update!");
            log.info(String.format("[%s][Permission] %s hooked.", plugin.getDescription().getName(), this.name));
         }
      }

   }

   public boolean isEnabled() {
      return this.permission == null ? false : this.permission.isEnabled();
   }

   public boolean playerInGroup(String worldName, String playerName, String groupName) {
      return this.permission.getHandler().inGroup(worldName, playerName, groupName);
   }

   public String getName() {
      return this.name;
   }

   public boolean has(CommandSender sender, String permission) {
      return !sender.isOp() && sender instanceof Player ? this.has(((Player)sender).getWorld().getName(), sender.getName(), permission) : true;
   }

   public boolean has(Player player, String permission) {
      return this.has(player.getWorld().getName(), player.getName(), permission);
   }

   public boolean playerAddGroup(String worldName, String playerName, String groupName) {
      if (worldName == null) {
         worldName = "*";
      }

      Group g = this.perms.getGroupObject(worldName, groupName);
      if (g == null) {
         return false;
      } else {
         try {
            this.perms.safeGetUser(worldName, playerName).addParent(g);
            return true;
         } catch (Exception e) {
            e.printStackTrace();
            return false;
         }
      }
   }

   public boolean playerRemoveGroup(String worldName, String playerName, String groupName) {
      if (worldName == null) {
         worldName = "*";
      }

      Group g = this.perms.getGroupObject(worldName, groupName);
      if (g == null) {
         return false;
      } else {
         try {
            this.perms.safeGetUser(worldName, playerName).removeParent(g);
            return true;
         } catch (Exception e) {
            e.printStackTrace();
            return false;
         }
      }
   }

   public boolean playerAdd(String worldName, String playerName, String permission) {
      this.perms.addUserPermission(worldName, playerName, permission);
      return true;
   }

   public boolean playerRemove(String worldName, String playerName, String permission) {
      this.perms.removeUserPermission(worldName, playerName, permission);
      return true;
   }

   public boolean groupAdd(String worldName, String groupName, String permission) {
      if (worldName == null) {
         worldName = "*";
      }

      this.perms.addGroupPermission(worldName, groupName, permission);
      return true;
   }

   public boolean groupRemove(String worldName, String groupName, String permission) {
      if (worldName == null) {
         worldName = "*";
      }

      this.perms.removeGroupPermission(worldName, groupName, permission);
      return true;
   }

   public boolean groupHas(String worldName, String groupName, String permission) {
      if (worldName == null) {
         worldName = "*";
      }

      try {
         return this.perms.safeGetGroup(worldName, groupName).hasPermission(permission);
      } catch (Exception e) {
         e.printStackTrace();
         return false;
      }
   }

   public String[] getPlayerGroups(String world, String playerName) {
      return this.perms.getGroups(world, playerName);
   }

   public String getPrimaryGroup(String world, String playerName) {
      return this.getPlayerGroups(world, playerName)[0];
   }

   public boolean playerHas(String worldName, String playerName, String permission) {
      Player p = this.plugin.getServer().getPlayer(playerName);
      return p != null && p.hasPermission(permission) ? true : this.perms.has(worldName, playerName, permission);
   }

   public boolean playerAddTransient(String player, String permission) {
      return this.playerAddTransient((String)null, (String)player, permission);
   }

   public boolean playerAddTransient(Player player, String permission) {
      return this.playerAddTransient((String)null, (String)player.getName(), permission);
   }

   public boolean playerAddTransient(String worldName, Player player, String permission) {
      return this.playerAddTransient(worldName, player.getName(), permission);
   }

   public boolean playerAddTransient(String worldName, String player, String permission) {
      if (worldName == null) {
         worldName = "*";
      }

      try {
         this.perms.safeGetUser(worldName, player).addTransientPermission(permission);
         return true;
      } catch (Exception var5) {
         return false;
      }
   }

   public boolean playerRemoveTransient(String player, String permission) {
      return this.playerRemoveTransient((String)null, (String)player, permission);
   }

   public boolean playerRemoveTransient(Player player, String permission) {
      return this.playerRemoveTransient((String)null, (String)player.getName(), permission);
   }

   public boolean playerRemoveTransient(String worldName, Player player, String permission) {
      return this.playerRemoveTransient(worldName, player.getName(), permission);
   }

   public boolean playerRemoveTransient(String worldName, String player, String permission) {
      if (worldName == null) {
         worldName = "*";
      }

      try {
         this.perms.safeGetUser(worldName, player).removeTransientPermission(permission);
         return true;
      } catch (Exception var5) {
         return false;
      }
   }

   public String[] getGroups() {
      Set<String> groupNames = new HashSet();

      for(World world : Bukkit.getServer().getWorlds()) {
         for(Group group : this.perms.getGroups(world.getName())) {
            groupNames.add(group.getName());
         }
      }

      return (String[])groupNames.toArray(new String[0]);
   }

   public boolean hasSuperPermsCompat() {
      return false;
   }

   public class PermissionServerListener implements Listener {
      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (Permission_Permissions3.this.permission == null) {
            Plugin permi = event.getPlugin();
            if ((permi.getDescription().getName().equals("Permissions") || permi.getDescription().getName().equals("vPerms")) && permi.getDescription().getVersion().startsWith("3") && permi.isEnabled()) {
               Permission_Permissions3.this.permission = (Permissions)permi;
               Permission_Permissions3.this.perms = (ModularControl)Permission_Permissions3.this.permission.getHandler();
               Permission_Permissions3.log.info(String.format("[%s][Permission] %s hooked.", Permission_Permissions3.this.plugin.getDescription().getName(), Permission_Permissions3.this.name));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (Permission_Permissions3.this.permission != null && (event.getPlugin().getDescription().getName().equals("Permissions") || event.getPlugin().getDescription().getName().equals("vPerms"))) {
            Permission_Permissions3.this.permission = null;
            Permission_Permissions3.this.perms = null;
            Permission_Permissions3.log.info(String.format("[%s][Permission] %s un-hooked.", Permission_Permissions3.this.plugin.getDescription().getName(), Permission_Permissions3.this.name));
         }

      }
   }
}
