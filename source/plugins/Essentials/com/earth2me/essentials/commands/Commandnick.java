package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.util.Locale;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandnick extends EssentialsCommand {
   public Commandnick() {
      super("nick");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else if (!this.ess.getSettings().changeDisplayName()) {
         throw new Exception(I18n._("nickDisplayName"));
      } else if (args.length > 1) {
         if (!user.isAuthorized("essentials.nick.others")) {
            throw new Exception(I18n._("nickOthersPermission"));
         } else {
            this.setNickname(server, this.getPlayer(server, user, args, 0), this.formatNickname(user, args[1]));
            user.sendMessage(I18n._("nickChanged"));
         }
      } else {
         this.setNickname(server, user, this.formatNickname(user, args[0]));
      }
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 2) {
         throw new NotEnoughArgumentsException();
      } else if (!this.ess.getSettings().changeDisplayName()) {
         throw new Exception(I18n._("nickDisplayName"));
      } else {
         if ((args[0].equalsIgnoreCase("*") || args[0].equalsIgnoreCase("all")) && args[1].equalsIgnoreCase("off")) {
            this.resetAllNicknames(server);
         } else {
            this.setNickname(server, this.getPlayer(server, args, 0, true, false), this.formatNickname((User)null, args[1]));
         }

         sender.sendMessage(I18n._("nickChanged"));
      }
   }

   private String formatNickname(User user, String nick) {
      return user == null ? Util.replaceFormat(nick) : Util.formatString(user, "essentials.nick", nick);
   }

   private void resetAllNicknames(Server server) {
      for(Player player : server.getOnlinePlayers()) {
         try {
            this.setNickname(server, this.ess.getUser(player), "off");
         } catch (Exception var7) {
         }
      }

   }

   private void setNickname(Server server, User target, String nick) throws Exception {
      if (!nick.matches("^[a-zA-Z_0-9§]+$")) {
         throw new Exception(I18n._("nickNamesAlpha"));
      } else {
         if (target.getName().equalsIgnoreCase(nick)) {
            target.setNickname(nick);
            target.setDisplayNick();
            target.sendMessage(I18n._("nickNoMore"));
         } else if ("off".equalsIgnoreCase(nick)) {
            target.setNickname((String)null);
            target.setDisplayNick();
            target.sendMessage(I18n._("nickNoMore"));
         } else {
            for(Player onlinePlayer : server.getOnlinePlayers()) {
               if (target.getBase() != onlinePlayer) {
                  String displayName = onlinePlayer.getDisplayName().toLowerCase(Locale.ENGLISH);
                  String name = onlinePlayer.getName().toLowerCase(Locale.ENGLISH);
                  String lowerNick = nick.toLowerCase(Locale.ENGLISH);
                  if (lowerNick.equals(displayName) || lowerNick.equals(name)) {
                     throw new Exception(I18n._("nickInUse"));
                  }
               }
            }

            target.setNickname(nick);
            target.setDisplayNick();
            target.sendMessage(I18n._("nickSet", target.getDisplayName() + "§7."));
         }

      }
   }
}
