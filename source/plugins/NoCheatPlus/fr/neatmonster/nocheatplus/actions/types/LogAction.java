package fr.neatmonster.nocheatplus.actions.types;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.actions.Action;
import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.config.ConfigFileWithActions;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import fr.neatmonster.nocheatplus.logging.StaticLogFile;
import fr.neatmonster.nocheatplus.utilities.ColorUtil;
import org.bukkit.ChatColor;

public class LogAction extends ActionWithParameters {
   private static final String PREFIX_CHAT;
   private static final String PREFIX_CONSOLE = "[NoCheatPlus] ";
   private static final String PREFIX_FILE = "";
   public final boolean toChat;
   public final boolean toConsole;
   public final boolean toFile;
   public final String prefixChat;
   public final String prefixConsole;
   public final String prefixFile;

   public LogAction(String name, int delay, int repeat, boolean toChat, boolean toConsole, boolean toFile, String message) {
      super(name, delay, repeat, message);
      this.toChat = toChat;
      this.toConsole = toConsole;
      this.toFile = toFile;
      this.prefixChat = PREFIX_CHAT;
      this.prefixConsole = "[NoCheatPlus] ";
      this.prefixFile = "";
   }

   private LogAction(String name, int delay, int repeat, String prefixChat, String prefixConsole, String prefixFile, String message) {
      super(name, delay, repeat, message);
      this.prefixChat = prefixChat;
      this.prefixConsole = prefixConsole;
      this.prefixFile = prefixFile;
      this.toChat = prefixChat != null;
      this.toConsole = prefixConsole != null;
      this.toFile = prefixFile != null;
   }

   public boolean execute(ViolationData violationData) {
      if (!violationData.player.hasPermission(violationData.getPermissionSilent())) {
         String message = super.getMessage(violationData);
         if (this.toChat) {
            NCPAPIProvider.getNoCheatPlusAPI().sendAdminNotifyMessage(ColorUtil.replaceColors(this.prefixChat + message));
         }

         if (this.toConsole) {
            LogUtil.logInfo(ColorUtil.removeColors(this.prefixConsole + message));
         }

         if (this.toFile) {
            StaticLogFile.fileLogger.info(ColorUtil.removeColors(this.prefixFile + message));
         }
      }

      return false;
   }

   public String toString() {
      return "log:" + this.name + ":" + this.delay + ":" + this.repeat + ":" + (this.toConsole ? "c" : "") + (this.toChat ? "i" : "") + (this.toFile ? "f" : "");
   }

   public Action getOptimizedCopy(ConfigFileWithActions config, Integer threshold) {
      if (!config.getBoolean("logging.active")) {
         return null;
      } else {
         String prefixChat = filterPrefix(config, "logging.backend.ingamechat.prefix", PREFIX_CHAT, this.toChat && config.getBoolean("logging.backend.ingamechat.active"));
         String prefixConsole = filterPrefix(config, "logging.backend.console.prefix", "[NoCheatPlus] ", this.toConsole && config.getBoolean("logging.backend.console.active"));
         String prefixFile = filterPrefix(config, "logging.backend.file.prefix", "", this.toFile && config.getBoolean("logging.backend.file.active"));
         return allNull(this.toChat, this.toConsole, this.toFile) ? null : new LogAction(this.name, this.delay, this.repeat, prefixChat, prefixConsole, prefixFile, this.message);
      }
   }

   private static boolean allNull(Object... objects) {
      for(int i = 0; i < objects.length; ++i) {
         if (objects[i] != null) {
            return false;
         }
      }

      return true;
   }

   private static final String filterPrefix(ConfigFileWithActions config, String path, String defaultValue, boolean use) {
      if (!use) {
         return null;
      } else {
         String prefix = config.getString(path);
         return prefix == null ? defaultValue : prefix;
      }
   }

   static {
      PREFIX_CHAT = ChatColor.RED + "NCP: " + ChatColor.WHITE;
   }
}
