package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Commandtppos extends EssentialsCommand {
   public Commandtppos() {
      super("tppos");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length < 3) {
         throw new NotEnoughArgumentsException();
      } else {
         double x = args[0].startsWith("~") ? user.getLocation().getX() + (double)Integer.parseInt(args[0].substring(1)) : (double)Integer.parseInt(args[0]);
         double y = args[1].startsWith("~") ? user.getLocation().getY() + (double)Integer.parseInt(args[1].substring(1)) : (double)Integer.parseInt(args[1]);
         double z = args[2].startsWith("~") ? user.getLocation().getZ() + (double)Integer.parseInt(args[2].substring(1)) : (double)Integer.parseInt(args[2]);
         Location location = new Location(user.getWorld(), x, y, z, user.getLocation().getYaw(), user.getLocation().getPitch());
         if (args.length > 3) {
            location.setYaw((Float.parseFloat(args[3]) + 180.0F + 360.0F) % 360.0F);
         }

         if (args.length > 4) {
            location.setPitch(Float.parseFloat(args[4]));
         }

         if (!(x > (double)3.0E7F) && !(y > (double)3.0E7F) && !(z > (double)3.0E7F) && !(x < (double)-3.0E7F) && !(y < (double)-3.0E7F) && !(z < (double)-3.0E7F)) {
            Trade charge = new Trade(this.getName(), this.ess);
            charge.isAffordableFor(user);
            user.sendMessage(I18n._("teleporting"));
            user.getTeleport().teleport(location, charge, TeleportCause.COMMAND);
            throw new NoChargeException();
         } else {
            throw new NotEnoughArgumentsException("Value of coordinates cannot be over 30000000");
         }
      }
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 4) {
         throw new NotEnoughArgumentsException();
      } else {
         User user = this.getPlayer(server, args, 0, true, false);
         double x = args[1].startsWith("~") ? user.getLocation().getX() + (double)Integer.parseInt(args[1].substring(1)) : (double)Integer.parseInt(args[1]);
         double y = args[2].startsWith("~") ? user.getLocation().getY() + (double)Integer.parseInt(args[2].substring(1)) : (double)Integer.parseInt(args[2]);
         double z = args[3].startsWith("~") ? user.getLocation().getZ() + (double)Integer.parseInt(args[3].substring(1)) : (double)Integer.parseInt(args[3]);
         Location location = new Location(user.getWorld(), x, y, z, user.getLocation().getYaw(), user.getLocation().getPitch());
         if (args.length > 4) {
            location.setYaw((Float.parseFloat(args[4]) + 180.0F + 360.0F) % 360.0F);
         }

         if (args.length > 5) {
            location.setPitch(Float.parseFloat(args[5]));
         }

         if (!(x > (double)3.0E7F) && !(y > (double)3.0E7F) && !(z > (double)3.0E7F) && !(x < (double)-3.0E7F) && !(y < (double)-3.0E7F) && !(z < (double)-3.0E7F)) {
            sender.sendMessage(I18n._("teleporting"));
            user.sendMessage(I18n._("teleporting"));
            user.getTeleport().teleport((Location)location, (Trade)null, TeleportCause.COMMAND);
         } else {
            throw new NotEnoughArgumentsException("Value of coordinates cannot be over 30000000");
         }
      }
   }
}
