package com.earth2me.essentials.commands;

import com.earth2me.essentials.Console;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.IReplyTo;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandmsg extends EssentialsCommand {
   public Commandmsg() {
      super("msg");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length >= 2 && args[0].trim().length() >= 2 && !args[1].trim().isEmpty()) {
         String message = getFinalArg(args, 1);
         if (sender instanceof Player) {
            User user = this.ess.getUser(sender);
            if (user.isMuted()) {
               throw new Exception(I18n._("voiceSilenced"));
            }

            message = Util.formatMessage(user, "essentials.msg", message);
         } else {
            message = Util.replaceFormat(message);
         }

         String translatedMe = I18n._("me");
         IReplyTo replyTo = (IReplyTo)(sender instanceof Player ? this.ess.getUser((Player)sender) : Console.getConsoleReplyTo());
         String senderName = sender instanceof Player ? ((Player)sender).getDisplayName() : "Console";
         if (args[0].equalsIgnoreCase("Console")) {
            sender.sendMessage(I18n._("msgFormat", translatedMe, "Console", message));
            CommandSender cs = Console.getCommandSender(server);
            cs.sendMessage(I18n._("msgFormat", senderName, translatedMe, message));
            replyTo.setReplyTo(cs);
            Console.getConsoleReplyTo().setReplyTo(sender);
         } else {
            boolean skipHidden = sender instanceof Player && !this.ess.getUser(sender).isAuthorized("essentials.vanish.interact");
            boolean foundUser = false;

            for(Player matchPlayer : server.matchPlayer(args[0])) {
               User matchedUser = this.ess.getUser(matchPlayer);
               if (!skipHidden || !matchedUser.isHidden()) {
                  foundUser = true;
                  if (matchedUser.isAfk()) {
                     sender.sendMessage(I18n._("userAFK", matchPlayer.getDisplayName()));
                  }

                  sender.sendMessage(I18n._("msgFormat", translatedMe, matchPlayer.getDisplayName(), message));
                  if (!(sender instanceof Player) || !matchedUser.isIgnoredPlayer(this.ess.getUser(sender))) {
                     matchPlayer.sendMessage(I18n._("msgFormat", senderName, translatedMe, message));
                     replyTo.setReplyTo(matchedUser);
                     this.ess.getUser(matchPlayer).setReplyTo(sender);
                  }
               }
            }

            if (!foundUser) {
               throw new Exception(I18n._("playerNotFound"));
            }
         }
      } else {
         throw new NotEnoughArgumentsException();
      }
   }
}
