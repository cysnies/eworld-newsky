package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandfly extends EssentialsCommand {
   public Commandfly() {
      super("fly");
   }

   protected void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         this.flyOtherPlayers(server, sender, args);
      }
   }

   protected void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length == 1) {
         if (!args[0].equalsIgnoreCase("on") && !args[0].startsWith("ena") && !args[0].equalsIgnoreCase("1")) {
            if (!args[0].equalsIgnoreCase("off") && !args[0].startsWith("dis") && !args[0].equalsIgnoreCase("0")) {
               if (user.isAuthorized("essentials.fly.others")) {
                  this.flyOtherPlayers(server, user, args);
                  return;
               }
            } else {
               user.setAllowFlight(false);
            }
         } else {
            user.setAllowFlight(true);
         }
      } else {
         if (args.length == 2 && user.isAuthorized("essentials.fly.others")) {
            this.flyOtherPlayers(server, user, args);
            return;
         }

         user.setAllowFlight(!user.getAllowFlight());
         if (!user.getAllowFlight()) {
            user.setFlying(false);
         }
      }

      user.sendMessage(I18n._("flyMode", I18n._(user.getAllowFlight() ? "enabled" : "disabled"), user.getDisplayName()));
   }

   private void flyOtherPlayers(Server server, CommandSender sender, String[] args) throws NotEnoughArgumentsException {
      boolean skipHidden = sender instanceof Player && !this.ess.getUser(sender).isAuthorized("essentials.vanish.interact");
      boolean foundUser = false;

      for(Player matchPlayer : server.matchPlayer(args[0])) {
         User player = this.ess.getUser(matchPlayer);
         if (!skipHidden || !player.isHidden()) {
            foundUser = true;
            if (args.length > 1) {
               if (!args[1].contains("on") && !args[1].contains("ena") && !args[1].equalsIgnoreCase("1")) {
                  player.setAllowFlight(false);
               } else {
                  player.setAllowFlight(true);
               }
            } else {
               player.setAllowFlight(!player.getAllowFlight());
            }

            if (!player.getAllowFlight()) {
               player.setFlying(false);
            }

            sender.sendMessage(I18n._("flyMode", I18n._(player.getAllowFlight() ? "enabled" : "disabled"), player.getDisplayName()));
         }
      }

      if (!foundUser) {
         throw new NotEnoughArgumentsException(I18n._("playerNotFound"));
      }
   }
}
