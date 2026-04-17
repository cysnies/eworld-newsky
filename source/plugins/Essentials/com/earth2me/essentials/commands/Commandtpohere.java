package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Commandtpohere extends EssentialsCommand {
   public Commandtpohere() {
      super("tpohere");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         User player = this.getPlayer(server, args, 0, true, false);
         if (user.getWorld() != player.getWorld() && this.ess.getSettings().isWorldTeleportPermissions() && !user.isAuthorized("essentials.worlds." + user.getWorld().getName())) {
            throw new Exception(I18n._("noPerm", "essentials.worlds." + user.getWorld().getName()));
         } else if (player.isHidden() && !user.isAuthorized("essentials.teleport.hidden")) {
            throw new NoSuchFieldException(I18n._("playerNotFound"));
         } else {
            player.getTeleport().now((Player)user, false, TeleportCause.COMMAND);
            user.sendMessage(I18n._("teleporting"));
         }
      }
   }
}
