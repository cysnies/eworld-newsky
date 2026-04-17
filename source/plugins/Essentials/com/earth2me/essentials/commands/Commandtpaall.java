package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandtpaall extends EssentialsCommand {
   public Commandtpaall() {
      super("tpaall");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         if (sender instanceof Player) {
            this.teleportAAllPlayers(server, sender, this.ess.getUser(sender));
         } else {
            throw new NotEnoughArgumentsException();
         }
      } else {
         User player = this.getPlayer(server, args, 0);
         this.teleportAAllPlayers(server, sender, player);
      }
   }

   private void teleportAAllPlayers(Server server, CommandSender sender, User user) {
      sender.sendMessage(I18n._("teleportAAll"));

      for(Player onlinePlayer : server.getOnlinePlayers()) {
         User player = this.ess.getUser(onlinePlayer);
         if (user != player && player.isTeleportEnabled() && (user.getWorld() == player.getWorld() || !this.ess.getSettings().isWorldTeleportPermissions() || user.isAuthorized("essentials.worlds." + user.getWorld().getName()))) {
            try {
               player.requestTeleport(user, true);
               player.sendMessage(I18n._("teleportHereRequest", user.getDisplayName()));
               player.sendMessage(I18n._("typeTpaccept"));
               if (this.ess.getSettings().getTpaAcceptCancellation() != 0L) {
                  player.sendMessage(I18n._("teleportRequestTimeoutInfo", this.ess.getSettings().getTpaAcceptCancellation()));
               }
            } catch (Exception ex) {
               this.ess.showError(sender, ex, this.getName());
            }
         }
      }

   }
}
