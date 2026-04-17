package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.IUser;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

public class Commandbroadcast extends EssentialsCommand {
   public Commandbroadcast() {
      super("broadcast");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         this.ess.broadcastMessage((IUser)null, I18n._("broadcast", Util.replaceFormat(getFinalArg(args, 0)), user.getDisplayName()));
      }
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         this.ess.broadcastMessage((IUser)null, I18n._("broadcast", Util.replaceFormat(getFinalArg(args, 0)), sender.getName()));
      }
   }
}
