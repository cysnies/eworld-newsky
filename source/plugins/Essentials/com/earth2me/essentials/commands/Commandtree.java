package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.TreeType;

public class Commandtree extends EssentialsCommand {
   public Commandtree() {
      super("tree");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         TreeType tree;
         if (args[0].equalsIgnoreCase("birch")) {
            tree = TreeType.BIRCH;
         } else if (args[0].equalsIgnoreCase("redwood")) {
            tree = TreeType.REDWOOD;
         } else if (args[0].equalsIgnoreCase("tree")) {
            tree = TreeType.TREE;
         } else if (args[0].equalsIgnoreCase("redmushroom")) {
            tree = TreeType.RED_MUSHROOM;
         } else if (args[0].equalsIgnoreCase("brownmushroom")) {
            tree = TreeType.BROWN_MUSHROOM;
         } else if (args[0].equalsIgnoreCase("jungle")) {
            tree = TreeType.SMALL_JUNGLE;
         } else if (args[0].equalsIgnoreCase("junglebush")) {
            tree = TreeType.JUNGLE_BUSH;
         } else {
            if (!args[0].equalsIgnoreCase("swamp")) {
               throw new NotEnoughArgumentsException();
            }

            tree = TreeType.SWAMP;
         }

         Location loc = Util.getTarget(user);
         Location safeLocation = Util.getSafeDestination(loc);
         boolean success = user.getWorld().generateTree(safeLocation, tree);
         if (success) {
            user.sendMessage(I18n._("treeSpawned"));
         } else {
            user.sendMessage(I18n._("treeFailure"));
         }

      }
   }
}
