package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import com.earth2me.essentials.craftbukkit.SetExpFix;
import java.util.Locale;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandexp extends EssentialsCommand {
   public Commandexp() {
      super("exp");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length == 0) {
         this.showExp(user, user);
      } else if (args[0].equalsIgnoreCase("set") && user.isAuthorized("essentials.exp.set")) {
         if (args.length == 3 && user.isAuthorized("essentials.exp.set.others")) {
            this.expMatch(server, user, args[1], args[2], false);
         } else {
            this.setExp(user, user, args[1], false);
         }
      } else if (args[0].equalsIgnoreCase("give") && user.isAuthorized("essentials.exp.give")) {
         if (args.length == 3 && user.isAuthorized("essentials.exp.give.others")) {
            this.expMatch(server, user, args[1], args[2], true);
         } else {
            this.setExp(user, user, args[1], true);
         }
      } else if (args[0].equalsIgnoreCase("show")) {
         if (args.length >= 2 && user.isAuthorized("essentials.exp.others")) {
            String match = args[1].trim();
            this.showMatch(server, user, match);
         } else {
            this.showExp(user, user);
         }
      } else if (args.length >= 1 && Util.isInt(args[0].toLowerCase(Locale.ENGLISH).replace("l", "")) && user.isAuthorized("essentials.exp.give")) {
         if (args.length >= 2 && user.isAuthorized("essentials.exp.give.others")) {
            this.expMatch(server, user, args[1], args[0], true);
         } else {
            this.setExp(user, user, args[0], true);
         }
      } else if (args.length >= 1 && user.isAuthorized("essentials.exp.others")) {
         String match = args[0].trim();
         this.showMatch(server, user, match);
      } else {
         this.showExp(user, user);
      }

   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         if (args.length > 2 && args[0].equalsIgnoreCase("set")) {
            this.expMatch(server, sender, args[1], args[2], false);
         } else if (args.length > 2 && args[0].equalsIgnoreCase("give")) {
            this.expMatch(server, sender, args[1], args[2], true);
         } else {
            String match = args[0].trim();
            if (args.length >= 2 && Util.isInt(args[0].toLowerCase(Locale.ENGLISH).replace("l", ""))) {
               match = args[1].trim();
               this.expMatch(server, sender, match, args[0], true);
            } else if (args.length == 1) {
               match = args[0].trim();
            }

            this.showMatch(server, sender, match);
         }

      }
   }

   private void showMatch(Server server, CommandSender sender, String match) throws NotEnoughArgumentsException {
      boolean skipHidden = sender instanceof Player && !this.ess.getUser(sender).isAuthorized("essentials.vanish.interact");
      boolean foundUser = false;

      for(Player matchPlayer : server.matchPlayer(match)) {
         User player = this.ess.getUser(matchPlayer);
         if (!skipHidden || !player.isHidden()) {
            foundUser = true;
            this.showExp(sender, player);
         }
      }

      if (!foundUser) {
         throw new NotEnoughArgumentsException(I18n._("playerNotFound"));
      }
   }

   private void expMatch(Server server, CommandSender sender, String match, String amount, boolean give) throws NotEnoughArgumentsException {
      boolean foundUser = false;

      for(Player matchPlayer : server.matchPlayer(match)) {
         User player = this.ess.getUser(matchPlayer);
         if (!player.isHidden()) {
            foundUser = true;
            this.setExp(sender, player, amount, give);
         }
      }

      if (!foundUser) {
         throw new NotEnoughArgumentsException(I18n._("playerNotFound"));
      }
   }

   private void showExp(CommandSender sender, User target) {
      sender.sendMessage(I18n._("exp", target.getDisplayName(), SetExpFix.getTotalExperience(target), target.getLevel(), SetExpFix.getExpUntilNextLevel(target)));
   }

   private void setExp(CommandSender sender, User target, String strAmount, boolean give) throws NotEnoughArgumentsException {
      strAmount = strAmount.toLowerCase(Locale.ENGLISH);
      long amount;
      if (strAmount.contains("l")) {
         strAmount = strAmount.replaceAll("l", "");
         int neededLevel = Integer.parseInt(strAmount);
         if (give) {
            neededLevel += target.getLevel();
         }

         amount = (long)SetExpFix.getExpToLevel(neededLevel);
         SetExpFix.setTotalExperience(target, 0);
      } else {
         amount = Long.parseLong(strAmount);
         if (amount > 2147483647L || amount < -2147483648L) {
            throw new NotEnoughArgumentsException();
         }
      }

      if (give) {
         amount += (long)SetExpFix.getTotalExperience(target);
      }

      if (amount > 2147483647L) {
         amount = 2147483647L;
      }

      if (amount < 0L) {
         amount = 0L;
      }

      SetExpFix.setTotalExperience(target, (int)amount);
      sender.sendMessage(I18n._("expSet", target.getDisplayName(), amount));
   }
}
