package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandtptoggle extends EssentialsCommand {
   public Commandtptoggle() {
      super("tptoggle");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         this.toggleOtherPlayers(server, sender, args);
      }
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length > 0 && args[0].trim().length() > 2 && user.isAuthorized("essentials.tptoggle.others")) {
         this.toggleOtherPlayers(server, user, args);
      } else {
         user.sendMessage(user.toggleTeleportEnabled() ? I18n._("teleportationEnabled") : I18n._("teleportationDisabled"));
      }
   }

   private void toggleOtherPlayers(Server server, CommandSender sender, String[] args) throws NotEnoughArgumentsException {
      boolean skipHidden = sender instanceof Player && !this.ess.getUser(sender).isAuthorized("essentials.teleport.hidden");
      boolean foundUser = false;

      for(Player matchPlayer : server.matchPlayer(args[0])) {
         User player = this.ess.getUser(matchPlayer);
         if (!skipHidden || !player.isHidden()) {
            foundUser = true;
            if (args.length > 1) {
               if (!args[1].contains("on") && !args[1].contains("ena") && !args[1].equalsIgnoreCase("1")) {
                  player.setTeleportEnabled(false);
               } else {
                  player.setTeleportEnabled(true);
               }
            } else {
               player.toggleTeleportEnabled();
            }

            boolean enabled = player.isTeleportEnabled();
            player.sendMessage(enabled ? I18n._("teleportationEnabled") : I18n._("teleportationDisabled"));
            sender.sendMessage(enabled ? I18n._("teleportationEnabledFor", matchPlayer.getDisplayName()) : I18n._("teleportationDisabledFor", matchPlayer.getDisplayName()));
         }
      }

      if (!foundUser) {
         throw new NotEnoughArgumentsException(I18n._("playerNotFound"));
      }
   }
}
