package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandmute extends EssentialsCommand {
   public Commandmute() {
      super("mute");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         User player = this.getPlayer(server, args, 0, true, true);
         if (sender instanceof Player && !player.isMuted() && player.isAuthorized("essentials.mute.exempt")) {
            throw new Exception(I18n._("muteExempt"));
         } else {
            long muteTimestamp = 0L;
            if (args.length > 1) {
               String time = getFinalArg(args, 1);
               muteTimestamp = Util.parseDateDiff(time, true);
               player.setMuted(true);
            } else {
               player.setMuted(!player.getMuted());
            }

            player.setMuteTimeout(muteTimestamp);
            boolean muted = player.getMuted();
            if (muted) {
               if (muteTimestamp > 0L) {
                  sender.sendMessage(I18n._("mutedPlayerFor", player.getDisplayName(), Util.formatDateDiff(muteTimestamp)));
                  player.sendMessage(I18n._("playerMutedFor", Util.formatDateDiff(muteTimestamp)));
               } else {
                  sender.sendMessage(I18n._("mutedPlayer", player.getDisplayName()));
                  player.sendMessage(I18n._("playerMuted"));
               }

               for(Player onlinePlayer : server.getOnlinePlayers()) {
                  User user = this.ess.getUser(onlinePlayer);
                  if (onlinePlayer != sender && user.isAuthorized("essentials.mute.notify")) {
                     onlinePlayer.sendMessage(I18n._("muteNotify", sender.getName(), player.getName()));
                  }
               }
            } else {
               sender.sendMessage(I18n._("unmutedPlayer", player.getDisplayName()));
               player.sendMessage(I18n._("playerUnmuted"));
            }

         }
      }
   }
}
