package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.Util;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandkickall extends EssentialsCommand {
   public Commandkickall() {
      super("kickall");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      String kickReason = args.length > 0 ? getFinalArg(args, 0) : I18n._("kickDefault");
      kickReason = Util.replaceFormat(kickReason.replace("\\n", "\n").replace("|", "\n"));

      for(Player onlinePlayer : server.getOnlinePlayers()) {
         if (!(sender instanceof Player) || !onlinePlayer.getName().equalsIgnoreCase(((Player)sender).getName())) {
            onlinePlayer.kickPlayer(kickReason);
         }
      }

      sender.sendMessage(I18n._("kickedAll"));
   }
}
