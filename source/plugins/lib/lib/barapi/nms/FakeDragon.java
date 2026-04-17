package lib.barapi.nms;

import lib.barapi.Util;
import org.bukkit.Location;

public abstract class FakeDragon {
   public static final int MAX_HEALTH = 200;
   private int x;
   private int y;
   private int z;
   private int pitch = 0;
   private int yaw = 0;
   private byte xvel = 0;
   private byte yvel = 0;
   private byte zvel = 0;
   public float health = 0.0F;
   private boolean visible = false;
   public String name;
   private Object world;

   public FakeDragon(String name, Location loc, int percent) {
      this.name = name;
      this.x = loc.getBlockX();
      this.y = loc.getBlockY();
      this.z = loc.getBlockZ();
      this.health = (float)percent / 100.0F * 200.0F;
      this.world = Util.getHandle(loc.getWorld());
   }

   public FakeDragon(String name, Location loc) {
      this.name = name;
      this.x = loc.getBlockX();
      this.y = loc.getBlockY();
      this.z = loc.getBlockZ();
      this.world = Util.getHandle(loc.getWorld());
   }

   public int getMaxHealth() {
      return 200;
   }

   public void setHealth(int percent) {
      this.health = (float)percent / 100.0F * 200.0F;
   }

   public void setName(String name) {
      this.name = name;
   }

   public int getX() {
      return this.x;
   }

   public void setX(int x) {
      this.x = x;
   }

   public int getY() {
      return this.y;
   }

   public void setY(int y) {
      this.y = y;
   }

   public int getZ() {
      return this.z;
   }

   public void setZ(int z) {
      this.z = z;
   }

   public int getPitch() {
      return this.pitch;
   }

   public void setPitch(int pitch) {
      this.pitch = pitch;
   }

   public int getYaw() {
      return this.yaw;
   }

   public void setYaw(int yaw) {
      this.yaw = yaw;
   }

   public byte getXvel() {
      return this.xvel;
   }

   public void setXvel(byte xvel) {
      this.xvel = xvel;
   }

   public byte getYvel() {
      return this.yvel;
   }

   public void setYvel(byte yvel) {
      this.yvel = yvel;
   }

   public byte getZvel() {
      return this.zvel;
   }

   public void setZvel(byte zvel) {
      this.zvel = zvel;
   }

   public boolean isVisible() {
      return this.visible;
   }

   public void setVisible(boolean visible) {
      this.visible = visible;
   }

   public Object getWorld() {
      return this.world;
   }

   public void setWorld(Object world) {
      this.world = world;
   }

   public abstract Object getSpawnPacket();

   public abstract Object getDestroyPacket();

   public abstract Object getMetaPacket(Object var1);

   public abstract Object getTeleportPacket(Location var1);

   public abstract Object getWatcher();
}
