package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.WitherSkull;
import org.bukkit.util.Vector;

public class Commandfireball extends EssentialsCommand {
   public Commandfireball() {
      super("fireball");
   }

   protected void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      boolean small = false;
      Class<? extends Entity> type = Fireball.class;
      int speed = 2;
      if (args.length > 0) {
         if (args[0].equalsIgnoreCase("small")) {
            type = SmallFireball.class;
         } else if (args[0].equalsIgnoreCase("arrow")) {
            type = Arrow.class;
         } else if (args[0].equalsIgnoreCase("skull")) {
            type = WitherSkull.class;
         } else if (args[0].equalsIgnoreCase("egg")) {
            type = Egg.class;
         } else if (args[0].equalsIgnoreCase("snowball")) {
            type = Snowball.class;
         } else if (args[0].equalsIgnoreCase("expbottle")) {
            type = ThrownExpBottle.class;
         } else if (args[0].equalsIgnoreCase("large")) {
            type = LargeFireball.class;
         }
      }

      Vector direction = user.getEyeLocation().getDirection().multiply(speed);
      Projectile projectile = (Projectile)user.getWorld().spawn(user.getEyeLocation().add(direction.getX(), direction.getY(), direction.getZ()), type);
      projectile.setShooter(user.getBase());
      projectile.setVelocity(direction);
   }
}
