package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Commandworld extends EssentialsCommand {
   public Commandworld() {
      super("world");
   }

   protected void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      World world;
      if (args.length < 1) {
         World nether = null;
         List<World> worlds = server.getWorlds();

         for(World world2 : worlds) {
            if (world2.getEnvironment() == Environment.NETHER) {
               nether = world2;
               break;
            }
         }

         if (nether == null) {
            return;
         }

         world = user.getWorld() == nether ? (World)worlds.get(0) : nether;
      } else {
         world = this.ess.getWorld(getFinalArg(args, 0));
         if (world == null) {
            user.sendMessage(I18n._("invalidWorld"));
            user.sendMessage(I18n._("possibleWorlds", server.getWorlds().size() - 1));
            user.sendMessage(I18n._("typeWorldName"));
            throw new NoChargeException();
         }
      }

      if (this.ess.getSettings().isWorldTeleportPermissions() && !user.isAuthorized("essentials.worlds." + world.getName())) {
         throw new Exception(I18n._("noPerm", "essentials.worlds." + world.getName()));
      } else {
         double factor;
         if (user.getWorld().getEnvironment() == Environment.NETHER && world.getEnvironment() == Environment.NORMAL) {
            factor = (double)8.0F;
         } else if (user.getWorld().getEnvironment() == Environment.NORMAL && world.getEnvironment() == Environment.NETHER) {
            factor = (double)0.125F;
         } else {
            factor = (double)1.0F;
         }

         Location loc = user.getLocation();
         Location target = new Location(world, (double)loc.getBlockX() * factor + (double)0.5F, (double)loc.getBlockY(), (double)loc.getBlockZ() * factor + (double)0.5F);
         Trade charge = new Trade(this.getName(), this.ess);
         charge.isAffordableFor(user);
         user.getTeleport().teleport(target, charge, TeleportCause.COMMAND);
         throw new NoChargeException();
      }
   }
}
