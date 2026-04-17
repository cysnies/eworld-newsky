package stops;

import java.io.File;
import java.util.regex.Pattern;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Main extends JavaPlugin implements Listener {
   private Server server;
   private String pn;
   private PluginManager pm;
   private BukkitScheduler scheduler;
   private String mainPath;
   private String pluginPath;
   private String dataFolder;
   private String pluginVersion;
   private Stops stops;
   private String per_stops_admin;

   public void onEnable() {
      this.initBasic();
      this.initConfig();
      this.loadConfig0(UtilConfig.getConfig(this.pn));
      this.stops = new Stops(this);
      this.server.getPluginManager().registerEvents(this, this);
      this.sendConsoleMessage(UtilFormat.format(this.pn, "pluginEnabled", new Object[]{this.pn, this.pluginVersion}));
   }

   public void onDisable() {
      if (this.scheduler != null) {
         this.scheduler.cancelAllTasks();
      }

      this.sendConsoleMessage(UtilFormat.format(this.pn, "pluginDisabled", new Object[]{this.pn, this.pluginVersion}));
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      String cmdName = cmd.getName();
      int length = args.length;

      try {
         if (cmdName.equalsIgnoreCase("stops")) {
            if (p != null && !UtilPer.checkPer(p, this.per_stops_admin)) {
               return true;
            }

            if ((length != 1 || !args[0].equalsIgnoreCase("?")) && length == 1) {
               if (args[0].equalsIgnoreCase("reload")) {
                  this.reloadConfig(sender);
                  return true;
               }

               if (args[0].equalsIgnoreCase("cancel")) {
                  this.stops.stop(sender);
                  return true;
               }

               int time = Integer.parseInt(args[0]);
               this.stops.stops(sender, time);
               return true;
            }

            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpHeader", new Object[]{this.get(272)}));
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(15), this.get(20)}));
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(275), this.get(280)}));
         }
      } catch (NumberFormatException var9) {
         sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(190)}));
      }

      return true;
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig0(e.getConfig());
      }

   }

   private void loadConfig0(YamlConfiguration config) {
      this.per_stops_admin = config.getString("per_stops_admin");
   }

   private void sendConsoleMessage(String msg) {
      if (this.server.getConsoleSender() != null) {
         this.server.getConsoleSender().sendMessage(msg);
      } else {
         this.server.getLogger().info(msg);
      }

   }

   private void initBasic() {
      this.server = this.getServer();
      this.pn = this.getName();
      this.pm = this.server.getPluginManager();
      this.scheduler = this.server.getScheduler();
      this.mainPath = System.getProperty("user.dir");
      this.pluginPath = this.getFile().getParentFile().getAbsolutePath();
      this.dataFolder = this.pluginPath + File.separator + this.pn;
      this.pluginVersion = Util.getPluginVersion(this.getFile());
   }

   private void initConfig() {
      HashList<Pattern> filter = UtilConfig.getDefaultFilter();
      UtilConfig.register(new File(this.pluginPath + File.separator + this.pn + ".jar"), this.dataFolder, filter, this.pn);
      this.loadConfig((CommandSender)null);
   }

   private void reloadConfig(CommandSender sender) {
      if (this.loadConfig(sender)) {
         sender.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(25)}));
      } else {
         sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(30)}));
      }

   }

   private boolean loadConfig(CommandSender sender) {
      try {
         return UtilConfig.loadConfig(this.pn);
      } catch (InvalidConfigurationException e) {
         if (sender == null) {
            sender = this.server.getConsoleSender();
         }

         if (sender != null) {
            sender.sendMessage(e.getMessage());
         } else {
            this.server.getLogger().info(e.getMessage());
         }

         return false;
      }
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   public PluginManager getPm() {
      return this.pm;
   }

   public String getMainPath() {
      return this.mainPath;
   }

   public String getPluginPath() {
      return this.pluginPath;
   }

   public String getPn() {
      return this.pn;
   }
}
