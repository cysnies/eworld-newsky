package com.earth2me.essentials.commands;

import com.earth2me.essentials.DescParseTickFormat;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandptime extends EssentialsCommand {
   public static final Set getAliases = new HashSet();

   public Commandptime() {
      super("ptime");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      String userSelector = null;
      if (args.length == 2) {
         userSelector = args[1];
      }

      Set<User> users = this.getUsers(server, sender, userSelector);
      if (args.length == 0) {
         this.getUsersTime(sender, users);
      } else {
         User user = this.ess.getUser(sender);
         if (user != null && (!users.contains(user) || users.size() > 1) && !user.isAuthorized("essentials.ptime.others")) {
            user.sendMessage(I18n._("pTimeOthersPermission"));
         } else {
            String timeParam = args[0];
            boolean relative = true;
            if (timeParam.startsWith("@")) {
               relative = false;
               timeParam = timeParam.substring(1);
            }

            if (getAliases.contains(timeParam)) {
               this.getUsersTime(sender, users);
            } else {
               Long ticks;
               if (DescParseTickFormat.meansReset(timeParam)) {
                  ticks = null;
               } else {
                  try {
                     ticks = DescParseTickFormat.parse(timeParam);
                  } catch (NumberFormatException e) {
                     throw new NotEnoughArgumentsException(e);
                  }
               }

               this.setUsersTime(sender, users, ticks, relative);
            }
         }
      }
   }

   private void getUsersTime(CommandSender sender, Collection users) {
      if (users.size() > 1) {
         sender.sendMessage(I18n._("pTimePlayers"));
      }

      for(User user : users) {
         if (user.getPlayerTimeOffset() == 0L) {
            sender.sendMessage(I18n._("pTimeNormal", user.getName()));
         } else {
            String time = DescParseTickFormat.format(user.getPlayerTime());
            if (!user.isPlayerTimeRelative()) {
               sender.sendMessage(I18n._("pTimeCurrentFixed", user.getName(), time));
            } else {
               sender.sendMessage(I18n._("pTimeCurrent", user.getName(), time));
            }
         }
      }

   }

   private void setUsersTime(CommandSender sender, Collection users, Long ticks, Boolean relative) {
      if (ticks == null) {
         for(User user : users) {
            user.resetPlayerTime();
         }
      } else {
         for(User user : users) {
            World world = user.getWorld();
            long time = user.getPlayerTime();
            time -= time % 24000L;
            time += 24000L + ticks;
            if (relative) {
               time -= world.getTime();
            }

            user.setPlayerTime(time, relative);
         }
      }

      StringBuilder msg = new StringBuilder();

      for(User user : users) {
         if (msg.length() > 0) {
            msg.append(", ");
         }

         msg.append(user.getName());
      }

      if (ticks == null) {
         sender.sendMessage(I18n._("pTimeReset", msg.toString()));
      } else {
         String time = DescParseTickFormat.format(ticks);
         if (!relative) {
            sender.sendMessage(I18n._("pTimeSetFixed", time, msg.toString()));
         } else {
            sender.sendMessage(I18n._("pTimeSet", time, msg.toString()));
         }
      }

   }

   private Set getUsers(Server server, CommandSender sender, String selector) throws Exception {
      Set<User> users = new TreeSet(new UserNameComparator());
      if (selector == null) {
         User user = this.ess.getUser(sender);
         if (user == null) {
            for(Player player : server.getOnlinePlayers()) {
               users.add(this.ess.getUser(player));
            }
         } else {
            users.add(user);
         }

         return users;
      } else {
         User user = null;
         List<Player> matchedPlayers = server.matchPlayer(selector);
         if (!matchedPlayers.isEmpty()) {
            user = this.ess.getUser(matchedPlayers.get(0));
         }

         if (user != null) {
            users.add(user);
         } else {
            if (!selector.equalsIgnoreCase("*") && !selector.equalsIgnoreCase("all")) {
               throw new Exception(I18n._("playerNotFound"));
            }

            for(Player player : server.getOnlinePlayers()) {
               users.add(this.ess.getUser(player));
            }
         }

         return users;
      }
   }

   static {
      getAliases.add("get");
      getAliases.add("list");
      getAliases.add("show");
      getAliases.add("display");
   }
}
