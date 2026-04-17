package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.util.Locale;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandeco extends EssentialsCommand {
   public Commandeco() {
      super("eco");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      Double broadcast = null;
      Double broadcastAll = null;
      double startingBalance = (double)this.ess.getSettings().getStartingBalance();
      if (args.length < 2) {
         throw new NotEnoughArgumentsException();
      } else {
         EcoCommands cmd;
         double amount;
         try {
            cmd = Commandeco.EcoCommands.valueOf(args[0].toUpperCase(Locale.ENGLISH));
            amount = Double.parseDouble(args[2].replaceAll("[^0-9\\.]", ""));
         } catch (Exception ex) {
            throw new NotEnoughArgumentsException(ex);
         }

         double minBalance = this.ess.getSettings().getMinMoney();
         if (args[1].contentEquals("**")) {
            for(String sUser : this.ess.getUserMap().getAllUniqueUsers()) {
               User player = this.ess.getUser(sUser);
               switch (cmd) {
                  case GIVE:
                     player.giveMoney(amount);
                     break;
                  case TAKE:
                     if (player.canAfford(amount, false)) {
                        player.takeMoney(amount);
                     } else if (player.getMoney() > (double)0.0F) {
                        player.setMoney((double)0.0F);
                     }
                     break;
                  case RESET:
                     player.setMoney(startingBalance);
                     broadcastAll = startingBalance;
                     break;
                  case SET:
                     boolean underMinimum = player.getMoney() - amount < minBalance;
                     player.setMoney(underMinimum ? minBalance : amount);
                     broadcastAll = underMinimum ? minBalance : amount;
               }
            }
         } else if (args[1].contentEquals("*")) {
            for(Player onlinePlayer : server.getOnlinePlayers()) {
               User player = this.ess.getUser(onlinePlayer);
               switch (cmd) {
                  case GIVE:
                     player.giveMoney(amount);
                     break;
                  case TAKE:
                     if (player.canAfford(amount)) {
                        player.takeMoney(amount);
                     } else if (player.getMoney() > (double)0.0F) {
                        player.setMoney((double)0.0F);
                     }
                     break;
                  case RESET:
                     player.setMoney(startingBalance);
                     broadcast = startingBalance;
                     break;
                  case SET:
                     boolean underMinimum = player.getMoney() - amount < minBalance;
                     player.setMoney(underMinimum ? minBalance : amount);
                     broadcast = underMinimum ? minBalance : amount;
               }
            }
         } else {
            User player = this.getPlayer(server, args, 1, true, true);
            switch (cmd) {
               case GIVE:
                  player.giveMoney(amount, sender);
                  break;
               case TAKE:
                  if (!player.canAfford(amount)) {
                     throw new Exception(I18n._("notEnoughMoney"));
                  }

                  player.takeMoney(amount, sender);
                  break;
               case RESET:
                  player.setMoney(startingBalance);
                  break;
               case SET:
                  boolean underMinimum = player.getMoney() - amount < minBalance;
                  player.setMoney(underMinimum ? minBalance : amount);
            }
         }

         if (broadcast != null) {
            server.broadcastMessage(I18n._("resetBal", Util.formatAsCurrency(broadcast)));
         }

         if (broadcastAll != null) {
            server.broadcastMessage(I18n._("resetBalAll", Util.formatAsCurrency(broadcastAll)));
         }

      }
   }

   private static enum EcoCommands {
      GIVE,
      TAKE,
      RESET,
      SET;
   }
}
