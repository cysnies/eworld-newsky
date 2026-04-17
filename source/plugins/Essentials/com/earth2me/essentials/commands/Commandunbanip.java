package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.util.logging.Level;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandunbanip extends EssentialsCommand {
   public Commandunbanip() {
      super("unbanip");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         String ipAddress;
         if (Util.validIP(args[0])) {
            ipAddress = args[0];
         } else {
            User user = this.getPlayer(server, args, 0, true, true);
            ipAddress = user.getLastLoginAddress();
            if (ipAddress.isEmpty()) {
               throw new PlayerNotFoundException();
            }
         }

         this.ess.getServer().unbanIP(ipAddress);
         String senderName = sender instanceof Player ? ((Player)sender).getDisplayName() : "Console";
         server.getLogger().log(Level.INFO, I18n._("playerUnbanIpAddress", senderName, ipAddress));

         for(Player onlinePlayer : server.getOnlinePlayers()) {
            User onlineUser = this.ess.getUser(onlinePlayer);
            if (onlinePlayer == sender || onlineUser.isAuthorized("essentials.ban.notify")) {
               onlinePlayer.sendMessage(I18n._("playerUnbanIpAddress", senderName, ipAddress));
            }
         }

      }
   }
}
