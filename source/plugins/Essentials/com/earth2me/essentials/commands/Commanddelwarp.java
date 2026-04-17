package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

public class Commanddelwarp extends EssentialsCommand {
   public Commanddelwarp() {
      super("delwarp");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         this.ess.getWarps().delWarp(args[0]);
         sender.sendMessage(I18n._("deleteWarp", args[0]));
      }
   }
}
