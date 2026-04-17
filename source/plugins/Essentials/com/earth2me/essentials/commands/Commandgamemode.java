package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import java.util.Locale;
import org.bukkit.GameMode;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandgamemode extends EssentialsCommand {
   public Commandgamemode() {
      super("gamemode");
   }

   protected void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length == 0) {
         throw new NotEnoughArgumentsException();
      } else {
         if (args.length == 1) {
            GameMode gameMode = this.matchGameMode(commandLabel);
            this.gamemodeOtherPlayers(server, sender, gameMode, args[0]);
         } else if (args.length == 2) {
            GameMode gameMode = this.matchGameMode(args[0].toLowerCase(Locale.ENGLISH));
            this.gamemodeOtherPlayers(server, sender, gameMode, args[1]);
         }

      }
   }

   protected void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      GameMode gameMode;
      if (args.length == 0) {
         gameMode = this.matchGameMode(commandLabel);
      } else {
         if (args.length > 1 && args[1].trim().length() > 2 && user.isAuthorized("essentials.gamemode.others")) {
            gameMode = this.matchGameMode(args[0].toLowerCase(Locale.ENGLISH));
            this.gamemodeOtherPlayers(server, user, gameMode, args[1]);
            return;
         }

         try {
            gameMode = this.matchGameMode(args[0].toLowerCase(Locale.ENGLISH));
         } catch (NotEnoughArgumentsException var7) {
            if (user.isAuthorized("essentials.gamemode.others")) {
               gameMode = this.matchGameMode(commandLabel);
               this.gamemodeOtherPlayers(server, user, gameMode, args[0]);
               return;
            }

            throw new NotEnoughArgumentsException();
         }
      }

      if (gameMode == null) {
         gameMode = user.getGameMode() == GameMode.SURVIVAL ? GameMode.CREATIVE : (user.getGameMode() == GameMode.CREATIVE ? GameMode.ADVENTURE : GameMode.SURVIVAL);
      }

      user.setGameMode(gameMode);
      user.sendMessage(I18n._("gameMode", I18n._(user.getGameMode().toString().toLowerCase(Locale.ENGLISH)), user.getDisplayName()));
   }

   private void gamemodeOtherPlayers(Server server, CommandSender sender, GameMode gameMode, String name) throws NotEnoughArgumentsException {
      if (name.trim().length() >= 2 && gameMode != null) {
         boolean skipHidden = sender instanceof Player && !this.ess.getUser(sender).isAuthorized("essentials.vanish.interact");
         boolean foundUser = false;

         for(Player matchPlayer : server.matchPlayer(name)) {
            User player = this.ess.getUser(matchPlayer);
            if (!skipHidden || !player.isHidden()) {
               foundUser = true;
               player.setGameMode(gameMode);
               sender.sendMessage(I18n._("gameMode", I18n._(player.getGameMode().toString().toLowerCase(Locale.ENGLISH)), player.getDisplayName()));
            }
         }

         if (!foundUser) {
            throw new NotEnoughArgumentsException(I18n._("playerNotFound"));
         }
      } else {
         throw new NotEnoughArgumentsException("You need to specify a valid player/mode.");
      }
   }

   private GameMode matchGameMode(String modeString) throws NotEnoughArgumentsException {
      GameMode mode = null;
      if (!modeString.equalsIgnoreCase("gmc") && !modeString.equalsIgnoreCase("egmc") && !modeString.contains("creat") && !modeString.equalsIgnoreCase("1") && !modeString.equalsIgnoreCase("c")) {
         if (!modeString.equalsIgnoreCase("gms") && !modeString.equalsIgnoreCase("egms") && !modeString.contains("survi") && !modeString.equalsIgnoreCase("0") && !modeString.equalsIgnoreCase("s")) {
            if (!modeString.equalsIgnoreCase("gma") && !modeString.equalsIgnoreCase("egma") && !modeString.contains("advent") && !modeString.equalsIgnoreCase("2") && !modeString.equalsIgnoreCase("a")) {
               if (!modeString.equalsIgnoreCase("gmt") && !modeString.equalsIgnoreCase("egmt") && !modeString.contains("toggle") && !modeString.contains("cycle") && !modeString.equalsIgnoreCase("t")) {
                  throw new NotEnoughArgumentsException();
               }

               mode = null;
            } else {
               mode = GameMode.ADVENTURE;
            }
         } else {
            mode = GameMode.SURVIVAL;
         }
      } else {
         mode = GameMode.CREATIVE;
      }

      return mode;
   }
}
