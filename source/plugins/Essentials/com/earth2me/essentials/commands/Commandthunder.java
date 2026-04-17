package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.World;

public class Commandthunder extends EssentialsCommand {
   public Commandthunder() {
      super("thunder");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         World world = user.getWorld();
         boolean setThunder = args[0].equalsIgnoreCase("true");
         if (args.length > 1) {
            world.setThundering(setThunder);
            world.setThunderDuration(Integer.parseInt(args[1]) * 20);
            user.sendMessage(I18n._("thunderDuration", setThunder ? I18n._("enabled") : I18n._("disabled"), Integer.parseInt(args[1])));
         } else {
            world.setThundering(setThunder);
            user.sendMessage(I18n._("thunder", setThunder ? I18n._("enabled") : I18n._("disabled")));
         }

      }
   }
}
