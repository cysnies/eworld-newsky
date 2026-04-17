package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Server;

public class Commandcompass extends EssentialsCommand {
   public Commandcompass() {
      super("compass");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      int bearing = (int)(user.getLocation().getYaw() + 180.0F + 360.0F) % 360;
      String dir;
      if (bearing < 23) {
         dir = "N";
      } else if (bearing < 68) {
         dir = "NE";
      } else if (bearing < 113) {
         dir = "E";
      } else if (bearing < 158) {
         dir = "SE";
      } else if (bearing < 203) {
         dir = "S";
      } else if (bearing < 248) {
         dir = "SW";
      } else if (bearing < 293) {
         dir = "W";
      } else if (bearing < 338) {
         dir = "NW";
      } else {
         dir = "N";
      }

      user.sendMessage(I18n._("compassBearing", dir, bearing));
   }
}
