package net.milkbowl.vault.chat.plugins;

import java.util.logging.Logger;
import net.D3GN.MiracleM4n.mChat.mChat;
import net.D3GN.MiracleM4n.mChat.mChatAPI;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

public class Chat_mChat extends Chat {
   private static final Logger log = Logger.getLogger("Minecraft");
   private final String name = "mChat";
   private Plugin plugin = null;
   private mChatAPI mChat = null;

   public Chat_mChat(Plugin plugin, Permission perms) {
      super(perms);
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new PermissionServerListener(this), plugin);
      if (this.mChat == null) {
         Plugin chat = plugin.getServer().getPluginManager().getPlugin("mChat");
         if (chat != null) {
            this.mChat = net.D3GN.MiracleM4n.mChat.mChat.API;
            log.info(String.format("[%s][Chat] %s hooked.", plugin.getDescription().getName(), "mChat"));
         }
      }

   }

   public String getName() {
      return "mChat";
   }

   public boolean isEnabled() {
      return this.mChat != null;
   }

   public String getPlayerPrefix(String world, String player) {
      Player p = Bukkit.getServer().getPlayer(player);
      if (p == null) {
         throw new UnsupportedOperationException("mChat does not support offline player prefixes");
      } else {
         return this.mChat.getPrefix(p);
      }
   }

   public void setPlayerPrefix(String world, String player, String prefix) {
      throw new UnsupportedOperationException("mChat does not support setting info nodes");
   }

   public String getPlayerSuffix(String world, String player) {
      Player p = Bukkit.getServer().getPlayer(player);
      if (p == null) {
         throw new UnsupportedOperationException("mChat does not support offline player prefixes");
      } else {
         return this.mChat.getSuffix(p);
      }
   }

   public void setPlayerSuffix(String world, String player, String suffix) {
      throw new UnsupportedOperationException("mChat does not support setting info nodes");
   }

   public String getGroupPrefix(String world, String group) {
      throw new UnsupportedOperationException("mChat does not support group info nodes");
   }

   public void setGroupPrefix(String world, String group, String prefix) {
      throw new UnsupportedOperationException("mChat does not support group info nodes");
   }

   public String getGroupSuffix(String world, String group) {
      throw new UnsupportedOperationException("mChat does not support group info nodes");
   }

   public void setGroupSuffix(String world, String group, String suffix) {
      throw new UnsupportedOperationException("mChat does not support group info nodes");
   }

   public int getPlayerInfoInteger(String world, String player, String node, int defaultValue) {
      String s = this.getPlayerInfoString(world, player, node, (String)null);
      if (s == null) {
         return defaultValue;
      } else {
         try {
            return Integer.valueOf(s);
         } catch (NumberFormatException var7) {
            return defaultValue;
         }
      }
   }

   public void setPlayerInfoInteger(String world, String player, String node, int value) {
      throw new UnsupportedOperationException("mChat does not support setting info nodes");
   }

   public int getGroupInfoInteger(String world, String group, String node, int defaultValue) {
      throw new UnsupportedOperationException("mChat does not support group info nodes");
   }

   public void setGroupInfoInteger(String world, String group, String node, int value) {
      throw new UnsupportedOperationException("mChat does not support group info nodes");
   }

   public double getPlayerInfoDouble(String world, String player, String node, double defaultValue) {
      String s = this.getPlayerInfoString(world, player, node, (String)null);
      if (s == null) {
         return defaultValue;
      } else {
         try {
            return Double.valueOf(s);
         } catch (NumberFormatException var8) {
            return defaultValue;
         }
      }
   }

   public void setPlayerInfoDouble(String world, String player, String node, double value) {
      throw new UnsupportedOperationException("mChat does not support setting info nodes");
   }

   public double getGroupInfoDouble(String world, String group, String node, double defaultValue) {
      throw new UnsupportedOperationException("mChat does not support group info nodes");
   }

   public void setGroupInfoDouble(String world, String group, String node, double value) {
      throw new UnsupportedOperationException("mChat does not support group info nodes");
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
      throw new UnsupportedOperationException("mChat does not support setting info nodes");
   }

   public boolean getGroupInfoBoolean(String world, String group, String node, boolean defaultValue) {
      throw new UnsupportedOperationException("mChat does not support group info nodes");
   }

   public void setGroupInfoBoolean(String world, String group, String node, boolean value) {
      throw new UnsupportedOperationException("mChat does not support group info nodes");
   }

   public String getPlayerInfoString(String world, String player, String node, String defaultValue) {
      Player p = Bukkit.getServer().getPlayer(player);
      if (p == null) {
         throw new UnsupportedOperationException("mChat does not support offline player prefixes");
      } else {
         String s = this.mChat.getInfo(p, node);
         return s == null ? defaultValue : s;
      }
   }

   public void setPlayerInfoString(String world, String player, String node, String value) {
      throw new UnsupportedOperationException("mChat does not support setting info nodes");
   }

   public String getGroupInfoString(String world, String group, String node, String defaultValue) {
      throw new UnsupportedOperationException("mChat does not support group info nodes");
   }

   public void setGroupInfoString(String world, String group, String node, String value) {
      throw new UnsupportedOperationException("mChat does not support group info nodes");
   }

   public class PermissionServerListener implements Listener {
      Chat_mChat chat = null;

      public PermissionServerListener(Chat_mChat chat) {
         this.chat = chat;
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (this.chat.mChat == null) {
            Plugin chat = Chat_mChat.this.plugin.getServer().getPluginManager().getPlugin("mChat");
            if (chat != null) {
               this.chat.mChat = net.D3GN.MiracleM4n.mChat.mChat.API;
               Chat_mChat.log.info(String.format("[%s][Chat] %s hooked.", Chat_mChat.this.plugin.getDescription().getName(), "mChat"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (this.chat.mChat != null && event.getPlugin().getDescription().getName().equals("mChat")) {
            this.chat.mChat = null;
            Chat_mChat.log.info(String.format("[%s][Chat] %s un-hooked.", Chat_mChat.this.plugin.getDescription().getName(), "mChat"));
         }

      }
   }
}
