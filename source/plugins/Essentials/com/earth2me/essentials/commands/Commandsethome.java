package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.util.Locale;
import org.bukkit.Location;
import org.bukkit.Server;

public class Commandsethome extends EssentialsCommand {
   public Commandsethome() {
      super("sethome");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      User usersHome = user;
      String name = "home";
      Location location = user.getLocation();
      if (args.length > 0) {
         String[] nameParts = args[0].split(":");
         if (nameParts[0].length() != args[0].length()) {
            args = nameParts;
         }

         if (args.length < 2) {
            name = args[0].toLowerCase(Locale.ENGLISH);
         } else if (user.isAuthorized("essentials.sethome.others")) {
            usersHome = this.ess.getUser(args[0]);
            if (usersHome == null) {
               throw new NoSuchFieldException(I18n._("playerNotFound"));
            }

            name = args[1].toLowerCase(Locale.ENGLISH);
         }
      }

      if (this.checkHomeLimit(user, usersHome, name)) {
         name = "home";
      }

      if (!"bed".equals(name) && !Util.isInt(name)) {
         usersHome.setHome(name, location);
         user.sendMessage(I18n._("homeSet", user.getLocation().getWorld().getName(), user.getLocation().getBlockX(), user.getLocation().getBlockY(), user.getLocation().getBlockZ()));
      } else {
         throw new NoSuchFieldException(I18n._("invalidHomeName"));
      }
   }

   private boolean checkHomeLimit(User user, User usersHome, String name) throws Exception {
      if (!user.isAuthorized("essentials.sethome.multiple.unlimited")) {
         int limit = this.ess.getSettings().getHomeLimit(user);
         if (usersHome.getHomes().size() == limit && usersHome.getHomes().contains(name)) {
            return false;
         }

         if (usersHome.getHomes().size() >= limit) {
            throw new Exception(I18n._("maxHomes", this.ess.getSettings().getHomeLimit(user)));
         }

         if (limit == 1) {
            return true;
         }
      }

      return false;
   }
}
