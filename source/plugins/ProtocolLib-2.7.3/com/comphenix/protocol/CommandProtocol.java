package com.comphenix.protocol;

import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.metrics.Updater;
import com.comphenix.protocol.timing.TimedListenerManager;
import com.comphenix.protocol.timing.TimingReportGenerator;
import com.comphenix.protocol.utility.WrappedScheduler;
import java.io.File;
import java.io.IOException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

class CommandProtocol extends CommandBase {
   public static final String NAME = "protocol";
   public static final ReportType REPORT_HTTP_ERROR = new ReportType("Http error: %s");
   public static final ReportType REPORT_CANNOT_CHECK_FOR_UPDATES = new ReportType("Cannot check updates for ProtocolLib.");
   public static final ReportType REPORT_CANNOT_UPDATE_PLUGIN = new ReportType("Cannot update ProtocolLib.");
   private Plugin plugin;
   private Updater updater;
   private ProtocolConfig config;

   public CommandProtocol(ErrorReporter reporter, Plugin plugin, Updater updater, ProtocolConfig config) {
      super(reporter, "protocol.admin", "protocol", 1);
      this.plugin = plugin;
      this.updater = updater;
      this.config = config;
   }

   protected boolean handleCommand(CommandSender sender, String[] args) {
      String subCommand = args[0];
      if (!subCommand.equalsIgnoreCase("config") && !subCommand.equalsIgnoreCase("reload")) {
         if (subCommand.equalsIgnoreCase("check")) {
            this.checkVersion(sender);
         } else if (subCommand.equalsIgnoreCase("update")) {
            this.updateVersion(sender);
         } else {
            if (!subCommand.equalsIgnoreCase("timings")) {
               return false;
            }

            this.toggleTimings(sender, args);
         }
      } else {
         this.reloadConfiguration(sender);
      }

      return true;
   }

   public void checkVersion(final CommandSender sender) {
      WrappedScheduler.runAsynchronouslyOnce(this.plugin, new Runnable() {
         public void run() {
            try {
               Updater.UpdateResult result = CommandProtocol.this.updater.update(Updater.UpdateType.NO_DOWNLOAD, true);
               sender.sendMessage(ChatColor.BLUE + "[ProtocolLib] " + result.toString());
            } catch (Exception e) {
               if (CommandProtocol.this.isHttpError(e)) {
                  CommandProtocol.this.getReporter().reportWarning(CommandProtocol.this, (Report.ReportBuilder)Report.newBuilder(CommandProtocol.REPORT_HTTP_ERROR).messageParam(e.getCause().getMessage()));
               } else {
                  CommandProtocol.this.getReporter().reportDetailed(CommandProtocol.this, (Report.ReportBuilder)Report.newBuilder(CommandProtocol.REPORT_CANNOT_CHECK_FOR_UPDATES).error(e).callerParam(sender));
               }
            }

         }
      }, 0L);
      this.updateFinished();
   }

   public void updateVersion(final CommandSender sender) {
      WrappedScheduler.runAsynchronouslyOnce(this.plugin, new Runnable() {
         public void run() {
            try {
               Updater.UpdateResult result = CommandProtocol.this.updater.update(Updater.UpdateType.DEFAULT, true);
               sender.sendMessage(ChatColor.BLUE + "[ProtocolLib] " + result.toString());
            } catch (Exception e) {
               if (CommandProtocol.this.isHttpError(e)) {
                  CommandProtocol.this.getReporter().reportWarning(CommandProtocol.this, (Report.ReportBuilder)Report.newBuilder(CommandProtocol.REPORT_HTTP_ERROR).messageParam(e.getCause().getMessage()));
               } else {
                  CommandProtocol.this.getReporter().reportDetailed(CommandProtocol.this, (Report.ReportBuilder)Report.newBuilder(CommandProtocol.REPORT_CANNOT_UPDATE_PLUGIN).error(e).callerParam(sender));
               }
            }

         }
      }, 0L);
      this.updateFinished();
   }

   private void toggleTimings(CommandSender sender, String[] args) {
      TimedListenerManager manager = TimedListenerManager.getInstance();
      boolean state = !manager.isTiming();
      if (args.length == 2) {
         Boolean parsed = this.parseBoolean(args, "start", 2);
         if (parsed == null) {
            sender.sendMessage(ChatColor.RED + "Specify a state: ON or OFF.");
            return;
         }

         state = parsed;
      } else if (args.length > 2) {
         sender.sendMessage(ChatColor.RED + "Too many parameters.");
         return;
      }

      if (state) {
         if (manager.startTiming()) {
            sender.sendMessage(ChatColor.GOLD + "Started timing packet listeners.");
         } else {
            sender.sendMessage(ChatColor.RED + "Packet timing already started.");
         }
      } else if (manager.stopTiming()) {
         this.saveTimings(manager);
         sender.sendMessage(ChatColor.GOLD + "Stopped and saved result in plugin folder.");
      } else {
         sender.sendMessage(ChatColor.RED + "Packet timing already stopped.");
      }

   }

   private void saveTimings(TimedListenerManager manager) {
      try {
         File destination = new File(this.plugin.getDataFolder(), "Timings - " + System.currentTimeMillis() + ".txt");
         TimingReportGenerator generator = new TimingReportGenerator();
         generator.saveTo(destination, manager);
         manager.clear();
      } catch (IOException e) {
         this.reporter.reportMinimal(this.plugin, "saveTimings()", e);
      }

   }

   private boolean isHttpError(Exception e) {
      Throwable cause = e.getCause();
      return cause instanceof IOException ? cause.getMessage().contains("HTTP response") : false;
   }

   public void updateFinished() {
      long currentTime = System.currentTimeMillis() / 1000L;
      this.config.setAutoLastTime(currentTime);
      this.config.saveAll();
   }

   public void reloadConfiguration(CommandSender sender) {
      this.plugin.reloadConfig();
      sender.sendMessage(ChatColor.BLUE + "Reloaded configuration!");
   }
}
