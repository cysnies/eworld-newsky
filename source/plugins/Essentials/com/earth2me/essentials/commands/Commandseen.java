package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.UserMap;
import com.earth2me.essentials.Util;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandseen extends EssentialsCommand {
   public Commandseen() {
      super("seen");
   }

   protected void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      this.seen(server, sender, args, true, true, true);
   }

   protected void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      this.seen(server, user, args, user.isAuthorized("essentials.seen.banreason"), user.isAuthorized("essentials.seen.extra"), user.isAuthorized("essentials.seen.ipsearch"));
   }

   protected void seen(Server server, CommandSender sender, String[] args, boolean showBan, boolean extra, boolean ipLookup) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         try {
            User user = this.getPlayer(server, args, 0);
            this.seenOnline(server, sender, user, showBan, extra);
         } catch (NoSuchFieldException var9) {
            User player = this.ess.getOfflineUser(args[0]);
            if (player == null) {
               if (ipLookup && Util.validIP(args[0])) {
                  this.seenIP(server, sender, args[0]);
                  return;
               }

               throw new Exception(I18n._("playerNotFound"));
            }

            this.seenOffline(server, sender, player, showBan, extra);
         }

      }
   }

   private void seenOnline(Server server, CommandSender sender, User user, boolean showBan, boolean extra) throws Exception {
      user.setDisplayNick();
      sender.sendMessage(I18n._("seenOnline", user.getDisplayName(), Util.formatDateDiff(user.getLastLogin())));
      if (user.isAfk()) {
         sender.sendMessage(I18n._("whoisAFK", I18n._("true")));
      }

      if (user.isJailed()) {
         sender.sendMessage(I18n._("whoisJail", user.getJailTimeout() > 0L ? Util.formatDateDiff(user.getJailTimeout()) : I18n._("true")));
      }

      if (user.isMuted()) {
         sender.sendMessage(I18n._("whoisMuted", user.getMuteTimeout() > 0L ? Util.formatDateDiff(user.getMuteTimeout()) : I18n._("true")));
      }

      if (extra) {
         sender.sendMessage(I18n._("whoisIPAddress", user.getAddress().getAddress().toString()));
         String location = user.getGeoLocation();
         if (location != null && (!(sender instanceof Player) || this.ess.getUser(sender).isAuthorized("essentials.geoip.show"))) {
            sender.sendMessage(I18n._("whoisGeoLocation", location));
         }
      }

   }

   private void seenOffline(Server server, CommandSender sender, User user, boolean showBan, boolean extra) throws Exception {
      user.setDisplayNick();
      if (user.getLastLogout() > 0L) {
         sender.sendMessage(I18n._("seenOffline", user.getName(), Util.formatDateDiff(user.getLastLogout())));
      } else {
         sender.sendMessage(I18n._("userUnknown", user.getName()));
      }

      if (user.isBanned()) {
         sender.sendMessage(I18n._("whoisBanned", showBan ? user.getBanReason() : I18n._("true")));
      }

      if (extra) {
         if (!user.getLastLoginAddress().isEmpty()) {
            sender.sendMessage(I18n._("whoisIPAddress", user.getLastLoginAddress()));
         }

         String location = user.getGeoLocation();
         if (location != null && (!(sender instanceof Player) || this.ess.getUser(sender).isAuthorized("essentials.geoip.show"))) {
            sender.sendMessage(I18n._("whoisGeoLocation", location));
         }

         Location loc = user.getLogoutLocation();
         if (loc != null) {
            sender.sendMessage(I18n._("whoisLocation", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
         }
      }

   }

   private void seenIP(Server server, final CommandSender sender, final String ipAddress) throws Exception {
      final UserMap userMap = this.ess.getUserMap();
      sender.sendMessage(I18n._("runningPlayerMatch", ipAddress));
      this.ess.runTaskAsynchronously(new Runnable() {
         public void run() {
            List<String> matches = new ArrayList();

            for(String u : userMap.getAllUniqueUsers()) {
               User user = Commandseen.this.ess.getUserMap().getUser(u);
               if (user != null) {
                  String uIPAddress = user.getLastLoginAddress();
                  if (!uIPAddress.isEmpty() && uIPAddress.equalsIgnoreCase(ipAddress)) {
                     matches.add(user.getName());
                  }
               }
            }

            if (matches.size() > 0) {
               sender.sendMessage(I18n._("matchingIPAddress"));
               sender.sendMessage(Util.joinList(matches));
            } else {
               sender.sendMessage(I18n._("noMatchingPlayers"));
            }

         }
      });
   }
}
