package land;

import java.io.Serializable;
import landMain.LandManager;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;

public class Pos implements Serializable, Cloneable {
   private static final long serialVersionUID = 1L;
   private static final int YMAX = 256;
   private static final int YMIN = 0;
   private static Server server;
   private String world;
   private int x;
   private int y;
   private int z;

   public Pos(String world, int x, int y, int z) {
      this.world = world;
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public static void init(LandManager landManager) {
      server = landManager.getServer();
   }

   public static Pos getPos(Location l) {
      return new Pos(l.getWorld().getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
   }

   public static Location toLoc(Pos pos) {
      World w = server.getWorld(pos.getWorld());
      return new Location(w, (double)pos.getX() + (double)0.5F, (double)pos.getY(), (double)pos.getZ() + (double)0.5F);
   }

   public boolean compare(Pos p) {
      return this.x <= p.getX() && this.y <= p.getY() && this.z <= p.getZ();
   }

   public String getWorld() {
      return this.world;
   }

   public void setWorld(String world) {
      this.world = world;
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
      if (y > 256) {
         this.y = 256;
      }

      if (y < 0) {
         this.y = 0;
      }

   }

   public int getZ() {
      return this.z;
   }

   public void setZ(int z) {
      this.z = z;
   }

   public int hashCode() {
      return this.world.hashCode() + this.x + this.y + this.z;
   }

   public boolean equals(Object obj) {
      Pos pos = (Pos)obj;
      return pos.world.equals(this.world) && pos.x == this.x && pos.y == this.y && pos.z == this.z;
   }

   public Pos clone() {
      return new Pos(this.world, this.x, this.y, this.z);
   }
}
