package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Commandtpall extends EssentialsCommand {
   public Commandtpall() {
      super("tpall");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         if (sender instanceof Player) {
            this.teleportAllPlayers(server, sender, this.ess.getUser(sender));
         } else {
            throw new NotEnoughArgumentsException();
         }
      } else {
         User player = this.getPlayer(server, args, 0);
         this.teleportAllPlayers(server, sender, player);
      }
   }

   private void teleportAllPlayers(Server server, CommandSender sender, User user) {
      sender.sendMessage(I18n._("teleportAll"));

      for(Player onlinePlayer : server.getOnlinePlayers()) {
         User player = this.ess.getUser(onlinePlayer);
         if (user != player && (user.getWorld() == player.getWorld() || !this.ess.getSettings().isWorldTeleportPermissions() || user.isAuthorized("essentials.worlds." + user.getWorld().getName()))) {
            try {
               player.getTeleport().now((Player)user, false, TeleportCause.COMMAND);
            } catch (Exception ex) {
               this.ess.showError(sender, ex, this.getName());
            }
         }
      }

   }
}
