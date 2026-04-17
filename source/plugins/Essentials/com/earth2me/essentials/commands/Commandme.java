package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import org.bukkit.Server;

public class Commandme extends EssentialsCommand {
   public Commandme() {
      super("me");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (user.isMuted()) {
         throw new Exception(I18n._("voiceSilenced"));
      } else if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         String message = getFinalArg(args, 0);
         message = Util.formatMessage(user, "essentials.chat", message);
         user.setDisplayNick();
         this.ess.broadcastMessage(user, I18n._("action", user.getDisplayName(), message));
      }
   }
}
