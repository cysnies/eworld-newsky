package lib.barapi.nms;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lib.barapi.Util;
import org.bukkit.Location;

public class v1_7 extends FakeDragon {
   private Object dragon;
   private int id;

   public v1_7(String name, Location loc) {
      super(name, loc);
   }

   public Object getSpawnPacket() {
      Class<?> Entity = Util.getCraftClass("Entity");
      Class<?> EntityLiving = Util.getCraftClass("EntityLiving");
      Class<?> EntityEnderDragon = Util.getCraftClass("EntityEnderDragon");
      Object packet = null;

      try {
         this.dragon = EntityEnderDragon.getConstructor(Util.getCraftClass("World")).newInstance(this.getWorld());
         Method setLocation = Util.getMethod(EntityEnderDragon, "setLocation", new Class[]{Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Float.TYPE});
         setLocation.invoke(this.dragon, this.getX(), this.getY(), this.getZ(), this.getPitch(), this.getYaw());
         Method setInvisible = Util.getMethod(EntityEnderDragon, "setInvisible", new Class[]{Boolean.TYPE});
         setInvisible.invoke(this.dragon, this.isVisible());
         Method setCustomName = Util.getMethod(EntityEnderDragon, "setCustomName", new Class[]{String.class});
         setCustomName.invoke(this.dragon, this.name);
         Method setHealth = Util.getMethod(EntityEnderDragon, "setHealth", new Class[]{Float.TYPE});
         setHealth.invoke(this.dragon, this.health);
         Field motX = Util.getField(Entity, "motX");
         motX.set(this.dragon, this.getXvel());
         Field motY = Util.getField(Entity, "motX");
         motY.set(this.dragon, this.getYvel());
         Field motZ = Util.getField(Entity, "motX");
         motZ.set(this.dragon, this.getZvel());
         Method getId = Util.getMethod(EntityEnderDragon, "getId", new Class[0]);
         this.id = (Integer)getId.invoke(this.dragon);
         Class<?> PacketPlayOutSpawnEntityLiving = Util.getCraftClass("PacketPlayOutSpawnEntityLiving");
         packet = PacketPlayOutSpawnEntityLiving.getConstructor(EntityLiving).newInstance(this.dragon);
      } catch (IllegalArgumentException e) {
         e.printStackTrace();
      } catch (SecurityException e) {
         e.printStackTrace();
      } catch (InstantiationException e) {
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      } catch (InvocationTargetException e) {
         e.printStackTrace();
      } catch (NoSuchMethodException e) {
         e.printStackTrace();
      }

      return packet;
   }

   public Object getDestroyPacket() {
      Class<?> PacketPlayOutEntityDestroy = Util.getCraftClass("PacketPlayOutEntityDestroy");
      Object packet = null;

      try {
         packet = PacketPlayOutEntityDestroy.newInstance();
         Field a = PacketPlayOutEntityDestroy.getDeclaredField("a");
         a.setAccessible(true);
         a.set(packet, new int[]{this.id});
      } catch (SecurityException e) {
         e.printStackTrace();
      } catch (NoSuchFieldException e) {
         e.printStackTrace();
      } catch (InstantiationException e) {
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      } catch (IllegalArgumentException e) {
         e.printStackTrace();
      }

      return packet;
   }

   public Object getMetaPacket(Object watcher) {
      Class<?> DataWatcher = Util.getCraftClass("DataWatcher");
      Class<?> PacketPlayOutEntityMetadata = Util.getCraftClass("PacketPlayOutEntityMetadata");
      Object packet = null;

      try {
         packet = PacketPlayOutEntityMetadata.getConstructor(Integer.TYPE, DataWatcher, Boolean.TYPE).newInstance(this.id, watcher, true);
      } catch (IllegalArgumentException e) {
         e.printStackTrace();
      } catch (SecurityException e) {
         e.printStackTrace();
      } catch (InstantiationException e) {
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      } catch (InvocationTargetException e) {
         e.printStackTrace();
      } catch (NoSuchMethodException e) {
         e.printStackTrace();
      }

      return packet;
   }

   public Object getTeleportPacket(Location loc) {
      Class<?> PacketPlayOutEntityTeleport = Util.getCraftClass("PacketPlayOutEntityTeleport");
      Object packet = null;

      try {
         packet = PacketPlayOutEntityTeleport.getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Byte.TYPE, Byte.TYPE).newInstance(this.id, loc.getBlockX() * 32, loc.getBlockY() * 32, loc.getBlockZ() * 32, (byte)((int)loc.getYaw() * 256 / 360), (byte)((int)loc.getPitch() * 256 / 360));
      } catch (IllegalArgumentException e) {
         e.printStackTrace();
      } catch (SecurityException e) {
         e.printStackTrace();
      } catch (InstantiationException e) {
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      } catch (InvocationTargetException e) {
         e.printStackTrace();
      } catch (NoSuchMethodException e) {
         e.printStackTrace();
      }

      return packet;
   }

   public Object getWatcher() {
      Class<?> Entity = Util.getCraftClass("Entity");
      Class<?> DataWatcher = Util.getCraftClass("DataWatcher");
      Object watcher = null;

      try {
         watcher = DataWatcher.getConstructor(Entity).newInstance(this.dragon);
         Method a = Util.getMethod(DataWatcher, "a", new Class[]{Integer.TYPE, Object.class});
         a.invoke(watcher, 0, Byte.valueOf((byte)(this.isVisible() ? 0 : 32)));
         a.invoke(watcher, 6, this.health);
         a.invoke(watcher, 7, 0);
         a.invoke(watcher, 8, 0);
         a.invoke(watcher, 10, this.name);
         a.invoke(watcher, 11, 1);
      } catch (IllegalArgumentException e) {
         e.printStackTrace();
      } catch (SecurityException e) {
         e.printStackTrace();
      } catch (InstantiationException e) {
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      } catch (InvocationTargetException e) {
         e.printStackTrace();
      } catch (NoSuchMethodException e) {
         e.printStackTrace();
      }

      return watcher;
   }
}
