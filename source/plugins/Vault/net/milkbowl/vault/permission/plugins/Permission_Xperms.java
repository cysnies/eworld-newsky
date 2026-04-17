package net.milkbowl.vault.permission.plugins;

import com.github.sebc722.Xperms.Xmain;
import java.util.ArrayList;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

public class Permission_Xperms extends Permission {
   private final String name = "Xperms";
   private Xmain perms = null;

   public Permission_Xperms(Plugin plugin) {
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new PermissionServerListener(this), plugin);
      if (this.perms == null) {
         Plugin perms = plugin.getServer().getPluginManager().getPlugin("Xperms");
         if (perms != null) {
            if (perms.isEnabled()) {
               try {
                  if (Double.valueOf(perms.getDescription().getVersion()) < 1.1) {
                     log.info(String.format("[%s] [Permission] %s Current version is not compatible with vault! Please Update!", plugin.getDescription().getName(), "Xperms"));
                  }
               } catch (NumberFormatException var4) {
                  log.info(String.format("[%s] [Permission] %s Current version is not compatibe with vault! Please Update!", plugin.getDescription().getName(), "Xperms"));
               }
            }

            Plugin var5 = (Xmain)perms;
            log.info(String.format("[%s][Permission] %s hooked.", plugin.getDescription().getName(), "Xperms"));
         }
      }

   }

   public String getName() {
      return "Xperms";
   }

   public boolean isEnabled() {
      return this.perms.isEnabled();
   }

   public boolean hasSuperPermsCompat() {
      return true;
   }

   public boolean playerHas(String world, String player, String permission) {
      return this.perms.getXplayer().hasPermission(player, permission);
   }

   public boolean playerAdd(String world, String player, String permission) {
      return this.perms.getXplayer().addPermission(player, permission);
   }

   public boolean playerRemove(String world, String player, String permission) {
      return this.perms.getXplayer().removePermission(player, permission);
   }

   public boolean groupHas(String world, String group, String permission) {
      return this.perms.getXgroup().hasPermission(group, permission);
   }

   public boolean groupAdd(String world, String group, String permission) {
      return this.perms.getXgroup().addPermission(group, permission);
   }

   public boolean groupRemove(String world, String group, String permission) {
      return this.perms.getXgroup().removePermission(group, permission);
   }

   public boolean playerInGroup(String world, String player, String group) {
      String userGroup = this.perms.getXusers().getUserGroup(player);
      return userGroup == group;
   }

   public boolean playerAddGroup(String world, String player, String group) {
      return this.perms.getXplayer().setGroup(player, group);
   }

   public boolean playerRemoveGroup(String world, String player, String group) {
      return this.perms.getXplayer().setGroup(player, "def");
   }

   public String[] getPlayerGroups(String world, String player) {
      ArrayList<String> playerGroup = new ArrayList();
      playerGroup.add(this.perms.getXusers().getUserGroup(player));
      String[] playerGroupArray = (String[])playerGroup.toArray(new String[0]);
      return playerGroupArray;
   }

   public String getPrimaryGroup(String world, String player) {
      return this.perms.getXusers().getUserGroup(player);
   }

   public String[] getGroups() {
      return this.perms.getXperms().getGroups();
   }

   public class PermissionServerListener implements Listener {
      Permission_Xperms permission = null;

      public PermissionServerListener(Permission_Xperms permission) {
         this.permission = permission;
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (event.getPlugin().getDescription().getName().equals("Xperms")) {
            Plugin perms = event.getPlugin();

            try {
               if (Double.valueOf(perms.getDescription().getVersion()) < 1.1) {
                  Permission_Xperms.log.info(String.format("[%s] [Permission] %s Current version is not compatible with vault! Please Update!", Permission_Xperms.this.plugin.getDescription().getName(), "Xperms"));
               }
            } catch (NumberFormatException var4) {
               Permission_Xperms.log.info(String.format("[%s] [Permission] %s Current version is not compatibe with vault! Please Update!", Permission_Xperms.this.plugin.getDescription().getName(), "Xperms"));
            }

            this.permission.perms = (Xmain)perms;
            Permission_Xperms.log.info(String.format("[%s][Permission] %s hooked.", Permission_Xperms.this.plugin.getDescription().getName(), "Xperms"));
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (this.permission.perms != null && event.getPlugin().getName().equals("Xperms")) {
            this.permission.perms = null;
            Permission_Xperms.log.info(String.format("[%s][Permission] %s un-hooked.", Permission_Xperms.this.plugin.getDescription().getName(), "Xperms"));
         }

      }
   }
}
