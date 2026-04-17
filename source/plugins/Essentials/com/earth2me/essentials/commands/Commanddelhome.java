package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import java.util.Locale;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

public class Commanddelhome extends EssentialsCommand {
   public Commanddelhome() {
      super("delhome");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         User user = this.ess.getUser(sender);
         String[] nameParts = args[0].split(":");
         String[] expandedArg;
         if (nameParts[0].length() != args[0].length()) {
            expandedArg = nameParts;
         } else {
            expandedArg = args;
         }

         String name;
         if (expandedArg.length <= 1 || user != null && !user.isAuthorized("essentials.delhome.others")) {
            if (user == null) {
               throw new NotEnoughArgumentsException();
            }

            name = expandedArg[0];
         } else {
            user = this.getPlayer(server, expandedArg, 0, true, true);
            name = expandedArg[1];
         }

         if (name.equalsIgnoreCase("bed")) {
            throw new Exception(I18n._("invalidHomeName"));
         } else {
            user.delHome(name.toLowerCase(Locale.ENGLISH));
            sender.sendMessage(I18n._("deleteHome", name));
         }
      }
   }
}
