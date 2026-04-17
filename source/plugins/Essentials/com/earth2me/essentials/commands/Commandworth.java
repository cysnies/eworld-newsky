package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.util.Locale;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

public class Commandworth extends EssentialsCommand {
   public Commandworth() {
      super("worth");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      ItemStack iStack = user.getInventory().getItemInHand();
      int amount = iStack.getAmount();
      if (args.length > 0) {
         iStack = this.ess.getItemDb().get(args[0]);
      }

      try {
         if (args.length > 1) {
            amount = Integer.parseInt(args[1]);
         }
      } catch (NumberFormatException var9) {
         amount = iStack.getType().getMaxStackSize();
      }

      iStack.setAmount(amount);
      double worth = this.ess.getWorth().getPrice(iStack);
      if (Double.isNaN(worth)) {
         throw new Exception(I18n._("itemCannotBeSold"));
      } else {
         user.sendMessage(iStack.getDurability() != 0 ? I18n._("worthMeta", iStack.getType().toString().toLowerCase(Locale.ENGLISH).replace("_", ""), iStack.getDurability(), Util.displayCurrency(worth * (double)amount, this.ess), amount, Util.displayCurrency(worth, this.ess)) : I18n._("worth", iStack.getType().toString().toLowerCase(Locale.ENGLISH).replace("_", ""), Util.displayCurrency(worth * (double)amount, this.ess), amount, Util.displayCurrency(worth, this.ess)));
      }
   }

   protected void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         ItemStack iStack = this.ess.getItemDb().get(args[0]);
         int amount = iStack.getAmount();

         try {
            if (args.length > 1) {
               amount = Integer.parseInt(args[1]);
            }
         } catch (NumberFormatException var9) {
            amount = iStack.getType().getMaxStackSize();
         }

         iStack.setAmount(amount);
         double worth = this.ess.getWorth().getPrice(iStack);
         if (Double.isNaN(worth)) {
            throw new Exception(I18n._("itemCannotBeSold"));
         } else {
            sender.sendMessage(iStack.getDurability() != 0 ? I18n._("worthMeta", iStack.getType().toString().toLowerCase(Locale.ENGLISH).replace("_", ""), iStack.getDurability(), Util.displayCurrency(worth * (double)amount, this.ess), amount, Util.displayCurrency(worth, this.ess)) : I18n._("worth", iStack.getType().toString().toLowerCase(Locale.ENGLISH).replace("_", ""), Util.displayCurrency(worth * (double)amount, this.ess), amount, Util.displayCurrency(worth, this.ess)));
         }
      }
   }
}
