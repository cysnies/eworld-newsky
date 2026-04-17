package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import java.util.Locale;
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

public class Commandremove extends EssentialsCommand {
   public Commandremove() {
      super("remove");
   }

   protected void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         World world = user.getWorld();
         int radius = 0;
         if (args.length < 2) {
            try {
               radius = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
               throw new Exception(I18n._("numberRequired"), e);
            }
         }

         ToRemove toRemove;
         try {
            toRemove = Commandremove.ToRemove.valueOf(args[0].toUpperCase(Locale.ENGLISH));
         } catch (IllegalArgumentException e) {
            throw new NotEnoughArgumentsException(e);
         }

         this.removeEntities(user, world, toRemove, radius);
      }
   }

   protected void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 2) {
         throw new NotEnoughArgumentsException();
      } else {
         World world = this.ess.getWorld(args[1]);
         if (world == null) {
            throw new Exception(I18n._("invalidWorld"));
         } else {
            ToRemove toRemove;
            try {
               toRemove = Commandremove.ToRemove.valueOf(args[0].toUpperCase(Locale.ENGLISH));
            } catch (IllegalArgumentException e) {
               throw new NotEnoughArgumentsException(e);
            }

            this.removeEntities(sender, world, toRemove, 0);
         }
      }
   }

   protected void removeEntities(CommandSender sender, World world, ToRemove toRemove, int radius) throws Exception {
      int removed = 0;
      if (radius > 0) {
         radius *= radius;
      }

      for(Chunk chunk : world.getLoadedChunks()) {
         for(Entity e : chunk.getEntities()) {
            if (radius <= 0 || !(((Player)sender).getLocation().distanceSquared(e.getLocation()) > (double)radius)) {
               if (toRemove == Commandremove.ToRemove.DROPS) {
                  if (e instanceof Item) {
                     e.remove();
                     ++removed;
                  }
               } else if (toRemove == Commandremove.ToRemove.ARROWS) {
                  if (e instanceof Projectile) {
                     e.remove();
                     ++removed;
                  }
               } else if (toRemove == Commandremove.ToRemove.BOATS) {
                  if (e instanceof Boat) {
                     e.remove();
                     ++removed;
                  }
               } else if (toRemove == Commandremove.ToRemove.MINECARTS) {
                  if (e instanceof Minecart) {
                     e.remove();
                     ++removed;
                  }
               } else if (toRemove == Commandremove.ToRemove.XP) {
                  if (e instanceof ExperienceOrb) {
                     e.remove();
                     ++removed;
                  }
               } else if (toRemove == Commandremove.ToRemove.PAINTINGS) {
                  if (e instanceof Painting) {
                     e.remove();
                     ++removed;
                  }
               } else if (toRemove == Commandremove.ToRemove.ITEMFRAMES && e instanceof Painting) {
                  e.remove();
                  ++removed;
               }
            }
         }
      }

      sender.sendMessage(I18n._("removed", removed));
   }

   private static enum ToRemove {
      DROPS,
      ARROWS,
      BOATS,
      MINECARTS,
      XP,
      PAINTINGS,
      ITEMFRAMES;
   }
}
