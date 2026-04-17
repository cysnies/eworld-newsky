package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

public class Commandweather extends EssentialsCommand {
   public Commandweather() {
      super("weather");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      boolean isStorm;
      if (args.length < 1) {
         if (!commandLabel.equalsIgnoreCase("sun") && !commandLabel.equalsIgnoreCase("esun")) {
            if (!commandLabel.equalsIgnoreCase("storm") && !commandLabel.equalsIgnoreCase("estorm") && !commandLabel.equalsIgnoreCase("rain") && !commandLabel.equalsIgnoreCase("erain")) {
               throw new NotEnoughArgumentsException();
            }

            isStorm = true;
         } else {
            isStorm = false;
         }
      } else {
         isStorm = args[0].equalsIgnoreCase("storm");
      }

      World world = user.getWorld();
      if (args.length > 1) {
         world.setStorm(isStorm);
         world.setWeatherDuration(Integer.parseInt(args[1]) * 20);
         user.sendMessage(isStorm ? I18n._("weatherStormFor", world.getName(), args[1]) : I18n._("weatherSunFor", world.getName(), args[1]));
      } else {
         world.setStorm(isStorm);
         user.sendMessage(isStorm ? I18n._("weatherStorm", world.getName()) : I18n._("weatherSun", world.getName()));
      }

   }

   protected void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 2) {
         throw new Exception("When running from console, usage is: /" + commandLabel + " <world> <storm/sun> [duration]");
      } else {
         boolean isStorm = args[1].equalsIgnoreCase("storm");
         World world = server.getWorld(args[0]);
         if (world == null) {
            throw new Exception("World named " + args[0] + " not found!");
         } else {
            if (args.length > 2) {
               world.setStorm(isStorm);
               world.setWeatherDuration(Integer.parseInt(args[2]) * 20);
               sender.sendMessage(isStorm ? I18n._("weatherStormFor", world.getName(), args[2]) : I18n._("weatherSunFor", world.getName(), args[2]));
            } else {
               world.setStorm(isStorm);
               sender.sendMessage(isStorm ? I18n._("weatherStorm", world.getName()) : I18n._("weatherSun", world.getName()));
            }

         }
      }
   }
}
