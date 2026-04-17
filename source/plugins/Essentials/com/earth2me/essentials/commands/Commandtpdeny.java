package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Server;

public class Commandtpdeny extends EssentialsCommand {
   public Commandtpdeny() {
      super("tpdeny");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      User player = user.getTeleportRequest();
      if (player == null) {
         throw new Exception(I18n._("noPendingRequest"));
      } else {
         user.sendMessage(I18n._("requestDenied"));
         player.sendMessage(I18n._("requestDeniedFrom", user.getDisplayName()));
         user.requestTeleport((User)null, false);
      }
   }
}
