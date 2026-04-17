package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Server;

public class Commandignore extends EssentialsCommand {
   public Commandignore() {
      super("ignore");
   }

   protected void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         User player;
         try {
            player = this.getPlayer(server, args, 0, true, true);
         } catch (NoSuchFieldException var7) {
            player = this.ess.getOfflineUser(args[0]);
         }

         if (player == null) {
            throw new Exception(I18n._("playerNotFound"));
         } else {
            if (user.isIgnoredPlayer(player)) {
               user.setIgnoredPlayer(player, false);
               user.sendMessage(I18n._("unignorePlayer", player.getName()));
            } else {
               user.setIgnoredPlayer(player, true);
               user.sendMessage(I18n._("ignorePlayer", player.getName()));
            }

         }
      }
   }
}
