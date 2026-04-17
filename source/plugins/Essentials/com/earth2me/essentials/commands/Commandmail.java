package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

public class Commandmail extends EssentialsCommand {
   private static int mailsPerMinute = 0;
   private static long timestamp = 0L;

   public Commandmail() {
      super("mail");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length >= 1 && "read".equalsIgnoreCase(args[0])) {
         List<String> mail = user.getMails();
         if (mail.isEmpty()) {
            user.sendMessage(I18n._("noMail"));
            throw new NoChargeException();
         } else {
            for(String messages : mail) {
               user.sendMessage(messages);
            }

            user.sendMessage(I18n._("mailClear"));
         }
      } else if (args.length >= 3 && "send".equalsIgnoreCase(args[0])) {
         if (!user.isAuthorized("essentials.mail.send")) {
            throw new Exception(I18n._("noPerm", "essentials.mail.send"));
         } else {
            User u = this.ess.getUser(args[1]);
            if (u == null) {
               throw new Exception(I18n._("playerNeverOnServer", args[1]));
            } else {
               if (!u.isIgnoredPlayer(user)) {
                  String mail = user.getName() + ": " + Util.sanitizeString(Util.stripFormat(getFinalArg(args, 2)));
                  if (mail.length() > 1000) {
                     throw new Exception("Mail message too long. Try to keep it below 1000");
                  }

                  if (Math.abs(System.currentTimeMillis() - timestamp) > 60000L) {
                     timestamp = System.currentTimeMillis();
                     mailsPerMinute = 0;
                  }

                  ++mailsPerMinute;
                  if (mailsPerMinute > this.ess.getSettings().getMailsPerMinute()) {
                     throw new Exception("Too many mails have been send within the last minute. Maximum: " + this.ess.getSettings().getMailsPerMinute());
                  }

                  u.addMail(mail);
               }

               user.sendMessage(I18n._("mailSent"));
            }
         }
      } else if (args.length > 1 && "sendall".equalsIgnoreCase(args[0])) {
         if (!user.isAuthorized("essentials.mail.sendall")) {
            throw new Exception(I18n._("noPerm", "essentials.mail.sendall"));
         } else {
            this.ess.runTaskAsynchronously(new SendAll(user.getName() + ": " + Util.stripFormat(getFinalArg(args, 1))));
            user.sendMessage(I18n._("mailSent"));
         }
      } else if (args.length >= 1 && "clear".equalsIgnoreCase(args[0])) {
         user.setMails((List)null);
         user.sendMessage(I18n._("mailCleared"));
      } else {
         throw new NotEnoughArgumentsException();
      }
   }

   protected void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length >= 1 && "read".equalsIgnoreCase(args[0])) {
         throw new Exception(I18n._("onlyPlayers", commandLabel + " read"));
      } else if (args.length >= 1 && "clear".equalsIgnoreCase(args[0])) {
         throw new Exception(I18n._("onlyPlayers", commandLabel + " clear"));
      } else if (args.length >= 3 && "send".equalsIgnoreCase(args[0])) {
         User u = this.ess.getUser(args[1]);
         if (u == null) {
            throw new Exception(I18n._("playerNeverOnServer", args[1]));
         } else {
            u.addMail("Server: " + getFinalArg(args, 2));
            sender.sendMessage(I18n._("mailSent"));
         }
      } else if (args.length >= 1 && "sendall".equalsIgnoreCase(args[0])) {
         this.ess.runTaskAsynchronously(new SendAll("Server: " + getFinalArg(args, 2)));
         sender.sendMessage(I18n._("mailSent"));
      } else if (args.length >= 2) {
         User u = this.ess.getUser(args[0]);
         if (u == null) {
            throw new Exception(I18n._("playerNeverOnServer", args[0]));
         } else {
            u.addMail("Server: " + getFinalArg(args, 1));
            sender.sendMessage(I18n._("mailSent"));
         }
      } else {
         throw new NotEnoughArgumentsException();
      }
   }

   private class SendAll implements Runnable {
      String message;

      public SendAll(String message) {
         this.message = message;
      }

      public void run() {
         for(String username : Commandmail.this.ess.getUserMap().getAllUniqueUsers()) {
            User user = Commandmail.this.ess.getUserMap().getUser(username);
            if (user != null) {
               user.addMail(this.message);
            }
         }

      }
   }
}
