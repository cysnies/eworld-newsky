package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.util.logging.Level;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class Commandhelpop extends EssentialsCommand {
   public Commandhelpop() {
      super("helpop");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         user.setDisplayNick();
         String message = I18n._("helpOp", user.getDisplayName(), Util.stripFormat(getFinalArg(args, 0)));
         logger.log(Level.INFO, message);

         for(Player onlinePlayer : server.getOnlinePlayers()) {
            User player = this.ess.getUser(onlinePlayer);
            if (player.isAuthorized("essentials.helpop.receive")) {
               player.sendMessage(message);
            }
         }

      }
   }
}
