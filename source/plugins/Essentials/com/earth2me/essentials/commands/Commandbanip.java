package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import java.util.logging.Level;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandbanip extends EssentialsCommand {
   public Commandbanip() {
      super("banip");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         String senderName = sender instanceof Player ? ((Player)sender).getDisplayName() : "Console";
         User player = this.ess.getUser(args[0]);
         String ipAddress;
         if (player == null) {
            ipAddress = args[0];
         } else {
            ipAddress = player.getLastLoginAddress();
            if (ipAddress.length() == 0) {
               throw new Exception(I18n._("playerNotFound"));
            }
         }

         this.ess.getServer().banIP(ipAddress);
         server.getLogger().log(Level.INFO, I18n._("playerBanIpAddress", senderName, ipAddress));

         for(Player onlinePlayer : server.getOnlinePlayers()) {
            User onlineUser = this.ess.getUser(onlinePlayer);
            if (onlinePlayer == sender || onlineUser.isAuthorized("essentials.ban.notify")) {
               onlinePlayer.sendMessage(I18n._("playerBanIpAddress", senderName, ipAddress));
            }
         }

      }
   }
}
