package com.comphenix.protocol;

import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

abstract class CommandBase implements CommandExecutor {
   public static final ReportType REPORT_COMMAND_ERROR = new ReportType("Cannot execute command %s.");
   public static final ReportType REPORT_UNEXPECTED_COMMAND = new ReportType("Incorrect command assigned to %s.");
   public static final String PERMISSION_ADMIN = "protocol.admin";
   private String permission;
   private String name;
   private int minimumArgumentCount;
   protected ErrorReporter reporter;

   public CommandBase(ErrorReporter reporter, String permission, String name) {
      this(reporter, permission, name, 0);
   }

   public CommandBase(ErrorReporter reporter, String permission, String name, int minimumArgumentCount) {
      this.reporter = reporter;
      this.name = name;
      this.permission = permission;
      this.minimumArgumentCount = minimumArgumentCount;
   }

   public final boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      try {
         if (!command.getName().equalsIgnoreCase(this.name)) {
            this.reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_UNEXPECTED_COMMAND).messageParam(this));
            return false;
         } else if (this.permission != null && !sender.hasPermission(this.permission)) {
            sender.sendMessage(ChatColor.RED + "You haven't got permission to run this command.");
            return true;
         } else if (args != null && args.length >= this.minimumArgumentCount) {
            return this.handleCommand(sender, args);
         } else {
            sender.sendMessage(ChatColor.RED + "Insufficient commands. You need at least " + this.minimumArgumentCount);
            return false;
         }
      } catch (Exception e) {
         this.reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(REPORT_COMMAND_ERROR).error(e).messageParam(this.name).callerParam(sender, label, args));
         return true;
      }
   }

   protected Boolean parseBoolean(String[] args, String parameterName, int index) {
      if (index < args.length) {
         String arg = args[index];
         if (!arg.equalsIgnoreCase("true") && !arg.equalsIgnoreCase("on")) {
            if (arg.equalsIgnoreCase(parameterName)) {
               return true;
            } else {
               return !arg.equalsIgnoreCase("false") && !arg.equalsIgnoreCase("off") ? null : false;
            }
         } else {
            return true;
         }
      } else {
         return null;
      }
   }

   public String getPermission() {
      return this.permission;
   }

   public String getName() {
      return this.name;
   }

   protected ErrorReporter getReporter() {
      return this.reporter;
   }

   protected abstract boolean handleCommand(CommandSender var1, String[] var2);
}
