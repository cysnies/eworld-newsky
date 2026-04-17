package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Server;

public class Commandtpa extends EssentialsCommand {
   public Commandtpa() {
      super("tpa");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         User player = this.getPlayer(server, args, 0);
         if (!player.isTeleportEnabled()) {
            throw new Exception(I18n._("teleportDisabled", player.getDisplayName()));
         } else if (user.getWorld() != player.getWorld() && this.ess.getSettings().isWorldTeleportPermissions() && !user.isAuthorized("essentials.worlds." + player.getWorld().getName())) {
            throw new Exception(I18n._("noPerm", "essentials.worlds." + player.getWorld().getName()));
         } else {
            if (!player.isIgnoredPlayer(user)) {
               player.requestTeleport(user, false);
               player.sendMessage(I18n._("teleportRequest", user.getDisplayName()));
               player.sendMessage(I18n._("typeTpaccept"));
               player.sendMessage(I18n._("typeTpdeny"));
               if (this.ess.getSettings().getTpaAcceptCancellation() != 0L) {
                  player.sendMessage(I18n._("teleportRequestTimeoutInfo", this.ess.getSettings().getTpaAcceptCancellation()));
               }
            }

            user.sendMessage(I18n._("requestSent", player.getDisplayName()));
         }
      }
   }
}
