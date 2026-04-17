package net.milkbowl.vault.chat.plugins;

import java.util.logging.Logger;
import net.TheDgtl.iChat.iChat;
import net.TheDgtl.iChat.iChatAPI;
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

public class Chat_iChat extends Chat {
   private static final Logger log = Logger.getLogger("Minecraft");
   private final String name = "iChat";
   private Plugin plugin = null;
   private iChatAPI iChat = null;

   public Chat_iChat(Plugin plugin, Permission perms) {
      super(perms);
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new PermissionServerListener(this), plugin);
      if (this.iChat == null) {
         Plugin chat = plugin.getServer().getPluginManager().getPlugin("iChat");
         if (chat != null) {
            this.iChat = ((iChat)chat).API;
            log.info(String.format("[%s][Chat] %s hooked.", plugin.getDescription().getName(), "iChat"));
         }
      }

   }

   public String getName() {
      return "iChat";
   }

   public boolean isEnabled() {
      return this.iChat != null;
   }

   public String getPlayerPrefix(String world, String player) {
      Player p = this.plugin.getServer().getPlayer(player);
      if (p == null) {
         throw new UnsupportedOperationException("iChat does not support offline player info nodes!");
      } else {
         return !p.getWorld().getName().equals(world) ? null : this.iChat.getPrefix(p);
      }
   }

   public void setPlayerPrefix(String world, String player, String prefix) {
      throw new UnsupportedOperationException("iChat does not support mutable info nodes!");
   }

   public String getPlayerSuffix(String world, String player) {
      Player p = this.plugin.getServer().getPlayer(player);
      if (p == null) {
         throw new UnsupportedOperationException("iChat does not support offline player info nodes!");
      } else {
         return !p.getWorld().getName().equals(world) ? null : this.iChat.getSuffix(p);
      }
   }

   public void setPlayerSuffix(String world, String player, String suffix) {
      throw new UnsupportedOperationException("iChat does not support mutable info nodes!");
   }

   public String getGroupPrefix(String world, String group) {
      throw new UnsupportedOperationException("iChat does not support group info nodes!");
   }

   public void setGroupPrefix(String world, String group, String prefix) {
      throw new UnsupportedOperationException("iChat does not support mutable info nodes!");
   }

   public String getGroupSuffix(String world, String group) {
      throw new UnsupportedOperationException("iChat does not support group info nodes!");
   }

   public void setGroupSuffix(String world, String group, String suffix) {
      throw new UnsupportedOperationException("iChat does not support mutable info nodes!");
   }

   public int getPlayerInfoInteger(String world, String player, String node, int defaultValue) {
      String val = this.getPlayerInfoString(world, player, node, (String)null);
      if (val == null) {
         return defaultValue;
      } else {
         Integer i = defaultValue;

         try {
            i = Integer.valueOf(val);
            return i;
         } catch (NumberFormatException var8) {
            return defaultValue;
         }
      }
   }

   public void setPlayerInfoInteger(String world, String player, String node, int value) {
      throw new UnsupportedOperationException("iChat does not support mutable info nodes!");
   }

   public int getGroupInfoInteger(String world, String group, String node, int defaultValue) {
      throw new UnsupportedOperationException("iChat does not support group info nodes!");
   }

   public void setGroupInfoInteger(String world, String group, String node, int value) {
      throw new UnsupportedOperationException("iChat does not support mutable info nodes!");
   }

   public double getPlayerInfoDouble(String world, String player, String node, double defaultValue) {
      String val = this.getPlayerInfoString(world, player, node, (String)null);
      if (val == null) {
         return defaultValue;
      } else {
         try {
            double d = Double.valueOf(val);
            return d;
         } catch (NumberFormatException var10) {
            return defaultValue;
         }
      }
   }

   public void setPlayerInfoDouble(String world, String player, String node, double value) {
      throw new UnsupportedOperationException("iChat does not support mutable info nodes!");
   }

   public double getGroupInfoDouble(String world, String group, String node, double defaultValue) {
      throw new UnsupportedOperationException("iChat does not support group info nodes!");
   }

   public void setGroupInfoDouble(String world, String group, String node, double value) {
      throw new UnsupportedOperationException("iChat does not support mutable info nodes!");
   }

   public boolean getPlayerInfoBoolean(String world, String player, String node, boolean defaultValue) {
      String val = this.getPlayerInfoString(world, player, node, (String)null);
      if (val == null) {
         return defaultValue;
      } else {
         Boolean v = Boolean.valueOf(val);
         return v != null ? v : defaultValue;
      }
   }

   public void setPlayerInfoBoolean(String world, String player, String node, boolean value) {
      throw new UnsupportedOperationException("iChat does not support mutable info nodes!");
   }

   public boolean getGroupInfoBoolean(String world, String group, String node, boolean defaultValue) {
      throw new UnsupportedOperationException("iChat does not support group info nodes!");
   }

   public void setGroupInfoBoolean(String world, String group, String node, boolean value) {
      throw new UnsupportedOperationException("iChat does not support mutable info nodes!");
   }

   public String getPlayerInfoString(String world, String player, String node, String defaultValue) {
      Player p = this.plugin.getServer().getPlayer(player);
      if (p == null) {
         throw new UnsupportedOperationException("iChat does not support offline player info nodes!");
      } else if (!p.getWorld().getName().equals(world)) {
         return null;
      } else {
         String val = this.iChat.getInfo(p, node);
         return val != null ? val : defaultValue;
      }
   }

   public void setPlayerInfoString(String world, String player, String node, String value) {
      throw new UnsupportedOperationException("iChat does not support mutable info nodes!");
   }

   public String getGroupInfoString(String world, String group, String node, String defaultValue) {
      throw new UnsupportedOperationException("iChat does not support group info nodes!");
   }

   public void setGroupInfoString(String world, String group, String node, String value) {
      throw new UnsupportedOperationException("iChat does not support mutable info nodes!");
   }

   public class PermissionServerListener implements Listener {
      Chat_iChat chat = null;

      public PermissionServerListener(Chat_iChat chat) {
         this.chat = chat;
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (this.chat.iChat == null) {
            Plugin chat = Chat_iChat.this.plugin.getServer().getPluginManager().getPlugin("iChat");
            if (chat != null) {
               this.chat.iChat = ((iChat)chat).API;
               Chat_iChat.log.info(String.format("[%s][Chat] %s hooked.", Chat_iChat.this.plugin.getDescription().getName(), "iChat"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (this.chat.iChat != null && event.getPlugin().getDescription().getName().equals("iChat")) {
            this.chat.iChat = null;
            Chat_iChat.log.info(String.format("[%s][Chat] %s un-hooked.", Chat_iChat.this.plugin.getDescription().getName(), "iChat"));
         }

      }
   }
}
