package join;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Join extends JavaPlugin implements Listener {
   private String pn;
   private Server server;
   private BukkitScheduler scheduler;
   private String dataFolder;
   private String pluginVersion;
   private String pluginPath;
   private String per_join_admin;
   private HashMap nameHash;

   public void onEnable() {
      Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
      this.initBasic();
      this.initConfig();
      this.loadConfig0(UtilConfig.getConfig(this.pn));
      this.server.getPluginManager().registerEvents(this, this);
      Util.sendConsoleMessage(UtilFormat.format(this.pn, "pluginEnabled", new Object[]{this.pn, this.pluginVersion}));
   }

   public void onDisable() {
      this.scheduler.cancelAllTasks();
      Util.sendConsoleMessage(UtilFormat.format(this.pn, "pluginDisabled", new Object[]{this.pn, this.pluginVersion}));
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      String cmdName = cmd.getName();
      int length = args.length;
      if (cmdName.equalsIgnoreCase("join")) {
         if (length != 1 || !args[0].equalsIgnoreCase("?")) {
            if (length == 0) {
               sender.sendMessage(this.get(50));
               return true;
            }

            if (length == 1) {
               if (args[0].equalsIgnoreCase("reload")) {
                  this.reloadConfig(sender);
                  return true;
               }

               if (p == null) {
                  sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(40)}));
                  return true;
               }

               String tar = args[0].toLowerCase();
               if (!UtilPer.checkPer(p, "per.join.server." + tar)) {
                  return true;
               }

               String name = tar;
               if (this.nameHash.containsKey(tar)) {
                  name = (String)this.nameHash.get(tar);
               }

               p.sendMessage(UtilFormat.format(this.pn, "tp", new Object[]{name}));
               ByteArrayOutputStream b = new ByteArrayOutputStream();
               DataOutputStream out = new DataOutputStream(b);

               try {
                  out.writeUTF("Connect");
                  out.writeUTF(tar);
               } catch (IOException var13) {
               }

               p.sendPluginMessage(this, "BungeeCord", b.toByteArray());
               return true;
            }
         }

         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpHeader", new Object[]{this.get(10)}));
         if (p == null || UtilPer.hasPer(p, this.per_join_admin)) {
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(15), this.get(20)}));
         }

         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(35), this.get(36)}));
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

   private void reloadConfig(CommandSender sender) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null || UtilPer.checkPer(p, this.per_join_admin)) {
         this.loadConfig(sender);
      }
   }

   private void loadConfig(CommandSender sender) {
      try {
         if (UtilConfig.loadConfig(this.pn)) {
            if (sender != null) {
               sender.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(25)}));
            }
         } else if (sender != null) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(30)}));
         }
      } catch (InvalidConfigurationException e) {
         if (sender == null) {
            Util.sendConsoleMessage(e.getMessage());
         } else {
            sender.sendMessage(e.getMessage());
         }
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   private void loadConfig0(FileConfiguration config) {
      this.per_join_admin = config.getString("per_join_admin");
      this.nameHash = new HashMap();

      for(String s : config.getStringList("names")) {
         String id = s.split(" ")[0];
         String name = s.split(" ")[1];
         this.nameHash.put(id, name);
      }

   }

   private void initBasic() {
      this.server = this.getServer();
      this.pn = this.getName();
      this.scheduler = this.server.getScheduler();
      this.pluginPath = this.getFile().getParentFile().getAbsolutePath();
      this.dataFolder = this.pluginPath + File.separator + this.pn;
      this.pluginVersion = Util.getPluginVersion(this.getFile());
   }

   private void initConfig() {
      HashList<Pattern> filter = UtilConfig.getDefaultFilter();
      UtilConfig.register(new File(this.pluginPath + File.separator + this.pn + ".jar"), this.dataFolder, filter, this.pn);
      this.loadConfig((CommandSender)null);
   }
}
