package landMain;

import java.io.File;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilPer;
import lib.util.UtilTypes;
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

public class LandMain extends JavaPlugin implements Listener {
   private Server server;
   private String pn;
   private PluginManager pm;
   private BukkitScheduler scheduler;
   private String mainPath;
   private String pluginPath;
   private String dataFolder;
   private String pluginVersion;
   private Dao dao;
   private String per_land_admin;
   private static LandManager landManager;
   private ShowManager showManager;

   public void onEnable() {
      this.initBasic();
      this.initConfig();
      this.initDatabase();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager = new LandManager(this);
      this.showManager = new ShowManager(this);
      this.pm.registerEvents(this, this);
      this.sendConsoleMessage(UtilFormat.format((String)null, "pluginEnabled", new Object[]{this.pn, this.pluginVersion}));
   }

   public void onDisable() {
      if (this.scheduler != null) {
         this.scheduler.cancelAllTasks();
      }

      if (this.dao != null) {
         this.dao.close();
      }

      this.sendConsoleMessage(UtilFormat.format((String)null, "pluginDisabled", new Object[]{this.pn, this.pluginVersion}));
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      String cmdName = cmd.getName();
      int length = args.length;
      if (!cmdName.equalsIgnoreCase("land") && !cmdName.equalsIgnoreCase("l")) {
         if (cmdName.equalsIgnoreCase("go")) {
            if (p == null) {
               sender.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(75)}));
               return true;
            }

            if ((length != 1 || !args[0].equalsIgnoreCase("?")) && length == 1) {
               landManager.getTpHandler().tp(p, args[0]);
               return true;
            }

            sender.sendMessage(UtilFormat.format((String)null, "cmdHelpHeader", new Object[]{this.get(1465)}));
            sender.sendMessage(UtilFormat.format((String)null, "cmdHelpItem", new Object[]{this.get(1470), this.get(1475)}));
         }
      } else {
         if (length != 1 || !args[0].equalsIgnoreCase("?")) {
            if (length == 0) {
               if (p == null) {
                  sender.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(75)}));
               } else {
                  this.showManager.showMainMenu(p);
               }

               return true;
            }

            if (length == 1) {
               if (args[0].equalsIgnoreCase("reload")) {
                  this.reloadConfig(sender);
                  return true;
               }

               if (p == null) {
                  sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(75)}));
               } else {
                  this.showManager.showLandInfo(p, args[0]);
               }

               return true;
            }

            if (length == 3) {
               if (args[0].equalsIgnoreCase("cmd") && args[1].equalsIgnoreCase("info")) {
                  landManager.getCmdHandler().show(sender, args[2]);
                  return true;
               }

               if (args[0].equalsIgnoreCase("cmd") && args[1].equalsIgnoreCase("remove")) {
                  landManager.getCmdHandler().remove(sender, args[2]);
                  return true;
               }
            } else if (length >= 4 && args[0].equalsIgnoreCase("cmd") && args[1].equalsIgnoreCase("set")) {
               String info = Util.combine(args, " ", 3, length);
               landManager.getCmdHandler().set(sender, args[2], info);
               return true;
            }
         }

         sender.sendMessage(UtilFormat.format((String)null, "cmdHelpHeader", new Object[]{this.get(10)}));
         if (p == null || UtilPer.hasPer(p, this.per_land_admin)) {
            sender.sendMessage(UtilFormat.format((String)null, "cmdHelpItem", new Object[]{this.get(15), this.get(20)}));
            sender.sendMessage(UtilFormat.format((String)null, "cmdHelpItem", new Object[]{this.get(1400), this.get(1405)}));
            sender.sendMessage(UtilFormat.format((String)null, "cmdHelpItem", new Object[]{this.get(1410), this.get(1415)}));
            sender.sendMessage(UtilFormat.format((String)null, "cmdHelpItem", new Object[]{this.get(1420), this.get(1425)}));
         }
      }

      return true;
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void reloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   public String getPn() {
      return this.pn;
   }

   public String getMainPath() {
      return this.mainPath;
   }

   public String getPluginPath() {
      return this.pluginPath;
   }

   public String getPluginVersion() {
      return this.pluginVersion;
   }

   public Dao getDao() {
      return this.dao;
   }

   public PluginManager getPm() {
      return this.pm;
   }

   public static LandManager getLandManager() {
      return landManager;
   }

   public ShowManager getShowManager() {
      return this.showManager;
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

   private void initDatabase() {
      this.dao = new Dao(this);
   }

   private void initConfig() {
      UtilConfig.register(new File(this.pluginPath + File.separator + this.pn + ".jar"), this.dataFolder, (HashList)null, this.pn);

      try {
         if (!UtilConfig.loadConfig(this.pn)) {
            this.pm.disablePlugin(this);
         }
      } catch (InvalidConfigurationException e) {
         e.printStackTrace();
      }

   }

   private void reloadConfig(CommandSender sender) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null || UtilPer.checkPer(p, this.per_land_admin)) {
         if (this.loadConfig(sender)) {
            sender.sendMessage(UtilFormat.format((String)null, "success", new Object[]{this.get(25)}));
         }

      }
   }

   private boolean loadConfig(CommandSender sender) {
      try {
         return UtilConfig.loadConfig(this.pn);
      } catch (InvalidConfigurationException e) {
         Util.sendConsoleMessage(e.getMessage());
         return false;
      }
   }

   private void loadConfig(YamlConfiguration config) {
      this.per_land_admin = config.getString("per_land_admin");
      UtilItems.reloadItems(this.pn, config);
      UtilTypes.reloadTypes(this.pn, config);
   }

   private void sendConsoleMessage(String msg) {
      if (this.server.getConsoleSender() != null) {
         this.server.getConsoleSender().sendMessage(msg);
      } else {
         this.server.getLogger().info(msg);
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
