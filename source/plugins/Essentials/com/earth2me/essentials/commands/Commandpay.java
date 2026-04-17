package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class Commandpay extends EssentialsCommand {
   public Commandpay() {
      super("pay");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length < 2) {
         throw new NotEnoughArgumentsException();
      } else if (args[0].trim().length() < 2) {
         throw new NotEnoughArgumentsException("You need to specify a player to pay.");
      } else {
         double amount = Double.parseDouble(args[1].replaceAll("[^0-9\\.]", ""));
         boolean skipHidden = !user.isAuthorized("essentials.vanish.interact");
         boolean foundUser = false;

         for(Player matchPlayer : server.matchPlayer(args[0])) {
            User player = this.ess.getUser(matchPlayer);
            if (!skipHidden || !player.isHidden()) {
               foundUser = true;
               user.payUser(player, amount);
               Trade.log("Command", "Pay", "Player", user.getName(), new Trade(amount, this.ess), player.getName(), new Trade(amount, this.ess), user.getLocation(), this.ess);
            }
         }

         if (!foundUser) {
            throw new NotEnoughArgumentsException(I18n._("playerNotFound"));
         }
      }
   }
}
