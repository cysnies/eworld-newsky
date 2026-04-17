package fr.neatmonster.nocheatplus;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.blockbreak.BlockBreakListener;
import fr.neatmonster.nocheatplus.checks.blockinteract.BlockInteractListener;
import fr.neatmonster.nocheatplus.checks.blockplace.BlockPlaceListener;
import fr.neatmonster.nocheatplus.checks.chat.ChatListener;
import fr.neatmonster.nocheatplus.checks.combined.CombinedListener;
import fr.neatmonster.nocheatplus.checks.fight.FightListener;
import fr.neatmonster.nocheatplus.checks.inventory.InventoryListener;
import fr.neatmonster.nocheatplus.checks.moving.MovingListener;
import fr.neatmonster.nocheatplus.clients.ModUtil;
import fr.neatmonster.nocheatplus.command.NoCheatPlusCommand;
import fr.neatmonster.nocheatplus.compat.DefaultComponentFactory;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.compat.MCAccessFactory;
import fr.neatmonster.nocheatplus.components.ComponentRegistry;
import fr.neatmonster.nocheatplus.components.ComponentWithName;
import fr.neatmonster.nocheatplus.components.ConsistencyChecker;
import fr.neatmonster.nocheatplus.components.IHoldSubComponents;
import fr.neatmonster.nocheatplus.components.INeedConfig;
import fr.neatmonster.nocheatplus.components.INotifyReload;
import fr.neatmonster.nocheatplus.components.JoinLeaveListener;
import fr.neatmonster.nocheatplus.components.MCAccessHolder;
import fr.neatmonster.nocheatplus.components.NCPListener;
import fr.neatmonster.nocheatplus.components.NameSetPermState;
import fr.neatmonster.nocheatplus.components.NoCheatPlusAPI;
import fr.neatmonster.nocheatplus.components.PermStateReceiver;
import fr.neatmonster.nocheatplus.components.TickListener;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.event.IHaveMethodOrder;
import fr.neatmonster.nocheatplus.event.ListenerManager;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import fr.neatmonster.nocheatplus.logging.StaticLogFile;
import fr.neatmonster.nocheatplus.metrics.Metrics;
import fr.neatmonster.nocheatplus.metrics.MetricsData;
import fr.neatmonster.nocheatplus.permissions.PermissionUtil;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.players.PlayerData;
import fr.neatmonster.nocheatplus.updates.Updates;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.OnDemandTickListener;
import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class NoCheatPlus extends JavaPlugin implements NoCheatPlusAPI {
   private static final String MSG_CONFIG_OUTDATED;
   private static final String MSG_NOTIFY_OFF;
   protected final NameSetPermState nameSetPerms = new NameSetPermState(new String[]{"nocheatplus.admin.notify"});
   private final Map denyLoginNames = Collections.synchronizedMap(new HashMap());
   protected MCAccess mcAccess = null;
   private boolean configOutdated = false;
   protected final DataManager dataMan = new DataManager();
   private int dataManTaskId = -1;
   protected Metrics metrics = null;
   protected List changedCommands = null;
   private final ListenerManager listenerManager = new ListenerManager(this, false);
   private boolean manageListeners = true;
   private final List listeners = new ArrayList();
   private final List notifyReload = new LinkedList();
   protected boolean useSubscriptions = false;
   protected final List permStateReceivers = new ArrayList();
   protected final List consistencyCheckers = new ArrayList();
   protected int consistencyCheckerIndex = 0;
   protected int consistencyCheckerTaskId = -1;
   protected final List joinLeaveListeners = new ArrayList();
   protected final List subRegistries = new ArrayList();
   protected final List subComponentholders = new ArrayList(20);
   protected Set allComponents = new LinkedHashSet(50);
   protected final OnDemandTickListener onDemandTickListener = new OnDemandTickListener() {
      public boolean delegateTick(int tick, long timeLast) {
         NoCheatPlus.this.processQueuedSubComponentHolders();
         return false;
      }
   };

   /** @deprecated */
   public static NoCheatPlusAPI getAPI() {
      return NCPAPIProvider.getNoCheatPlusAPI();
   }

   private void checkDenyLoginsNames() {
      long ts = System.currentTimeMillis();
      List<String> rem = new LinkedList();
      synchronized(this.denyLoginNames) {
         for(Map.Entry entry : this.denyLoginNames.entrySet()) {
            if ((Long)entry.getValue() < ts) {
               rem.add(entry.getKey());
            }
         }

         for(String name : rem) {
            this.denyLoginNames.remove(name);
         }

      }
   }

   public boolean allowLogin(String playerName) {
      playerName = playerName.trim().toLowerCase();
      Long time = (Long)this.denyLoginNames.remove(playerName);
      if (time == null) {
         return false;
      } else {
         return System.currentTimeMillis() <= time;
      }
   }

   public int allowLoginAll() {
      int denied = 0;
      long now = System.currentTimeMillis();

      for(String playerName : this.denyLoginNames.keySet()) {
         Long time = (Long)this.denyLoginNames.get(playerName);
         if (time != null && time > now) {
            ++denied;
         }
      }

      this.denyLoginNames.clear();
      return denied;
   }

   public void denyLogin(String playerName, long duration) {
      long ts = System.currentTimeMillis() + duration;
      playerName = playerName.trim().toLowerCase();
      synchronized(this.denyLoginNames) {
         Long oldTs = (Long)this.denyLoginNames.get(playerName);
         if (oldTs != null && ts < oldTs) {
            return;
         }

         this.denyLoginNames.put(playerName, ts);
      }

      this.checkDenyLoginsNames();
   }

   public boolean isLoginDenied(String playerName) {
      return this.isLoginDenied(playerName, System.currentTimeMillis());
   }

   public String[] getLoginDeniedPlayers() {
      this.checkDenyLoginsNames();
      String[] kicked = new String[this.denyLoginNames.size()];
      this.denyLoginNames.keySet().toArray(kicked);
      return kicked;
   }

   public boolean isLoginDenied(String playerName, long time) {
      playerName = playerName.trim().toLowerCase();
      Long oldTs = (Long)this.denyLoginNames.get(playerName);
      if (oldTs == null) {
         return false;
      } else {
         return time < oldTs;
      }
   }

   public int sendAdminNotifyMessage(String message) {
      return this.useSubscriptions ? this.sendAdminNotifyMessageSubscriptions(message) : this.sendAdminNotifyMessageStored(message);
   }

   private final boolean hasTurnedOffNotifications(String playerName) {
      PlayerData data = DataManager.getPlayerData(playerName, false);
      return data != null && data.getNotifyOff();
   }

   public int sendAdminNotifyMessageStored(String message) {
      Set<String> names = this.nameSetPerms.getPlayers("nocheatplus.admin.notify");
      if (names == null) {
         return 0;
      } else {
         int done = 0;

         for(String name : names) {
            if (!this.hasTurnedOffNotifications(name)) {
               Player player = DataManager.getPlayerExact(name);
               if (player != null) {
                  player.sendMessage(message);
                  ++done;
               }
            }
         }

         return done;
      }
   }

   public int sendAdminNotifyMessageSubscriptions(String message) {
      Set<Permissible> permissibles = Bukkit.getPluginManager().getPermissionSubscriptions("nocheatplus.admin.notify");
      Set<String> names = this.nameSetPerms.getPlayers("nocheatplus.admin.notify");
      Set<String> done = new HashSet(permissibles.size() + (names == null ? 0 : names.size()));

      for(Permissible permissible : permissibles) {
         if (permissible instanceof CommandSender && permissible.hasPermission("nocheatplus.admin.notify")) {
            CommandSender sender = (CommandSender)permissible;
            if (!(sender instanceof Player) || !this.hasTurnedOffNotifications(((Player)sender).getName())) {
               sender.sendMessage(message);
               done.add(sender.getName());
            }
         }
      }

      if (names != null) {
         for(String name : names) {
            if (!done.contains(name)) {
               Player player = DataManager.getPlayerExact(name);
               if (player != null && player.hasPermission("nocheatplus.admin.notify") && !this.hasTurnedOffNotifications(player.getName())) {
                  player.sendMessage(message);
                  done.add(name);
               }
            }
         }
      }

      return done.size();
   }

   public Collection getComponentRegistries(Class clazz) {
      List<ComponentRegistry<T>> result = new LinkedList();

      for(ComponentRegistry registry : this.subRegistries) {
         if (clazz.isAssignableFrom(registry.getClass())) {
            try {
               result.add(registry);
            } catch (Throwable var6) {
            }
         }
      }

      return result;
   }

   public boolean addComponent(Object obj) {
      return this.addComponent(obj, true);
   }

   public boolean addComponent(Object obj, boolean allowComponentRegistry) {
      if (obj == this) {
         throw new IllegalArgumentException("Can not register NoCheatPlus with itself.");
      } else if (this.allComponents.contains(obj)) {
         return false;
      } else {
         boolean added = false;
         if (obj instanceof Listener) {
            this.addListener((Listener)obj);
            added = true;
         }

         if (obj instanceof INotifyReload) {
            this.notifyReload.add((INotifyReload)obj);
            if (obj instanceof INeedConfig) {
               ((INeedConfig)obj).onReload();
            }

            added = true;
         }

         if (obj instanceof TickListener) {
            TickTask.addTickListener((TickListener)obj);
            added = true;
         }

         if (obj instanceof PermStateReceiver) {
            this.permStateReceivers.add((PermStateReceiver)obj);
            added = true;
         }

         if (obj instanceof MCAccessHolder) {
            ((MCAccessHolder)obj).setMCAccess(this.getMCAccess());
            added = true;
         }

         if (obj instanceof ConsistencyChecker) {
            this.consistencyCheckers.add((ConsistencyChecker)obj);
            added = true;
         }

         if (obj instanceof JoinLeaveListener) {
            this.joinLeaveListeners.add((JoinLeaveListener)obj);
            added = true;
         }

         for(ComponentRegistry registry : this.subRegistries) {
            Object res = ReflectionUtil.invokeGenericMethodOneArg(registry, "addComponent", obj);
            if (res != null && res instanceof Boolean && (Boolean)res) {
               added = true;
            }
         }

         if (allowComponentRegistry && obj instanceof ComponentRegistry) {
            this.subRegistries.add((ComponentRegistry)obj);
            added = true;
         }

         if (obj instanceof IHoldSubComponents) {
            this.subComponentholders.add((IHoldSubComponents)obj);
            this.onDemandTickListener.register();
            added = true;
         }

         if (added) {
            this.allComponents.add(obj);
         }

         return added;
      }
   }

   private void addListener(Listener listener) {
      if (this.manageListeners) {
         String tag = "NoCheatPlus";
         if (listener instanceof ComponentWithName) {
            tag = ((ComponentWithName)listener).getComponentName();
         }

         this.listenerManager.registerAllEventHandlers(listener, tag);
         this.listeners.add(listener);
      } else {
         Bukkit.getPluginManager().registerEvents(listener, this);
         if (listener instanceof IHaveMethodOrder) {
            LogUtil.logWarning("[NoCheatPlus] Listener demands registration order, but listeners are not managed: " + listener.getClass().getName());
         }
      }

   }

   public boolean doesManageListeners() {
      return this.manageListeners;
   }

   public void removeComponent(Object obj) {
      if (obj instanceof Listener) {
         this.listeners.remove(obj);
         this.listenerManager.remove((Listener)obj);
      }

      if (obj instanceof PermStateReceiver) {
         this.permStateReceivers.remove((PermStateReceiver)obj);
      }

      if (obj instanceof TickListener) {
         TickTask.removeTickListener((TickListener)obj);
      }

      if (obj instanceof INotifyReload) {
         this.notifyReload.remove(obj);
      }

      if (obj instanceof ConsistencyChecker) {
         this.consistencyCheckers.remove(obj);
      }

      if (obj instanceof JoinLeaveListener) {
         this.joinLeaveListeners.remove((JoinLeaveListener)obj);
      }

      if (obj instanceof ComponentRegistry) {
         this.subRegistries.remove(obj);
      }

      for(ComponentRegistry registry : this.subRegistries) {
         ReflectionUtil.invokeGenericMethodOneArg(registry, "removeComponent", obj);
      }

      this.allComponents.remove(obj);
   }

   public void onDisable() {
      boolean verbose = ConfigManager.getConfigFile().getBoolean("logging.debug");
      if (verbose) {
         if (this.listenerManager.hasListenerMethods()) {
            LogUtil.logInfo("[NoCheatPlus] Cleanup ListenerManager...");
         } else {
            LogUtil.logInfo("[NoCheatPlus] (ListenerManager not in use, prevent registering...)");
         }
      }

      this.listenerManager.setRegisterDirectly(false);
      this.listenerManager.clear();
      BukkitScheduler sched = this.getServer().getScheduler();
      if (this.dataManTaskId != -1) {
         sched.cancelTask(this.dataManTaskId);
         this.dataManTaskId = -1;
      }

      if (verbose) {
         LogUtil.logInfo("[NoCheatPlus] Stop TickTask...");
      }

      TickTask.setLocked(true);
      TickTask.purge();
      TickTask.cancel();
      TickTask.removeAllTickListeners();
      if (this.metrics != null) {
         if (verbose) {
            LogUtil.logInfo("[NoCheatPlus] Stop Metrics...");
         }

         this.metrics.cancel();
         this.metrics = null;
      }

      if (this.consistencyCheckerTaskId != -1) {
         sched.cancelTask(this.consistencyCheckerTaskId);
      }

      if (verbose) {
         LogUtil.logInfo("[NoCheatPlus] Stop all remaining tasks...");
      }

      sched.cancelTasks(this);
      if (verbose) {
         LogUtil.logInfo("[NoCheatPlus] Reset ExemptionManager...");
      }

      NCPExemptionManager.clear();
      if (verbose) {
         LogUtil.logInfo("[NoCheatPlus] Cleanup DataManager...");
      }

      this.dataMan.onDisable();
      if (verbose) {
         LogUtil.logInfo("[NoCheatPlus] Unregister all registered components...");
      }

      ArrayList<Object> allComponents = new ArrayList(this.allComponents);

      for(int i = allComponents.size() - 1; i >= 0; --i) {
         this.removeComponent(allComponents.get(i));
      }

      if (verbose) {
         LogUtil.logInfo("[NoCheatPlus] Cleanup BlockProperties...");
      }

      BlockProperties.cleanup();
      if (verbose) {
         LogUtil.logInfo("[NoCheatPlus] Cleanup some mappings...");
      }

      this.listeners.clear();
      this.notifyReload.clear();
      this.permStateReceivers.clear();
      this.subRegistries.clear();
      this.subComponentholders.clear();
      if (this.changedCommands != null) {
         this.changedCommands.clear();
         this.changedCommands = null;
      }

      if (verbose) {
         LogUtil.logInfo("[NoCheatPlus] Cleanup ConfigManager...");
      }

      ConfigManager.cleanup();
      if (verbose) {
         LogUtil.logInfo("[NoCheatPlus] Cleanup file logger...");
      }

      StaticLogFile.cleanup();
      if (verbose) {
         LogUtil.logInfo("[NoCheatPlus] All cleanup done.");
      }

      PluginDescriptionFile pdfFile = this.getDescription();
      LogUtil.logInfo("[NoCheatPlus] Version " + pdfFile.getVersion() + " is disabled.");
   }

   /** @deprecated */
   public void undoCommandChanges() {
      if (this.changedCommands != null) {
         while(!this.changedCommands.isEmpty()) {
            PermissionUtil.CommandProtectionEntry entry = (PermissionUtil.CommandProtectionEntry)this.changedCommands.remove(this.changedCommands.size() - 1);
            entry.restore();
         }

         this.changedCommands = null;
      }

   }

   protected void setupCommandProtection() {
      List<PermissionUtil.CommandProtectionEntry> changedCommands = PermissionUtil.protectCommands(Arrays.asList("plugins", "version", "icanhasbukkit"), "feature.command", false);
      if (this.changedCommands == null) {
         this.changedCommands = changedCommands;
      } else {
         this.changedCommands.addAll(changedCommands);
      }

   }

   public void onLoad() {
      NCPAPIProvider.setNoCheatPlusAPI(this);
      super.onLoad();
   }

   public void onEnable() {
      TickTask.setLocked(true);
      TickTask.purge();
      TickTask.cancel();
      TickTask.reset();
      ConfigManager.init(this);
      StaticLogFile.setupLogger(new File(this.getDataFolder(), ConfigManager.getConfigFile().getString("logging.backend.file.filename")));
      ConfigFile config = ConfigManager.getConfigFile();
      this.useSubscriptions = config.getBoolean("logging.backend.ingamechat.subscriptions");
      this.initMCAccess(config);
      this.initBlockProperties(config);
      this.dataMan.onEnable();
      TickTask.setLocked(false);
      this.manageListeners = config.getBoolean("miscellaneous.managelisteners");
      if (this.manageListeners) {
         this.listenerManager.setRegisterDirectly(true);
         this.listenerManager.registerAllWithBukkit();
      } else {
         this.listenerManager.setRegisterDirectly(false);
         this.listenerManager.clear();
      }

      for(Object obj : new Object[]{this.nameSetPerms, this.getCoreListener(), new INotifyReload() {
         public void onReload() {
            NoCheatPlus.this.processReload();
         }
      }, NCPExemptionManager.getListener(), new ConsistencyChecker() {
         public void checkConsistency(Player[] onlinePlayers) {
            NCPExemptionManager.checkConsistency(onlinePlayers);
         }
      }, this.dataMan}) {
         this.addComponent(obj);
         this.processQueuedSubComponentHolders();
      }

      for(Object obj : new Object[]{new BlockInteractListener(), new BlockBreakListener(), new BlockPlaceListener(), new ChatListener(), new CombinedListener(), new FightListener(), new InventoryListener(), new MovingListener()}) {
         this.addComponent(obj);
         this.processQueuedSubComponentHolders();
      }

      DefaultComponentFactory dcf = new DefaultComponentFactory();

      for(Object obj : dcf.getAvailableComponentsOnEnable()) {
         this.addComponent(obj);
         this.processQueuedSubComponentHolders();
      }

      PluginCommand command = this.getCommand("nocheatplus");
      final NoCheatPlusCommand commandHandler = new NoCheatPlusCommand(this, this.notifyReload);
      command.setExecutor(commandHandler);
      TickTask.start(this);
      this.dataManTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
         public void run() {
            NoCheatPlus.this.dataMan.checkExpiration();
         }
      }, 1207L, 1207L);
      this.scheduleConsistencyCheckers();
      this.setMetrics();
      this.configOutdated = Updates.isConfigOutdated(490, config);
      if (config.getBoolean("protection.plugins.hide.active")) {
         Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
               NoCheatPlus.this.setupCommandProtection();
            }
         });
      }

      final Player[] onlinePlayers = this.getServer().getOnlinePlayers();
      Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
         public void run() {
            NoCheatPlus.this.postEnable(onlinePlayers, new Runnable() {
               public void run() {
                  PermissionUtil.addChildPermission(commandHandler.getAllSubCommandPermissions(), "feature.command.nocheatplus", PermissionDefault.OP);
               }
            });
         }
      });
      LogUtil.logInfo("[NoCheatPlus] Version " + this.getDescription().getVersion() + " is enabled.");
   }

   private void setMetrics() {
      ConfigFile config = ConfigManager.getConfigFile();
      if (config.getBoolean("miscellaneous.reporttometrics")) {
         if (this.metrics != null) {
            return;
         }

         this.startMetrics();
      } else if (this.metrics != null) {
         this.metrics.cancel();
         this.metrics = null;
      }

   }

   private void startMetrics() {
      MetricsData.initialize();

      try {
         this.metrics = new Metrics(this);
         fr.neatmonster.nocheatplus.metrics.org.mcstats.Metrics.Graph checksFailed = this.metrics.createGraph("Checks Failed");

         for(final CheckType type : CheckType.values()) {
            if (type.getParent() != null) {
               checksFailed.addPlotter(new fr.neatmonster.nocheatplus.metrics.org.mcstats.Metrics.Plotter(type.name()) {
                  public int getValue() {
                     return MetricsData.getFailed(type);
                  }
               });
            }
         }

         fr.neatmonster.nocheatplus.metrics.org.mcstats.Metrics.Graph serverTicks = this.metrics.createGraph("Server Ticks");

         for(final int i = 0; i <= 20; ++i) {
            serverTicks.addPlotter(new fr.neatmonster.nocheatplus.metrics.org.mcstats.Metrics.Plotter(i + " tick(s)") {
               public int getValue() {
                  return MetricsData.getTicks(i);
               }
            });
         }

         this.metrics.start();
      } catch (Exception e) {
         LogUtil.logWarning("[NoCheatPlus] Failed to initialize metrics:");
         LogUtil.logWarning((Throwable)e);
         if (this.metrics != null) {
            this.metrics.cancel();
            this.metrics = null;
         }
      }

   }

   protected void processQueuedSubComponentHolders() {
      if (!this.subComponentholders.isEmpty()) {
         List<IHoldSubComponents> copied = new ArrayList(this.subComponentholders);
         this.subComponentholders.clear();

         for(IHoldSubComponents holder : copied) {
            for(Object component : holder.getSubComponents()) {
               this.addComponent(component);
            }
         }

      }
   }

   protected void processReload() {
      ConfigFile config = ConfigManager.getConfigFile();
      this.initMCAccess(config);
      this.initBlockProperties(config);
      this.undoCommandChanges();
      if (config.getBoolean("protection.plugins.hide.active")) {
         this.setupCommandProtection();
      }

      this.scheduleConsistencyCheckers();
      this.useSubscriptions = config.getBoolean("logging.backend.ingamechat.subscriptions");
      this.setMetrics();
   }

   public MCAccess getMCAccess() {
      if (this.mcAccess == null) {
         this.initMCAccess();
      }

      return this.mcAccess;
   }

   private void initMCAccess() {
      this.getServer().getScheduler().callSyncMethod(this, new Callable() {
         public MCAccess call() throws Exception {
            return NoCheatPlus.this.mcAccess != null ? NoCheatPlus.this.mcAccess : NoCheatPlus.this.initMCAccess(ConfigManager.getConfigFile());
         }
      });
   }

   public MCAccess initMCAccess(ConfigFile config) {
      MCAccess mcAccess = (new MCAccessFactory()).getMCAccess(config.getBoolean("compatibility.bukkitapionly"));
      this.setMCAccess(mcAccess);
      return mcAccess;
   }

   public void setMCAccess(MCAccess mcAccess) {
      this.mcAccess = mcAccess;

      for(Object obj : this.allComponents) {
         if (obj instanceof MCAccessHolder) {
            try {
               ((MCAccessHolder)obj).setMCAccess(mcAccess);
            } catch (Throwable t) {
               LogUtil.logSevere("[NoCheatPlus] MCAccessHolder(" + obj.getClass().getName() + ") failed to set MCAccess: " + t.getClass().getSimpleName());
               LogUtil.logSevere(t);
            }
         }
      }

      LogUtil.logInfo("[NoCheatPlus] McAccess set to: " + mcAccess.getMCVersion() + " / " + mcAccess.getServerVersionTag());
   }

   protected void initBlockProperties(ConfigFile config) {
      BlockProperties.init(this.getMCAccess(), ConfigManager.getWorldConfigProvider());
      BlockProperties.applyConfig(config, "compatibility.blocks.");
      Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
         public void run() {
            ConfigFile config = ConfigManager.getConfigFile();
            BlockProperties.dumpBlocks(config.getBoolean("checks.blockbreak.fastbreak.debug", config.getBoolean("checks.blockbreak.debug", config.getBoolean("checks.debug", false))));
         }
      });
   }

   private void postEnable(Player[] onlinePlayers, Runnable... runnables) {
      for(Runnable runnable : runnables) {
         try {
            runnable.run();
         } catch (Throwable t) {
            LogUtil.logSevere("[NoCheatPlus] Encountered a problem during post-enable: " + t.getClass().getSimpleName());
            LogUtil.logSevere(t);
         }
      }

      for(Player player : onlinePlayers) {
         this.updatePermStateReceivers(player);
         NCPExemptionManager.registerPlayer(player);
      }

   }

   private Listener getCoreListener() {
      return new NCPListener() {
         @EventHandler(
            priority = EventPriority.NORMAL
         )
         public void onPlayerLogin(PlayerLoginEvent event) {
            if (event.getResult() == Result.ALLOWED) {
               Player player = event.getPlayer();
               NoCheatPlus.this.checkDenyLoginsNames();
               if (!player.hasPermission("nocheatplus.bypassdenylogin")) {
                  if (NoCheatPlus.this.isLoginDenied(player.getName())) {
                     event.setResult(Result.KICK_OTHER);
                     event.setKickMessage("You are temporarily denied to join this server.");
                  }

               }
            }
         }

         @EventHandler(
            priority = EventPriority.MONITOR
         )
         public void onPlayerJoin(PlayerJoinEvent event) {
            NoCheatPlus.this.onJoin(event.getPlayer());
         }

         @EventHandler(
            priority = EventPriority.MONITOR
         )
         public void onPlayerchangedWorld(PlayerChangedWorldEvent event) {
            Player player = event.getPlayer();
            NoCheatPlus.this.updatePermStateReceivers(player);
         }

         @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
         )
         public void onPlayerKick(PlayerKickEvent event) {
            NoCheatPlus.this.onLeave(event.getPlayer());
         }

         @EventHandler(
            priority = EventPriority.MONITOR
         )
         public void onPlayerQuitMonitor(PlayerQuitEvent event) {
            NoCheatPlus.this.onLeave(event.getPlayer());
         }
      };
   }

   protected void onJoin(Player player) {
      this.updatePermStateReceivers(player);
      String playerName = player.getName();
      if (this.nameSetPerms.hasPermission(playerName, "nocheatplus.admin.notify")) {
         if (this.configOutdated) {
            player.sendMessage(MSG_CONFIG_OUTDATED);
         }

         if (this.hasTurnedOffNotifications(playerName)) {
            player.sendMessage(MSG_NOTIFY_OFF);
         }
      }

      ModUtil.motdOnJoin(player);

      for(JoinLeaveListener jlListener : this.joinLeaveListeners) {
         try {
            jlListener.playerJoins(player);
         } catch (Throwable t) {
            LogUtil.logSevere("[NoCheatPlus] JoinLeaveListener(" + jlListener.getClass().getName() + ") generated an exception (join): " + t.getClass().getSimpleName());
            LogUtil.logSevere(t);
         }
      }

   }

   protected void onLeave(Player player) {
      for(PermStateReceiver pr : this.permStateReceivers) {
         pr.removePlayer(player.getName());
      }

      for(JoinLeaveListener jlListener : this.joinLeaveListeners) {
         try {
            jlListener.playerLeaves(player);
         } catch (Throwable t) {
            LogUtil.logSevere("[NoCheatPlus] JoinLeaveListener(" + jlListener.getClass().getName() + ") generated an exception (leave): " + t.getClass().getSimpleName());
            LogUtil.logSevere(t);
         }
      }

   }

   protected void updatePermStateReceivers(Player player) {
      Map<String, Boolean> checked = new HashMap(20);
      String name = player.getName();

      for(PermStateReceiver pr : this.permStateReceivers) {
         for(String permission : pr.getDefaultPermissions()) {
            Boolean state = (Boolean)checked.get(permission);
            if (state == null) {
               state = player.hasPermission(permission);
               checked.put(permission, state);
            }

            pr.setPermission(name, permission, state);
         }
      }

   }

   protected void scheduleConsistencyCheckers() {
      BukkitScheduler sched = this.getServer().getScheduler();
      if (this.consistencyCheckerTaskId != -1) {
         sched.cancelTask(this.consistencyCheckerTaskId);
      }

      ConfigFile config = ConfigManager.getConfigFile();
      if (config.getBoolean("data.consistencychecks.active", true)) {
         long delay = 20L * config.getInt("data.consistencychecks.interval", 1, 3600, 10);
         this.consistencyCheckerTaskId = sched.scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
               NoCheatPlus.this.runConsistencyChecks();
            }
         }, delay, delay);
      }
   }

   protected void runConsistencyChecks() {
      long tStart = System.currentTimeMillis();
      ConfigFile config = ConfigManager.getConfigFile();
      if (config.getBoolean("data.consistencychecks.active") && !this.consistencyCheckers.isEmpty()) {
         long tEnd = tStart + config.getLong("data.consistencychecks.maxtime", 1L, 50L, 2L);
         if (this.consistencyCheckerIndex >= this.consistencyCheckers.size()) {
            this.consistencyCheckerIndex = 0;
         }

         Player[] onlinePlayers = this.getServer().getOnlinePlayers();

         while(this.consistencyCheckerIndex < this.consistencyCheckers.size()) {
            ConsistencyChecker checker = (ConsistencyChecker)this.consistencyCheckers.get(this.consistencyCheckerIndex);

            try {
               checker.checkConsistency(onlinePlayers);
            } catch (Throwable t) {
               LogUtil.logSevere("[NoCheatPlus] ConsistencyChecker(" + checker.getClass().getName() + ") encountered an exception:");
               LogUtil.logSevere(t);
            }

            ++this.consistencyCheckerIndex;
            long now = System.currentTimeMillis();
            if (now < tStart || now >= tEnd) {
               break;
            }
         }

         boolean debug = config.getBoolean("logging.debug");
         if (this.consistencyCheckerIndex < this.consistencyCheckers.size()) {
            this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
               public void run() {
                  NoCheatPlus.this.runConsistencyChecks();
               }
            });
            if (debug) {
               LogUtil.logInfo("[NoCheatPlus] Re-scheduled consistency-checks.");
            }
         } else if (debug) {
            LogUtil.logInfo("[NoCheatPlus] Consistency-checks run.");
         }

      } else {
         this.consistencyCheckerIndex = 0;
      }
   }

   static {
      MSG_CONFIG_OUTDATED = ChatColor.RED + "NCP: " + ChatColor.WHITE + "Your configuration might be outdated.\n" + "Some settings could have changed, you should regenerate it!";
      MSG_NOTIFY_OFF = ChatColor.RED + "NCP: " + ChatColor.WHITE + "Notifications are turned " + ChatColor.RED + "OFF" + ChatColor.WHITE + ".";
   }
}
