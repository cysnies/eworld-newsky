package net.milkbowl.vault.chat.plugins;

import de.bananaco.permissions.Permissions;
import de.bananaco.permissions.info.InfoReader;
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

public class Chat_bPermissions extends Chat {
   private static final Logger log = Logger.getLogger("Minecraft");
   private final String name = "bInfo";
   private Plugin plugin = null;
   InfoReader chat;

   public Chat_bPermissions(Plugin plugin, Permission perms) {
      super(perms);
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new PermissionServerListener(this), plugin);
      if (this.chat == null) {
         Plugin p = plugin.getServer().getPluginManager().getPlugin("bPermissions");
         if (p != null) {
            this.chat = Permissions.getInfoReader();
            log.info(String.format("[%s][Chat] %s hooked.", plugin.getDescription().getName(), "bPermissions"));
         }
      }

   }

   public String getName() {
      return "bInfo";
   }

   public boolean isEnabled() {
      return this.chat != null;
   }

   public String getPlayerPrefix(String world, String player) {
      return this.chat.getPrefix(player, world);
   }

   public void setPlayerPrefix(String world, String player, String prefix) {
      throw new UnsupportedOperationException("bPermissions does not support altering info nodes");
   }

   public String getPlayerSuffix(String world, String player) {
      return this.chat.getSuffix(player, world);
   }

   public void setPlayerSuffix(String world, String player, String suffix) {
      throw new UnsupportedOperationException("bPermissions does not support altering info nodes");
   }

   public String getGroupPrefix(String world, String group) {
      return this.chat.getGroupPrefix(group, world);
   }

   public void setGroupPrefix(String world, String group, String prefix) {
      throw new UnsupportedOperationException("bPermissions does not support altering info nodes");
   }

   public String getGroupSuffix(String world, String group) {
      return this.chat.getGroupSuffix(group, world);
   }

   public void setGroupSuffix(String world, String group, String suffix) {
      throw new UnsupportedOperationException("bPermissions does not support altering info nodes");
   }

   public int getPlayerInfoInteger(String world, String player, String node, int defaultValue) {
      String s = this.getPlayerInfoString(world, player, node, (String)null);
      if (s == null) {
         return defaultValue;
      } else {
         try {
            int i = Integer.valueOf(s);
            return i;
         } catch (NumberFormatException var7) {
            return defaultValue;
         }
      }
   }

   public void setPlayerInfoInteger(String world, String player, String node, int value) {
      throw new UnsupportedOperationException("bPermissions does not support altering info nodes");
   }

   public int getGroupInfoInteger(String world, String group, String node, int defaultValue) {
      String s = this.getGroupInfoString(world, group, node, (String)null);
      if (s == null) {
         return defaultValue;
      } else {
         try {
            int i = Integer.valueOf(s);
            return i;
         } catch (NumberFormatException var7) {
            return defaultValue;
         }
      }
   }

   public void setGroupInfoInteger(String world, String group, String node, int value) {
      throw new UnsupportedOperationException("bPermissions does not support altering info nodes");
   }

   public double getPlayerInfoDouble(String world, String player, String node, double defaultValue) {
      String s = this.getPlayerInfoString(world, player, node, (String)null);
      if (s == null) {
         return defaultValue;
      } else {
         try {
            double d = Double.valueOf(s);
            return d;
         } catch (NumberFormatException var9) {
            return defaultValue;
         }
      }
   }

   public void setPlayerInfoDouble(String world, String player, String node, double value) {
      throw new UnsupportedOperationException("bPermissions does not support altering info nodes");
   }

   public double getGroupInfoDouble(String world, String group, String node, double defaultValue) {
      String s = this.getGroupInfoString(world, group, node, (String)null);
      if (s == null) {
         return defaultValue;
      } else {
         try {
            double d = Double.valueOf(s);
            return d;
         } catch (NumberFormatException var9) {
            return defaultValue;
         }
      }
   }

   public void setGroupInfoDouble(String world, String group, String node, double value) {
      throw new UnsupportedOperationException("bPermissions does not support altering info nodes");
   }

   public boolean getPlayerInfoBoolean(String world, String player, String node, boolean defaultValue) {
      String s = this.getPlayerInfoString(world, player, node, (String)null);
      if (s == null) {
         return defaultValue;
      } else {
         Boolean val = Boolean.valueOf(s);
         return val != null ? val : defaultValue;
      }
   }

   public void setPlayerInfoBoolean(String world, String player, String node, boolean value) {
      throw new UnsupportedOperationException("bPermissions does not support altering info nodes");
   }

   public boolean getGroupInfoBoolean(String world, String group, String node, boolean defaultValue) {
      String s = this.getGroupInfoString(world, group, node, (String)null);
      if (s == null) {
         return defaultValue;
      } else {
         Boolean val = Boolean.valueOf(s);
         return val != null ? val : defaultValue;
      }
   }

   public void setGroupInfoBoolean(String world, String group, String node, boolean value) {
      throw new UnsupportedOperationException("bPermissions does not support altering info nodes");
   }

   public String getPlayerInfoString(String world, String player, String node, String defaultValue) {
      String val = this.chat.getValue(player, world, node);
      return val != null && val != "BLANKWORLD" ? val : defaultValue;
   }

   public void setPlayerInfoString(String world, String player, String node, String value) {
      throw new UnsupportedOperationException("bPermissions does not support altering info nodes");
   }

   public String getGroupInfoString(String world, String group, String node, String defaultValue) {
      String val = this.chat.getGroupValue(group, world, node);
      return val != null && val != "BLANKWORLD" ? val : defaultValue;
   }

   public void setGroupInfoString(String world, String group, String node, String value) {
      throw new UnsupportedOperationException("bPermissions does not support altering info nodes");
   }

   public class PermissionServerListener implements Listener {
      Chat_bPermissions chat = null;

      public PermissionServerListener(Chat_bPermissions chat) {
         this.chat = chat;
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (this.chat.chat == null) {
            Plugin chat = Chat_bPermissions.this.plugin.getServer().getPluginManager().getPlugin("bPermissions");
            if (chat != null) {
               this.chat.chat = Permissions.getInfoReader();
               Chat_bPermissions.log.info(String.format("[%s][Chat] %s hooked.", Chat_bPermissions.this.plugin.getDescription().getName(), "bPermissions"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (this.chat.chat != null && event.getPlugin().getDescription().getName().equals("bPermissions")) {
            this.chat.chat = null;
            Chat_bPermissions.log.info(String.format("[%s][Chat] %s un-hooked.", Chat_bPermissions.this.plugin.getDescription().getName(), "bPermissions"));
         }

      }
   }
}
