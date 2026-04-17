package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.IEssentialsModule;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class EssentialsCommand implements IEssentialsCommand {
   private final transient String name;
   protected transient IEssentials ess;
   protected transient IEssentialsModule module;
   protected static final Logger logger = Logger.getLogger("Minecraft");

   protected EssentialsCommand(String name) {
      this.name = name;
   }

   public void setEssentials(IEssentials ess) {
      this.ess = ess;
   }

   public void setEssentialsModule(IEssentialsModule module) {
      this.module = module;
   }

   public String getName() {
      return this.name;
   }

   protected User getPlayer(Server server, String[] args, int pos) throws NoSuchFieldException, NotEnoughArgumentsException {
      return this.getPlayer(server, args, pos, false, false);
   }

   protected User getPlayer(Server server, User user, String[] args, int pos) throws NoSuchFieldException, NotEnoughArgumentsException {
      return this.getPlayer(server, args, pos, user.isAuthorized("essentials.vanish.interact"), false);
   }

   protected User getPlayer(Server server, String[] args, int pos, boolean getHidden, boolean getOffline) throws NoSuchFieldException, NotEnoughArgumentsException {
      if (args.length <= pos) {
         throw new NotEnoughArgumentsException();
      } else if (args[pos].isEmpty()) {
         throw new PlayerNotFoundException();
      } else {
         User user = this.ess.getUser(args[pos]);
         if (user != null) {
            if (!getOffline && !user.isOnline()) {
               throw new PlayerNotFoundException();
            } else if (!getHidden && user.isHidden()) {
               throw new PlayerNotFoundException();
            } else {
               return user;
            }
         } else {
            List<Player> matches = server.matchPlayer(args[pos]);
            if (!matches.isEmpty()) {
               for(Player player : matches) {
                  User userMatch = this.ess.getUser(player);
                  if (userMatch.getDisplayName().startsWith(args[pos]) && (getHidden || !userMatch.isHidden())) {
                     return userMatch;
                  }
               }

               User userMatch = this.ess.getUser(matches.get(0));
               if (getHidden || !userMatch.isHidden()) {
                  return userMatch;
               }
            }

            throw new PlayerNotFoundException();
         }
      }
   }

   public final void run(Server server, User user, String commandLabel, Command cmd, String[] args) throws Exception {
      Trade charge = new Trade(this.getName(), this.ess);
      charge.isAffordableFor(user);
      this.run(server, user, commandLabel, args);
      charge.charge(user);
   }

   protected void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      this.run(server, (CommandSender)user.getBase(), commandLabel, args);
   }

   public final void run(Server server, CommandSender sender, String commandLabel, Command cmd, String[] args) throws Exception {
      this.run(server, sender, commandLabel, args);
   }

   protected void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      throw new Exception(I18n._("onlyPlayers", commandLabel));
   }

   public static String getFinalArg(String[] args, int start) {
      StringBuilder bldr = new StringBuilder();

      for(int i = start; i < args.length; ++i) {
         if (i != start) {
            bldr.append(" ");
         }

         bldr.append(args[i]);
      }

      return bldr.toString();
   }
}
