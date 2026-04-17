package com.earth2me.essentials;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public enum Mob {
   CHICKEN("Chicken", Mob.Enemies.FRIENDLY, EntityType.CHICKEN),
   COW("Cow", Mob.Enemies.FRIENDLY, EntityType.COW),
   CREEPER("Creeper", Mob.Enemies.ENEMY, EntityType.CREEPER),
   GHAST("Ghast", Mob.Enemies.ENEMY, EntityType.GHAST),
   GIANT("Giant", Mob.Enemies.ENEMY, EntityType.GIANT),
   PIG("Pig", Mob.Enemies.FRIENDLY, EntityType.PIG),
   PIGZOMB("PigZombie", Mob.Enemies.NEUTRAL, EntityType.PIG_ZOMBIE),
   SHEEP("Sheep", Mob.Enemies.FRIENDLY, "", EntityType.SHEEP),
   SKELETON("Skeleton", Mob.Enemies.ENEMY, EntityType.SKELETON),
   SLIME("Slime", Mob.Enemies.ENEMY, EntityType.SLIME),
   SPIDER("Spider", Mob.Enemies.ENEMY, EntityType.SPIDER),
   SQUID("Squid", Mob.Enemies.FRIENDLY, EntityType.SQUID),
   ZOMBIE("Zombie", Mob.Enemies.ENEMY, EntityType.ZOMBIE),
   WOLF("Wolf", Mob.Enemies.NEUTRAL, EntityType.WOLF),
   CAVESPIDER("CaveSpider", Mob.Enemies.ENEMY, EntityType.CAVE_SPIDER),
   ENDERMAN("Enderman", Mob.Enemies.ENEMY, "", EntityType.ENDERMAN),
   SILVERFISH("Silverfish", Mob.Enemies.ENEMY, "", EntityType.SILVERFISH),
   ENDERDRAGON("EnderDragon", Mob.Enemies.ENEMY, EntityType.ENDER_DRAGON),
   VILLAGER("Villager", Mob.Enemies.FRIENDLY, EntityType.VILLAGER),
   BLAZE("Blaze", Mob.Enemies.ENEMY, EntityType.BLAZE),
   MUSHROOMCOW("MushroomCow", Mob.Enemies.FRIENDLY, EntityType.MUSHROOM_COW),
   MAGMACUBE("MagmaCube", Mob.Enemies.ENEMY, EntityType.MAGMA_CUBE),
   SNOWMAN("Snowman", Mob.Enemies.FRIENDLY, "", EntityType.SNOWMAN),
   OCELOT("Ocelot", Mob.Enemies.NEUTRAL, EntityType.OCELOT),
   IRONGOLEM("IronGolem", Mob.Enemies.NEUTRAL, EntityType.IRON_GOLEM),
   WITHER("Wither", Mob.Enemies.ENEMY, EntityType.WITHER),
   BAT("Bat", Mob.Enemies.FRIENDLY, EntityType.BAT),
   WITCH("Witch", Mob.Enemies.ENEMY, EntityType.WITCH),
   BOAT("Boat", Mob.Enemies.NEUTRAL, EntityType.BOAT),
   MINECART("Minecart", Mob.Enemies.NEUTRAL, EntityType.MINECART),
   MINECART_CHEST("ChestMinecart", Mob.Enemies.NEUTRAL, EntityType.MINECART_CHEST),
   MINECART_FURNACE("FurnaceMinecart", Mob.Enemies.NEUTRAL, EntityType.MINECART_FURNACE),
   MINECART_TNT("TNTMinecart", Mob.Enemies.NEUTRAL, EntityType.MINECART_TNT),
   MINECART_HOPPER("HopperMinecart", Mob.Enemies.NEUTRAL, EntityType.MINECART_HOPPER),
   ENDERCRYSTAL("EnderCrystal", Mob.Enemies.NEUTRAL, EntityType.ENDER_CRYSTAL),
   EXPERIENCEORB("ExperienceOrb", Mob.Enemies.NEUTRAL, EntityType.EXPERIENCE_ORB);

   public static final Logger logger = Logger.getLogger("Minecraft");
   public String suffix = "s";
   public final String name;
   public final Enemies type;
   private final EntityType bukkitType;
   private static final Map hashMap = new HashMap();

   private Mob(String n, Enemies en, String s, EntityType type) {
      this.suffix = s;
      this.name = n;
      this.type = en;
      this.bukkitType = type;
   }

   private Mob(String n, Enemies en, EntityType type) {
      this.name = n;
      this.type = en;
      this.bukkitType = type;
   }

   public static Set getMobList() {
      return Collections.unmodifiableSet(hashMap.keySet());
   }

   public Entity spawn(Player player, Server server, Location loc) throws MobException {
      return this.spawn(player.getWorld(), server, loc);
   }

   public Entity spawn(World world, Server server, Location loc) throws MobException {
      Entity entity = world.spawn(loc, this.bukkitType.getEntityClass());
      if (entity == null) {
         logger.log(Level.WARNING, I18n._("unableToSpawnMob"));
         throw new MobException();
      } else {
         return entity;
      }
   }

   public EntityType getType() {
      return this.bukkitType;
   }

   public static Mob fromName(String name) {
      return (Mob)hashMap.get(name.toLowerCase(Locale.ENGLISH));
   }

   static {
      for(Mob mob : values()) {
         hashMap.put(mob.name.toLowerCase(Locale.ENGLISH), mob);
      }

   }

   public static enum Enemies {
      FRIENDLY("friendly"),
      NEUTRAL("neutral"),
      ENEMY("enemy");

      protected final String type;

      private Enemies(String type) {
         this.type = type;
      }
   }

   public static class MobException extends Exception {
      private static final long serialVersionUID = 1L;
   }
}
