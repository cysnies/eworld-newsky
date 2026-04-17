package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

public class Commandafk extends EssentialsCommand {
   public Commandafk() {
      super("afk");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length > 0 && user.isAuthorized("essentials.afk.others")) {
         User afkUser = this.getPlayer(server, user, args, 0);
         this.toggleAfk(afkUser);
      } else {
         this.toggleAfk(user);
      }

   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length > 0) {
         User afkUser = this.getPlayer(server, args, 0, true, false);
         this.toggleAfk(afkUser);
      } else {
         throw new NotEnoughArgumentsException();
      }
   }

   private void toggleAfk(User user) {
      user.setDisplayNick();
      String msg = "";
      if (!user.toggleAfk()) {
         if (!user.isHidden()) {
            msg = I18n._("userIsNotAway", user.getDisplayName());
         }

         user.updateActivity(false);
      } else if (!user.isHidden()) {
         msg = I18n._("userIsAway", user.getDisplayName());
      }

      if (!msg.isEmpty()) {
         this.ess.broadcastMessage(user, msg);
      }

   }
}
