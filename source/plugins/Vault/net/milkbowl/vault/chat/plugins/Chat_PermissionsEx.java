package net.milkbowl.vault.chat.plugins;

import java.util.logging.Logger;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class Chat_PermissionsEx extends Chat {
   private static final Logger log = Logger.getLogger("Minecraft");
   private final String name = "PermissionsEx_Chat";
   private Plugin plugin = null;
   private PermissionsEx chat = null;

   public Chat_PermissionsEx(Plugin plugin, Permission perms) {
      super(perms);
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new PermissionServerListener(this), plugin);
      if (this.chat == null) {
         Plugin p = plugin.getServer().getPluginManager().getPlugin("PermissionsEx");
         if (p != null && p.isEnabled()) {
            this.chat = (PermissionsEx)p;
            log.info(String.format("[%s][Chat] %s hooked.", plugin.getDescription().getName(), "PermissionsEx_Chat"));
         }
      }

   }

   public String getName() {
      return "PermissionsEx_Chat";
   }

   public boolean isEnabled() {
      return this.chat == null ? false : this.chat.isEnabled();
   }

   public int getPlayerInfoInteger(String world, String playerName, String node, int defaultValue) {
      return PermissionsEx.getPermissionManager().getUser(playerName).getOptionInteger(node, world, defaultValue);
   }

   public double getPlayerInfoDouble(String world, String playerName, String node, double defaultValue) {
      return PermissionsEx.getPermissionManager().getUser(playerName).getOptionDouble(node, world, defaultValue);
   }

   public boolean getPlayerInfoBoolean(String world, String playerName, String node, boolean defaultValue) {
      return PermissionsEx.getPermissionManager().getUser(playerName).getOptionBoolean(node, world, defaultValue);
   }

   public String getPlayerInfoString(String world, String playerName, String node, String defaultValue) {
      return PermissionsEx.getPermissionManager().getUser(playerName).getOption(node, world, defaultValue);
   }

   public void setPlayerInfoInteger(String world, String playerName, String node, int value) {
      PermissionUser user = PermissionsEx.getPermissionManager().getUser(playerName);
      if (user != null) {
         user.setOption(node, String.valueOf(value), world);
      }

   }

   public void setPlayerInfoDouble(String world, String playerName, String node, double value) {
      PermissionUser user = PermissionsEx.getPermissionManager().getUser(playerName);
      if (user != null) {
         user.setOption(node, String.valueOf(value), world);
      }

   }

   public void setPlayerInfoBoolean(String world, String playerName, String node, boolean value) {
      PermissionUser user = PermissionsEx.getPermissionManager().getUser(playerName);
      if (user != null) {
         user.setOption(node, String.valueOf(value), world);
      }

   }

   public void setPlayerInfoString(String world, String playerName, String node, String value) {
      PermissionUser user = PermissionsEx.getPermissionManager().getUser(playerName);
      if (user != null) {
         user.setOption(node, String.valueOf(value), world);
      }

   }

   public int getGroupInfoInteger(String world, String groupName, String node, int defaultValue) {
      PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(groupName);
      return group == null ? defaultValue : group.getOptionInteger(node, world, defaultValue);
   }

   public void setGroupInfoInteger(String world, String groupName, String node, int value) {
      PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(groupName);
      if (group != null) {
         group.setOption(node, world, String.valueOf(value));
      }
   }

   public double getGroupInfoDouble(String world, String groupName, String node, double defaultValue) {
      PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(groupName);
      return group == null ? defaultValue : group.getOptionDouble(node, world, defaultValue);
   }

   public void setGroupInfoDouble(String world, String groupName, String node, double value) {
      PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(groupName);
      if (group != null) {
         group.setOption(node, world, String.valueOf(value));
      }
   }

   public boolean getGroupInfoBoolean(String world, String groupName, String node, boolean defaultValue) {
      PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(groupName);
      return group == null ? defaultValue : group.getOptionBoolean(node, world, defaultValue);
   }

   public void setGroupInfoBoolean(String world, String groupName, String node, boolean value) {
      PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(groupName);
      if (group != null) {
         group.setOption(node, world, String.valueOf(value));
      }
   }

   public String getGroupInfoString(String world, String groupName, String node, String defaultValue) {
      PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(groupName);
      return group == null ? defaultValue : group.getOption(node, world, defaultValue);
   }

   public void setGroupInfoString(String world, String groupName, String node, String value) {
      PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(groupName);
      if (group != null) {
         group.setOption(node, world, value);
      }
   }

   public String getPlayerPrefix(String world, String playerName) {
      PermissionUser user = PermissionsEx.getPermissionManager().getUser(playerName);
      return user != null ? user.getPrefix(world) : null;
   }

   public String getPlayerSuffix(String world, String playerName) {
      PermissionUser user = PermissionsEx.getPermissionManager().getUser(playerName);
      return user != null ? user.getSuffix(world) : null;
   }

   public void setPlayerSuffix(String world, String player, String suffix) {
      PermissionUser user = PermissionsEx.getPermissionManager().getUser(player);
      if (user != null) {
         user.setSuffix(suffix, world);
      }

   }

   public void setPlayerPrefix(String world, String player, String prefix) {
      PermissionUser user = PermissionsEx.getPermissionManager().getUser(player);
      if (user != null) {
         user.setPrefix(prefix, world);
      }

   }

   public String getGroupPrefix(String world, String group) {
      PermissionGroup pGroup = PermissionsEx.getPermissionManager().getGroup(group);
      return group != null ? pGroup.getPrefix(world) : null;
   }

   public void setGroupPrefix(String world, String group, String prefix) {
      PermissionGroup pGroup = PermissionsEx.getPermissionManager().getGroup(group);
      if (group != null) {
         pGroup.setPrefix(prefix, world);
      }

   }

   public String getGroupSuffix(String world, String group) {
      PermissionGroup pGroup = PermissionsEx.getPermissionManager().getGroup(group);
      return group != null ? pGroup.getSuffix(world) : null;
   }

   public void setGroupSuffix(String world, String group, String suffix) {
      PermissionGroup pGroup = PermissionsEx.getPermissionManager().getGroup(group);
      if (group != null) {
         pGroup.setSuffix(suffix, world);
      }

   }

   public class PermissionServerListener implements Listener {
      Chat_PermissionsEx chat = null;

      public PermissionServerListener(Chat_PermissionsEx chat) {
         this.chat = chat;
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (this.chat.chat == null) {
            Plugin perms = Chat_PermissionsEx.this.plugin.getServer().getPluginManager().getPlugin("PermissionsEx");
            if (perms != null && perms.isEnabled()) {
               this.chat.chat = (PermissionsEx)perms;
               Chat_PermissionsEx.log.info(String.format("[%s][Chat] %s hooked.", Chat_PermissionsEx.this.plugin.getDescription().getName(), "PermissionsEx_Chat"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (this.chat.chat != null && event.getPlugin().getDescription().getName().equals("PermissionsEx")) {
            this.chat.chat = null;
            Chat_PermissionsEx.log.info(String.format("[%s][Chat] %s un-hooked.", Chat_PermissionsEx.this.plugin.getDescription().getName(), "PermissionsEx_Chat"));
         }

      }
   }
}
