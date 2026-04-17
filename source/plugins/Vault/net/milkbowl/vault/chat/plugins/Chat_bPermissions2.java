package net.milkbowl.vault.chat.plugins;

import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.CalculableType;
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

public class Chat_bPermissions2 extends Chat {
   private static final Logger log = Logger.getLogger("Minecraft");
   private final String name = "bInfo";
   private Plugin plugin = null;
   private boolean hooked = false;

   public Chat_bPermissions2(Plugin plugin, Permission perms) {
      super(perms);
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new PermissionServerListener(this), plugin);
      if (!this.hooked) {
         Plugin p = plugin.getServer().getPluginManager().getPlugin("bPermissions");
         if (p != null) {
            this.hooked = true;
            log.info(String.format("[%s][Chat] %s hooked.", plugin.getDescription().getName(), "bPermissions2"));
         }
      }

   }

   public String getName() {
      return "bInfo";
   }

   public boolean isEnabled() {
      return this.hooked;
   }

   public String getPlayerPrefix(String world, String player) {
      return ApiLayer.getValue(world, CalculableType.USER, player, "prefix");
   }

   public void setPlayerPrefix(String world, String player, String prefix) {
      ApiLayer.setValue(world, CalculableType.USER, player, "prefix", prefix);
   }

   public String getPlayerSuffix(String world, String player) {
      return ApiLayer.getValue(world, CalculableType.USER, player, "suffix");
   }

   public void setPlayerSuffix(String world, String player, String suffix) {
      ApiLayer.setValue(world, CalculableType.USER, player, "suffix", suffix);
   }

   public String getGroupPrefix(String world, String group) {
      return ApiLayer.getValue(world, CalculableType.GROUP, group, "prefix");
   }

   public void setGroupPrefix(String world, String group, String prefix) {
      ApiLayer.setValue(world, CalculableType.GROUP, group, "prefix", prefix);
   }

   public String getGroupSuffix(String world, String group) {
      return ApiLayer.getValue(world, CalculableType.GROUP, group, "suffix");
   }

   public void setGroupSuffix(String world, String group, String suffix) {
      ApiLayer.setValue(world, CalculableType.GROUP, group, "suffix", suffix);
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
      ApiLayer.setValue(world, CalculableType.USER, player, node, String.valueOf(value));
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
      ApiLayer.setValue(world, CalculableType.GROUP, group, node, String.valueOf(value));
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
      ApiLayer.setValue(world, CalculableType.USER, player, node, String.valueOf(value));
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
      ApiLayer.setValue(world, CalculableType.GROUP, group, node, String.valueOf(value));
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
      ApiLayer.setValue(world, CalculableType.USER, player, node, String.valueOf(value));
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
      ApiLayer.setValue(world, CalculableType.GROUP, group, node, String.valueOf(value));
   }

   public String getPlayerInfoString(String world, String player, String node, String defaultValue) {
      String val = ApiLayer.getValue(world, CalculableType.USER, player, node);
      return val != null && val != "BLANKWORLD" && val != "" ? val : defaultValue;
   }

   public void setPlayerInfoString(String world, String player, String node, String value) {
      ApiLayer.setValue(world, CalculableType.USER, player, node, value);
   }

   public String getGroupInfoString(String world, String group, String node, String defaultValue) {
      String val = ApiLayer.getValue(world, CalculableType.GROUP, group, node);
      return val != null && val != "BLANKWORLD" && val != "" ? val : defaultValue;
   }

   public void setGroupInfoString(String world, String group, String node, String value) {
      ApiLayer.setValue(world, CalculableType.GROUP, group, node, value);
   }

   public class PermissionServerListener implements Listener {
      Chat_bPermissions2 chat = null;

      public PermissionServerListener(Chat_bPermissions2 chat) {
         this.chat = chat;
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (!Chat_bPermissions2.this.hooked) {
            Plugin chat = Chat_bPermissions2.this.plugin.getServer().getPluginManager().getPlugin("bPermissions");
            if (chat != null) {
               Chat_bPermissions2.this.hooked = true;
               Chat_bPermissions2.log.info(String.format("[%s][Chat] %s hooked.", Chat_bPermissions2.this.plugin.getDescription().getName(), "bPermissions2"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (Chat_bPermissions2.this.hooked && event.getPlugin().getDescription().getName().equals("bPermissions")) {
            Chat_bPermissions2.this.hooked = false;
            Chat_bPermissions2.log.info(String.format("[%s][Chat] %s un-hooked.", Chat_bPermissions2.this.plugin.getDescription().getName(), "bPermissions2"));
         }

      }
   }
}
