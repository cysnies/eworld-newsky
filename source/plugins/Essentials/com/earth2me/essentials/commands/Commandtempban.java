package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.util.GregorianCalendar;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandtempban extends EssentialsCommand {
   public Commandtempban() {
      super("tempban");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 2) {
         throw new NotEnoughArgumentsException();
      } else {
         User user = this.getPlayer(server, args, 0, true, true);
         if (!user.isOnline()) {
            if (sender instanceof Player && !this.ess.getUser(sender).isAuthorized("essentials.tempban.offline")) {
               sender.sendMessage(I18n._("tempbanExempt"));
               return;
            }
         } else if (user.isAuthorized("essentials.tempban.exempt") && sender instanceof Player) {
            sender.sendMessage(I18n._("tempbanExempt"));
            return;
         }

         String time = getFinalArg(args, 1);
         long banTimestamp = Util.parseDateDiff(time, true);
         long maxBanLength = this.ess.getSettings().getMaxTempban() * 1000L;
         if (maxBanLength > 0L && banTimestamp - GregorianCalendar.getInstance().getTimeInMillis() > maxBanLength && !this.ess.getUser(sender).isAuthorized("essentials.tempban.unlimited")) {
            sender.sendMessage(I18n._("oversizedTempban"));
            throw new NoChargeException();
         } else {
            String senderName = sender instanceof Player ? ((Player)sender).getDisplayName() : "Console";
            String banReason = I18n._("tempBanned", Util.formatDateDiff(banTimestamp), senderName);
            user.setBanReason(banReason);
            user.setBanTimeout(banTimestamp);
            user.setBanned(true);
            user.kickPlayer(banReason);

            for(Player onlinePlayer : server.getOnlinePlayers()) {
               User player = this.ess.getUser(onlinePlayer);
               if (player.isAuthorized("essentials.ban.notify")) {
                  onlinePlayer.sendMessage(I18n._("playerBanned", senderName, user.getName(), banReason));
               }
            }

         }
      }
   }
}
