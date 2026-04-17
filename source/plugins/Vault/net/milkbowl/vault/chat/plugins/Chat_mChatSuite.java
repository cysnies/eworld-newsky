package net.milkbowl.vault.chat.plugins;

import com.miraclem4n.mchat.api.Reader;
import com.miraclem4n.mchat.api.Writer;
import com.miraclem4n.mchat.types.InfoType;
import in.mDev.MiracleM4n.mChatSuite.mChatSuite;
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

public class Chat_mChatSuite extends Chat {
   private static final Logger log = Logger.getLogger("Minecraft");
   private final String name = "mChatSuite";
   private Plugin plugin = null;
   private mChatSuite mChat = null;

   public Chat_mChatSuite(Plugin plugin, Permission perms) {
      super(perms);
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new PermissionServerListener(), plugin);
      if (this.mChat == null) {
         Plugin chat = plugin.getServer().getPluginManager().getPlugin("mChatSuite");
         if (chat != null && chat.isEnabled()) {
            this.mChat = (mChatSuite)chat;
            log.info(String.format("[%s][Chat] %s hooked.", plugin.getDescription().getName(), "mChatSuite"));
         }
      }

   }

   public String getName() {
      return "mChatSuite";
   }

   public boolean isEnabled() {
      return this.mChat != null && this.mChat.isEnabled();
   }

   public String getPlayerPrefix(String world, String player) {
      return Reader.getPrefix(player, InfoType.USER, world);
   }

   public void setPlayerPrefix(String world, String player, String prefix) {
      this.setPlayerInfoValue(world, player, "prefix", prefix);
   }

   public String getPlayerSuffix(String world, String player) {
      return Reader.getSuffix(player, InfoType.USER, world);
   }

   public void setPlayerSuffix(String world, String player, String suffix) {
      this.setPlayerInfoValue(world, player, "suffix", suffix);
   }

   public String getGroupPrefix(String world, String group) {
      return Reader.getPrefix(group, InfoType.GROUP, world);
   }

   public void setGroupPrefix(String world, String group, String prefix) {
      this.setGroupInfoValue(world, group, "prefix", prefix);
   }

   public String getGroupSuffix(String world, String group) {
      return Reader.getSuffix(group, InfoType.GROUP, world);
   }

   public void setGroupSuffix(String world, String group, String suffix) {
      this.setGroupInfoValue(world, group, "suffix", suffix);
   }

   public int getPlayerInfoInteger(String world, String player, String node, int defaultValue) {
      String val = this.getPlayerInfoValue(world, player, node);
      if (val != null && !val.equals("")) {
         try {
            return Integer.parseInt(val);
         } catch (NumberFormatException var7) {
            return defaultValue;
         }
      } else {
         return defaultValue;
      }
   }

   public void setPlayerInfoInteger(String world, String player, String node, int value) {
      this.setPlayerInfoValue(world, player, node, value);
   }

   public int getGroupInfoInteger(String world, String group, String node, int defaultValue) {
      String val = this.getGroupInfoValue(world, group, node);
      if (val != null && !val.equals("")) {
         try {
            return Integer.parseInt(val);
         } catch (NumberFormatException var7) {
            return defaultValue;
         }
      } else {
         return defaultValue;
      }
   }

   public void setGroupInfoInteger(String world, String group, String node, int value) {
      this.setGroupInfoValue(world, group, node, value);
   }

   public double getPlayerInfoDouble(String world, String player, String node, double defaultValue) {
      String val = this.getPlayerInfoValue(world, player, node);
      if (val != null && !val.equals("")) {
         try {
            return Double.parseDouble(val);
         } catch (NumberFormatException var8) {
            return defaultValue;
         }
      } else {
         return defaultValue;
      }
   }

   public void setPlayerInfoDouble(String world, String player, String node, double value) {
      this.setPlayerInfoValue(world, player, node, value);
   }

   public double getGroupInfoDouble(String world, String group, String node, double defaultValue) {
      String val = this.getGroupInfoValue(world, group, node);
      if (val != null && !val.equals("")) {
         try {
            return Double.parseDouble(val);
         } catch (NumberFormatException var8) {
            return defaultValue;
         }
      } else {
         return defaultValue;
      }
   }

   public void setGroupInfoDouble(String world, String group, String node, double value) {
      this.setGroupInfoValue(world, group, node, value);
   }

   public boolean getPlayerInfoBoolean(String world, String player, String node, boolean defaultValue) {
      String val = this.getPlayerInfoValue(world, player, node);
      return val != null && !val.equals("") ? Boolean.parseBoolean(val) : defaultValue;
   }

   public void setPlayerInfoBoolean(String world, String player, String node, boolean value) {
      this.setPlayerInfoValue(world, player, node, value);
   }

   public boolean getGroupInfoBoolean(String world, String group, String node, boolean defaultValue) {
      String val = this.getGroupInfoValue(world, group, node);
      return val != null && !val.equals("") ? Boolean.valueOf(val) : defaultValue;
   }

   public void setGroupInfoBoolean(String world, String group, String node, boolean value) {
      this.setGroupInfoValue(world, group, node, value);
   }

   public String getPlayerInfoString(String world, String player, String node, String defaultValue) {
      String val = this.getPlayerInfoValue(world, player, node);
      return val == null ? defaultValue : val;
   }

   public void setPlayerInfoString(String world, String player, String node, String value) {
      this.setPlayerInfoValue(world, player, node, value);
   }

   public String getGroupInfoString(String world, String group, String node, String defaultValue) {
      String val = this.getGroupInfoValue(world, group, node);
      return val == null ? defaultValue : val;
   }

   public void setGroupInfoString(String world, String group, String node, String value) {
      this.setGroupInfoValue(world, group, node, value);
   }

   private void setPlayerInfoValue(String world, String player, String node, Object value) {
      if (world != null) {
         Writer.setWorldVar(player, InfoType.USER, world, node, value.toString());
      } else {
         Writer.setInfoVar(player, InfoType.USER, node, value.toString());
      }

   }

   private void setGroupInfoValue(String world, String group, String node, Object value) {
      if (world != null) {
         Writer.setWorldVar(group, InfoType.GROUP, world, node, value);
      } else {
         Writer.setInfoVar(group, InfoType.GROUP, node, value);
      }

   }

   private String getPlayerInfoValue(String world, String player, String node) {
      return Reader.getInfo(player, InfoType.USER, world, node);
   }

   private String getGroupInfoValue(String world, String group, String node) {
      return Reader.getInfo(group, InfoType.GROUP, world, node);
   }

   public class PermissionServerListener implements Listener {
      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (Chat_mChatSuite.this.mChat == null) {
            Plugin chat = Chat_mChatSuite.this.plugin.getServer().getPluginManager().getPlugin("mChatSuite");
            if (chat != null) {
               Chat_mChatSuite.this.mChat = (mChatSuite)chat;
               Chat_mChatSuite.log.info(String.format("[%s][Chat] %s hooked.", Chat_mChatSuite.this.plugin.getDescription().getName(), "mChatSuite"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (Chat_mChatSuite.this.mChat != null && event.getPlugin().getDescription().getName().equals("mChatSuite")) {
            Chat_mChatSuite.this.mChat = null;
            Chat_mChatSuite.log.info(String.format("[%s][Chat] %s un-hooked.", Chat_mChatSuite.this.plugin.getDescription().getName(), "mChatSuite"));
         }

      }
   }
}
