package com.sk89q.worldedit.bukkit;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.bukkit.entity.BukkitEntity;
import com.sk89q.worldedit.bukkit.entity.BukkitExpOrb;
import com.sk89q.worldedit.bukkit.entity.BukkitItem;
import com.sk89q.worldedit.bukkit.entity.BukkitPainting;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;

public class BukkitUtil {
   public static final double EQUALS_PRECISION = 1.0E-4;

   private BukkitUtil() {
   }

   public static LocalWorld getLocalWorld(World w) {
      return new BukkitWorld(w);
   }

   public static BlockVector toVector(Block block) {
      return new BlockVector(block.getX(), block.getY(), block.getZ());
   }

   public static BlockVector toVector(BlockFace face) {
      return new BlockVector(face.getModX(), face.getModY(), face.getModZ());
   }

   public static BlockWorldVector toWorldVector(Block block) {
      return new BlockWorldVector(getLocalWorld(block.getWorld()), block.getX(), block.getY(), block.getZ());
   }

   public static Vector toVector(Location loc) {
      return new Vector(loc.getX(), loc.getY(), loc.getZ());
   }

   public static com.sk89q.worldedit.Location toLocation(Location loc) {
      return new com.sk89q.worldedit.Location(getLocalWorld(loc.getWorld()), new Vector(loc.getX(), loc.getY(), loc.getZ()), loc.getYaw(), loc.getPitch());
   }

   public static Vector toVector(org.bukkit.util.Vector vector) {
      return new Vector(vector.getX(), vector.getY(), vector.getZ());
   }

   public static Location toLocation(WorldVector pt) {
      return new Location(toWorld(pt), pt.getX(), pt.getY(), pt.getZ());
   }

   public static Location toLocation(World world, Vector pt) {
      return new Location(world, pt.getX(), pt.getY(), pt.getZ());
   }

   public static Location center(Location loc) {
      return new Location(loc.getWorld(), (double)loc.getBlockX() + (double)0.5F, (double)loc.getBlockY() + (double)0.5F, (double)loc.getBlockZ() + (double)0.5F, loc.getPitch(), loc.getYaw());
   }

   public static Player matchSinglePlayer(Server server, String name) {
      List<Player> players = server.matchPlayer(name);
      return players.size() == 0 ? null : (Player)players.get(0);
   }

   public static Block toBlock(BlockWorldVector pt) {
      return toWorld((WorldVector)pt).getBlockAt(toLocation((WorldVector)pt));
   }

   public static World toWorld(WorldVector pt) {
      return ((BukkitWorld)pt.getWorld()).getWorld();
   }

   public static boolean equals(Location a, Location b) {
      if (Math.abs(a.getX() - b.getX()) > 1.0E-4) {
         return false;
      } else if (Math.abs(a.getY() - b.getY()) > 1.0E-4) {
         return false;
      } else {
         return !(Math.abs(a.getZ() - b.getZ()) > 1.0E-4);
      }
   }

   public static Location toLocation(com.sk89q.worldedit.Location teleportLocation) {
      Vector pt = teleportLocation.getPosition();
      return new Location(toWorld(teleportLocation.getWorld()), pt.getX(), pt.getY(), pt.getZ(), teleportLocation.getYaw(), teleportLocation.getPitch());
   }

   public static World toWorld(LocalWorld world) {
      return ((BukkitWorld)world).getWorld();
   }

   public static BukkitEntity toLocalEntity(Entity e) {
      switch (e.getType()) {
         case EXPERIENCE_ORB:
            return new BukkitExpOrb(toLocation(e.getLocation()), e.getUniqueId(), ((ExperienceOrb)e).getExperience());
         case PAINTING:
            Painting paint = (Painting)e;
            return new BukkitPainting(toLocation(e.getLocation()), paint.getArt(), paint.getFacing(), e.getUniqueId());
         case DROPPED_ITEM:
            return new BukkitItem(toLocation(e.getLocation()), ((Item)e).getItemStack(), e.getUniqueId());
         default:
            return new BukkitEntity(toLocation(e.getLocation()), e.getType(), e.getUniqueId());
      }
   }
}
