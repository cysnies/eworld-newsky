package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandtogglejail extends EssentialsCommand {
   public Commandtogglejail() {
      super("togglejail");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         User player = this.getPlayer(server, args, 0, true, true);
         if (args.length >= 2 && !player.isJailed()) {
            if (!player.isOnline()) {
               if (sender instanceof Player && !this.ess.getUser(sender).isAuthorized("essentials.togglejail.offline")) {
                  sender.sendMessage(I18n._("mayNotJail"));
                  return;
               }
            } else if (player.isAuthorized("essentials.jail.exempt")) {
               sender.sendMessage(I18n._("mayNotJail"));
               return;
            }

            if (player.isOnline()) {
               this.ess.getJails().sendToJail(player, args[1]);
            } else {
               this.ess.getJails().getJail(args[1]);
            }

            player.setJailed(true);
            player.sendMessage(I18n._("userJailed"));
            player.setJail((String)null);
            player.setJail(args[1]);
            long timeDiff = 0L;
            if (args.length > 2) {
               String time = getFinalArg(args, 2);
               timeDiff = Util.parseDateDiff(time, true);
               player.setJailTimeout(timeDiff);
            }

            sender.sendMessage(timeDiff > 0L ? I18n._("playerJailedFor", player.getName(), Util.formatDateDiff(timeDiff)) : I18n._("playerJailed", player.getName()));
         } else if (args.length >= 2 && player.isJailed() && !args[1].equalsIgnoreCase(player.getJail())) {
            sender.sendMessage(I18n._("jailAlreadyIncarcerated", player.getJail()));
         } else if (args.length >= 2 && player.isJailed() && args[1].equalsIgnoreCase(player.getJail())) {
            String time = getFinalArg(args, 2);
            long timeDiff = Util.parseDateDiff(time, true);
            player.setJailTimeout(timeDiff);
            sender.sendMessage(I18n._("jailSentenceExtended", Util.formatDateDiff(timeDiff)));
         } else {
            if (args.length == 1 || args.length == 2 && args[1].equalsIgnoreCase(player.getJail())) {
               if (!player.isJailed()) {
                  throw new NotEnoughArgumentsException();
               }

               player.setJailed(false);
               player.setJailTimeout(0L);
               player.sendMessage(I18n._("jailReleasedPlayerNotify"));
               player.setJail((String)null);
               if (player.isOnline()) {
                  player.getTeleport().back();
               }

               sender.sendMessage(I18n._("jailReleased", player.getName()));
            }

         }
      }
   }
}
