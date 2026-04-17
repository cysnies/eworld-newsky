package net.milkbowl.vault.chat.plugins;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
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

public class Chat_Permissions3 extends Chat {
   private static final Logger log = Logger.getLogger("Minecraft");
   private String name = "Permissions 3 (Yeti) - Chat";
   private PermissionHandler perms;
   private Plugin plugin = null;
   private Permissions chat = null;

   public Chat_Permissions3(Plugin plugin, Permission perms) {
      super(perms);
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new PermissionServerListener(), plugin);
      if (this.chat == null) {
         Plugin p = plugin.getServer().getPluginManager().getPlugin("Permissions");
         if (p == null) {
            plugin.getServer().getPluginManager().getPlugin("vPerms");
            this.name = "vPerms - Chat";
         }

         if (p != null && p.isEnabled() && p.getDescription().getVersion().startsWith("3")) {
            this.chat = (Permissions)p;
            this.perms = this.chat.getHandler();
            log.info(String.format("[%s][Chat] %s hooked.", plugin.getDescription().getName(), this.name));
         }
      }

   }

   public String getName() {
      return this.name;
   }

   public boolean isEnabled() {
      return this.chat == null ? false : this.chat.isEnabled();
   }

   public int getPlayerInfoInteger(String world, String playerName, String node, int defaultValue) {
      Integer i = this.perms.getPermissionInteger(world, playerName, node);
      return i == null ? defaultValue : i;
   }

   public double getPlayerInfoDouble(String world, String playerName, String node, double defaultValue) {
      Double d = this.perms.getPermissionDouble(world, playerName, node);
      return d == null ? defaultValue : d;
   }

   public boolean getPlayerInfoBoolean(String world, String playerName, String node, boolean defaultValue) {
      Boolean b = this.perms.getPermissionBoolean(world, playerName, node);
      return b == null ? defaultValue : b;
   }

   public String getPlayerInfoString(String world, String playerName, String node, String defaultValue) {
      String s = this.perms.getPermissionString(world, playerName, node);
      return s == null ? defaultValue : s;
   }

   public String getPlayerPrefix(String world, String playerName) {
      return this.getPlayerInfoString(world, playerName, "prefix", (String)null);
   }

   public String getPlayerSuffix(String world, String playerName) {
      return this.getPlayerInfoString(world, playerName, "suffix", (String)null);
   }

   public void setPlayerSuffix(String world, String player, String suffix) {
   }

   public void setPlayerPrefix(String world, String player, String prefix) {
   }

   public void setPlayerInfo(String world, String playerName, String node, Object value) {
   }

   public void setPlayerInfoInteger(String world, String playerName, String node, int value) {
      this.setPlayerInfo(world, playerName, node, value);
   }

   public void setPlayerInfoDouble(String world, String playerName, String node, double value) {
      this.setPlayerInfo(world, playerName, node, value);
   }

   public void setPlayerInfoBoolean(String world, String playerName, String node, boolean value) {
      this.setPlayerInfo(world, playerName, node, value);
   }

   public void setPlayerInfoString(String world, String playerName, String node, String value) {
      this.setPlayerInfo(world, playerName, node, value);
   }

   public int getGroupInfoInteger(String world, String groupName, String node, int defaultValue) {
      int i = this.perms.getGroupPermissionInteger(world, groupName, node);
      return i != -1 ? i : defaultValue;
   }

   public void setGroupInfo(String world, String groupName, String node, Object value) {
      this.perms.addGroupInfo(world, groupName, node, value);
   }

   public void setGroupInfoInteger(String world, String groupName, String node, int value) {
      this.setGroupInfo(world, groupName, node, value);
   }

   public double getGroupInfoDouble(String world, String groupName, String node, double defaultValue) {
      double d = this.perms.getGroupPermissionDouble(world, groupName, node);
      return d != (double)-1.0F ? d : defaultValue;
   }

   public void setGroupInfoDouble(String world, String groupName, String node, double value) {
      this.setGroupInfo(world, groupName, node, value);
   }

   public boolean getGroupInfoBoolean(String world, String groupName, String node, boolean defaultValue) {
      return this.perms.getGroupPermissionBoolean(world, groupName, node);
   }

   public void setGroupInfoBoolean(String world, String groupName, String node, boolean value) {
      this.setGroupInfo(world, groupName, node, value);
   }

   public String getGroupInfoString(String world, String groupName, String node, String defaultValue) {
      String s = this.perms.getGroupPermissionString(world, groupName, node);
      return s != null ? s : defaultValue;
   }

   public void setGroupInfoString(String world, String groupName, String node, String value) {
      this.setGroupInfo(world, groupName, node, value);
   }

   public String getGroupPrefix(String world, String group) {
      return this.perms.getGroupPrefix(world, group);
   }

   public void setGroupPrefix(String world, String group, String prefix) {
      this.perms.addGroupInfo(world, group, "prefix", prefix);
   }

   public String getGroupSuffix(String world, String group) {
      return this.perms.getGroupSuffix(world, group);
   }

   public void setGroupSuffix(String world, String group, String suffix) {
      this.perms.addGroupInfo(world, group, "suffix", suffix);
   }

   public class PermissionServerListener implements Listener {
      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (Chat_Permissions3.this.chat == null) {
            Plugin permChat = event.getPlugin();
            if ((permChat.getDescription().getName().equals("Permissions") || permChat.getDescription().getName().equals("vPerms")) && permChat.getDescription().getVersion().startsWith("3") && permChat.isEnabled()) {
               Chat_Permissions3.this.chat = (Permissions)permChat;
               Chat_Permissions3.this.perms = Chat_Permissions3.this.chat.getHandler();
               Chat_Permissions3.log.info(String.format("[%s][Chat] %s hooked.", Chat_Permissions3.this.plugin.getDescription().getName(), Chat_Permissions3.this.name));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (Chat_Permissions3.this.chat != null && (event.getPlugin().getDescription().getName().equals("Permissions") || event.getPlugin().getDescription().getName().equals("vPerms"))) {
            Chat_Permissions3.this.chat = null;
            Chat_Permissions3.this.perms = null;
            Chat_Permissions3.log.info(String.format("[%s][Chat] %s un-hooked.", Chat_Permissions3.this.plugin.getDescription().getName(), Chat_Permissions3.this.name));
         }

      }
   }
}
