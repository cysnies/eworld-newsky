package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import org.bukkit.Server;

public class Commandsetjail extends EssentialsCommand {
   public Commandsetjail() {
      super("setjail");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         this.ess.getJails().setJail(args[0], user.getLocation());
         user.sendMessage(I18n._("jailSet", Util.sanitizeString(args[0])));
      }
   }
}
