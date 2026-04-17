package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.TreeType;

public class Commandbigtree extends EssentialsCommand {
   public Commandbigtree() {
      super("bigtree");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      TreeType tree;
      if (args.length > 0 && args[0].equalsIgnoreCase("redwood")) {
         tree = TreeType.TALL_REDWOOD;
      } else if (args.length > 0 && args[0].equalsIgnoreCase("tree")) {
         tree = TreeType.BIG_TREE;
      } else {
         if (args.length <= 0 || !args[0].equalsIgnoreCase("jungle")) {
            throw new NotEnoughArgumentsException();
         }

         tree = TreeType.JUNGLE;
      }

      Location loc = Util.getTarget(user);
      Location safeLocation = Util.getSafeDestination(loc);
      boolean success = user.getWorld().generateTree(safeLocation, tree);
      if (success) {
         user.sendMessage(I18n._("bigTreeSuccess"));
      } else {
         throw new Exception(I18n._("bigTreeFailure"));
      }
   }
}
