package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandgod extends EssentialsCommand {
   public Commandgod() {
      super("god");
   }

   protected void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         this.godOtherPlayers(server, sender, args);
      }
   }

   protected void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length > 0 && args[0].trim().length() > 2 && user.isAuthorized("essentials.god.others")) {
         this.godOtherPlayers(server, user, args);
      } else {
         this.godPlayer(user, !user.isGodModeEnabled());
         user.sendMessage(I18n._("godMode", user.isGodModeEnabled() ? I18n._("enabled") : I18n._("disabled")));
      }
   }

   private void godPlayer(User player, boolean enabled) {
      player.setGodModeEnabled(enabled);
      if (enabled && player.getHealth() != 0) {
         player.setHealth(player.getMaxHealth());
         player.setFoodLevel(20);
      }

   }

   private void godOtherPlayers(Server server, CommandSender sender, String[] args) throws NotEnoughArgumentsException {
      boolean skipHidden = sender instanceof Player && !this.ess.getUser(sender).isAuthorized("essentials.vanish.interact");
      boolean foundUser = false;

      for(Player matchPlayer : server.matchPlayer(args[0])) {
         User player = this.ess.getUser(matchPlayer);
         if (!skipHidden || !player.isHidden()) {
            foundUser = true;
            boolean enabled;
            if (args.length > 1) {
               if (!args[1].contains("on") && !args[1].contains("ena") && !args[1].equalsIgnoreCase("1")) {
                  enabled = false;
               } else {
                  enabled = true;
               }
            } else {
               enabled = !player.isGodModeEnabled();
            }

            this.godPlayer(player, enabled);
            player.sendMessage(I18n._("godMode", enabled ? I18n._("enabled") : I18n._("disabled")));
            sender.sendMessage(I18n._("godMode", I18n._(enabled ? "godEnabledFor" : "godDisabledFor", matchPlayer.getDisplayName())));
         }
      }

      if (!foundUser) {
         throw new NotEnoughArgumentsException(I18n._("playerNotFound"));
      }
   }
}
