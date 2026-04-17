package com.comphenix.protocol;

import com.comphenix.protocol.async.AsyncFilterManager;
import com.comphenix.protocol.error.BasicErrorReporter;
import com.comphenix.protocol.error.DelegatedErrorReporter;
import com.comphenix.protocol.error.DetailedErrorReporter;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.injector.DelayedSingleTask;
import com.comphenix.protocol.injector.InternalManager;
import com.comphenix.protocol.injector.PacketFilterManager;
import com.comphenix.protocol.metrics.Statistics;
import com.comphenix.protocol.metrics.Updater;
import com.comphenix.protocol.reflect.compiler.BackgroundCompiler;
import com.comphenix.protocol.utility.ChatExtensions;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Server;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ProtocolLibrary extends JavaPlugin {
   public static final ReportType REPORT_CANNOT_LOAD_CONFIG = new ReportType("Cannot load configuration");
   public static final ReportType REPORT_CANNOT_DELETE_CONFIG = new ReportType("Cannot delete old ProtocolLib configuration.");
   public static final ReportType REPORT_CANNOT_PARSE_INJECTION_METHOD = new ReportType("Cannot parse injection method. Using default.");
   public static final ReportType REPORT_PLUGIN_LOAD_ERROR = new ReportType("Cannot load ProtocolLib.");
   public static final ReportType REPORT_PLUGIN_ENABLE_ERROR = new ReportType("Cannot enable ProtocolLib.");
   public static final ReportType REPORT_METRICS_IO_ERROR = new ReportType("Unable to enable metrics due to network problems.");
   public static final ReportType REPORT_METRICS_GENERIC_ERROR = new ReportType("Unable to enable metrics due to network problems.");
   public static final ReportType REPORT_CANNOT_PARSE_MINECRAFT_VERSION = new ReportType("Unable to retrieve current Minecraft version. Assuming %s");
   public static final ReportType REPORT_CANNOT_DETECT_CONFLICTING_PLUGINS = new ReportType("Unable to detect conflicting plugin versions.");
   public static final ReportType REPORT_CANNOT_REGISTER_COMMAND = new ReportType("Cannot register command %s: %s");
   public static final ReportType REPORT_CANNOT_CREATE_TIMEOUT_TASK = new ReportType("Unable to create packet timeout task.");
   public static final ReportType REPORT_CANNOT_UPDATE_PLUGIN = new ReportType("Cannot perform automatic updates.");
   public static final String MINIMUM_MINECRAFT_VERSION = "1.0.0";
   public static final String MAXIMUM_MINECRAFT_VERSION = "1.6.4";
   public static final String MINECRAFT_LAST_RELEASE_DATE = "2013-07-08";
   static final long MILLI_PER_SECOND = 1000L;
   private static final String PERMISSION_INFO = "protocol.info";
   private static InternalManager protocolManager;
   private static ErrorReporter reporter = new BasicErrorReporter();
   private Statistics statistisc;
   private BackgroundCompiler backgroundCompiler;
   private int asyncPacketTask = -1;
   private int tickCounter = 0;
   private static final int ASYNC_PACKET_DELAY = 1;
   private DelayedSingleTask unhookTask;
   private ProtocolConfig config;
   private Updater updater;
   private boolean updateDisabled;
   private Logger logger;
   private Handler redirectHandler;
   private CommandProtocol commandProtocol;
   private CommandPacket commandPacket;
   private CommandFilter commandFilter;
   private boolean skipDisable;

   public void onLoad() {
      this.logger = this.getLoggerSafely();
      DetailedErrorReporter detailedReporter = new DetailedErrorReporter(this);
      reporter = this.getFilteredReporter(detailedReporter);

      try {
         this.config = new ProtocolConfig(this);
      } catch (Exception e) {
         reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_LOAD_CONFIG).error(e));
         if (this.deleteConfig()) {
            this.config = new ProtocolConfig(this);
         } else {
            reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_DELETE_CONFIG));
         }
      }

      if (this.config.isDebug()) {
         this.logger.warning("Debug mode is enabled!");
      }

      if (this.config.isDetailedErrorReporting()) {
         detailedReporter.setDetailedReporting(true);
         this.logger.warning("Detailed error reporting enabled!");
      }

      try {
         this.checkConflictingVersions();
         MinecraftVersion version = this.verifyMinecraftVersion();
         this.updater = new Updater(this, this.logger, "protocollib", this.getFile(), "protocol.info");
         this.unhookTask = new DelayedSingleTask(this);
         protocolManager = PacketFilterManager.newBuilder().classLoader(this.getClassLoader()).server(this.getServer()).library(this).minecraftVersion(version).unhookTask(this.unhookTask).reporter(reporter).build();
         detailedReporter.addGlobalParameter("manager", protocolManager);

         try {
            PacketFilterManager.PlayerInjectHooks hook = this.config.getInjectionMethod();
            if (!protocolManager.getPlayerHook().equals(hook)) {
               this.logger.info("Changing player hook from " + protocolManager.getPlayerHook() + " to " + hook);
               protocolManager.setPlayerHook(hook);
            }
         } catch (IllegalArgumentException e) {
            reporter.reportWarning(this.config, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_PARSE_INJECTION_METHOD).error(e));
         }

         this.commandProtocol = new CommandProtocol(reporter, this, this.updater, this.config);
         this.commandFilter = new CommandFilter(reporter, this, this.config);
         this.commandPacket = new CommandPacket(reporter, this, this.logger, this.commandFilter, protocolManager);
         this.setupBroadcastUsers("protocol.info");
      } catch (Throwable e) {
         reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(REPORT_PLUGIN_LOAD_ERROR).error(e).callerParam(protocolManager));
         this.disablePlugin();
      }

   }

   private ErrorReporter getFilteredReporter(ErrorReporter reporter) {
      return new DelegatedErrorReporter(reporter) {
         private int lastModCount = -1;
         private Set reports = Sets.newHashSet();

         protected Report filterReport(Object sender, Report report, boolean detailed) {
            try {
               String canonicalName = ReportType.getReportName(sender, report.getType());
               String reportName = ((String)Iterables.getLast(Splitter.on("#").split(canonicalName))).toUpperCase();
               if (ProtocolLibrary.this.config != null && ProtocolLibrary.this.config.getModificationCount() != this.lastModCount) {
                  this.reports = Sets.newHashSet(ProtocolLibrary.this.config.getSuppressedReports());
                  this.lastModCount = ProtocolLibrary.this.config.getModificationCount();
               }

               if (this.reports.contains(canonicalName) || this.reports.contains(reportName)) {
                  return null;
               }
            } catch (Exception e) {
               ProtocolLibrary.this.logger.warning("Error filtering reports: " + e.toString());
            }

            return report;
         }
      };
   }

   private boolean deleteConfig() {
      return this.config.getFile().delete();
   }

   public void reloadConfig() {
      super.reloadConfig();
      if (this.config != null) {
         this.config.reloadConfig();
      }

   }

   private void setupBroadcastUsers(final String permission) {
      if (this.redirectHandler == null) {
         this.redirectHandler = new Handler() {
            public void publish(LogRecord record) {
               if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                  ProtocolLibrary.this.commandPacket.broadcastMessageSilently(record.getMessage(), permission);
               }

            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }
         };
         this.logger.addHandler(this.redirectHandler);
      }
   }

   public void onEnable() {
      try {
         Server server = this.getServer();
         PluginManager manager = server.getPluginManager();
         if (manager == null) {
            return;
         }

         if (protocolManager == null) {
            Logger directLogging = Logger.getLogger("Minecraft");
            String[] message = new String[]{" PROTOCOLLIB DOES NOT SUPPORT PLUGIN RELOADERS. ", " PLEASE USE THE BUILT-IN RELOAD COMMAND. "};

            for(String line : ChatExtensions.toFlowerBox(message, "*", 3, 1)) {
               directLogging.severe(line);
            }

            this.disablePlugin();
            return;
         }

         if (this.backgroundCompiler == null && this.config.isBackgroundCompilerEnabled()) {
            this.backgroundCompiler = new BackgroundCompiler(this.getClassLoader(), reporter);
            BackgroundCompiler.setInstance(this.backgroundCompiler);
            this.logger.info("Started structure compiler thread.");
         } else {
            this.logger.info("Structure compiler thread has been disabled.");
         }

         this.registerCommand("protocol", this.commandProtocol);
         this.registerCommand("packet", this.commandPacket);
         this.registerCommand("filter", this.commandFilter);
         protocolManager.registerEvents(manager, this);
         this.createAsyncTask(server);
      } catch (Throwable e) {
         reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(REPORT_PLUGIN_ENABLE_ERROR).error(e));
         this.disablePlugin();
         return;
      }

      try {
         if (this.config.isMetricsEnabled()) {
            this.statistisc = new Statistics(this);
         }
      } catch (IOException e) {
         reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(REPORT_METRICS_IO_ERROR).error(e).callerParam(this.statistisc));
      } catch (Throwable e) {
         reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(REPORT_METRICS_GENERIC_ERROR).error(e).callerParam(this.statistisc));
      }

   }

   private MinecraftVersion verifyMinecraftVersion() {
      MinecraftVersion minimum = new MinecraftVersion("1.0.0");
      MinecraftVersion maximum = new MinecraftVersion("1.6.4");

      try {
         MinecraftVersion current = new MinecraftVersion(this.getServer());
         if (!this.config.getIgnoreVersionCheck().equals(current.getVersion())) {
            if (current.compareTo(minimum) < 0) {
               this.logger.warning("Version " + current + " is lower than the minimum " + minimum);
            }

            if (current.compareTo(maximum) > 0) {
               this.logger.warning("Version " + current + " has not yet been tested! Proceed with caution.");
            }
         }

         return current;
      } catch (Exception e) {
         reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_PARSE_MINECRAFT_VERSION).error(e).messageParam(maximum));
         return maximum;
      }
   }

   private void checkConflictingVersions() {
      Pattern ourPlugin = Pattern.compile("ProtocolLib-(.*)\\.jar");
      MinecraftVersion currentVersion = new MinecraftVersion(this.getDescription().getVersion());
      MinecraftVersion newestVersion = null;
      File loadedFile = this.getFile();

      try {
         File pluginFolder = new File("plugins/");

         for(File candidate : pluginFolder.listFiles()) {
            if (candidate.isFile() && !candidate.equals(loadedFile)) {
               Matcher match = ourPlugin.matcher(candidate.getName());
               if (match.matches()) {
                  MinecraftVersion version = new MinecraftVersion(match.group(1));
                  if (candidate.length() == 0L) {
                     this.logger.info((candidate.delete() ? "Deleted " : "Could not delete ") + candidate);
                  } else if (newestVersion == null || newestVersion.compareTo(version) < 0) {
                     newestVersion = version;
                  }
               }
            }
         }
      } catch (Exception e) {
         reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_DETECT_CONFLICTING_PLUGINS).error(e));
      }

      if (newestVersion != null && currentVersion.compareTo(newestVersion) < 0) {
         this.skipDisable = true;
         throw new IllegalStateException(String.format("Detected a newer version of ProtocolLib (%s) in plugin folder than the current (%s). Disabling.", newestVersion.getVersion(), currentVersion.getVersion()));
      }
   }

   private void registerCommand(String name, CommandExecutor executor) {
      try {
         if (executor == null) {
            throw new RuntimeException("Executor was NULL.");
         }

         PluginCommand command = this.getCommand(name);
         if (command == null) {
            throw new RuntimeException("plugin.yml might be corrupt.");
         }

         command.setExecutor(executor);
      } catch (RuntimeException e) {
         reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_REGISTER_COMMAND).messageParam(name, e.getMessage()).error(e));
      }

   }

   private void disablePlugin() {
      this.getServer().getPluginManager().disablePlugin(this);
   }

   private void createAsyncTask(Server server) {
      try {
         if (this.asyncPacketTask >= 0) {
            throw new IllegalStateException("Async task has already been created");
         }

         this.asyncPacketTask = server.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
               AsyncFilterManager manager = (AsyncFilterManager)ProtocolLibrary.protocolManager.getAsynchronousManager();
               manager.sendProcessedPackets(ProtocolLibrary.this.tickCounter++, true);
               if (!ProtocolLibrary.this.updateDisabled) {
                  ProtocolLibrary.this.checkUpdates();
               }

            }
         }, 1L, 1L);
      } catch (Throwable e) {
         if (this.asyncPacketTask == -1) {
            reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_CREATE_TIMEOUT_TASK).error(e));
         }
      }

   }

   private void checkUpdates() {
      long currentTime = System.currentTimeMillis() / 1000L;

      try {
         long updateTime = this.config.getAutoLastTime() + this.config.getAutoDelay();
         if (currentTime > updateTime) {
            if (this.config.isAutoDownload()) {
               this.commandProtocol.updateVersion(this.getServer().getConsoleSender());
            } else if (this.config.isAutoNotify()) {
               this.commandProtocol.checkVersion(this.getServer().getConsoleSender());
            } else {
               this.commandProtocol.updateFinished();
            }
         }
      } catch (Exception e) {
         reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_UPDATE_PLUGIN).error(e));
         this.updateDisabled = true;
      }

   }

   public void onDisable() {
      if (!this.skipDisable) {
         if (this.backgroundCompiler != null) {
            this.backgroundCompiler.shutdownAll();
            this.backgroundCompiler = null;
            BackgroundCompiler.setInstance((BackgroundCompiler)null);
         }

         if (this.asyncPacketTask >= 0) {
            this.getServer().getScheduler().cancelTask(this.asyncPacketTask);
            this.asyncPacketTask = -1;
         }

         if (this.redirectHandler != null) {
            this.logger.removeHandler(this.redirectHandler);
         }

         if (protocolManager != null) {
            protocolManager.close();
            if (this.unhookTask != null) {
               this.unhookTask.close();
            }

            protocolManager = null;
            this.statistisc = null;
            reporter = new BasicErrorReporter();
            if (this.updater == null || this.updater.getResult() != Updater.UpdateResult.SUCCESS) {
               CleanupStaticMembers cleanup = new CleanupStaticMembers(this.getClassLoader(), reporter);
               cleanup.resetAll();
            }

         }
      }
   }

   private Logger getLoggerSafely() {
      Logger log = null;

      try {
         log = this.getLogger();
      } catch (Throwable var3) {
      }

      if (log == null) {
         log = Logger.getLogger("Minecraft");
      }

      return log;
   }

   public static ErrorReporter getErrorReporter() {
      return reporter;
   }

   public static ProtocolManager getProtocolManager() {
      return protocolManager;
   }

   public Statistics getStatistics() {
      return this.statistisc;
   }
}
