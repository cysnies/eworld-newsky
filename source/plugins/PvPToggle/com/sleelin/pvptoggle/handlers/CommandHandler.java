package com.sleelin.pvptoggle.handlers;

import com.sleelin.pvptoggle.PvPToggle;
import com.sleelin.pvptoggle.commands.Global;
import com.sleelin.pvptoggle.commands.Help;
import com.sleelin.pvptoggle.commands.Region;
import com.sleelin.pvptoggle.commands.Reset;
import com.sleelin.pvptoggle.commands.Status;
import com.sleelin.pvptoggle.commands.Toggle;
import com.sleelin.pvptoggle.commands.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements CommandExecutor {
   private final PvPToggle plugin;

   public CommandHandler(PvPToggle instance) {
      this.plugin = instance;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      Player player = null;
      if (sender instanceof Player) {
         player = (Player)sender;
      }

      if (args.length == 0) {
         if (player != null) {
            (new Toggle(this.plugin, sender, command, label, args)).exec();
         } else {
            (new Global(this.plugin, sender, command, label, args)).exec();
         }
      } else if (!args[0].equalsIgnoreCase("on") && !args[0].equalsIgnoreCase("enable") && !args[0].equalsIgnoreCase("off") && !args[0].equalsIgnoreCase("disable")) {
         if (!args[0].equalsIgnoreCase("status") && !args[0].equalsIgnoreCase("s")) {
            if (!args[0].equalsIgnoreCase("reset") && !args[0].equalsIgnoreCase("r")) {
               if (!args[0].startsWith("w:") && !args[0].equalsIgnoreCase("world") && !args[0].equalsIgnoreCase("w")) {
                  if (!args[0].equalsIgnoreCase("global") && !args[0].equalsIgnoreCase("g")) {
                     if (args[0].equalsIgnoreCase("region")) {
                        (new Region(this.plugin, sender, command, label, args)).exec();
                     } else if (args[0].equalsIgnoreCase("help")) {
                        (new Help(this.plugin, sender, command, label, args)).exec();
                     } else {
                        new Help(this.plugin, sender);
                     }
                  } else {
                     (new Global(this.plugin, sender, command, label, args)).exec();
                  }
               } else {
                  (new World(this.plugin, sender, command, label, args)).exec();
               }
            } else {
               (new Reset(this.plugin, sender, command, label, args)).exec();
            }
         } else {
            (new Status(this.plugin, sender, command, label, args)).exec();
         }
      } else {
         (new Toggle(this.plugin, sender, command, label, args)).exec();
      }

      return true;
   }
}
