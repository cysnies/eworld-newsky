package com.sleelin.pvptoggle.commands;

import com.sleelin.pvptoggle.PvPCommand;
import com.sleelin.pvptoggle.PvPLocalisation;
import com.sleelin.pvptoggle.PvPToggle;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Global extends PvPCommand {
   public Global(PvPToggle plugin, CommandSender sender, Command command, String label, String[] args) {
      super(plugin, sender, command, label, args);
   }

   public Global(PvPToggle plugin, CommandSender sender) {
      super(plugin, sender);
   }

   protected boolean processCommand() {
      if (this.args.length == 2) {
         if (this.args[1].equalsIgnoreCase("status")) {
            this.getGlobalStatus(this.sender);
         } else if (this.args[1].equalsIgnoreCase("reset")) {
            this.resetGlobal(this.sender);
         } else if (checkNewValue(this.args[1]) != null) {
            this.toggleGlobal(this.sender, checkNewValue(this.args[1]));
         } else {
            this.sendUsage(this.sender);
         }
      } else {
         this.getGlobalStatus(this.sender);
      }

      return true;
   }

   protected void sendUsage(CommandSender sender) {
      sender.sendMessage(this.helpHeader);
      ChatColor messagecolour = ChatColor.GOLD;
      if (this.plugin.permissionsCheck(sender, "pvptoggle.global.toggle", true)) {
         sender.sendMessage(messagecolour + "/pvp global on " + ChatColor.GRAY + "- Enables global PvP");
         sender.sendMessage(messagecolour + "/pvp global off " + ChatColor.GRAY + "- Disables global PvP");
      }

      if (this.plugin.permissionsCheck(sender, "pvptoggle.global.reset", true)) {
         sender.sendMessage(messagecolour + "/pvp global reset " + ChatColor.GRAY + "- Resets PvP status of all players in all worlds to login default");
      }

      if (this.plugin.permissionsCheck(sender, "pvptoggle.global.reset", true)) {
         sender.sendMessage(messagecolour + "/pvp global " + ChatColor.GRAY + "- Shows global PvP status");
         sender.sendMessage(messagecolour + "/pvp global status " + ChatColor.GRAY + "- Shows global PvP status");
      }

   }

   protected void getGlobalStatus(CommandSender sender) {
      if (this.plugin.permissionsCheck(sender, "pvptoggle.global.status", true)) {
         if ((Boolean)this.plugin.getGlobalSetting("enabled")) {
            PvPLocalisation.display(sender, "", "", PvPLocalisation.Strings.PVP_ENABLED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_GLOBAL_STATUS);
         } else {
            PvPLocalisation.display(sender, "", "", PvPLocalisation.Strings.PVP_DISABLED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_GLOBAL_STATUS);
         }
      } else {
         PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.NO_PERMISSION);
      }

   }

   private void toggleGlobal(CommandSender sender, boolean newval) {
      if (!this.plugin.permissionsCheck(sender, "pvptoggle.global.toggle", true) && !this.plugin.permissionsCheck(sender, "pvptoggle.admin", true)) {
         PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.NO_PERMISSION);
      } else {
         this.toggleGlobalStatus(newval);
         if (newval) {
            PvPLocalisation.display(sender, "", "", PvPLocalisation.Strings.PVP_ENABLED.toString(), PvPLocalisation.Strings.PVP_GLOBAL_TOGGLE_SENDER);
         } else {
            PvPLocalisation.display(sender, "", "", PvPLocalisation.Strings.PVP_DISABLED.toString(), PvPLocalisation.Strings.PVP_GLOBAL_TOGGLE_SENDER);
         }

      }
   }

   private void resetGlobal(CommandSender sender) {
      if (!this.plugin.permissionsCheck(sender, "pvptoggle.global.reset", true) && !this.plugin.permissionsCheck(sender, "pvptoggle.admin", true)) {
         PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.NO_PERMISSION);
      } else {
         Player[] players = this.plugin.getServer().getOnlinePlayers();

         for(Player p : players) {
            for(org.bukkit.World world : this.plugin.getServer().getWorlds()) {
               this.plugin.setPlayerStatus(p, world.getName(), this.plugin.getWorldDefault(world.getName()));
            }

            this.plugin.setLastAction(p, "toggle");
            PvPLocalisation.display(p, "", "", "", PvPLocalisation.Strings.PVP_RESET_GLOBAL);
         }

         PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.PVP_RESET_GLOBAL_SENDER);
      }
   }
}
