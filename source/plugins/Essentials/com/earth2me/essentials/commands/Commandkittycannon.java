package com.earth2me.essentials.commands;

import com.earth2me.essentials.Mob;
import com.earth2me.essentials.User;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Ocelot.Type;

public class Commandkittycannon extends EssentialsCommand {
   private static Random random = new Random();

   public Commandkittycannon() {
      super("kittycannon");
   }

   protected void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      Mob cat = Mob.OCELOT;
      final Ocelot ocelot = (Ocelot)cat.spawn((Player)user, server, user.getEyeLocation());
      if (ocelot != null) {
         int i = random.nextInt(Type.values().length);
         ocelot.setCatType(Type.values()[i]);
         ocelot.setTamed(true);
         ocelot.setBaby();
         ocelot.setVelocity(user.getEyeLocation().getDirection().multiply(2));
         this.ess.scheduleSyncDelayedTask(new Runnable() {
            public void run() {
               Location loc = ocelot.getLocation();
               ocelot.remove();
               loc.getWorld().createExplosion(loc, 0.0F);
            }
         }, 20L);
      }
   }
}
