package com.earth2me.essentials.commands;

import com.earth2me.essentials.DescParseTickFormat;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

public class Commandtime extends EssentialsCommand {
   public Commandtime() {
      super("time");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      boolean add = false;
      List<String> argList = new ArrayList(Arrays.asList(args));
      if (argList.remove("set") && !argList.isEmpty() && Util.isInt((String)argList.get(0))) {
         argList.set(0, (String)argList.get(0) + "t");
      }

      if (argList.remove("add") && !argList.isEmpty() && Util.isInt((String)argList.get(0))) {
         add = true;
         argList.set(0, (String)argList.get(0) + "t");
      }

      String[] validArgs = (String[])argList.toArray(new String[0]);
      String worldSelector = null;
      if (validArgs.length == 2) {
         worldSelector = validArgs[1];
      }

      Set<World> worlds = this.getWorlds(server, sender, worldSelector);
      String setTime;
      if (validArgs.length == 0) {
         if (!commandLabel.equalsIgnoreCase("day") && !commandLabel.equalsIgnoreCase("eday")) {
            if (!commandLabel.equalsIgnoreCase("night") && !commandLabel.equalsIgnoreCase("enight")) {
               this.getWorldsTime(sender, worlds);
               return;
            }

            setTime = "night";
         } else {
            setTime = "day";
         }
      } else {
         setTime = validArgs[0];
      }

      User user = this.ess.getUser(sender);
      if (user != null && !user.isAuthorized("essentials.time.set")) {
         user.sendMessage(I18n._("timeSetPermission"));
      } else {
         long ticks;
         try {
            ticks = DescParseTickFormat.parse(setTime);
         } catch (NumberFormatException e) {
            throw new NotEnoughArgumentsException(e);
         }

         this.setWorldsTime(sender, worlds, ticks, add);
      }
   }

   private void getWorldsTime(CommandSender sender, Collection worlds) {
      if (worlds.size() == 1) {
         Iterator<World> iter = worlds.iterator();
         sender.sendMessage(DescParseTickFormat.format(((World)iter.next()).getTime()));
      } else {
         for(World world : worlds) {
            sender.sendMessage(I18n._("timeWorldCurrent", world.getName(), DescParseTickFormat.format(world.getTime())));
         }

      }
   }

   private void setWorldsTime(CommandSender sender, Collection worlds, long ticks, boolean add) {
      for(World world : worlds) {
         long time = world.getTime();
         if (!add) {
            time -= time % 24000L;
         }

         world.setTime(time + (long)(add ? 0 : 24000) + ticks);
      }

      StringBuilder output = new StringBuilder();

      for(World world : worlds) {
         if (output.length() > 0) {
            output.append(", ");
         }

         output.append(world.getName());
      }

      sender.sendMessage(I18n._("timeWorldSet", DescParseTickFormat.format(ticks), output.toString()));
   }

   private Set getWorlds(Server server, CommandSender sender, String selector) throws Exception {
      Set<World> worlds = new TreeSet(new WorldNameComparator());
      if (selector == null) {
         User user = this.ess.getUser(sender);
         if (user == null) {
            worlds.addAll(server.getWorlds());
         } else {
            worlds.add(user.getWorld());
         }

         return worlds;
      } else {
         World world = server.getWorld(selector);
         if (world != null) {
            worlds.add(world);
         } else {
            if (!selector.equalsIgnoreCase("*") && !selector.equalsIgnoreCase("all")) {
               throw new Exception(I18n._("invalidWorld"));
            }

            worlds.addAll(server.getWorlds());
         }

         return worlds;
      }
   }
}
