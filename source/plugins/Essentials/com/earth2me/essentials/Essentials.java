package com.earth2me.essentials;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.IJails;
import com.earth2me.essentials.commands.IEssentialsCommand;
import com.earth2me.essentials.commands.NoChargeException;
import com.earth2me.essentials.commands.NotEnoughArgumentsException;
import com.earth2me.essentials.metrics.Metrics;
import com.earth2me.essentials.metrics.MetricsListener;
import com.earth2me.essentials.metrics.MetricsStarter;
import com.earth2me.essentials.perm.PermissionsHandler;
import com.earth2me.essentials.register.payment.Methods;
import com.earth2me.essentials.signs.SignBlockListener;
import com.earth2me.essentials.signs.SignEntityListener;
import com.earth2me.essentials.signs.SignPlayerListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.VanillaCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.yaml.snakeyaml.error.YAMLException;

public class Essentials extends JavaPlugin implements IEssentials {
   public static final int BUKKIT_VERSION = 2648;
   private static final Logger LOGGER = Logger.getLogger("Minecraft");
   private transient ISettings settings;
   private final transient TNTExplodeListener tntListener = new TNTExplodeListener(this);
   private transient Jails jails;
   private transient Warps warps;
   private transient Worth worth;
   private transient List confList;
   private transient Backup backup;
   private transient ItemDb itemDb;
   private final transient Methods paymentMethod = new Methods();
   private transient PermissionsHandler permissionsHandler;
   private transient AlternativeCommandsHandler alternativeCommandsHandler;
   private transient UserMap userMap;
   private transient ExecuteTimer execTimer;
   private transient I18n i18n;
   private transient Metrics metrics;
   private transient EssentialsTimer timer;
   private transient List vanishedPlayers = new ArrayList();
   private transient SimpleCommandMap scm;

   public ISettings getSettings() {
      return this.settings;
   }

   public void setupForTesting(Server server) throws IOException, InvalidDescriptionException {
      File dataFolder = File.createTempFile("essentialstest", "");
      if (!dataFolder.delete()) {
         throw new IOException();
      } else if (!dataFolder.mkdir()) {
         throw new IOException();
      } else {
         this.i18n = new I18n(this);
         this.i18n.onEnable();
         LOGGER.log(Level.INFO, I18n._("usingTempFolderForTesting"));
         LOGGER.log(Level.INFO, dataFolder.toString());
         this.initialize((PluginLoader)null, server, new PluginDescriptionFile(new FileReader(new File("src" + File.separator + "plugin.yml"))), dataFolder, (File)null, (ClassLoader)null);
         this.settings = new Settings(this);
         this.i18n.updateLocale("en");
         this.userMap = new UserMap(this);
         this.permissionsHandler = new PermissionsHandler(this, false);
         Economy.setEss(this);
      }
   }

   public void onEnable() {
      this.execTimer = new ExecuteTimer();
      this.execTimer.start();
      this.i18n = new I18n(this);
      this.i18n.onEnable();
      this.execTimer.mark("I18n1");
      this.scm = new SimpleCommandMap(this.getServer());
      PluginManager pm = this.getServer().getPluginManager();

      for(Plugin plugin : pm.getPlugins()) {
         if (plugin.getDescription().getName().startsWith("Essentials") && !plugin.getDescription().getVersion().equals(this.getDescription().getVersion()) && !plugin.getDescription().getName().equals("EssentialsAntiCheat")) {
            LOGGER.log(Level.WARNING, I18n._("versionMismatch", plugin.getDescription().getName()));
         }
      }

      Matcher versionMatch = Pattern.compile("git-Bukkit-(?:(?:[0-9]+)\\.)+[0-9]+-R[\\.0-9]+-(?:[0-9]+-g[0-9a-f]+-)?b([0-9]+)jnks.*").matcher(this.getServer().getVersion());
      if (versionMatch.matches()) {
         int versionNumber = Integer.parseInt(versionMatch.group(1));
         if (versionNumber < 2648 && versionNumber > 100) {
            LOGGER.log(Level.SEVERE, " * ! * ! * ! * ! * ! * ! * ! * ! * ! * ! * ! * ! *");
            LOGGER.log(Level.SEVERE, I18n._("notRecommendedBukkit"));
            LOGGER.log(Level.SEVERE, I18n._("requiredBukkit", Integer.toString(2648)));
            LOGGER.log(Level.SEVERE, " * ! * ! * ! * ! * ! * ! * ! * ! * ! * ! * ! * ! *");
            this.setEnabled(false);
            return;
         }
      } else {
         LOGGER.log(Level.INFO, I18n._("bukkitFormatChanged"));
         LOGGER.log(Level.INFO, this.getServer().getVersion());
         LOGGER.log(Level.INFO, this.getServer().getBukkitVersion());
      }

      this.execTimer.mark("BukkitCheck");

      try {
         EssentialsUpgrade upgrade = new EssentialsUpgrade(this);
         upgrade.beforeSettings();
         this.execTimer.mark("Upgrade");
         this.confList = new ArrayList();
         this.settings = new Settings(this);
         this.confList.add(this.settings);
         this.execTimer.mark("Settings");
         upgrade.afterSettings();
         this.execTimer.mark("Upgrade2");
         this.i18n.updateLocale(this.settings.getLocale());
         this.userMap = new UserMap(this);
         this.confList.add(this.userMap);
         this.execTimer.mark("Init(Usermap)");
         this.warps = new Warps(this.getServer(), this.getDataFolder());
         this.confList.add(this.warps);
         this.execTimer.mark("Init(Spawn/Warp)");
         this.worth = new Worth(this.getDataFolder());
         this.confList.add(this.worth);
         this.itemDb = new ItemDb(this);
         this.confList.add(this.itemDb);
         this.execTimer.mark("Init(Worth/ItemDB)");
         this.jails = new Jails(this);
         this.confList.add(this.jails);
         this.reload();
      } catch (YAMLException exception) {
         if (pm.getPlugin("EssentialsUpdate") != null) {
            LOGGER.log(Level.SEVERE, I18n._("essentialsHelp2"));
         } else {
            LOGGER.log(Level.SEVERE, I18n._("essentialsHelp1"));
         }

         LOGGER.log(Level.SEVERE, exception.toString());
         pm.registerEvents(new Listener() {
            @EventHandler(
               priority = EventPriority.LOW
            )
            public void onPlayerJoin(PlayerJoinEvent event) {
               event.getPlayer().sendMessage("Essentials failed to load, read the log file.");
            }
         }, this);

         for(Player player : this.getServer().getOnlinePlayers()) {
            player.sendMessage("Essentials failed to load, read the log file.");
         }

         this.setEnabled(false);
         return;
      }

      this.backup = new Backup(this);
      this.permissionsHandler = new PermissionsHandler(this, this.settings.useBukkitPermissions());
      this.alternativeCommandsHandler = new AlternativeCommandsHandler(this);
      this.timer = new EssentialsTimer(this);
      this.getScheduler().scheduleSyncRepeatingTask(this, this.timer, 100L, 100L);
      Economy.setEss(this);
      this.execTimer.mark("RegHandler");
      MetricsStarter metricsStarter = new MetricsStarter(this);
      if (metricsStarter.getStart() != null && metricsStarter.getStart()) {
         this.getScheduler().runTaskLaterAsynchronously(this, metricsStarter, 1L);
      } else if (metricsStarter.getStart() != null && !metricsStarter.getStart()) {
         MetricsListener metricsListener = new MetricsListener(this, metricsStarter);
         pm.registerEvents(metricsListener, this);
      }

      String timeroutput = this.execTimer.end();
      if (this.getSettings().isDebug()) {
         LOGGER.log(Level.INFO, "Essentials load " + timeroutput);
      }

   }

   private void registerListeners(PluginManager pm) {
      HandlerList.unregisterAll(this);
      if (this.getSettings().isDebug()) {
         LOGGER.log(Level.INFO, "Registering Listeners");
      }

      EssentialsPluginListener serverListener = new EssentialsPluginListener(this);
      pm.registerEvents(serverListener, this);
      this.confList.add(serverListener);
      EssentialsPlayerListener playerListener = new EssentialsPlayerListener(this);
      pm.registerEvents(playerListener, this);
      EssentialsBlockListener blockListener = new EssentialsBlockListener(this);
      pm.registerEvents(blockListener, this);
      SignBlockListener signBlockListener = new SignBlockListener(this);
      pm.registerEvents(signBlockListener, this);
      SignPlayerListener signPlayerListener = new SignPlayerListener(this);
      pm.registerEvents(signPlayerListener, this);
      SignEntityListener signEntityListener = new SignEntityListener(this);
      pm.registerEvents(signEntityListener, this);
      EssentialsEntityListener entityListener = new EssentialsEntityListener(this);
      pm.registerEvents(entityListener, this);
      EssentialsWorldListener worldListener = new EssentialsWorldListener(this);
      pm.registerEvents(worldListener, this);
      pm.registerEvents(this.tntListener, this);
      this.jails.resetListener();
   }

   public void onDisable() {
      for(Player p : this.getServer().getOnlinePlayers()) {
         User user = this.getUser(p);
         if (user.isVanished()) {
            user.toggleVanished();
            p.sendMessage(I18n._("unvanishedReload"));
         }
      }

      this.cleanupOpenInventories();
      this.i18n.onDisable();
      Economy.setEss((IEssentials)null);
      Trade.closeLog();
   }

   public void reload() {
      Trade.closeLog();

      for(IConf iConf : this.confList) {
         iConf.reloadConfig();
         this.execTimer.mark("Reload(" + iConf.getClass().getSimpleName() + ")");
      }

      this.i18n.updateLocale(this.settings.getLocale());
      PluginManager pm = this.getServer().getPluginManager();
      this.registerListeners(pm);
   }

   public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
      return this.onCommandEssentials(sender, command, commandLabel, args, Essentials.class.getClassLoader(), "com.earth2me.essentials.commands.Command", "essentials.", (IEssentialsModule)null);
   }

   public boolean onCommandEssentials(CommandSender sender, Command command, String commandLabel, String[] args, ClassLoader classLoader, String commandPath, String permissionPrefix, IEssentialsModule module) {
      if (!this.getSettings().isCommandOverridden(command.getName()) && (!commandLabel.startsWith("e") || commandLabel.equalsIgnoreCase(command.getName()))) {
         PluginCommand pc = this.alternativeCommandsHandler.getAlternative(commandLabel);
         if (pc != null) {
            this.alternativeCommandsHandler.executed(commandLabel, pc);

            try {
               return pc.execute(sender, commandLabel, args);
            } catch (Exception ex) {
               Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
               sender.sendMessage(ChatColor.RED + "An internal error occurred while attempting to perform this command");
               return true;
            }
         }
      }

      try {
         User user = null;
         if (sender instanceof Player) {
            user = this.getUser((Object)sender);
         }

         if (user != null && !this.getSettings().isCommandDisabled("mail") && !command.getName().equals("mail") && user.isAuthorized("essentials.mail")) {
            List<String> mail = user.getMails();
            if (mail != null && !mail.isEmpty()) {
               user.sendMessage(I18n._("youHaveNewMail", mail.size()));
            }
         }

         if (commandLabel.equalsIgnoreCase("essversion")) {
            sender.sendMessage("This server is running Essentials " + this.getDescription().getVersion());
            return true;
         } else if (!this.getSettings().isCommandDisabled(commandLabel)) {
            IEssentialsCommand cmd;
            try {
               cmd = (IEssentialsCommand)classLoader.loadClass(commandPath + command.getName()).newInstance();
               cmd.setEssentials(this);
               cmd.setEssentialsModule(module);
            } catch (Exception ex) {
               sender.sendMessage(I18n._("commandNotLoaded", commandLabel));
               LOGGER.log(Level.SEVERE, I18n._("commandNotLoaded", commandLabel), ex);
               return true;
            }

            if (user != null && !user.isAuthorized(cmd, permissionPrefix)) {
               LOGGER.log(Level.WARNING, I18n._("deniedAccessCommand", user.getName()));
               user.sendMessage(I18n._("noAccessCommand"));
               return true;
            } else if (user != null && user.isJailed() && !user.isAuthorized(cmd, "essentials.jail.allow.")) {
               if (user.getJailTimeout() > 0L) {
                  user.sendMessage(I18n._("playerJailedFor", user.getName(), Util.formatDateDiff(user.getJailTimeout())));
               } else {
                  user.sendMessage(I18n._("jailMessage"));
               }

               return true;
            } else {
               try {
                  if (user == null) {
                     cmd.run(this.getServer(), sender, commandLabel, command, args);
                  } else {
                     cmd.run(this.getServer(), user, commandLabel, command, args);
                  }

                  return true;
               } catch (NoChargeException var14) {
                  return true;
               } catch (NotEnoughArgumentsException ex) {
                  sender.sendMessage(command.getDescription());
                  sender.sendMessage(command.getUsage().replaceAll("<command>", commandLabel));
                  if (!ex.getMessage().isEmpty()) {
                     sender.sendMessage(ex.getMessage());
                  }

                  return true;
               } catch (Throwable ex) {
                  this.showError(sender, ex, commandLabel);
                  return true;
               }
            }
         } else {
            if (this.scm != null) {
               for(VanillaCommand cmd : this.scm.getFallbackCommands()) {
                  if (cmd.matches(commandLabel)) {
                     cmd.execute(sender, commandLabel, args);
                  }
               }
            }

            return true;
         }
      } catch (Throwable ex) {
         LOGGER.log(Level.SEVERE, I18n._("commandFailed", commandLabel), ex);
         return true;
      }
   }

   public void cleanupOpenInventories() {
      for(Player player : this.getServer().getOnlinePlayers()) {
         User user = this.getUser(player);
         if (user.isRecipeSee()) {
            user.getPlayer().getOpenInventory().getTopInventory().clear();
            user.getPlayer().getOpenInventory().close();
            user.setRecipeSee(false);
         }

         if (user.isInvSee() || user.isEnderSee()) {
            user.getPlayer().getOpenInventory().close();
            user.setInvSee(false);
            user.setEnderSee(false);
         }
      }

   }

   public void showError(CommandSender sender, Throwable exception, String commandLabel) {
      sender.sendMessage(I18n._("errorWithMessage", exception.getMessage()));
      if (this.getSettings().isDebug()) {
         LOGGER.log(Level.WARNING, I18n._("errorCallingCommand", commandLabel), exception);
      }

   }

   public BukkitScheduler getScheduler() {
      return this.getServer().getScheduler();
   }

   public IJails getJails() {
      return this.jails;
   }

   public Warps getWarps() {
      return this.warps;
   }

   public Worth getWorth() {
      return this.worth;
   }

   public Backup getBackup() {
      return this.backup;
   }

   public Metrics getMetrics() {
      return this.metrics;
   }

   public void setMetrics(Metrics metrics) {
      this.metrics = metrics;
   }

   public User getUser(Object base) {
      if (base instanceof Player) {
         return this.getUser((Player)base);
      } else {
         return base instanceof String ? this.getOfflineUser((String)base) : null;
      }
   }

   public User getOfflineUser(String name) {
      User user = this.userMap.getUser(name);
      if (user != null && user.getBase() instanceof OfflinePlayer) {
         ((OfflinePlayer)user.getBase()).setName(name);
      }

      return user;
   }

   private User getUser(Player base) {
      if (base == null) {
         return null;
      } else if (base instanceof User) {
         return (User)base;
      } else if (this.userMap == null) {
         LOGGER.log(Level.WARNING, "Essentials userMap not initialized");
         return null;
      } else {
         User user = this.userMap.getUser(base.getName());
         if (user == null) {
            user = new User(base, this);
         } else {
            user.update(base);
         }

         return user;
      }
   }

   public World getWorld(String name) {
      if (name.matches("[0-9]+")) {
         int worldId = Integer.parseInt(name);
         if (worldId < this.getServer().getWorlds().size()) {
            return (World)this.getServer().getWorlds().get(worldId);
         }
      }

      return this.getServer().getWorld(name);
   }

   public void addReloadListener(IConf listener) {
      this.confList.add(listener);
   }

   public Methods getPaymentMethod() {
      return this.paymentMethod;
   }

   public int broadcastMessage(IUser sender, String message) {
      if (sender == null) {
         return this.getServer().broadcastMessage(message);
      } else if (sender.isHidden()) {
         return 0;
      } else {
         Player[] players = this.getServer().getOnlinePlayers();

         for(Player player : players) {
            User user = this.getUser(player);
            if (!user.isIgnoredPlayer(sender)) {
               player.sendMessage(message);
            }
         }

         return players.length;
      }
   }

   public BukkitTask runTaskAsynchronously(Runnable run) {
      return this.getScheduler().runTaskAsynchronously(this, run);
   }

   public BukkitTask runTaskLaterAsynchronously(Runnable run, long delay) {
      return this.getScheduler().runTaskLaterAsynchronously(this, run, delay);
   }

   public int scheduleSyncDelayedTask(Runnable run) {
      return this.getScheduler().scheduleSyncDelayedTask(this, run);
   }

   public int scheduleSyncDelayedTask(Runnable run, long delay) {
      return this.getScheduler().scheduleSyncDelayedTask(this, run, delay);
   }

   public int scheduleSyncRepeatingTask(Runnable run, long delay, long period) {
      return this.getScheduler().scheduleSyncRepeatingTask(this, run, delay, period);
   }

   public TNTExplodeListener getTNTListener() {
      return this.tntListener;
   }

   public PermissionsHandler getPermissionsHandler() {
      return this.permissionsHandler;
   }

   public AlternativeCommandsHandler getAlternativeCommandsHandler() {
      return this.alternativeCommandsHandler;
   }

   public ItemDb getItemDb() {
      return this.itemDb;
   }

   public UserMap getUserMap() {
      return this.userMap;
   }

   public I18n getI18n() {
      return this.i18n;
   }

   public EssentialsTimer getTimer() {
      return this.timer;
   }

   public List getVanishedPlayers() {
      return this.vanishedPlayers;
   }

   private static class EssentialsWorldListener implements Listener, Runnable {
      private final transient IEssentials ess;

      public EssentialsWorldListener(IEssentials ess) {
         this.ess = ess;
      }

      @EventHandler(
         priority = EventPriority.LOW
      )
      public void onWorldLoad(WorldLoadEvent event) {
         this.ess.getJails().onReload();
         this.ess.getWarps().reloadConfig();

         for(IConf iConf : ((Essentials)this.ess).confList) {
            if (iConf instanceof IEssentialsModule) {
               iConf.reloadConfig();
            }
         }

      }

      @EventHandler(
         priority = EventPriority.LOW
      )
      public void onWorldUnload(WorldUnloadEvent event) {
         this.ess.getJails().onReload();
         this.ess.getWarps().reloadConfig();

         for(IConf iConf : ((Essentials)this.ess).confList) {
            if (iConf instanceof IEssentialsModule) {
               iConf.reloadConfig();
            }
         }

      }

      public void run() {
         this.ess.reload();
      }
   }
}
