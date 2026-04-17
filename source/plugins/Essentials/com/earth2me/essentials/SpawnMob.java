package com.earth2me.essentials;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.Ocelot.Type;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.material.Colorable;

public class SpawnMob {
   public static String mobList(User user) {
      Set<String> mobList = Mob.getMobList();
      Set<String> availableList = new HashSet();

      for(String mob : mobList) {
         if (user.isAuthorized("essentials.spawnmob." + mob.toLowerCase(Locale.ENGLISH))) {
            availableList.add(mob);
         }
      }

      if (availableList.isEmpty()) {
         availableList.add(I18n._("none"));
      }

      return Util.joinList(availableList);
   }

   public static List mobParts(String mobString) {
      String[] mobParts = mobString.split(",");
      List<String> mobs = new ArrayList();

      for(String mobPart : mobParts) {
         String[] mobDatas = mobPart.split(":");
         mobs.add(mobDatas[0]);
      }

      return mobs;
   }

   public static List mobData(String mobString) {
      String[] mobParts = mobString.split(",");
      List<String> mobData = new ArrayList();

      for(String mobPart : mobParts) {
         String[] mobDatas = mobPart.split(":");
         if (mobDatas.length == 1) {
            mobData.add((Object)null);
         } else {
            mobData.add(mobDatas[1]);
         }
      }

      return mobData;
   }

   public static void spawnmob(IEssentials ess, Server server, User user, List parts, List data, int mobCount) throws Exception {
      Block block = Util.getTarget(user).getBlock();
      if (block == null) {
         throw new Exception(I18n._("unableToSpawnMob"));
      } else {
         spawnmob(ess, server, user, user, block.getLocation(), parts, data, mobCount);
      }
   }

   public static void spawnmob(IEssentials ess, Server server, CommandSender sender, Location loc, List parts, List data, int mobCount) throws Exception {
      spawnmob(ess, server, sender, (User)null, loc, parts, data, mobCount);
   }

   public static void spawnmob(IEssentials ess, Server server, CommandSender sender, User target, List parts, List data, int mobCount) throws Exception {
      spawnmob(ess, server, sender, target, target.getLocation(), parts, data, mobCount);
   }

   public static void spawnmob(IEssentials ess, Server server, CommandSender sender, User target, Location loc, List parts, List data, int mobCount) throws Exception {
      Location sloc = Util.getSafeDestination(loc);

      for(int i = 0; i < parts.size(); ++i) {
         Mob mob = Mob.fromName((String)parts.get(i));
         checkSpawnable(ess, sender, mob);
      }

      int serverLimit = ess.getSettings().getSpawnMobLimit();
      if (mobCount > serverLimit) {
         mobCount = serverLimit;
         sender.sendMessage(I18n._("mobSpawnLimit"));
      }

      Mob mob = Mob.fromName((String)parts.get(0));

      try {
         for(int i = 0; i < mobCount; ++i) {
            spawnMob(ess, server, sender, target, sloc, parts, data);
         }

         sender.sendMessage(mobCount + " " + mob.name.toLowerCase(Locale.ENGLISH) + mob.suffix + " " + I18n._("spawned"));
      } catch (Mob.MobException e1) {
         throw new Exception(I18n._("unableToSpawnMob"), e1);
      } catch (NumberFormatException e2) {
         throw new Exception(I18n._("numberRequired"), e2);
      } catch (NullPointerException np) {
         throw new Exception(I18n._("soloMob"), np);
      }
   }

   private static void spawnMob(IEssentials ess, Server server, CommandSender sender, User target, Location sloc, List parts, List data) throws Exception {
      Entity spawnedMob = null;

      for(int i = 0; i < parts.size(); ++i) {
         if (i == 0) {
            Mob mob = Mob.fromName((String)parts.get(i));
            spawnedMob = mob.spawn(sloc.getWorld(), server, sloc);
            if (data.get(i) != null) {
               changeMobData(mob.getType(), spawnedMob, (String)data.get(i), target);
            }
         }

         int next = i + 1;
         if (next < parts.size()) {
            Mob mMob = Mob.fromName((String)parts.get(next));
            Entity spawnedMount = mMob.spawn(sloc.getWorld(), server, sloc);
            if (data.get(next) != null) {
               changeMobData(mMob.getType(), spawnedMount, (String)data.get(next), target);
            }

            spawnedMob.setPassenger(spawnedMount);
            spawnedMob = spawnedMount;
         }
      }

   }

   private static void checkSpawnable(IEssentials ess, CommandSender sender, Mob mob) throws Exception {
      if (mob == null) {
         throw new Exception(I18n._("invalidMob"));
      } else if (ess.getSettings().getProtectPreventSpawn(mob.getType().toString().toLowerCase(Locale.ENGLISH))) {
         throw new Exception(I18n._("disabledToSpawnMob"));
      } else if (sender instanceof User && !((User)sender).isAuthorized("essentials.spawnmob." + mob.name.toLowerCase(Locale.ENGLISH))) {
         throw new Exception(I18n._("noPermToSpawnMob"));
      }
   }

   private static void changeMobData(EntityType type, Entity spawned, String data, User target) throws Exception {
      data = data.toLowerCase(Locale.ENGLISH);
      if (spawned instanceof Slime) {
         try {
            ((Slime)spawned).setSize(Integer.parseInt(data));
         } catch (Exception e) {
            throw new Exception(I18n._("slimeMalformedSize"), e);
         }
      }

      if (spawned instanceof Ageable && data.contains("baby")) {
         ((Ageable)spawned).setBaby();
         data = data.replace("baby", "");
      }

      if (spawned instanceof Colorable) {
         String color = data.toUpperCase(Locale.ENGLISH);

         try {
            if (color.equals("RANDOM")) {
               Random rand = new Random();
               ((Colorable)spawned).setColor(DyeColor.values()[rand.nextInt(DyeColor.values().length)]);
            } else if (color.length() > 1) {
               ((Colorable)spawned).setColor(DyeColor.valueOf(color));
            }
         } catch (Exception e) {
            throw new Exception(I18n._("sheepMalformedColor"), e);
         }
      }

      if (spawned instanceof Tameable && data.contains("tamed") && target != null) {
         Tameable tameable = (Tameable)spawned;
         tameable.setTamed(true);
         tameable.setOwner(target.getBase());
         data = data.replace("tamed", "");
      }

      if (type == EntityType.WOLF && data.contains("angry")) {
         ((Wolf)spawned).setAngry(true);
      }

      if (type == EntityType.CREEPER && data.contains("powered")) {
         ((Creeper)spawned).setPowered(true);
      }

      if (type == EntityType.OCELOT) {
         if (!data.contains("siamese") && !data.contains("white")) {
            if (!data.contains("red") && !data.contains("orange") && !data.contains("tabby")) {
               if (data.contains("black") || data.contains("tuxedo")) {
                  ((Ocelot)spawned).setCatType(Type.BLACK_CAT);
               }
            } else {
               ((Ocelot)spawned).setCatType(Type.RED_CAT);
            }
         } else {
            ((Ocelot)spawned).setCatType(Type.SIAMESE_CAT);
         }
      }

      if (type == EntityType.VILLAGER) {
         for(Villager.Profession prof : Profession.values()) {
            if (data.contains(prof.toString().toLowerCase(Locale.ENGLISH))) {
               ((Villager)spawned).setProfession(prof);
            }
         }
      }

      if (spawned instanceof Zombie) {
         if (data.contains("villager")) {
            ((Zombie)spawned).setVillager(true);
         }

         if (data.contains("baby")) {
            ((Zombie)spawned).setBaby(true);
         }
      }

      if (type == EntityType.SKELETON && data.contains("wither")) {
         ((Skeleton)spawned).setSkeletonType(SkeletonType.WITHER);
      }

      if (type == EntityType.EXPERIENCE_ORB && Util.isInt(data)) {
         ((ExperienceOrb)spawned).setExperience(Integer.parseInt(data));
      }

   }
}
