package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.Util;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

public class Commandping extends EssentialsCommand {
   public Commandping() {
      super("ping");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         sender.sendMessage(I18n._("pong"));
      } else {
         sender.sendMessage(Util.replaceFormat(getFinalArg(args, 0)));
      }

   }
}
