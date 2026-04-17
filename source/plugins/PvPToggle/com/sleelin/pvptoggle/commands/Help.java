package com.sleelin.pvptoggle.commands;

import com.sleelin.pvptoggle.PvPCommand;
import com.sleelin.pvptoggle.PvPToggle;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class Help extends PvPCommand {
   public Help(PvPToggle plugin, CommandSender sender, Command command, String label, String[] args) {
      super(plugin, sender, command, label, args);
   }

   public Help(PvPToggle plugin, CommandSender sender) {
      super(plugin, sender);
   }

   protected boolean processCommand() {
      if (this.args.length == 2) {
         if (this.args[1].equalsIgnoreCase("toggle")) {
            new Toggle(this.plugin, this.sender);
         } else if (this.args[1].equalsIgnoreCase("status")) {
            new Status(this.plugin, this.sender);
         } else if (this.args[1].equalsIgnoreCase("reset")) {
            new Reset(this.plugin, this.sender);
         } else if (this.args[1].equalsIgnoreCase("world")) {
            new World(this.plugin, this.sender);
         } else if (this.args[1].equalsIgnoreCase("global")) {
            new Global(this.plugin, this.sender);
         } else {
            this.sendUsage(this.sender);
         }
      } else {
         this.sendUsage(this.sender);
      }

      return true;
   }

   protected void sendUsage(CommandSender sender) {
      sender.sendMessage(this.helpHeader);
      ChatColor messagecolour = ChatColor.GOLD;
      sender.sendMessage(messagecolour + "/pvp help toggle " + ChatColor.GRAY + "- Show usage information for toggling");
      sender.sendMessage(messagecolour + "/pvp help status " + ChatColor.GRAY + "- Show usage information for checking status");
      sender.sendMessage(messagecolour + "/pvp help reset " + ChatColor.GRAY + "- Show usage information for reset command");
      sender.sendMessage(messagecolour + "/pvp help world " + ChatColor.GRAY + "- Show usage information for world command");
      sender.sendMessage(messagecolour + "/pvp help global " + ChatColor.GRAY + "- Show usage information for global command");
   }
}
