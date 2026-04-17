package com.sleelin.pvptoggle.commands;

import com.sleelin.pvptoggle.PvPCommand;
import com.sleelin.pvptoggle.PvPLocalisation;
import com.sleelin.pvptoggle.PvPToggle;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Reset extends PvPCommand {
   public Reset(PvPToggle plugin, CommandSender sender, Command command, String label, String[] args) {
      super(plugin, sender, command, label, args);
   }

   public Reset(PvPToggle plugin, CommandSender sender) {
      super(plugin, sender);
   }

   protected boolean processCommand() {
      switch (this.args.length) {
         case 2:
            this.resetPlayer(this.sender, this.getPlayer(this.sender, this.args[1], true), "*");
            break;
         case 3:
            this.resetPlayer(this.sender, this.getPlayer(this.sender, this.args[1], true), this.isWorld(this.sender, this.args[2]));
            break;
         default:
            this.sendUsage(this.sender);
      }

      return true;
   }

   protected void sendUsage(CommandSender sender) {
      sender.sendMessage(this.helpHeader);
      ChatColor messagecolour = ChatColor.GOLD;
      if (this.plugin.permissionsCheck(sender, "pvptoggle.other.reset", true)) {
         sender.sendMessage(messagecolour + "/pvp reset [player] " + ChatColor.GRAY + "- Resets player's PvP status to default across all worlds");
         sender.sendMessage(messagecolour + "/pvp reset [player] [world] " + ChatColor.GRAY + "- Resets player's PvP status to default in specified world");
      }

   }

   private void resetPlayer(CommandSender sender, Player player, String worldname) {
      if (player != null && worldname != null) {
         if ((!this.plugin.permissionsCheck(sender, "pvptoggle.self.reset", true) || !sender.getName().equalsIgnoreCase(player.getName())) && !this.plugin.permissionsCheck(sender, "pvptoggle.other.reset", true) && !this.plugin.permissionsCheck(sender, "pvptoggle.admin", true)) {
            PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.NO_PERMISSION);
         } else {
            if (worldname.equalsIgnoreCase("*")) {
               for(org.bukkit.World world : this.plugin.getServer().getWorlds()) {
                  this.plugin.setPlayerStatus(player, world.getName(), this.plugin.getWorldDefault(world.getName()));
               }

               this.plugin.setLastAction(player, "toggle");
               PvPLocalisation.display(player, "", "", "", PvPLocalisation.Strings.PVP_RESET_PLAYER_GLOBAL);
               PvPLocalisation.display(sender, player.getName(), "", "", PvPLocalisation.Strings.PVP_RESET_PLAYER_GLOBAL_SENDER);
            } else {
               this.plugin.setPlayerStatus(player, worldname, this.plugin.getWorldDefault(worldname));
               this.plugin.setLastAction(player, "toggle");
               PvPLocalisation.display(player, "", worldname, "", PvPLocalisation.Strings.PVP_RESET_PLAYER);
               PvPLocalisation.display(sender, player.getName(), worldname, "", PvPLocalisation.Strings.PVP_RESET_PLAYER_SENDER);
            }

         }
      }
   }
}
