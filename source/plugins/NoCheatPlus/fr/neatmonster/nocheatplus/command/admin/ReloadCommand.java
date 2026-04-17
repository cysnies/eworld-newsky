package fr.neatmonster.nocheatplus.command.admin;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.command.NoCheatPlusCommand;
import fr.neatmonster.nocheatplus.components.INotifyReload;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.logging.StaticLogFile;
import fr.neatmonster.nocheatplus.players.DataManager;
import java.io.File;
import java.util.Collection;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class ReloadCommand extends BaseCommand {
   private final Collection notifyReload;

   public ReloadCommand(JavaPlugin plugin, Collection notifyReload) {
      super(plugin, "reload", "nocheatplus.admin.reload");
      this.notifyReload = notifyReload;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length != 1) {
         return false;
      } else {
         this.handleReloadCommand(sender);
         return true;
      }
   }

   private void handleReloadCommand(CommandSender sender) {
      sender.sendMessage(TAG + "Reloading configuration...");
      ConfigManager.cleanup();
      ConfigManager.init((Plugin)this.access);
      StaticLogFile.cleanup();
      StaticLogFile.setupLogger(new File(((JavaPlugin)this.access).getDataFolder(), ConfigManager.getConfigFile().getString("logging.backend.file.filename")));
      DataManager.clearConfigs();

      for(CheckType checkType : new CheckType[]{CheckType.BLOCKBREAK, CheckType.FIGHT}) {
         DataManager.clearData(checkType);
      }

      for(INotifyReload component : this.notifyReload) {
         component.onReload();
      }

      Bukkit.getPluginManager().callEvent(new NoCheatPlusCommand.NCPReloadEvent());
      sender.sendMessage(TAG + "Configuration reloaded!");
      String info = "[NoCheatPlus] Configuration reloaded.";
      if (!(sender instanceof ConsoleCommandSender)) {
         Bukkit.getLogger().info("[NoCheatPlus] Configuration reloaded.");
      }

      ConfigFile config = ConfigManager.getConfigFile();
      if (config.getBoolean("logging.active") && config.getBoolean("logging.backend.file.active")) {
         StaticLogFile.fileLogger.info("[NoCheatPlus] Configuration reloaded.");
      }

   }
}
