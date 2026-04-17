package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import org.bukkit.Server;

public class Commandback extends EssentialsCommand {
   public Commandback() {
      super("back");
   }

   protected void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (user.getWorld() != user.getLastLocation().getWorld() && this.ess.getSettings().isWorldTeleportPermissions() && !user.isAuthorized("essentials.worlds." + user.getLastLocation().getWorld().getName())) {
         throw new Exception(I18n._("noPerm", "essentials.worlds." + user.getLastLocation().getWorld().getName()));
      } else {
         Trade charge = new Trade(this.getName(), this.ess);
         charge.isAffordableFor(user);
         user.sendMessage(I18n._("backUsageMsg"));
         user.getTeleport().back(charge);
         throw new NoChargeException();
      }
   }
}
