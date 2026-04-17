package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Server;

public class Commandvanish extends EssentialsCommand {
   public Commandvanish() {
      super("vanish");
   }

   protected void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         user.toggleVanished();
         if (user.isVanished()) {
            user.sendMessage(I18n._("vanished"));
         } else {
            user.sendMessage(I18n._("unvanished"));
         }
      } else {
         if (!args[0].contains("on") && !args[0].contains("ena") && !args[0].equalsIgnoreCase("1")) {
            user.setVanished(false);
         } else {
            user.setVanished(true);
         }

         user.sendMessage(user.isVanished() ? I18n._("vanished") : I18n._("unvanished"));
      }

   }
}
