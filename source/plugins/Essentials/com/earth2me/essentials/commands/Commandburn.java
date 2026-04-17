package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

public class Commandburn extends EssentialsCommand {
   public Commandburn() {
      super("burn");
   }

   protected void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 2) {
         throw new NotEnoughArgumentsException();
      } else if (args[0].trim().length() < 2) {
         throw new NotEnoughArgumentsException("You need to specify a player to burn.");
      } else {
         User user = this.getPlayer(server, args, 0);
         user.setFireTicks(Integer.parseInt(args[1]) * 20);
         sender.sendMessage(I18n._("burnMsg", user.getDisplayName(), Integer.parseInt(args[1])));
      }
   }
}
