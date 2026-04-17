package net.milkbowl.vault.chat.plugins;

import java.util.logging.Logger;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.data.Group;
import org.anjocaido.groupmanager.data.User;
import org.anjocaido.groupmanager.dataholder.OverloadedWorldHolder;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

public class Chat_GroupManager extends Chat {
   private static final Logger log = Logger.getLogger("Minecraft");
   private final String name = "GroupManager - Chat";
   private Plugin plugin = null;
   private GroupManager groupManager;

   public Chat_GroupManager(Plugin plugin, Permission perms) {
      super(perms);
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new PermissionServerListener(this), plugin);
      if (this.groupManager == null) {
         Plugin chat = plugin.getServer().getPluginManager().getPlugin("GroupManager");
         if (chat != null && chat.isEnabled()) {
            this.groupManager = (GroupManager)chat;
            log.info(String.format("[%s][Chat] %s hooked.", plugin.getDescription().getName(), "GroupManager - Chat"));
         }
      }

   }

   public String getName() {
      this.getClass();
      return "GroupManager - Chat";
   }

   public boolean isEnabled() {
      return this.groupManager == null ? false : this.groupManager.isEnabled();
   }

   public int getPlayerInfoInteger(String worldName, String playerName, String node, int defaultValue) {
      AnjoPermissionsHandler handler;
      if (worldName == null) {
         handler = this.groupManager.getWorldsHolder().getWorldPermissionsByPlayerName(playerName);
      } else {
         handler = this.groupManager.getWorldsHolder().getWorldPermissions(worldName);
      }

      if (handler == null) {
         return defaultValue;
      } else {
         Integer val = handler.getUserPermissionInteger(playerName, node);
         return val != null ? val : defaultValue;
      }
   }

   public void setPlayerInfoInteger(String worldName, String playerName, String node, int value) {
      this.setPlayerValue(worldName, playerName, node, value);
   }

   public int getGroupInfoInteger(String worldName, String groupName, String node, int defaultValue) {
      AnjoPermissionsHandler handler;
      if (worldName == null) {
         handler = this.groupManager.getWorldsHolder().getDefaultWorld().getPermissionsHandler();
      } else {
         handler = this.groupManager.getWorldsHolder().getWorldPermissions(worldName);
      }

      if (handler == null) {
         return defaultValue;
      } else {
         Integer val = handler.getGroupPermissionInteger(groupName, node);
         return val != null ? val : defaultValue;
      }
   }

   public void setGroupInfoInteger(String worldName, String groupName, String node, int value) {
      this.setGroupValue(worldName, groupName, node, value);
   }

   public double getPlayerInfoDouble(String worldName, String playerName, String node, double defaultValue) {
      AnjoPermissionsHandler handler;
      if (worldName == null) {
         handler = this.groupManager.getWorldsHolder().getWorldPermissionsByPlayerName(playerName);
      } else {
         handler = this.groupManager.getWorldsHolder().getWorldPermissions(worldName);
      }

      if (handler == null) {
         return defaultValue;
      } else {
         Double val = handler.getUserPermissionDouble(playerName, node);
         return val != null ? val : defaultValue;
      }
   }

   public void setPlayerInfoDouble(String worldName, String playerName, String node, double value) {
      this.setPlayerValue(worldName, playerName, node, value);
   }

   public double getGroupInfoDouble(String worldName, String groupName, String node, double defaultValue) {
      AnjoPermissionsHandler handler;
      if (worldName == null) {
         handler = this.groupManager.getWorldsHolder().getDefaultWorld().getPermissionsHandler();
      } else {
         handler = this.groupManager.getWorldsHolder().getWorldPermissions(worldName);
      }

      if (handler == null) {
         return defaultValue;
      } else {
         Double val = handler.getGroupPermissionDouble(groupName, node);
         return val != null ? val : defaultValue;
      }
   }

   public void setGroupInfoDouble(String worldName, String groupName, String node, double value) {
      this.setGroupValue(worldName, groupName, node, value);
   }

   public boolean getPlayerInfoBoolean(String worldName, String playerName, String node, boolean defaultValue) {
      AnjoPermissionsHandler handler;
      if (worldName == null) {
         handler = this.groupManager.getWorldsHolder().getWorldPermissionsByPlayerName(playerName);
      } else {
         handler = this.groupManager.getWorldsHolder().getWorldPermissions(worldName);
      }

      if (handler == null) {
         return defaultValue;
      } else {
         Boolean val = handler.getUserPermissionBoolean(playerName, node);
         return val != null ? val : defaultValue;
      }
   }

   public void setPlayerInfoBoolean(String worldName, String playerName, String node, boolean value) {
      this.setPlayerValue(worldName, playerName, node, value);
   }

   public boolean getGroupInfoBoolean(String worldName, String groupName, String node, boolean defaultValue) {
      AnjoPermissionsHandler handler;
      if (worldName == null) {
         handler = this.groupManager.getWorldsHolder().getDefaultWorld().getPermissionsHandler();
      } else {
         handler = this.groupManager.getWorldsHolder().getWorldPermissions(worldName);
      }

      if (handler == null) {
         return defaultValue;
      } else {
         Boolean val = handler.getGroupPermissionBoolean(groupName, node);
         return val != null ? val : defaultValue;
      }
   }

   public void setGroupInfoBoolean(String worldName, String groupName, String node, boolean value) {
      this.setGroupValue(worldName, groupName, node, value);
   }

   public String getPlayerInfoString(String worldName, String playerName, String node, String defaultValue) {
      AnjoPermissionsHandler handler;
      if (worldName == null) {
         handler = this.groupManager.getWorldsHolder().getWorldPermissionsByPlayerName(playerName);
      } else {
         handler = this.groupManager.getWorldsHolder().getWorldPermissions(worldName);
      }

      if (handler == null) {
         return defaultValue;
      } else {
         String val = handler.getUserPermissionString(playerName, node);
         return val != null ? val : defaultValue;
      }
   }

   public void setPlayerInfoString(String worldName, String playerName, String node, String value) {
      this.setPlayerValue(worldName, playerName, node, value);
   }

   public String getGroupInfoString(String worldName, String groupName, String node, String defaultValue) {
      AnjoPermissionsHandler handler;
      if (worldName == null) {
         handler = this.groupManager.getWorldsHolder().getDefaultWorld().getPermissionsHandler();
      } else {
         handler = this.groupManager.getWorldsHolder().getWorldPermissions(worldName);
      }

      if (handler == null) {
         return defaultValue;
      } else {
         String val = handler.getGroupPermissionString(groupName, node);
         return val != null ? val : defaultValue;
      }
   }

   public void setGroupInfoString(String worldName, String groupName, String node, String value) {
      this.setGroupValue(worldName, groupName, node, value);
   }

   public String getPlayerPrefix(String worldName, String playerName) {
      AnjoPermissionsHandler handler;
      if (worldName == null) {
         handler = this.groupManager.getWorldsHolder().getWorldPermissionsByPlayerName(playerName);
      } else {
         handler = this.groupManager.getWorldsHolder().getWorldPermissions(worldName);
      }

      return handler == null ? "" : handler.getUserPrefix(playerName);
   }

   public String getPlayerSuffix(String worldName, String playerName) {
      AnjoPermissionsHandler handler;
      if (worldName == null) {
         handler = this.groupManager.getWorldsHolder().getWorldPermissionsByPlayerName(playerName);
      } else {
         handler = this.groupManager.getWorldsHolder().getWorldPermissions(worldName);
      }

      return handler == null ? "" : handler.getUserSuffix(playerName);
   }

   public void setPlayerSuffix(String worldName, String player, String suffix) {
      this.setPlayerInfoString(worldName, player, "suffix", suffix);
   }

   public void setPlayerPrefix(String worldName, String player, String prefix) {
      this.setPlayerInfoString(worldName, player, "prefix", prefix);
   }

   public String getGroupPrefix(String worldName, String group) {
      return this.getGroupInfoString(worldName, group, "prefix", "");
   }

   public void setGroupPrefix(String worldName, String group, String prefix) {
      this.setGroupInfoString(worldName, group, "prefix", prefix);
   }

   public String getGroupSuffix(String worldName, String group) {
      return this.getGroupInfoString(worldName, group, "suffix", "");
   }

   public void setGroupSuffix(String worldName, String group, String suffix) {
      this.setGroupInfoString(worldName, group, "suffix", suffix);
   }

   public String getPrimaryGroup(String worldName, String playerName) {
      AnjoPermissionsHandler handler;
      if (worldName == null) {
         handler = this.groupManager.getWorldsHolder().getWorldPermissionsByPlayerName(playerName);
      } else {
         handler = this.groupManager.getWorldsHolder().getWorldPermissions(worldName);
      }

      return handler.getGroup(playerName);
   }

   private void setPlayerValue(String worldName, String playerName, String node, Object value) {
      OverloadedWorldHolder owh;
      if (worldName == null) {
         owh = this.groupManager.getWorldsHolder().getWorldDataByPlayerName(playerName);
      } else {
         owh = this.groupManager.getWorldsHolder().getWorldData(worldName);
      }

      if (owh != null) {
         User user = owh.getUser(playerName);
         if (user != null) {
            user.getVariables().addVar(node, value);
         }
      }
   }

   private void setGroupValue(String worldName, String groupName, String node, Object value) {
      OverloadedWorldHolder owh;
      if (worldName == null) {
         owh = this.groupManager.getWorldsHolder().getDefaultWorld();
      } else {
         owh = this.groupManager.getWorldsHolder().getWorldData(worldName);
      }

      if (owh != null) {
         Group group = owh.getGroup(groupName);
         if (group != null) {
            group.getVariables().addVar(node, value);
         }
      }
   }

   public class PermissionServerListener implements Listener {
      Chat_GroupManager chat = null;

      public PermissionServerListener(Chat_GroupManager chat) {
         this.chat = chat;
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (this.chat.groupManager == null) {
            Plugin perms = Chat_GroupManager.this.plugin.getServer().getPluginManager().getPlugin("GroupManager");
            if (perms != null && perms.isEnabled()) {
               this.chat.groupManager = (GroupManager)perms;
               Chat_GroupManager.log.info(String.format("[%s][Chat] %s hooked.", Chat_GroupManager.this.plugin.getDescription().getName(), "GroupManager - Chat"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (this.chat.groupManager != null && event.getPlugin().getDescription().getName().equals("GroupManager")) {
            this.chat.groupManager = null;
            Chat_GroupManager.log.info(String.format("[%s][Chat] %s un-hooked.", Chat_GroupManager.this.plugin.getDescription().getName(), "GroupManager - Chat"));
         }

      }
   }
}
