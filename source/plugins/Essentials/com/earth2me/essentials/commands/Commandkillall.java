package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.Mob;
import java.util.ArrayList;
import java.util.Locale;
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ComplexLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Flying;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.WaterMob;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.EntityDeathEvent;

public class Commandkillall extends EssentialsCommand {
   public Commandkillall() {
      super("killall");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      String type = "all";
      int radius = -1;
      World world;
      if (sender instanceof Player) {
         world = ((Player)sender).getWorld();
         if (args.length == 1) {
            try {
               radius = Integer.parseInt(args[0]);
            } catch (NumberFormatException var24) {
               type = args[0];
            }
         } else if (args.length > 1) {
            type = args[0];

            try {
               radius = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
               throw new Exception(I18n._("numberRequired"), e);
            }
         }
      } else {
         if (args.length == 0) {
            throw new NotEnoughArgumentsException();
         }

         if (args.length == 1) {
            world = this.ess.getWorld(args[0]);
         } else {
            type = args[0];
            world = this.ess.getWorld(args[1]);
         }
      }

      if (radius >= 0) {
         radius *= radius;
      }

      String killType = type.toLowerCase(Locale.ENGLISH);
      boolean animals = killType.startsWith("animal");
      boolean monster = killType.startsWith("monster") || killType.startsWith("mob");
      boolean all = killType.equals("all");
      Class<? extends Entity> entityClass = null;
      if (!animals && !monster && !all) {
         if (Mob.fromName(killType) == null) {
            throw new Exception(I18n._("invalidMob"));
         }

         entityClass = Mob.fromName(killType).getType().getEntityClass();
      }

      int numKills = 0;

      for(Chunk chunk : world.getLoadedChunks()) {
         for(Entity entity : chunk.getEntities()) {
            if ((!(sender instanceof Player) || radius < 0 || !(((Player)sender).getLocation().distanceSquared(entity.getLocation()) > (double)radius)) && entity instanceof LivingEntity && !(entity instanceof HumanEntity) && (!(entity instanceof Wolf) || !((Wolf)entity).isTamed()) && (!(entity instanceof Ocelot) || !((Ocelot)entity).isTamed())) {
               if (animals) {
                  if (entity instanceof Animals || entity instanceof NPC || entity instanceof Snowman || entity instanceof WaterMob) {
                     EntityDeathEvent event = new EntityDeathEvent((LivingEntity)entity, new ArrayList(0));
                     this.ess.getServer().getPluginManager().callEvent(event);
                     entity.remove();
                     ++numKills;
                  }
               } else if (monster) {
                  if (entity instanceof Monster || entity instanceof ComplexLivingEntity || entity instanceof Flying || entity instanceof Slime) {
                     EntityDeathEvent event = new EntityDeathEvent((LivingEntity)entity, new ArrayList(0));
                     this.ess.getServer().getPluginManager().callEvent(event);
                     entity.remove();
                     ++numKills;
                  }
               } else if (all) {
                  EntityDeathEvent event = new EntityDeathEvent((LivingEntity)entity, new ArrayList(0));
                  this.ess.getServer().getPluginManager().callEvent(event);
                  entity.remove();
                  ++numKills;
               } else if (entityClass != null && entityClass.isAssignableFrom(entity.getClass())) {
                  EntityDeathEvent event = new EntityDeathEvent((LivingEntity)entity, new ArrayList(0));
                  this.ess.getServer().getPluginManager().callEvent(event);
                  entity.remove();
                  ++numKills;
               }
            }
         }
      }

      sender.sendMessage(I18n._("kill", numKills));
   }
}
