package com.goncalomb.bukkit.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.bukkit.Location;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.ThrownPotion;

public final class WorldUtils {
   private static boolean _isPrepared = false;
   private static Method _getHandle;
   private static Method _getBukkitEntity;
   private static Method _setPositionRotation;
   private static Method _addEntity;
   private static Constructor _xpOrbConstructor;
   private static Constructor _potionConstructor;
   private static Constructor _enderPearlConstructor;

   public static void prepareReflection() {
      if (!_isPrepared) {
         try {
            Class<?> craftWorldClass = BukkitReflect.getCraftBukkitClass("CraftWorld");
            _getHandle = craftWorldClass.getMethod("getHandle");
            Class<?> minecraftEntityClass = BukkitReflect.getMinecraftClass("Entity");
            _getBukkitEntity = minecraftEntityClass.getMethod("getBukkitEntity");
            _setPositionRotation = minecraftEntityClass.getMethod("setPositionRotation", Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Float.TYPE);
            Class<?> minecraftWorldClass = BukkitReflect.getMinecraftClass("World");
            _addEntity = minecraftWorldClass.getMethod("addEntity", minecraftEntityClass);
            Class<?> minecraftEntityExperienceOrbClass = BukkitReflect.getMinecraftClass("EntityExperienceOrb");
            _xpOrbConstructor = minecraftEntityExperienceOrbClass.getConstructor(minecraftWorldClass, Double.TYPE, Double.TYPE, Double.TYPE, Integer.TYPE);
            Class<?> minecraftEntityPotionClass = BukkitReflect.getMinecraftClass("EntityPotion");
            _potionConstructor = minecraftEntityPotionClass.getConstructor(minecraftWorldClass, Double.TYPE, Double.TYPE, Double.TYPE, BukkitReflect.getMinecraftClass("ItemStack"));
            Class<?> minecraftEntityEnderPearl = BukkitReflect.getMinecraftClass("EntityEnderPearl");
            _enderPearlConstructor = minecraftEntityEnderPearl.getConstructor(minecraftWorldClass);
         } catch (Exception e) {
            throw new Error("Error while preparing WorldUtils.", e);
         }

         _isPrepared = true;
      }

   }

   private WorldUtils() {
   }

   public static ExperienceOrb spawnXPOrb(Location location, short value) {
      prepareReflection();
      Object world = BukkitReflect.invokeMethod(location.getWorld(), _getHandle);
      Object entity = BukkitReflect.newInstance(_xpOrbConstructor, world, (double)location.getBlockX() + (double)0.5F, location.getBlockY(), location.getZ(), Integer.valueOf(value));
      BukkitReflect.invokeMethod(entity, _setPositionRotation, (double)location.getBlockX() + (double)0.5F, location.getBlockY(), location.getZ(), 0, 0);
      BukkitReflect.invokeMethod(world, _addEntity, entity);
      return (ExperienceOrb)BukkitReflect.invokeMethod(entity, _getBukkitEntity);
   }

   public static ThrownPotion spawnPotion(Location location, NBTTagCompoundWrapper data) {
      Object world = BukkitReflect.invokeMethod(location.getWorld(), _getHandle);
      Object entity = BukkitReflect.newInstance(_potionConstructor, world, (double)location.getBlockX() + (double)0.5F, location.getBlockY(), location.getZ(), null);
      NBTUtils.setMineEntityNBTTagCompound(entity, data);
      BukkitReflect.invokeMethod(world, _addEntity, entity);
      return (ThrownPotion)BukkitReflect.invokeMethod(entity, _getBukkitEntity);
   }

   public static EnderPearl spawnEnderpearl(Location location, NBTTagCompoundWrapper data) {
      Object world = BukkitReflect.invokeMethod(location.getWorld(), _getHandle);
      Object entity = BukkitReflect.newInstance(_enderPearlConstructor, world);
      BukkitReflect.invokeMethod(entity, _setPositionRotation, (double)location.getBlockX() + (double)0.5F, location.getBlockY(), location.getZ(), 0, 0);
      NBTUtils.setMineEntityNBTTagCompound(entity, data);
      BukkitReflect.invokeMethod(world, _addEntity, entity);
      return (EnderPearl)BukkitReflect.invokeMethod(entity, _getBukkitEntity);
   }
}
