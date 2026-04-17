package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Server;

public class Commandtpahere extends EssentialsCommand {
   public Commandtpahere() {
      super("tpahere");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         User player = this.getPlayer(server, args, 0);
         if (!player.isTeleportEnabled()) {
            throw new Exception(I18n._("teleportDisabled", player.getDisplayName()));
         } else if (user.getWorld() != player.getWorld() && this.ess.getSettings().isWorldTeleportPermissions() && !user.isAuthorized("essentials.worlds." + user.getWorld().getName())) {
            throw new Exception(I18n._("noPerm", "essentials.worlds." + user.getWorld().getName()));
         } else {
            player.requestTeleport(user, true);
            player.sendMessage(I18n._("teleportHereRequest", user.getDisplayName()));
            player.sendMessage(I18n._("typeTpaccept"));
            player.sendMessage(I18n._("typeTpdeny"));
            if (this.ess.getSettings().getTpaAcceptCancellation() != 0L) {
               player.sendMessage(I18n._("teleportRequestTimeoutInfo", this.ess.getSettings().getTpaAcceptCancellation()));
            }

            user.sendMessage(I18n._("requestSent", player.getDisplayName()));
         }
      }
   }
}
