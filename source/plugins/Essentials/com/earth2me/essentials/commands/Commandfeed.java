package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandfeed extends EssentialsCommand {
   public Commandfeed() {
      super("feed");
   }

   protected void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length > 0 && user.isAuthorized("essentials.feed.others")) {
         this.feedOtherPlayers(server, user, args[0]);
      } else {
         user.setFoodLevel(20);
         user.setSaturation(10.0F);
         user.sendMessage(I18n._("feed"));
      }

   }

   private void feedOtherPlayers(Server server, CommandSender sender, String name) throws NotEnoughArgumentsException {
      boolean skipHidden = sender instanceof Player && !this.ess.getUser(sender).isAuthorized("essentials.vanish.interact");
      boolean foundUser = false;

      for(Player matchPlayer : server.matchPlayer(name)) {
         User player = this.ess.getUser(matchPlayer);
         if (!skipHidden || !player.isHidden()) {
            foundUser = true;
            matchPlayer.setFoodLevel(20);
            matchPlayer.setSaturation(10.0F);
            sender.sendMessage(I18n._("feedOther", matchPlayer.getDisplayName()));
         }
      }

      if (!foundUser) {
         throw new NotEnoughArgumentsException(I18n._("playerNotFound"));
      }
   }
}
