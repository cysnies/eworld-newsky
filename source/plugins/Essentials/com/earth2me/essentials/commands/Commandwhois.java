package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import com.earth2me.essentials.craftbukkit.SetExpFix;
import java.util.Locale;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandwhois extends EssentialsCommand {
   public Commandwhois() {
      super("whois");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         boolean showhidden = false;
         if (sender instanceof Player) {
            if (this.ess.getUser(sender).isAuthorized("essentials.list.hidden")) {
               showhidden = true;
            }
         } else {
            showhidden = true;
         }

         String whois = args[0].toLowerCase(Locale.ENGLISH);
         int prefixLength = Util.stripFormat(this.ess.getSettings().getNicknamePrefix()).length();
         boolean foundUser = false;

         for(Player onlinePlayer : server.getOnlinePlayers()) {
            User user = this.ess.getUser(onlinePlayer);
            if (!user.isHidden() || showhidden) {
               String nickName = Util.stripFormat(user.getNickname());
               if (whois.equalsIgnoreCase(nickName) || whois.substring(prefixLength).equalsIgnoreCase(nickName) || whois.equalsIgnoreCase(user.getName())) {
                  foundUser = true;
                  sender.sendMessage(I18n._("whoisTop", user.getName()));
                  user.setDisplayNick();
                  sender.sendMessage(I18n._("whoisNick", user.getDisplayName()));
                  sender.sendMessage(I18n._("whoisHealth", user.getHealth()));
                  sender.sendMessage(I18n._("whoisExp", SetExpFix.getTotalExperience(user), user.getLevel()));
                  sender.sendMessage(I18n._("whoisLocation", user.getLocation().getWorld().getName(), user.getLocation().getBlockX(), user.getLocation().getBlockY(), user.getLocation().getBlockZ()));
                  if (!this.ess.getSettings().isEcoDisabled()) {
                     sender.sendMessage(I18n._("whoisMoney", Util.displayCurrency(user.getMoney(), this.ess)));
                  }

                  sender.sendMessage(I18n._("whoisIPAddress", user.getAddress().getAddress().toString()));
                  String location = user.getGeoLocation();
                  if (location != null && (!(sender instanceof Player) || this.ess.getUser(sender).isAuthorized("essentials.geoip.show"))) {
                     sender.sendMessage(I18n._("whoisGeoLocation", location));
                  }

                  sender.sendMessage(I18n._("whoisGamemode", I18n._(user.getGameMode().toString().toLowerCase(Locale.ENGLISH))));
                  sender.sendMessage(I18n._("whoisGod", user.isGodModeEnabled() ? I18n._("true") : I18n._("false")));
                  sender.sendMessage(I18n._("whoisOp", user.isOp() ? I18n._("true") : I18n._("false")));
                  sender.sendMessage(I18n._("whoisFly", user.getAllowFlight() ? I18n._("true") : I18n._("false"), user.isFlying() ? I18n._("flying") : I18n._("notFlying")));
                  sender.sendMessage(I18n._("whoisAFK", user.isAfk() ? I18n._("true") : I18n._("false")));
                  sender.sendMessage(I18n._("whoisJail", user.isJailed() ? (user.getJailTimeout() > 0L ? Util.formatDateDiff(user.getJailTimeout()) : I18n._("true")) : I18n._("false")));
                  sender.sendMessage(I18n._("whoisMuted", user.isMuted() ? (user.getMuteTimeout() > 0L ? Util.formatDateDiff(user.getMuteTimeout()) : I18n._("true")) : I18n._("false")));
               }
            }
         }

         if (!foundUser) {
            throw new NoSuchFieldException(I18n._("playerNotFound"));
         }
      }
   }
}
