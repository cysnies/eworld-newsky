package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

public class Commandgetpos extends EssentialsCommand {
   public Commandgetpos() {
      super("getpos");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length > 0 && user.isAuthorized("essentials.getpos.others")) {
         User otherUser = this.getPlayer(server, args, 0, true, false);
         if (!otherUser.isHidden() || user.isAuthorized("essentials.list.hidden")) {
            this.outputPosition(user, otherUser.getLocation(), user.getLocation());
            return;
         }
      }

      this.outputPosition(user, user.getLocation(), (Location)null);
   }

   protected void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         User user = this.getPlayer(server, args, 0, true, false);
         this.outputPosition(sender, user.getLocation(), (Location)null);
      }
   }

   private void outputPosition(CommandSender sender, Location coords, Location distance) {
      sender.sendMessage(I18n._("currentWorld", coords.getWorld().getName()));
      sender.sendMessage(I18n._("posX", coords.getBlockX()));
      sender.sendMessage(I18n._("posY", coords.getBlockY()));
      sender.sendMessage(I18n._("posZ", coords.getBlockZ()));
      sender.sendMessage(I18n._("posYaw", (coords.getYaw() + 180.0F + 360.0F) % 360.0F));
      sender.sendMessage(I18n._("posPitch", coords.getPitch()));
      if (distance != null && coords.getWorld().equals(distance.getWorld())) {
         sender.sendMessage(I18n._("distance", coords.distance(distance)));
      }

   }
}
