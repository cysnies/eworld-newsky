package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

public class Commandbalance extends EssentialsCommand {
   public Commandbalance() {
      super("balance");
   }

   protected void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         User target = this.getPlayer(server, args, 0, true, true);
         sender.sendMessage(I18n._("balanceOther", target.getDisplayName(), Util.displayCurrency(target.getMoney(), this.ess)));
      }
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length >= 1 && user.isAuthorized("essentials.balance.others")) {
         User target = this.getPlayer(server, args, 0, true, true);
         double bal = target.getMoney();
         user.sendMessage(I18n._("balanceOther", target.getDisplayName(), Util.displayCurrency(bal, this.ess)));
      } else {
         double bal = user.getMoney();
         user.sendMessage(I18n._("balance", Util.displayCurrency(bal, this.ess)));
      }

   }
}
