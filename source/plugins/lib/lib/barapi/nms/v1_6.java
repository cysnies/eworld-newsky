package lib.barapi.nms;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lib.barapi.Util;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

public class v1_6 extends FakeDragon {
   private static final Integer EntityID = 6000;

   public v1_6(String name, Location loc) {
      super(name, loc);
   }

   public Object getSpawnPacket() {
      Class<?> mob_class = Util.getCraftClass("Packet24MobSpawn");
      Object mobPacket = null;

      try {
         mobPacket = mob_class.newInstance();
         Field a = Util.getField(mob_class, "a");
         a.setAccessible(true);
         a.set(mobPacket, EntityID);
         Field b = Util.getField(mob_class, "b");
         b.setAccessible(true);
         b.set(mobPacket, EntityType.ENDER_DRAGON.getTypeId());
         Field c = Util.getField(mob_class, "c");
         c.setAccessible(true);
         c.set(mobPacket, this.getX());
         Field d = Util.getField(mob_class, "d");
         d.setAccessible(true);
         d.set(mobPacket, this.getY());
         Field e = Util.getField(mob_class, "e");
         e.setAccessible(true);
         e.set(mobPacket, this.getZ());
         Field f = Util.getField(mob_class, "f");
         f.setAccessible(true);
         f.set(mobPacket, (byte)((int)((float)this.getPitch() * 256.0F / 360.0F)));
         Field g = Util.getField(mob_class, "g");
         g.setAccessible(true);
         g.set(mobPacket, (byte)0);
         Field h = Util.getField(mob_class, "h");
         h.setAccessible(true);
         h.set(mobPacket, (byte)((int)((float)this.getYaw() * 256.0F / 360.0F)));
         Field i = Util.getField(mob_class, "i");
         i.setAccessible(true);
         i.set(mobPacket, this.getXvel());
         Field j = Util.getField(mob_class, "j");
         j.setAccessible(true);
         j.set(mobPacket, this.getYvel());
         Field k = Util.getField(mob_class, "k");
         k.setAccessible(true);
         k.set(mobPacket, this.getZvel());
         Object watcher = this.getWatcher();
         Field t = Util.getField(mob_class, "t");
         t.setAccessible(true);
         t.set(mobPacket, watcher);
      } catch (InstantiationException e1) {
         e1.printStackTrace();
      } catch (IllegalAccessException e1) {
         e1.printStackTrace();
      }

      return mobPacket;
   }

   public Object getDestroyPacket() {
      Class<?> packet_class = Util.getCraftClass("Packet29DestroyEntity");
      Object packet = null;

      try {
         packet = packet_class.newInstance();
         Field a = Util.getField(packet_class, "a");
         a.setAccessible(true);
         a.set(packet, new int[]{EntityID});
      } catch (InstantiationException e) {
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      }

      return packet;
   }

   public Object getMetaPacket(Object watcher) {
      Class<?> packet_class = Util.getCraftClass("Packet40EntityMetadata");
      Object packet = null;

      try {
         packet = packet_class.newInstance();
         Field a = Util.getField(packet_class, "a");
         a.setAccessible(true);
         a.set(packet, EntityID);
         Method watcher_c = Util.getMethod(watcher.getClass(), "c");
         Field b = Util.getField(packet_class, "b");
         b.setAccessible(true);
         b.set(packet, watcher_c.invoke(watcher));
      } catch (InstantiationException e) {
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      } catch (IllegalArgumentException e) {
         e.printStackTrace();
      } catch (InvocationTargetException e) {
         e.printStackTrace();
      }

      return packet;
   }

   public Object getTeleportPacket(Location loc) {
      Class<?> packet_class = Util.getCraftClass("Packet34EntityTeleport");
      Object packet = null;

      try {
         packet = packet_class.newInstance();
         Field a = Util.getField(packet_class, "a");
         a.setAccessible(true);
         a.set(packet, EntityID);
         Field b = Util.getField(packet_class, "b");
         b.setAccessible(true);
         b.set(packet, (int)Math.floor(loc.getX() * (double)32.0F));
         Field c = Util.getField(packet_class, "c");
         c.setAccessible(true);
         c.set(packet, (int)Math.floor(loc.getY() * (double)32.0F));
         Field d = Util.getField(packet_class, "d");
         d.setAccessible(true);
         d.set(packet, (int)Math.floor(loc.getZ() * (double)32.0F));
         Field e = Util.getField(packet_class, "e");
         e.setAccessible(true);
         e.set(packet, (byte)((int)(loc.getYaw() * 256.0F / 360.0F)));
         Field f = Util.getField(packet_class, "f");
         f.setAccessible(true);
         f.set(packet, (byte)((int)(loc.getPitch() * 256.0F / 360.0F)));
      } catch (InstantiationException e) {
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      }

      return packet;
   }

   public Object getWatcher() {
      Class<?> watcher_class = Util.getCraftClass("DataWatcher");
      Object watcher = null;

      try {
         watcher = watcher_class.newInstance();
         Method a = Util.getMethod(watcher_class, "a", new Class[]{Integer.TYPE, Object.class});
         a.setAccessible(true);
         a.invoke(watcher, 0, Byte.valueOf((byte)(this.isVisible() ? 0 : 32)));
         a.invoke(watcher, 6, this.health);
         a.invoke(watcher, 7, 0);
         a.invoke(watcher, 8, 0);
         a.invoke(watcher, 10, this.name);
         a.invoke(watcher, 11, 1);
      } catch (InstantiationException e) {
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      } catch (IllegalArgumentException e) {
         e.printStackTrace();
      } catch (InvocationTargetException e) {
         e.printStackTrace();
      }

      return watcher;
   }
}
