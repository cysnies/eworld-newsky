package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

public class Commandsetworth extends EssentialsCommand {
   public Commandsetworth() {
      super("setworth");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         ItemStack stack;
         String price;
         if (args.length == 1) {
            stack = user.getInventory().getItemInHand();
            price = args[0];
         } else {
            stack = this.ess.getItemDb().get(args[0]);
            price = args[1];
         }

         this.ess.getWorth().setPrice(stack, Double.parseDouble(price));
         user.sendMessage(I18n._("worthSet"));
      }
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 2) {
         throw new NotEnoughArgumentsException();
      } else {
         ItemStack stack = this.ess.getItemDb().get(args[0]);
         this.ess.getWorth().setPrice(stack, Double.parseDouble(args[1]));
         sender.sendMessage(I18n._("worthSet"));
      }
   }
}
