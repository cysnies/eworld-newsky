package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Commandjump extends EssentialsCommand {
   public Commandjump() {
      super("jump");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length > 0 && args[0].contains("lock") && user.isAuthorized("essentials.jump.lock")) {
         if (user.isFlyClickJump()) {
            user.setRightClickJump(false);
            user.sendMessage("Flying wizard mode disabled");
         } else {
            user.setRightClickJump(true);
            user.sendMessage("Enabling flying wizard mode");
         }

      } else {
         Location cloc = user.getLocation();

         Location loc;
         try {
            loc = Util.getTarget(user);
            loc.setYaw(cloc.getYaw());
            loc.setPitch(cloc.getPitch());
            loc.setY(loc.getY() + (double)1.0F);
         } catch (NullPointerException ex) {
            throw new Exception(I18n._("jumpError"), ex);
         }

         Trade charge = new Trade(this.getName(), this.ess);
         charge.isAffordableFor(user);
         user.getTeleport().teleport(loc, charge, TeleportCause.COMMAND);
         throw new NoChargeException();
      }
   }
}
