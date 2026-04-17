package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandext extends EssentialsCommand {
   public Commandext() {
      super("ext");
   }

   protected void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         this.extinguishPlayers(server, sender, args[0]);
      }
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         user.setFireTicks(0);
         user.sendMessage(I18n._("extinguish"));
      } else {
         this.extinguishPlayers(server, user, commandLabel);
      }
   }

   private void extinguishPlayers(Server server, CommandSender sender, String name) throws Exception {
      boolean skipHidden = sender instanceof Player && !this.ess.getUser(sender).isAuthorized("essentials.vanish.interact");
      boolean foundUser = false;

      for(Player matchPlayer : server.matchPlayer(name)) {
         User player = this.ess.getUser(matchPlayer);
         if (!skipHidden || !player.isHidden()) {
            foundUser = true;
            matchPlayer.setFireTicks(0);
            sender.sendMessage(I18n._("extinguishOthers", matchPlayer.getDisplayName()));
         }
      }

      if (!foundUser) {
         throw new NotEnoughArgumentsException(I18n._("playerNotFound"));
      }
   }
}
