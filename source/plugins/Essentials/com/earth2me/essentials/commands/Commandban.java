package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.OfflinePlayer;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.util.logging.Level;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandban extends EssentialsCommand {
   public Commandban() {
      super("ban");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      boolean nomatch = false;
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         User user;
         try {
            user = this.getPlayer(server, args, 0, true, true);
         } catch (NoSuchFieldException var14) {
            nomatch = true;
            user = this.ess.getUser(new OfflinePlayer(args[0], this.ess));
         }

         if (!user.isOnline()) {
            if (sender instanceof Player && !this.ess.getUser(sender).isAuthorized("essentials.ban.offline")) {
               throw new Exception(I18n._("banExempt"));
            }
         } else if (user.isAuthorized("essentials.ban.exempt") && sender instanceof Player) {
            throw new Exception(I18n._("banExempt"));
         }

         String senderName = sender instanceof Player ? ((Player)sender).getDisplayName() : "Console";
         String banReason;
         if (args.length > 1) {
            banReason = Util.replaceFormat(getFinalArg(args, 1).replace("\\n", "\n").replace("|", "\n"));
         } else {
            banReason = I18n._("defaultBanReason");
         }

         user.setBanReason(I18n._("banFormat", banReason, senderName));
         user.setBanned(true);
         user.setBanTimeout(0L);
         user.kickPlayer(I18n._("banFormat", banReason, senderName));
         server.getLogger().log(Level.INFO, I18n._("playerBanned", senderName, user.getName(), banReason));
         if (nomatch) {
            sender.sendMessage(I18n._("userUnknown", user.getName()));
         }

         for(Player onlinePlayer : server.getOnlinePlayers()) {
            User player = this.ess.getUser(onlinePlayer);
            if (onlinePlayer == sender || player.isAuthorized("essentials.ban.notify")) {
               onlinePlayer.sendMessage(I18n._("playerBanned", senderName, user.getName(), banReason));
            }
         }

      }
   }
}
