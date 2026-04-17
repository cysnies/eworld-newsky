package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Commandtphere extends EssentialsCommand {
   public Commandtphere() {
      super("tphere");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      User player = this.getPlayer(server, args, 0);
      if (!player.isTeleportEnabled()) {
         throw new Exception(I18n._("teleportDisabled", player.getDisplayName()));
      } else if (user.getWorld() != player.getWorld() && this.ess.getSettings().isWorldTeleportPermissions() && !user.isAuthorized("essentials.worlds." + user.getWorld().getName())) {
         throw new Exception(I18n._("noPerm", "essentials.worlds." + user.getWorld().getName()));
      } else {
         user.getTeleport().teleportToMe(player, new Trade(this.getName(), this.ess), TeleportCause.COMMAND);
         user.sendMessage(I18n._("teleporting"));
         player.sendMessage(I18n._("teleporting"));
         throw new NoChargeException();
      }
   }
}
