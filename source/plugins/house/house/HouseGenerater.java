package house;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionFactory;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import land.Land;
import land.Pos;
import land.Range;
import landMain.LandMain;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class HouseGenerater implements Listener {
   private House house;
   private String pn;
   private String houseWorldName;
   private World houseWorld;
   private SchematicFormat schematicFormat;
   private String suf;
   private int level;
   private int start;
   private int yyFix;
   private int xxFix;
   private int zzFix;
   private int initHeiht;
   private int size;
   private int edgeSize;
   private double spawnX;
   private double spawnY;
   private double spawnZ;
   private float spawnYaw;
   private float spawnPitch;
   private String houseFile;
   private int nowSize;
   private HashMap hash;
   private HashMap addFlagsHash;

   public HouseGenerater(House house) {
      this.house = house;
      this.pn = house.getPn();
      this.hash = house.getHash();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      Bukkit.getPluginManager().registerEvents(this, house);
      this.houseWorld = house.getServer().getWorld(this.houseWorldName);
      if (this.houseWorld == null) {
         house.getServer().createWorld(new WorldCreator(this.houseWorldName));
         this.houseWorld = house.getServer().getWorld(this.houseWorldName);
      }

      this.schematicFormat = SchematicFormat.getFormat(new File(house.getDataFolder() + File.separator + "house.schematic"));
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   public void addUser(Player p) {
      String name = p.getName();

      for(int x = 0; x < this.nowSize; ++x) {
         for(int z = 0; z < this.nowSize; ++z) {
            if (!this.hash.containsKey(x) || !((HashMap)this.hash.get(x)).containsKey(z)) {
               HouseUser houseUser = new HouseUser(name, x, z);
               this.house.addHouseUser(houseUser);
               Pos p1 = new Pos(this.houseWorldName, x * this.size + this.edgeSize, this.start, z * this.size + this.edgeSize);
               Pos p2 = new Pos(this.houseWorldName, x * this.size + this.size - 1 - this.edgeSize, this.start + this.initHeiht, z * this.size + this.size - 1 - this.edgeSize);
               Range range = new Range(p1, p2);
               Land land;
               if (LandMain.getLandManager().getLand(name + this.suf) != null) {
                  land = LandManager.createLand(2, false, name, range, this.level);
               } else {
                  land = LandManager.createLand(2, false, name + this.suf, name, range, this.level);
               }

               for(String flag : this.addFlagsHash.keySet()) {
                  LandMain.getLandManager().getFlagHandler().addFlag(land, flag, (Integer)this.addFlagsHash.get(flag));
               }

               Location spawn = new Location(this.houseWorld, (double)(x * this.size + this.edgeSize) + this.spawnX, (double)(this.start + 1) + this.spawnY, (double)(z * this.size + this.edgeSize) + this.spawnZ, this.spawnYaw, this.spawnPitch);
               LandMain.getLandManager().getTpHandler().setTp(land, spawn);
               LandMain.getLandManager().addLand(land);
               this.generateHouse(x, z, false);
               return;
            }
         }
      }

      ++this.nowSize;
      YamlConfiguration config2 = new YamlConfiguration();
      config2.set("nowSize", this.nowSize);

      try {
         config2.save(this.house.getDataFolder() + File.separator + "nowSize.yml");
      } catch (IOException var12) {
      }

      this.addUser(p);
   }

   public void repair(Player p) {
      this.generateHouse(p.getLocation().getBlockX() / this.size, p.getLocation().getBlockZ() / this.size, true);
   }

   public void generateHouse(int xPos, int zPos, boolean force) {
      try {
         int xx = xPos * this.size;
         int zz = zPos * this.size;

         for(int xChunk = xx / 16; xChunk <= (xx + this.size) / 16; ++xChunk) {
            for(int zChunk = zz / 16; zChunk <= (zz + this.size) / 16; ++zChunk) {
               if (!this.houseWorld.getChunkAt(xChunk, zChunk).isLoaded()) {
                  this.houseWorld.getChunkAt(xChunk, zChunk).load(true);
               }
            }
         }

         int yy = this.yyFix + 3;
         if (!force && !this.houseWorld.getBlockAt(xx + this.xxFix - 1, yy - 1, zz + this.zzFix - 1).getType().equals(Material.AIR)) {
            return;
         }

         CuboidClipboard cuboidClipboard = this.schematicFormat.load(new File(this.house.getDataFolder() + File.separator + this.houseFile));
         EditSessionFactory editSessionFactory = new EditSessionFactory();
         LocalWorld localWorld = new BukkitWorld(this.houseWorld);
         EditSession editSession = editSessionFactory.getEditSession(localWorld, 100000);
         Vector newOrigin = new Vector(xx + this.xxFix - 1, yy, zz + this.zzFix - 1);
         cuboidClipboard.paste(editSession, newOrigin, true, true);
      } catch (IOException e) {
         e.printStackTrace();
      } catch (DataException e) {
         e.printStackTrace();
      } catch (MaxChangedBlocksException e) {
         e.printStackTrace();
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.addFlagsHash = new HashMap();

      for(String s : config.getStringList("addFlags")) {
         String flag = s.split(":")[0];
         int value = Integer.parseInt(s.split(":")[1]);
         this.addFlagsHash.put(flag, value);
      }

      this.suf = config.getString("house.suf");
      this.level = config.getInt("house.level");
      this.start = config.getInt("house.start");
      this.yyFix = config.getInt("house.yyFix");
      this.xxFix = config.getInt("house.xxFix");
      this.zzFix = config.getInt("house.zzFix");
      this.initHeiht = config.getInt("house.initHeiht");
      this.size = config.getInt("house.size");
      this.edgeSize = config.getInt("house.edgeSize");
      this.spawnX = config.getDouble("house.spawn.x");
      this.spawnY = config.getDouble("house.spawn.y");
      this.spawnZ = config.getDouble("house.spawn.z");
      this.spawnYaw = (float)config.getDouble("house.spawn.yaw");
      this.spawnPitch = (float)config.getDouble("house.spawn.pitch");
      this.houseFile = config.getString("house.generate.houseFile");
      this.houseWorldName = config.getString("houseWorldName");
      YamlConfiguration config2 = new YamlConfiguration();

      try {
         config2.load(this.house.getDataFolder() + File.separator + "nowSize.yml");
         this.nowSize = config2.getInt("nowSize");
      } catch (FileNotFoundException var6) {
      } catch (IOException var7) {
      } catch (InvalidConfigurationException var8) {
      }

   }
}
