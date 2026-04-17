package com.sleelin.pvptoggle.commands;

import com.sleelin.pvptoggle.PvPCommand;
import com.sleelin.pvptoggle.PvPLocalisation;
import com.sleelin.pvptoggle.PvPToggle;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class World extends PvPCommand {
   public World(PvPToggle plugin, CommandSender sender, Command command, String label, String[] args) {
      super(plugin, sender, command, label, args);
   }

   public World(PvPToggle plugin, CommandSender sender) {
      super(plugin, sender);
   }

   protected boolean processCommand() {
      String worldname = null;
      String action = this.args[this.args.length - 1];
      if (this.args[0].startsWith("w:")) {
         worldname = this.isWorld(this.sender, this.args[0].substring(2));
      } else if (this.args.length > 1) {
         worldname = this.isWorld(this.sender, this.args[1]);
      } else {
         this.sendUsage(this.sender);
      }

      if (worldname != null) {
         if (!action.equalsIgnoreCase("status") && !worldname.equalsIgnoreCase(action) && (!worldname.equalsIgnoreCase(action.substring(2)) || !action.startsWith("w:"))) {
            if (action.equalsIgnoreCase("reset")) {
               this.resetWorld(this.sender, worldname, this.plugin.getWorldDefault(worldname));
            } else if (!action.equalsIgnoreCase("on") && !action.equalsIgnoreCase("off")) {
               this.sendUsage(this.sender);
            } else {
               this.toggleWorld(this.sender, worldname, checkNewValue(action));
            }
         } else {
            this.getWorldStatus(this.sender, worldname);
         }
      }

      return true;
   }

   public void sendUsage(CommandSender sender) {
      sender.sendMessage(this.helpHeader);
      ChatColor messagecolour = ChatColor.GOLD;
      if (this.plugin.permissionsCheck(sender, "pvptoggle.world.toggle", true)) {
         sender.sendMessage(messagecolour + "/pvp w:[world] on " + ChatColor.GRAY + "- Sets PvP status of specified world to on");
         sender.sendMessage(messagecolour + "/pvp w:[world] off " + ChatColor.GRAY + "- Sets PvP status of specified world to off");
      }

      if (this.plugin.permissionsCheck(sender, "pvptoggle.world.reset", true)) {
         sender.sendMessage(messagecolour + "/pvp w:[world] reset " + ChatColor.GRAY + "- Resets PvP status of all players in specified world");
      }

      if (this.plugin.permissionsCheck(sender, "pvptoggle.world.status", true)) {
         sender.sendMessage(messagecolour + "/pvp w:[world] " + ChatColor.GRAY + "- Shows PvP status of specified world");
         sender.sendMessage(messagecolour + "/pvp w:[world] status " + ChatColor.GRAY + "- Shows PvP status of specified world");
      }

   }

   private void getWorldStatus(CommandSender sender, String worldname) {
      if (this.plugin.permissionsCheck(sender, "pvptoggle.world.status", true)) {
         if (worldname != null) {
            if (this.plugin.getWorldStatus(worldname)) {
               PvPLocalisation.display(sender, "", worldname, PvPLocalisation.Strings.PVP_ENABLED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_WORLD_STATUS);
            } else {
               PvPLocalisation.display(sender, "", worldname, PvPLocalisation.Strings.PVP_DISABLED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_WORLD_STATUS);
            }
         }
      } else {
         PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.NO_PERMISSION);
      }

   }

   private void toggleWorld(CommandSender sender, String targetworld, boolean newval) {
      if (!this.plugin.permissionsCheck(sender, "pvptoggle.world.toggle", true) && !this.plugin.permissionsCheck(sender, "pvptoggle.admin", true)) {
         PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.NO_PERMISSION);
      } else {
         if (targetworld != null) {
            this.setWorldStatus(targetworld, newval);
            if (newval) {
               PvPLocalisation.display(sender, "", targetworld, PvPLocalisation.Strings.PVP_ENABLED.toString(), PvPLocalisation.Strings.PVP_WORLD_TOGGLE_SENDER);
            } else {
               PvPLocalisation.display(sender, "", targetworld, PvPLocalisation.Strings.PVP_DISABLED.toString(), PvPLocalisation.Strings.PVP_WORLD_TOGGLE_SENDER);
            }
         }

      }
   }

   private void resetWorld(CommandSender sender, String worldname, boolean newval) {
      if (!this.plugin.permissionsCheck(sender, "pvptoggle.world.reset", true) && !this.plugin.permissionsCheck(sender, "pvptoggle.admin", true)) {
         PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.NO_PERMISSION);
      } else {
         Player[] players = this.plugin.getServer().getOnlinePlayers();

         for(Player p : players) {
            this.plugin.setPlayerStatus(p, worldname, newval);
            this.plugin.setLastAction(p, "toggle");
            if (newval) {
               PvPLocalisation.display(p, "", worldname, PvPLocalisation.Strings.PVP_ENABLED.toString(), PvPLocalisation.Strings.PVP_RESET_WORLD);
            } else {
               PvPLocalisation.display(p, "", worldname, PvPLocalisation.Strings.PVP_DISABLED.toString(), PvPLocalisation.Strings.PVP_RESET_WORLD);
            }
         }

         if (newval) {
            PvPLocalisation.display(sender, "", worldname, PvPLocalisation.Strings.PVP_ENABLED.toString(), PvPLocalisation.Strings.PVP_RESET_WORLD_SENDER);
         } else {
            PvPLocalisation.display(sender, "", worldname, PvPLocalisation.Strings.PVP_DISABLED.toString(), PvPLocalisation.Strings.PVP_RESET_WORLD_SENDER);
         }

      }
   }
}
