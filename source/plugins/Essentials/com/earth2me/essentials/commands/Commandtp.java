package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Commandtp extends EssentialsCommand {
   public Commandtp() {
      super("tp");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      switch (args.length) {
         case 0:
            throw new NotEnoughArgumentsException();
         case 1:
            User player = this.getPlayer(server, args, 0);
            if (!player.isTeleportEnabled()) {
               throw new Exception(I18n._("teleportDisabled", player.getDisplayName()));
            }

            if (user.getWorld() != player.getWorld() && this.ess.getSettings().isWorldTeleportPermissions() && !user.isAuthorized("essentials.worlds." + player.getWorld().getName())) {
               throw new Exception(I18n._("noPerm", "essentials.worlds." + player.getWorld().getName()));
            }

            user.sendMessage(I18n._("teleporting"));
            Trade charge = new Trade(this.getName(), this.ess);
            charge.isAffordableFor(user);
            user.getTeleport().teleport((Player)player, charge, TeleportCause.COMMAND);
            throw new NoChargeException();
         case 2:
         case 3:
         default:
            if (!user.isAuthorized("essentials.tp.others")) {
               throw new Exception(I18n._("noPerm", "essentials.tp.others"));
            }

            User target = this.getPlayer(server, args, 0);
            User toPlayer = this.getPlayer(server, args, 1);
            if (!target.isTeleportEnabled()) {
               throw new Exception(I18n._("teleportDisabled", target.getDisplayName()));
            }

            if (!toPlayer.isTeleportEnabled()) {
               throw new Exception(I18n._("teleportDisabled", toPlayer.getDisplayName()));
            }

            if (target.getWorld() != toPlayer.getWorld() && this.ess.getSettings().isWorldTeleportPermissions() && !user.isAuthorized("essentials.worlds." + toPlayer.getWorld().getName())) {
               throw new Exception(I18n._("noPerm", "essentials.worlds." + toPlayer.getWorld().getName()));
            }

            target.getTeleport().now((Player)toPlayer, false, TeleportCause.COMMAND);
            user.sendMessage(I18n._("teleporting"));
            target.sendMessage(I18n._("teleportAtoB", user.getDisplayName(), toPlayer.getDisplayName()));
            break;
         case 4:
            if (!user.isAuthorized("essentials.tp.others")) {
               throw new Exception(I18n._("noPerm", "essentials.tp.others"));
            }

            User target2 = this.getPlayer(server, args, 0);
            double x = args[1].startsWith("~") ? target2.getLocation().getX() + (double)Integer.parseInt(args[1].substring(1)) : (double)Integer.parseInt(args[1]);
            double y = args[2].startsWith("~") ? target2.getLocation().getY() + (double)Integer.parseInt(args[2].substring(1)) : (double)Integer.parseInt(args[2]);
            double z = args[3].startsWith("~") ? target2.getLocation().getZ() + (double)Integer.parseInt(args[3].substring(1)) : (double)Integer.parseInt(args[3]);
            if (x > (double)3.0E7F || y > (double)3.0E7F || z > (double)3.0E7F || x < (double)-3.0E7F || y < (double)-3.0E7F || z < (double)-3.0E7F) {
               throw new NotEnoughArgumentsException("Value of coordinates cannot be over 30000000");
            }

            Location location = new Location(target2.getWorld(), x, y, z, target2.getLocation().getYaw(), target2.getLocation().getPitch());
            if (!target2.isTeleportEnabled()) {
               throw new Exception(I18n._("teleportDisabled", target2.getDisplayName()));
            }

            target2.getTeleport().now(location, false, TeleportCause.COMMAND);
            user.sendMessage(I18n._("teleporting"));
            target2.sendMessage(I18n._("teleporting"));
      }

   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 2) {
         throw new NotEnoughArgumentsException();
      } else {
         User target = this.getPlayer(server, args, 0);
         if (args.length == 2) {
            User toPlayer = this.getPlayer(server, args, 1);
            target.getTeleport().now((Player)toPlayer, false, TeleportCause.COMMAND);
            target.sendMessage(I18n._("teleportAtoB", "Console", toPlayer.getDisplayName()));
         } else {
            if (args.length <= 3) {
               throw new NotEnoughArgumentsException();
            }

            double x = args[1].startsWith("~") ? target.getLocation().getX() + (double)Integer.parseInt(args[1].substring(1)) : (double)Integer.parseInt(args[1]);
            double y = args[2].startsWith("~") ? target.getLocation().getY() + (double)Integer.parseInt(args[2].substring(1)) : (double)Integer.parseInt(args[2]);
            double z = args[3].startsWith("~") ? target.getLocation().getZ() + (double)Integer.parseInt(args[3].substring(1)) : (double)Integer.parseInt(args[3]);
            if (x > (double)3.0E7F || y > (double)3.0E7F || z > (double)3.0E7F || x < (double)-3.0E7F || y < (double)-3.0E7F || z < (double)-3.0E7F) {
               throw new NotEnoughArgumentsException("Value of coordinates cannot be over 30000000");
            }

            Location location = new Location(target.getWorld(), x, y, z, target.getLocation().getYaw(), target.getLocation().getPitch());
            target.getTeleport().now(location, false, TeleportCause.COMMAND);
            target.sendMessage(I18n._("teleporting"));
         }

         sender.sendMessage(I18n._("teleporting"));
      }
   }
}
