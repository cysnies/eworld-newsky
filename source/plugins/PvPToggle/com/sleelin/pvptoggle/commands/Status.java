package com.sleelin.pvptoggle.commands;

import com.sleelin.pvptoggle.PvPCommand;
import com.sleelin.pvptoggle.PvPLocalisation;
import com.sleelin.pvptoggle.PvPToggle;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Status extends PvPCommand {
   public Status(PvPToggle plugin, CommandSender sender, Command command, String label, String[] args) {
      super(plugin, sender, command, label, args);
   }

   public Status(PvPToggle plugin, CommandSender sender) {
      super(plugin, sender);
   }

   protected boolean processCommand() {
      switch (this.args.length) {
         case 1:
            if (this.player != null) {
               this.getPlayerStatus(this.sender, this.player, this.player.getWorld().getName());
            } else {
               (new Global(this.plugin, this.sender, this.command, this.label, this.args)).getGlobalStatus(this.sender);
            }
            break;
         case 2:
            Player retrieved = this.getPlayer(this.sender, this.args[1], true);
            if (retrieved != null) {
               this.getPlayerStatus(this.sender, retrieved, retrieved.getWorld().getName());
            }
            break;
         case 3:
            this.getPlayerStatus(this.sender, this.getPlayer(this.sender, this.args[1], true), this.isWorld(this.sender, this.args[2]));
            break;
         default:
            this.sendUsage(this.sender);
      }

      return true;
   }

   protected void sendUsage(CommandSender sender) {
      sender.sendMessage(this.helpHeader);
      ChatColor messagecolour = ChatColor.GOLD;
      if (this.plugin.permissionsCheck(sender, "pvptoggle.self.status", true)) {
         sender.sendMessage(messagecolour + "/pvp status " + ChatColor.GRAY + "- Shows own PvP status in current world");
      }

      if (this.plugin.permissionsCheck(sender, "pvptoggle.other.status", true)) {
         sender.sendMessage(messagecolour + "/pvp status [player] " + ChatColor.GRAY + "- Shows player's PvP status in current world");
         sender.sendMessage(messagecolour + "/pvp status [player] [world] " + ChatColor.GRAY + "- Shows player's PvP status in specified world");
      }

   }

   private void getPlayerStatus(CommandSender sender, Player target, String world) {
      if ((!this.plugin.permissionsCheck(sender, "pvptoggle.self.status", true) || !sender.getName().equalsIgnoreCase(target.getName())) && !this.plugin.permissionsCheck(sender, "pvptoggle.other.status", true) && !this.plugin.permissionsCheck(sender, "pvptoggle.admin", true)) {
         PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.NO_PERMISSION);
      } else {
         if (target != null && world != null) {
            if (sender.getName().equalsIgnoreCase(target.getName())) {
               if (this.plugin.permissionsCheck(target, "pvptoggle.pvp.force", false)) {
                  PvPLocalisation.display(sender, "", world, PvPLocalisation.Strings.PVP_FORCED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_STATUS);
               } else if (this.plugin.permissionsCheck(target, "pvptoggle.pvp.deny", false)) {
                  PvPLocalisation.display(sender, "", world, PvPLocalisation.Strings.PVP_DENIED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_STATUS);
               } else if (this.plugin.checkPlayerStatus(target, target.getWorld().getName())) {
                  PvPLocalisation.display(sender, "", world, PvPLocalisation.Strings.PVP_ENABLED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_STATUS);
               } else {
                  PvPLocalisation.display(sender, "", world, PvPLocalisation.Strings.PVP_DISABLED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_STATUS);
               }
            } else if (this.plugin.permissionsCheck(target, "pvptoggle.pvp.force", false)) {
               PvPLocalisation.display(sender, target.getName(), world, PvPLocalisation.Strings.PVP_FORCED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_OTHER_STATUS);
            } else if (this.plugin.permissionsCheck(target, "pvptoggle.pvp.deny", false)) {
               PvPLocalisation.display(sender, target.getName(), world, PvPLocalisation.Strings.PVP_DENIED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_OTHER_STATUS);
            } else if (this.plugin.checkPlayerStatus(target, world)) {
               PvPLocalisation.display(sender, target.getName(), world, PvPLocalisation.Strings.PVP_ENABLED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_OTHER_STATUS);
            } else {
               PvPLocalisation.display(sender, target.getName(), world, PvPLocalisation.Strings.PVP_DISABLED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_OTHER_STATUS);
            }
         }

      }
   }
}
