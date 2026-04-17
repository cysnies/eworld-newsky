package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.util.List;
import java.util.Locale;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Commandhome extends EssentialsCommand {
   public Commandhome() {
      super("home");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      Trade charge = new Trade(this.getName(), this.ess);
      User player = user;
      String homeName = "";
      if (args.length > 0) {
         String[] nameParts = args[0].split(":");
         if (nameParts[0].length() != args[0].length() && user.isAuthorized("essentials.home.others")) {
            player = this.getPlayer(server, nameParts, 0, true, true);
            if (nameParts.length > 1) {
               homeName = nameParts[1];
            }
         } else {
            homeName = nameParts[0];
         }
      }

      try {
         if ("bed".equalsIgnoreCase(homeName) && user.isAuthorized("essentials.home.bed")) {
            Location bed = player.getBedSpawnLocation();
            if (bed != null) {
               user.getTeleport().teleport(bed, charge, TeleportCause.COMMAND);
               throw new NoChargeException();
            }

            throw new Exception(I18n._("bedMissing"));
         }

         this.goHome(user, player, homeName.toLowerCase(Locale.ENGLISH), charge);
      } catch (NotEnoughArgumentsException var12) {
         Location bed = player.getBedSpawnLocation();
         List<String> homes = player.getHomes();
         if (homes.isEmpty() && player.equals(user)) {
            user.getTeleport().respawn(charge, TeleportCause.COMMAND);
         } else {
            if (homes.isEmpty()) {
               throw new Exception(I18n._("noHomeSetPlayer"));
            }

            if (homes.size() == 1 && player.equals(user)) {
               this.goHome(user, player, (String)homes.get(0), charge);
            } else {
               if (user.isAuthorized("essentials.home.bed")) {
                  if (bed != null) {
                     homes.add(I18n._("bed"));
                  } else {
                     homes.add(I18n._("bedNull"));
                  }
               }

               user.sendMessage(I18n._("homes", Util.joinList(homes)));
            }
         }
      }

      throw new NoChargeException();
   }

   private void goHome(User user, User player, String home, Trade charge) throws Exception {
      Location loc = player.getHome(home);
      if (loc == null) {
         throw new NotEnoughArgumentsException();
      } else if (user.getWorld() != loc.getWorld() && this.ess.getSettings().isWorldHomePermissions() && !user.isAuthorized("essentials.worlds." + loc.getWorld().getName())) {
         throw new Exception(I18n._("noPerm", "essentials.worlds." + loc.getWorld().getName()));
      } else {
         user.getTeleport().home(loc, charge);
      }
   }
}
