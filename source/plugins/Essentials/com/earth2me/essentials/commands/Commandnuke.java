package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;

public class Commandnuke extends EssentialsCommand {
   public Commandnuke() {
      super("nuke");
   }

   protected void run(Server server, CommandSender sender, String commandLabel, String[] args) throws NoSuchFieldException, NotEnoughArgumentsException {
      List<Player> targets;
      if (args.length > 0) {
         targets = new ArrayList();
         int pos = 0;

         for(String var10000 : args) {
            targets.add(this.getPlayer(server, args, pos));
            ++pos;
         }
      } else {
         targets = Arrays.asList(server.getOnlinePlayers());
      }

      this.ess.getTNTListener().enable();

      for(Player player : targets) {
         if (player != null) {
            player.sendMessage(I18n._("nuke"));
            Location loc = player.getLocation();
            World world = loc.getWorld();

            for(int x = -10; x <= 10; x += 5) {
               for(int z = -10; z <= 10; z += 5) {
                  Location tntloc = new Location(world, (double)(loc.getBlockX() + x), (double)(world.getHighestBlockYAt(loc) + 64), (double)(loc.getBlockZ() + z));
                  TNTPrimed tnt = (TNTPrimed)world.spawn(tntloc, TNTPrimed.class);
               }
            }
         }
      }

   }
}
