package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.Kit;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

public class Commandkit extends EssentialsCommand {
   public Commandkit() {
      super("kit");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         String kitList = Kit.listKits(this.ess, user);
         user.sendMessage(kitList.length() > 0 ? I18n._("kits", kitList) : I18n._("noKits"));
         throw new NoChargeException();
      } else {
         if (args.length > 1 && user.isAuthorized("essentials.kit.others")) {
            User userTo = this.getPlayer(server, user, args, 1);
            String kitName = Util.sanitizeString(args[0].toLowerCase(Locale.ENGLISH)).trim();
            this.giveKit(userTo, user, kitName);
         } else {
            String kitName = Util.sanitizeString(args[0].toLowerCase(Locale.ENGLISH)).trim();
            this.giveKit(user, user, kitName);
         }

      }
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 2) {
         String kitList = Kit.listKits(this.ess, (User)null);
         sender.sendMessage(kitList.length() > 0 ? I18n._("kits", kitList) : I18n._("noKits"));
         throw new NoChargeException();
      } else {
         User userTo = this.getPlayer(server, args, 1, true, false);
         String kitName = args[0].toLowerCase(Locale.ENGLISH);
         Map<String, Object> kit = this.ess.getSettings().getKit(kitName);
         List<String> items = Kit.getItems(userTo, kit);
         Kit.expandItems(this.ess, userTo, items);
         sender.sendMessage(I18n._("kitGiveTo", kitName, userTo.getDisplayName()));
         userTo.sendMessage(I18n._("kitReceive", kitName));
      }
   }

   private void giveKit(User userTo, User userFrom, String kitName) throws Exception {
      if (kitName.isEmpty()) {
         throw new Exception(I18n._("kitError2"));
      } else {
         Map<String, Object> kit = this.ess.getSettings().getKit(kitName);
         if (!userFrom.isAuthorized("essentials.kits." + kitName)) {
            throw new Exception(I18n._("noKitPermission", "essentials.kits." + kitName));
         } else {
            List<String> items = Kit.getItems(userTo, kit);
            Kit.checkTime(userFrom, kitName, kit);
            Trade charge = new Trade("kit-" + kitName, this.ess);
            charge.isAffordableFor(userFrom);
            Kit.expandItems(this.ess, userTo, items);
            charge.charge(userFrom);
            userFrom.sendMessage(I18n._("kitGiveTo", kitName, userTo.getDisplayName()));
            userTo.sendMessage(I18n._("kitReceive", kitName));
         }
      }
   }
}
