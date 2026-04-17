package mine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import land.Land;
import land.Pos;
import landMain.LandMain;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.hashList.ChanceHashList;
import lib.hashList.ChanceHashListImpl;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.time.TimeEvent;
import lib.types.InvalidTypeException;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import lib.util.UtilTypes;
import net.minecraft.server.v1_6_R2.Packet63WorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;

public class Mine implements Listener {
   private static final String FLAG = "mine";
   private static final BlockFace[] faces;
   private Random r = new Random();
   private String pn;
   private String savePath;
   private LandManager landManager;
   private String per_mine_admin;
   private String per_mine_luck;
   private int item;
   private int interval;
   private int chance;
   private int fixMax;
   private List fixList;
   private HashMap fixs;
   private int times;
   private int luck;
   private int netInterval;
   private int netChance;
   private int netTimes;
   private String name;
   private float offset;
   private float speed;
   private int count;
   private int showRange;
   private float volume;
   private float pitch;
   private HashMap levelsHash;
   private HashMap oreHash;
   private ChanceHashList monsList;
   private HashMap locHash;
   private HashList onList;

   static {
      faces = new BlockFace[]{BlockFace.SELF, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST};
   }

   public Mine(Main main) {
      this.pn = main.getPn();
      this.savePath = main.getPluginPath() + File.separator + this.pn + File.separator + "mine.yml";
      this.onList = new HashListImpl();
      this.landManager = LandMain.getLandManager();
      this.loadMines();
      this.loadConfig0(UtilConfig.getConfig(this.pn));
      Bukkit.getPluginManager().registerEvents(this, main);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig0(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockBreak(BlockBreakEvent e) {
      try {
         if (e.getPlayer().isOp()) {
            return;
         }

         Land land = this.landManager.getHighestPriorityLand(e.getBlock().getLocation());
         if (land != null && land.hasFlag("mine")) {
            int id = e.getBlock().getTypeId();
            if (id == 30) {
               boolean result = false;

               BlockFace[] var8;
               for(BlockFace bf : var8 = faces) {
                  Location l = e.getBlock().getRelative(bf).getLocation();

                  for(int y = 0; y < l.getBlockY(); ++y) {
                     if (!UtilTypes.checkItem((String)null, "safeBlocks", l.getBlock().getRelative(0, -y, 0).getTypeId() + ":" + l.getBlock().getData())) {
                        if (UtilTypes.checkItem((String)null, "safeBlocks", l.getBlock().getRelative(0, 1 - y, 0).getTypeId() + ":" + l.getBlock().getData()) && UtilTypes.checkItem((String)null, "safeBlocks", l.getBlock().getRelative(0, 2 - y, 0).getTypeId() + ":" + l.getBlock().getData())) {
                           Entity entity = e.getBlock().getWorld().spawnEntity(l.getBlock().getRelative(0, 1 - y, 0).getLocation(), EntityType.ZOMBIE);
                           Zombie z = (Zombie)entity;
                           EntityEquipment equip = z.getEquipment();
                           equip.setHelmetDropChance(0.0F);
                           equip.setChestplateDropChance(0.0F);
                           equip.setLeggingsDropChance(0.0F);
                           equip.setBootsDropChance(0.0F);
                           z.setTarget(e.getPlayer());
                           Util.eject(z, z.getLocation(), e.getPlayer().getLocation());
                           result = true;
                        }
                        break;
                     }
                  }

                  if (result) {
                     break;
                  }
               }
            } else if (UtilTypes.checkItem(this.pn, "mine", id + ":" + e.getBlock().getData())) {
               Ore ore = (Ore)this.oreHash.get(id);
               if (ore != null && this.r.nextInt(100) < ore.getChance()) {
                  if (!UtilPer.hasPer(e.getPlayer(), this.per_mine_luck) || this.r.nextInt(100) >= this.luck) {
                     e.setCancelled(true);
                     e.getBlock().setTypeId(0);
                     if (UtilPer.hasPer(e.getPlayer(), this.per_mine_luck)) {
                        e.getPlayer().sendMessage(this.get(75));
                     } else {
                        e.getPlayer().sendMessage(this.get(90));
                     }

                     for(int i = 0; i < ore.getTimes(); ++i) {
                        if (this.r.nextInt(100) < ore.getSuccess()) {
                           try {
                              int spawnId = (Integer)this.monsList.getRandom();
                              boolean result = false;

                              BlockFace[] var25;
                              for(BlockFace bf : var25 = faces) {
                                 Location l = e.getBlock().getRelative(bf).getLocation();

                                 for(int y = 0; y < l.getBlockY(); ++y) {
                                    if (!UtilTypes.checkItem((String)null, "safeBlocks", l.getBlock().getRelative(0, -y, 0).getTypeId() + ":" + l.getBlock().getData())) {
                                       if (UtilTypes.checkItem((String)null, "safeBlocks", l.getBlock().getRelative(0, 1 - y, 0).getTypeId() + ":" + l.getBlock().getData()) && UtilTypes.checkItem((String)null, "safeBlocks", l.getBlock().getRelative(0, 2 - y, 0).getTypeId() + ":" + l.getBlock().getData())) {
                                          Entity entity = e.getBlock().getWorld().spawnEntity(l.getBlock().getRelative(0, 1 - y, 0).getLocation(), EntityType.fromId(spawnId));
                                          if (entity instanceof Creature) {
                                             Creature creature = (Creature)entity;
                                             creature.setTarget(e.getPlayer());
                                          }

                                          Util.eject(entity, entity.getLocation(), e.getPlayer().getLocation());
                                          result = true;
                                       }
                                       break;
                                    }
                                 }

                                 if (result) {
                                    break;
                                 }
                              }
                           } catch (Exception var16) {
                           }
                        }
                     }
                  } else {
                     e.getPlayer().sendMessage(this.get(85));
                  }
               } else {
                  e.getPlayer().sendMessage(this.get(80));
               }
            } else {
               e.setCancelled(true);
            }
         }
      } catch (InvalidTypeException e1) {
         e1.printStackTrace();
         e.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerInteract(PlayerInteractEvent e) {
      if (this.onList.has(e.getPlayer()) && e.getAction().equals(Action.LEFT_CLICK_BLOCK) && e.hasItem() && e.getItem().getTypeId() == this.item) {
         e.setCancelled(true);
         Location l = e.getClickedBlock().getLocation();
         Integer level = (Integer)this.locHash.get(Pos.getPos(l));
         if (level == null) {
            level = 0;
         }

         Player p = e.getPlayer();
         level = level + 1;
         if (level > this.levelsHash.size()) {
            level = 0;
         }

         if (level == 0) {
            this.locHash.remove(Pos.getPos(l));
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(70)}));
         } else {
            this.locHash.put(Pos.getPos(l), level);
            p.sendMessage(UtilFormat.format(this.pn, "setTip", new Object[]{level}));
         }

         this.saveMines();
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onTime(TimeEvent e) {
      if (TimeEvent.getTime() % (long)this.netInterval == 0L && this.r.nextInt(100) < this.netChance && this.locHash.size() > 0) {
         Location l = Pos.toLoc((Pos)this.locHash.keySet().toArray()[this.r.nextInt(this.locHash.size())]);

         for(int i = 0; i < this.netTimes; ++i) {
            int x = this.r.nextInt(3) - 1;
            int y = this.r.nextInt(3) - 1;
            int z = this.r.nextInt(3) - 1;
            Block b = l.getBlock().getRelative(x, y, z);
            if (b.getTypeId() == 0) {
               b.setTypeId(30, false);
               break;
            }
         }
      }

      if (TimeEvent.getTime() % (long)this.interval == 0L) {
         int online = Bukkit.getOnlinePlayers().length;
         int c = this.fixMax;

         for(int check : this.fixList) {
            if (online <= check) {
               c = (Integer)this.fixs.get(check);
               break;
            }
         }

         if (this.r.nextInt(this.fixMax) < c && this.r.nextInt(100) < this.chance && this.locHash.size() > 0) {
            for(int i = 0; i < this.times; ++i) {
               Location l = Pos.toLoc((Pos)this.locHash.keySet().toArray()[this.r.nextInt(this.locHash.size())]);
               Block b = l.getBlock();
               if (b.getTypeId() == 0 || b.getTypeId() == 1) {
                  int level = (Integer)this.locHash.get(Pos.getPos(l));
                  int id = (Integer)((ChanceHashList)this.levelsHash.get(level)).getRandom();
                  b.setTypeId(id);

                  try {
                     this.showEffect(l);
                     l.getWorld().playSound(l, Sound.LEVEL_UP, this.volume, this.pitch);
                  } catch (Exception var10) {
                  }

                  return;
               }
            }
         }
      }

   }

   public void on(Player p) {
      if (UtilPer.checkPer(p, this.per_mine_admin)) {
         if (this.onList.has(p)) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(50)}));
         } else {
            this.onList.add(p);
            p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(55)}));
         }
      }
   }

   public void off(Player p) {
      if (UtilPer.checkPer(p, this.per_mine_admin)) {
         if (!this.onList.has(p)) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(60)}));
         } else {
            this.onList.remove(p);
            p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(65)}));
         }
      }
   }

   private void showEffect(Location l) {
      Packet63WorldParticles packet = new Packet63WorldParticles(this.name, (float)l.getX(), (float)l.getY(), (float)l.getZ(), this.offset, this.offset, this.offset, this.speed, this.count);

      Player[] var6;
      for(Player p : var6 = Bukkit.getServer().getOnlinePlayers()) {
         if (p.getWorld().equals(l.getWorld()) && p.getLocation().distance(l) < (double)this.showRange) {
            ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
         }
      }

   }

   private void loadMines() {
      try {
         (new File(this.savePath)).createNewFile();
      } catch (IOException e1) {
         e1.printStackTrace();
      }

      this.locHash = new HashMap();
      YamlConfiguration config = new YamlConfiguration();

      try {
         config.load(this.savePath);
         Map<String, Object> map = config.getValues(false);

         for(String s : map.keySet()) {
            int level = (Integer)map.get(s);
            String world = s.split("_")[0];
            int x = Integer.parseInt(s.split("_")[1]);
            int y = Integer.parseInt(s.split("_")[2]);
            int z = Integer.parseInt(s.split("_")[3]);
            this.locHash.put(Pos.getPos(Bukkit.getWorld(world).getBlockAt(x, y, z).getLocation()), level);
         }
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (InvalidConfigurationException e) {
         e.printStackTrace();
      }

   }

   private void saveMines() {
      YamlConfiguration config = new YamlConfiguration();

      for(Pos pos : this.locHash.keySet()) {
         int level = (Integer)this.locHash.get(pos);
         config.set(pos.getWorld() + "_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ(), level);
      }

      try {
         config.save(this.savePath);
      } catch (IOException e1) {
         e1.printStackTrace();
      }

   }

   private void loadConfig0(FileConfiguration config) {
      this.per_mine_admin = config.getString("per_mine_admin");
      this.per_mine_luck = config.getString("per_mine_luck");
      this.item = config.getInt("item");
      this.interval = config.getInt("interval");
      this.chance = config.getInt("chance");
      this.fixMax = config.getInt("fixMax");
      this.fixList = new ArrayList();
      this.fixs = new HashMap();

      for(String s : config.getStringList("fixs")) {
         int amount = Integer.parseInt(s.split(" ")[0]);
         int chance = Integer.parseInt(s.split(" ")[1]);
         this.fixList.add(amount);
         this.fixs.put(amount, chance);
      }

      this.times = config.getInt("times");
      this.luck = config.getInt("luck");
      this.netInterval = config.getInt("net.interval");
      this.netChance = config.getInt("net.chance");
      this.netTimes = config.getInt("net.times");
      this.name = config.getString("name");
      this.offset = (float)config.getDouble("offset");
      this.speed = (float)config.getDouble("speed");
      this.count = config.getInt("count");
      this.showRange = config.getInt("showRange");
      this.volume = (float)config.getDouble("volume");
      this.pitch = (float)config.getDouble("pitch");
      this.levelsHash = new HashMap();

      for(int index = 1; config.contains("levels.level" + index); ++index) {
         this.levelsHash.put(index, new ChanceHashListImpl());

         for(String s : config.getStringList("levels.level" + index)) {
            int id = Integer.parseInt(s.split(" ")[0]);
            int chance = Integer.parseInt(s.split(" ")[1]);
            ((ChanceHashList)this.levelsHash.get(index)).addChance(id, chance);
         }
      }

      this.oreHash = new HashMap();

      for(String s : config.getStringList("ores")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         int chance = Integer.parseInt(s.split(" ")[1]);
         int times = Integer.parseInt(s.split(" ")[2]);
         int success = Integer.parseInt(s.split(" ")[3]);
         Ore ore = new Ore(id, chance, times, success);
         this.oreHash.put(id, ore);
      }

      this.monsList = new ChanceHashListImpl();

      for(String s : config.getStringList("monsters")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         int chance = Integer.parseInt(s.split(" ")[1]);
         this.monsList.addChance(id, chance);
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   public class Ore {
      private int id;
      private int chance;
      private int times;
      private int success;

      public Ore(int id, int chance, int times, int success) {
         this.id = id;
         this.chance = chance;
         this.times = times;
         this.success = success;
      }

      public int getId() {
         return this.id;
      }

      public int getChance() {
         return this.chance;
      }

      public int getTimes() {
         return this.times;
      }

      public int getSuccess() {
         return this.success;
      }
   }
}
