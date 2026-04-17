package com.comphenix.protocol.injector;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedIntHashMap;
import com.google.common.collect.Lists;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

class EntityUtilities {
   private static Field entityTrackerField;
   private static Field trackedEntitiesField;
   private static Field trackedPlayersField;
   private static Field trackerField;
   private static Method scanPlayersMethod;

   public static void updateEntity(Entity entity, List observers) throws FieldAccessException {
      try {
         Object trackerEntry = getEntityTrackerEntry(entity.getWorld(), entity.getEntityId());
         if (trackedPlayersField == null) {
            trackedPlayersField = FuzzyReflection.fromObject(trackerEntry).getFieldByType("java\\.util\\..*");
         }

         Collection<?> trackedPlayers = (Collection)FieldUtils.readField(trackedPlayersField, trackerEntry, false);
         List<Object> nmsPlayers = unwrapBukkit(observers);
         trackedPlayers.removeAll(nmsPlayers);
         if (scanPlayersMethod == null) {
            scanPlayersMethod = trackerEntry.getClass().getMethod("scanPlayers", List.class);
         }

         scanPlayersMethod.invoke(trackerEntry, nmsPlayers);
      } catch (IllegalArgumentException e) {
         throw e;
      } catch (IllegalAccessException e) {
         throw new FieldAccessException("Security limitation prevents access to 'get' method in IntHashMap", e);
      } catch (InvocationTargetException e) {
         throw new RuntimeException("Exception occurred in Minecraft.", e);
      } catch (SecurityException e) {
         throw new FieldAccessException("Security limitation prevents access to 'scanPlayers' method in trackerEntry.", e);
      } catch (NoSuchMethodException e) {
         throw new FieldAccessException("Cannot find 'scanPlayers' method. Is ProtocolLib up to date?", e);
      }
   }

   public static List getEntityTrackers(Entity entity) {
      try {
         List<Player> result = new ArrayList();
         Object trackerEntry = getEntityTrackerEntry(entity.getWorld(), entity.getEntityId());
         if (trackerEntry == null) {
            throw new IllegalArgumentException("Cannot find entity trackers for " + entity + (entity.isDead() ? " - entity is dead." : "."));
         } else {
            if (trackedPlayersField == null) {
               trackedPlayersField = FuzzyReflection.fromObject(trackerEntry).getFieldByType("java\\.util\\..*");
            }

            for(Object tracker : (Collection)FieldUtils.readField(trackedPlayersField, trackerEntry, false)) {
               if (MinecraftReflection.isMinecraftPlayer(tracker)) {
                  result.add((Player)MinecraftReflection.getBukkitEntity(tracker));
               }
            }

            return result;
         }
      } catch (IllegalAccessException e) {
         throw new FieldAccessException("Security limitation prevented access to the list of tracked players.", e);
      } catch (InvocationTargetException e) {
         throw new FieldAccessException("Exception occurred in Minecraft.", e);
      }
   }

   private static Object getEntityTrackerEntry(World world, int entityID) throws FieldAccessException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
      BukkitUnwrapper unwrapper = new BukkitUnwrapper();
      Object worldServer = unwrapper.unwrapItem(world);
      if (entityTrackerField == null) {
         entityTrackerField = FuzzyReflection.fromObject(worldServer).getFieldByType("tracker", MinecraftReflection.getEntityTrackerClass());
      }

      Object tracker = null;

      try {
         tracker = FieldUtils.readField(entityTrackerField, worldServer, false);
      } catch (IllegalAccessException e) {
         throw new FieldAccessException("Cannot access 'tracker' field due to security limitations.", e);
      }

      if (trackedEntitiesField == null) {
         Set<Class> ignoredTypes = new HashSet();

         for(Constructor constructor : tracker.getClass().getConstructors()) {
            for(Class type : constructor.getParameterTypes()) {
               ignoredTypes.add(type);
            }
         }

         trackedEntitiesField = FuzzyReflection.fromObject(tracker, true).getFieldByType(MinecraftReflection.getMinecraftObjectRegex(), ignoredTypes);
      }

      Object trackedEntities = null;

      try {
         trackedEntities = FieldUtils.readField(trackedEntitiesField, tracker, true);
      } catch (IllegalAccessException e) {
         throw new FieldAccessException("Cannot access 'trackedEntities' field due to security limitations.", e);
      }

      return WrappedIntHashMap.fromHandle(trackedEntities).get(entityID);
   }

   public static Entity getEntityFromID(World world, int entityID) throws FieldAccessException {
      try {
         Object trackerEntry = getEntityTrackerEntry(world, entityID);
         Object tracker = null;
         if (trackerEntry != null) {
            if (trackerField == null) {
               try {
                  trackerField = trackerEntry.getClass().getField("tracker");
               } catch (NoSuchFieldException var5) {
                  trackerField = FuzzyReflection.fromObject(trackerEntry).getFieldByType("tracker", MinecraftReflection.getEntityClass());
               }
            }

            tracker = FieldUtils.readField(trackerField, trackerEntry, true);
         }

         return tracker != null ? (Entity)MinecraftReflection.getBukkitEntity(tracker) : null;
      } catch (Exception e) {
         throw new FieldAccessException("Cannot find entity from ID " + entityID + ".", e);
      }
   }

   private static List unwrapBukkit(List players) {
      List<Object> output = Lists.newArrayList();
      BukkitUnwrapper unwrapper = new BukkitUnwrapper();

      for(Player player : players) {
         Object result = unwrapper.unwrapItem(player);
         if (result == null) {
            throw new IllegalArgumentException("Cannot unwrap item " + player);
         }

         output.add(result);
      }

      return output;
   }
}
