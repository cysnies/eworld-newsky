package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.util.Locale;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandrealname extends EssentialsCommand {
   public Commandrealname() {
      super("realname");
   }

   protected void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         String whois = args[0].toLowerCase(Locale.ENGLISH);
         boolean foundUser = false;

         for(Player onlinePlayer : server.getOnlinePlayers()) {
            User u = this.ess.getUser(onlinePlayer);
            if (!u.isHidden()) {
               u.setDisplayNick();
               String displayName = Util.stripFormat(u.getDisplayName()).toLowerCase(Locale.ENGLISH);
               if (displayName.contains(whois)) {
                  foundUser = true;
                  sender.sendMessage(u.getDisplayName() + " " + I18n._("is") + " " + u.getName());
               }
            }
         }

         if (!foundUser) {
            throw new NoSuchFieldException(I18n._("playerNotFound"));
         }
      }
   }
}
