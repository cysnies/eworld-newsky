package com.earth2me.essentials.commands;

import com.earth2me.essentials.Console;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.IReplyTo;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandr extends EssentialsCommand {
   public Commandr() {
      super("r");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         String message = getFinalArg(args, 0);
         IReplyTo replyTo;
         String senderName;
         if (sender instanceof Player) {
            User user = this.ess.getUser(sender);
            message = Util.formatMessage(user, "essentials.msg", message);
            replyTo = user;
            senderName = user.getDisplayName();
         } else {
            message = Util.replaceFormat(message);
            replyTo = Console.getConsoleReplyTo();
            senderName = "Console";
         }

         CommandSender target = replyTo.getReplyTo();
         String targetName = target instanceof Player ? ((Player)target).getDisplayName() : "Console";
         if (target != null && (!(target instanceof Player) || ((Player)target).isOnline())) {
            sender.sendMessage(I18n._("msgFormat", I18n._("me"), targetName, message));
            if (target instanceof Player) {
               User player = this.ess.getUser(target);
               if (sender instanceof Player && player.isIgnoredPlayer(this.ess.getUser(sender))) {
                  return;
               }
            }

            target.sendMessage(I18n._("msgFormat", senderName, I18n._("me"), message));
            replyTo.setReplyTo(target);
            if (target != sender) {
               if (target instanceof Player) {
                  this.ess.getUser((Player)target).setReplyTo(sender);
               } else {
                  Console.getConsoleReplyTo().setReplyTo(sender);
               }
            }

         } else {
            throw new Exception(I18n._("foreverAlone"));
         }
      }
   }
}
