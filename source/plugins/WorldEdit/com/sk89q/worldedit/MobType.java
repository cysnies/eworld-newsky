package com.sk89q.worldedit;

public enum MobType {
   BAT("Bat"),
   BLAZE("Blaze"),
   CAVE_SPIDER("CaveSpider"),
   CHICKEN("Chicken"),
   COW("Cow"),
   CREEPER("Creeper"),
   ENDERDRAGON("EnderDragon"),
   ENDERMAN("Enderman"),
   GHAST("Ghast"),
   GIANT("Giant"),
   VILLAGER_GOLEM("VillagerGolem"),
   HORSE("EntityHorse"),
   MAGMA_CUBE("LavaSlime"),
   MOOSHROOM("MushroomCow"),
   OCELOT("Ozelot"),
   PIG("Pig"),
   PIG_ZOMBIE("PigZombie"),
   SHEEP("Sheep"),
   SILVERFISH("Silverfish"),
   SKELETON("Skeleton"),
   SLIME("Slime"),
   SNOWMAN("SnowMan"),
   SPIDER("Spider"),
   SQUID("Squid"),
   VILLAGER("Villager"),
   WITCH("Witch"),
   WITHER("WitherBoss"),
   WOLF("Wolf"),
   ZOMBIE("Zombie");

   private String name;

   private MobType(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }
}
