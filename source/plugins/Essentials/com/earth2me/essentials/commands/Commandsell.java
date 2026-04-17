package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.util.Locale;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;

public class Commandsell extends EssentialsCommand {
   public Commandsell() {
      super("sell");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      double totalWorth = (double)0.0F;
      String type = "";
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         ItemStack is = null;
         if (args[0].equalsIgnoreCase("hand")) {
            is = user.getItemInHand();
         } else {
            if (args[0].equalsIgnoreCase("inventory") || args[0].equalsIgnoreCase("invent") || args[0].equalsIgnoreCase("all")) {
               for(ItemStack stack : user.getInventory().getContents()) {
                  if (stack != null && stack.getType() != Material.AIR) {
                     try {
                        totalWorth += this.sellItem(user, stack, args, true);
                     } catch (Exception var15) {
                     }
                  }
               }

               if (totalWorth > (double)0.0F) {
                  user.sendMessage(I18n._("totalWorthAll", type, Util.displayCurrency(totalWorth, this.ess)));
               }

               return;
            }

            if (args[0].equalsIgnoreCase("blocks")) {
               for(ItemStack stack : user.getInventory().getContents()) {
                  if (stack != null && stack.getTypeId() <= 255 && stack.getType() != Material.AIR) {
                     try {
                        totalWorth += this.sellItem(user, stack, args, true);
                     } catch (Exception var14) {
                     }
                  }
               }

               if (totalWorth > (double)0.0F) {
                  user.sendMessage(I18n._("totalWorthBlocks", type, Util.displayCurrency(totalWorth, this.ess)));
               }

               return;
            }
         }

         if (is == null) {
            is = this.ess.getItemDb().get(args[0]);
         }

         this.sellItem(user, is, args, false);
      }
   }

   private double sellItem(User user, ItemStack is, String[] args, boolean isBulkSell) throws Exception {
      if (is != null && is.getType() != Material.AIR) {
         int id = is.getTypeId();
         int amount = 0;
         if (args.length > 1) {
            amount = Integer.parseInt(args[1].replaceAll("[^0-9]", ""));
            if (args[1].startsWith("-")) {
               amount = -amount;
            }
         }

         double worth = this.ess.getWorth().getPrice(is);
         boolean stack = args.length > 1 && args[1].endsWith("s");
         boolean requireStack = this.ess.getSettings().isTradeInStacks(id);
         if (Double.isNaN(worth)) {
            throw new Exception(I18n._("itemCannotBeSold"));
         } else if (requireStack && !stack) {
            throw new Exception(I18n._("itemMustBeStacked"));
         } else {
            int max = 0;

            for(ItemStack s : user.getInventory().getContents()) {
               if (s != null && s.isSimilar(is)) {
                  max += s.getAmount();
               }
            }

            if (stack) {
               amount *= is.getType().getMaxStackSize();
            }

            if (amount < 1) {
               amount += max;
            }

            if (requireStack) {
               amount -= amount % is.getType().getMaxStackSize();
            }

            if (amount <= max && amount >= 1) {
               ItemStack ris = is.clone();
               ris.setAmount(amount);
               if (!user.getInventory().containsAtLeast(ris, amount)) {
                  throw new IllegalStateException("Trying to remove more items than are available.");
               } else {
                  user.getInventory().removeItem(new ItemStack[]{ris});
                  user.updateInventory();
                  Trade.log("Command", "Sell", "Item", user.getName(), new Trade(ris, this.ess), user.getName(), new Trade(worth * (double)amount, this.ess), user.getLocation(), this.ess);
                  user.giveMoney(worth * (double)amount);
                  user.sendMessage(I18n._("itemSold", Util.displayCurrency(worth * (double)amount, this.ess), amount, is.getType().toString().toLowerCase(Locale.ENGLISH), Util.displayCurrency(worth, this.ess)));
                  logger.log(Level.INFO, I18n._("itemSoldConsole", user.getDisplayName(), is.getType().toString().toLowerCase(Locale.ENGLISH), Util.displayCurrency(worth * (double)amount, this.ess), amount, Util.displayCurrency(worth, this.ess)));
                  return worth * (double)amount;
               }
            } else if (!isBulkSell) {
               user.sendMessage(I18n._("itemNotEnough1"));
               user.sendMessage(I18n._("itemNotEnough2"));
               throw new Exception(I18n._("itemNotEnough3"));
            } else {
               return worth * (double)amount;
            }
         }
      } else {
         throw new Exception(I18n._("itemSellAir"));
      }
   }
}
