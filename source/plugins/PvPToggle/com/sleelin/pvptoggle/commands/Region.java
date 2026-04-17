package com.sleelin.pvptoggle.commands;

import com.sleelin.pvptoggle.PvPCommand;
import com.sleelin.pvptoggle.PvPLocalisation;
import com.sleelin.pvptoggle.PvPToggle;
import com.sleelin.pvptoggle.handlers.RegionHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Region extends PvPCommand {
   public Region(PvPToggle plugin, CommandSender sender, Command command, String label, String[] args) {
      super(plugin, sender, command, label, args);
   }

   public Region(PvPToggle plugin, CommandSender sender) {
      super(plugin, sender);
   }

   protected boolean processCommand() {
      if (this.args.length > 2) {
         if (!this.args[1].equalsIgnoreCase("add") && !this.args[1].equalsIgnoreCase("a")) {
            if (this.args[1].equalsIgnoreCase("remove") || this.args[1].equalsIgnoreCase("r")) {
               if (this.args.length > 3) {
                  String world = this.isWorld(this.sender, this.args[3]);
                  if (world != null) {
                     this.removeRegion(this.sender, world, this.args[2]);
                  }
               } else if (this.sender instanceof Player) {
                  this.removeRegion(this.sender, this.player.getWorld().getName(), this.args[2]);
               } else {
                  this.sendUsage(this.sender);
               }
            }
         } else if (this.args.length > 3) {
            String world = this.isWorld(this.sender, this.args[3]);
            if (world != null) {
               this.addRegion(this.sender, world, this.args[2]);
            }
         } else if (this.sender instanceof Player) {
            this.addRegion(this.sender, this.player.getWorld().getName(), this.args[2]);
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
      if (this.plugin.permissionsCheck(sender, "pvptoggle.region.add", true)) {
         if (sender instanceof Player) {
            sender.sendMessage(messagecolour + "/pvp region add [region] " + ChatColor.GRAY + "- Adds a region to the current world");
         }

         sender.sendMessage(messagecolour + "/pvp region add [region] [world] " + ChatColor.GRAY + "- Adds a region to the specified world");
      }

      if (this.plugin.permissionsCheck(sender, "pvptoggle.region.remove", true)) {
         if (sender instanceof Player) {
            sender.sendMessage(messagecolour + "/pvp region remove [region] " + ChatColor.GRAY + "- Removes a region from the current world");
         }

         sender.sendMessage(messagecolour + "/pvp region remove [region] [world] " + ChatColor.GRAY + "- Removes a region from the specified world");
      }

   }

   private void addRegion(CommandSender sender, String world, String region) {
      if (this.plugin.permissionsCheck(sender, "pvptoggle.regions.add", true)) {
         RegionHandler.addRegion(sender, world, this.args[2]);
         PvPLocalisation.display(sender, this.args[2], world, "", PvPLocalisation.Strings.WORLDGUARD_REGION_ADDED);
      } else {
         PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.NO_PERMISSION);
      }

   }

   private void removeRegion(CommandSender sender, String world, String region) {
      if (this.plugin.permissionsCheck(sender, "pvptoggle.regions.add", true)) {
         RegionHandler.removeRegion(sender, world, this.args[2]);
         PvPLocalisation.display(sender, this.args[2], world, "", PvPLocalisation.Strings.WORLDGUARD_REGION_REMOVED);
      } else {
         PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.NO_PERMISSION);
      }

   }
}
