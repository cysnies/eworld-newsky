package net.milkbowl.vault.permission.plugins;

import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.Calculable;
import de.bananaco.bpermissions.api.CalculableType;
import de.bananaco.bpermissions.api.WorldManager;
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

public class Permission_bPermissions2 extends Permission {
   private final String name = "bPermissions2";
   private boolean hooked = false;

   public Permission_bPermissions2(Plugin plugin) {
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new PermissionServerListener(), plugin);
      if (!this.hooked) {
         Plugin p = plugin.getServer().getPluginManager().getPlugin("bPermissions");
         if (p != null) {
            this.hooked = true;
            log.info(String.format("[%s][Permission] %s hooked.", plugin.getDescription().getName(), "bPermissions2"));
         }
      }

   }

   public String getName() {
      return "bPermissions2";
   }

   public boolean isEnabled() {
      return this.hooked;
   }

   public boolean has(Player player, String permission) {
      return this.playerHas(player.getWorld().getName(), player.getName(), permission);
   }

   public boolean has(String world, String player, String permission) {
      return this.playerHas(world, player, permission);
   }

   public boolean has(CommandSender sender, String permission) {
      if (sender instanceof Player) {
         Player player = (Player)sender;
         return this.has(player, permission);
      } else {
         return sender.hasPermission(permission);
      }
   }

   public boolean has(World world, String player, String permission) {
      return this.playerHas(world.getName(), player, permission);
   }

   public boolean playerHas(String world, String player, String permission) {
      return ApiLayer.hasPermission(world, CalculableType.USER, player, permission);
   }

   public boolean playerAdd(String world, String player, String permission) {
      ApiLayer.addPermission(world, CalculableType.USER, player, de.bananaco.bpermissions.api.Permission.loadFromString(permission));
      return true;
   }

   public boolean playerRemove(String world, String player, String permission) {
      ApiLayer.removePermission(world, CalculableType.USER, player, permission);
      return true;
   }

   public boolean groupHas(String world, String group, String permission) {
      return ApiLayer.hasPermission(world, CalculableType.GROUP, group, permission);
   }

   public boolean groupAdd(String world, String group, String permission) {
      ApiLayer.addPermission(world, CalculableType.GROUP, group, de.bananaco.bpermissions.api.Permission.loadFromString(permission));
      return true;
   }

   public boolean groupRemove(String world, String group, String permission) {
      ApiLayer.removePermission(world, CalculableType.GROUP, group, permission);
      return true;
   }

   public boolean playerInGroup(String world, String player, String group) {
      return ApiLayer.hasGroup(world, CalculableType.USER, player, group);
   }

   public boolean playerAddGroup(String world, String player, String group) {
      ApiLayer.addGroup(world, CalculableType.USER, player, group);
      return true;
   }

   public boolean playerRemoveGroup(String world, String player, String group) {
      ApiLayer.removeGroup(world, CalculableType.USER, player, group);
      return true;
   }

   public String[] getPlayerGroups(String world, String player) {
      return ApiLayer.getGroups(world, CalculableType.USER, player);
   }

   public String getPrimaryGroup(String world, String player) {
      String[] groups = this.getPlayerGroups(world, player);
      return groups != null && groups.length > 0 ? groups[0] : null;
   }

   public String[] getGroups() {
      String[] groups = null;
      Set<String> gSet = new HashSet();

      for(de.bananaco.bpermissions.api.World world : WorldManager.getInstance().getAllWorlds()) {
         for(Calculable c : world.getAll(CalculableType.GROUP)) {
            gSet.add(c.getNameLowerCase());
         }
      }

      groups = (String[])gSet.toArray(new String[gSet.size()]);
      return groups;
   }

   public boolean hasSuperPermsCompat() {
      return true;
   }

   public class PermissionServerListener implements Listener {
      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (!Permission_bPermissions2.this.hooked) {
            Plugin p = event.getPlugin();
            if (p.getDescription().getName().equals("bPermissions") && p.isEnabled()) {
               Permission_bPermissions2.this.hooked = true;
               Permission_bPermissions2.log.info(String.format("[%s][Permission] %s hooked.", Permission_bPermissions2.this.plugin.getDescription().getName(), "bPermissions2"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (Permission_bPermissions2.this.hooked && event.getPlugin().getDescription().getName().equals("bPermissions")) {
            Permission_bPermissions2.this.hooked = false;
            Permission_bPermissions2.log.info(String.format("[%s][Permission] %s un-hooked.", Permission_bPermissions2.this.plugin.getDescription().getName(), "bPermissions2"));
         }

      }
   }
}
