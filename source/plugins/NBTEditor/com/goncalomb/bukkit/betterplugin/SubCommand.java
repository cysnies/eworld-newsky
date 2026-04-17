package com.goncalomb.bukkit.betterplugin;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

class SubCommand {
   protected BetterCommand _base;
   private Method _method = null;
   private BetterCommandType _type;
   private String _usage;
   private int _minArgs;
   private int _maxArgs;
   private LinkedHashMap _subCommands;

   protected SubCommand() {
      this._type = BetterCommandType.DEFAULT;
      this._subCommands = new LinkedHashMap();
   }

   private SubCommand(BetterCommand base) {
      this._type = BetterCommandType.DEFAULT;
      this._subCommands = new LinkedHashMap();
      this._base = base;
   }

   protected void addSubCommand(Command config, Method method) {
      String[] argsArray = config.args().trim().split("\\s+");
      argsArray = argsArray.length == 1 && argsArray[0].isEmpty() ? new String[0] : argsArray;
      this.addSubCommand(argsArray, 0, config, method);
   }

   private void addSubCommand(String[] args, int argsIndex, Command config, Method method) {
      if (args.length == argsIndex) {
         this._method = method;
         this._type = config.type();
         this._usage = config.usage();
         this._minArgs = config.minargs() >= 0 ? config.minargs() : 0;
         this._maxArgs = config.maxargs() >= 0 ? config.maxargs() : 0;
         if (this._minArgs > this._maxArgs) {
            this._maxArgs = this._minArgs;
         }

      } else {
         SubCommand subCommand = (SubCommand)this._subCommands.get(args[argsIndex]);
         if (subCommand == null) {
            subCommand = new SubCommand(this._base);
            this._subCommands.put(args[argsIndex], subCommand);
         }

         subCommand.addSubCommand(args, argsIndex + 1, config, method);
      }
   }

   void execute(CommandSender sender, String label, String[] args) {
      this.execute(sender, label, args, 0);
   }

   void execute(CommandSender sender, String label, String[] args, int argsIndex) {
      if (argsIndex < args.length) {
         SubCommand subCommand = (SubCommand)this._subCommands.get(args[argsIndex]);
         if (subCommand != null) {
            subCommand.execute(sender, label, args, argsIndex + 1);
            return;
         }
      }

      if (this._method != null) {
         if (!this._type.isValidSender(sender)) {
            sender.sendMessage(this._type.getInvalidSenderMessage());
            return;
         }

         int argsLeft = args.length - argsIndex;
         if (argsLeft >= this._minArgs && argsLeft <= this._maxArgs && this._base.invokeMethod(this._method, sender, (String[])Arrays.copyOfRange(args, argsIndex, args.length))) {
            return;
         }
      }

      String prefix = "/" + label + " " + (argsIndex > 0 ? StringUtils.join(args, ' ', 0, argsIndex) + " " : "");
      if (this._method != null) {
         sender.sendMessage(ChatColor.RESET + prefix + this._usage);
      }

      sendAllSubCommands(sender, this, prefix);
   }

   private static void sendAllSubCommands(CommandSender sender, SubCommand command, String prefix) {
      for(Map.Entry subCommandEntry : command._subCommands.entrySet()) {
         SubCommand subCommand = (SubCommand)subCommandEntry.getValue();
         if (subCommand._type.isValidSender(sender)) {
            String newPrefix = prefix + (String)subCommandEntry.getKey() + " ";
            if (subCommand._method != null) {
               sender.sendMessage(ChatColor.GRAY + newPrefix + subCommand._usage);
            }

            sendAllSubCommands(sender, subCommand, newPrefix);
         }
      }

   }

   List tabComplete(CommandSender sender, String[] args) {
      SubCommand subCommand = this.getSubCommand(args, 0);
      if (subCommand != null) {
         ArrayList<String> allowedCommands = new ArrayList();

         for(Map.Entry command : subCommand._subCommands.entrySet()) {
            String name = (String)command.getKey();
            if (name.startsWith(args[args.length - 1]) && ((SubCommand)command.getValue())._type.isValidSender(sender)) {
               allowedCommands.add(name);
            }
         }

         return allowedCommands;
      } else {
         return null;
      }
   }

   SubCommand getSubCommand(String[] args, int argsIndex) {
      if (argsIndex == args.length - 1) {
         return this;
      } else {
         SubCommand subCommand = (SubCommand)this._subCommands.get(args[argsIndex]);
         return subCommand != null ? subCommand.getSubCommand(args, argsIndex + 1) : null;
      }
   }

   @Retention(RetentionPolicy.RUNTIME)
   protected @interface Command {
      String args();

      BetterCommandType type() default BetterCommandType.DEFAULT;

      String usage() default "";

      int minargs() default 0;

      int maxargs() default 0;
   }
}
