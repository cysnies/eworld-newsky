package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.util.logging.Level;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandkick extends EssentialsCommand {
   public Commandkick() {
      super("kick");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         User target = this.getPlayer(server, args, 0, true, false);
         if (sender instanceof Player) {
            User user = this.ess.getUser(sender);
            if (target.isHidden() && !user.isAuthorized("essentials.vanish.interact")) {
               throw new PlayerNotFoundException();
            }

            if (target.isAuthorized("essentials.kick.exempt")) {
               throw new Exception(I18n._("kickExempt"));
            }
         }

         String kickReason = args.length > 1 ? getFinalArg(args, 1) : I18n._("kickDefault");
         kickReason = Util.replaceFormat(kickReason.replace("\\n", "\n").replace("|", "\n"));
         target.kickPlayer(kickReason);
         String senderName = sender instanceof Player ? ((Player)sender).getDisplayName() : "Console";
         server.getLogger().log(Level.INFO, I18n._("playerKicked", senderName, target.getName(), kickReason));

         for(Player onlinePlayer : server.getOnlinePlayers()) {
            User player = this.ess.getUser(onlinePlayer);
            if (player.isAuthorized("essentials.kick.notify")) {
               onlinePlayer.sendMessage(I18n._("playerKicked", senderName, target.getName(), kickReason));
            }
         }

      }
   }
}
