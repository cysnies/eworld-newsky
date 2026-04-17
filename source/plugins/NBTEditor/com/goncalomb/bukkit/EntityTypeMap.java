package com.goncalomb.bukkit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.EntityType;

public final class EntityTypeMap {
   private static String _livingEntitiesNames;

   static {
      List<EntityType> livingEntitiesTypes = new ArrayList(32);

      EntityType[] var4;
      for(EntityType type : var4 = EntityType.values()) {
         if (type.isAlive() && type != EntityType.PLAYER) {
            livingEntitiesTypes.add(type);
         }
      }

      _livingEntitiesNames = getEntityNames(livingEntitiesTypes);
   }

   private EntityTypeMap() {
   }

   public static String getLivingEntityNames() {
      return _livingEntitiesNames;
   }

   public static EntityType getByName(String name) {
      if (name.equalsIgnoreCase("ThrownPotion")) {
         return EntityType.SPLASH_POTION;
      } else {
         return name.equalsIgnoreCase("MinecartSpawner") ? EntityType.MINECART_MOB_SPAWNER : EntityType.fromName(name);
      }
   }

   public static String getName(EntityType type) {
      if (type == EntityType.SPLASH_POTION) {
         return "ThrownPotion";
      } else {
         return type == EntityType.MINECART_MOB_SPAWNER ? "MinecartSpawner" : type.getName();
      }
   }

   public static String getEntityNames(Collection types) {
      String[] names = new String[types.size()];
      int i = 0;

      for(EntityType type : types) {
         names[i++] = getName(type);
      }

      return StringUtils.join(names, ", ");
   }
}
