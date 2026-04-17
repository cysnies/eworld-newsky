package clear;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Monster;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Clear implements Listener {
   private static final int DELAY_SHOW = 35;
   private static final int CHEST_ID = 54;
   private Random r = new Random();
   private Main main;
   private Server server;
   private ServerManager serverManager;
   private boolean tip;
   private HashMap ignoreWorlds;
   private int checkInterval;
   private int startClearEntitys;
   private int mustClearAmount;
   private int mustClearLevel;
   private List levelList;
   private int gridSize;
   private HashMap clearList;
   private int ticksLived;
   private int clearMode;
   private HashMap clearWhite;
   private HashMap clearBlack;
   private boolean ske;
   private HashMap clearMonsterList;
   private int maxPerGrid;
   private boolean firstAll;
   private int heightMax;
   private int heightMin;
   private HashMap clearTypes;
   private ClearTimer clearTimer;
   private HashMap airBlocks;

   public Clear(Main main) {
      this.main = main;
      this.server = main.getServer();
      this.serverManager = main.getServerManager();
      YamlConfiguration config = new YamlConfiguration();

      try {
         config.load(main.getPluginPath() + File.separator + main.getPn() + File.separator + "config.yml");
         this.loadConfig(config);
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (InvalidConfigurationException e) {
         e.printStackTrace();
      }

      this.clearTimer = new ClearTimer();
      main.getServer().getScheduler().scheduleSyncDelayedTask(main, this.clearTimer, (long)(this.checkInterval * 20));
   }

   public void info(CommandSender sender) {
      HashMap<String, Integer> entityHash = new HashMap();
      int total = 0;

      for(World w : this.server.getWorlds()) {
         List<Entity> list = w.getEntities();
         total += list.size();

         for(Entity e : list) {
            if (!entityHash.containsKey(e.getType().getName())) {
               entityHash.put(e.getType().getName(), 0);
            }

            entityHash.put(e.getType().getName(), (Integer)entityHash.get(e.getType().getName()) + 1);
         }
      }

      sender.sendMessage(this.main.format("success", this.get(1200)));

      for(String s : entityHash.keySet()) {
         String name = "";

         try {
            name = Names.getEntityName(EntityType.fromName(s).getTypeId());
         } catch (Exception var9) {
         }

         sender.sendMessage(this.main.format("broadcastInfo", name, entityHash.get(s)));
      }

      sender.sendMessage(this.main.format("clearInfo2", total));
   }

   public void clear(boolean force, int clearLevel) {
      HashMap<Short, Integer> startHash = new HashMap();
      int startTotal = 0;

      for(World w : this.server.getWorlds()) {
         List<Entity> list = w.getEntities();
         startTotal += list.size();

         for(Entity e : list) {
            if (!startHash.containsKey(e.getType().getTypeId())) {
               startHash.put(e.getType().getTypeId(), 0);
            }

            startHash.put(e.getType().getTypeId(), (Integer)startHash.get(e.getType().getTypeId()) + 1);
         }
      }

      if (startTotal >= this.mustClearAmount) {
         clearLevel = this.mustClearLevel;
      } else {
         if (!force && this.serverManager.getServerStatus() == 0) {
            return;
         }

         if (!force && startTotal < this.startClearEntitys) {
            return;
         }
      }

      if (clearLevel == -1) {
         clearLevel = this.serverManager.getServerStatus();
      } else if (clearLevel < 0) {
         clearLevel = 0;
      } else if (clearLevel > 3) {
         clearLevel = 3;
      }

      this.server.broadcastMessage(this.main.format("success", this.get(1205)));
      this.server.broadcastMessage(this.main.format("clearLevel", ((Level)this.levelList.get(clearLevel)).getShow()));
      if (((Level)this.levelList.get(clearLevel)).isEntity()) {
         if (this.tip) {
            this.server.broadcastMessage(this.get(1295) + this.get(1890));
         } else {
            Main.sendConsoleMessage(this.get(1295) + this.get(1890));
         }

         for(World w : this.server.getWorlds()) {
            if (!this.ignoreWorlds.containsKey(w.getName())) {
               Iterator<Entity> it = w.getEntities().iterator();

               while(it.hasNext()) {
                  Entity e = (Entity)it.next();

                  try {
                     int id = e.getType().getTypeId();
                     if (id == 1) {
                        Item item = (Item)e;
                        if (item.getTicksLived() >= this.ticksLived) {
                           ItemStack is = item.getItemStack();
                           int itemId = is.getTypeId();
                           if (this.clearMode == 1 && !this.clearWhite.containsKey(itemId) || this.clearMode == 2 && this.clearBlack.containsKey(itemId)) {
                              e.remove();
                              it.remove();
                           }
                        }
                     } else if (this.clearList.containsKey(id)) {
                        e.remove();
                        it.remove();
                     }
                  } catch (Exception var23) {
                  }
               }
            }
         }
      } else if (this.tip) {
         this.server.broadcastMessage(this.get(1295) + this.get(1891));
      } else {
         Main.sendConsoleMessage(this.get(1295) + this.get(1891));
      }

      if (((Level)this.levelList.get(clearLevel)).isMonster()) {
         if (this.tip) {
            this.server.broadcastMessage(this.get(1305) + this.get(1890));
         } else {
            Main.sendConsoleMessage(this.get(1305) + this.get(1890));
         }

         for(World w : this.server.getWorlds()) {
            if (!this.ignoreWorlds.containsKey(w.getName())) {
               Iterator<Monster> it = w.getEntitiesByClass(Monster.class).iterator();

               while(it.hasNext()) {
                  Monster mon = (Monster)it.next();
                  if (this.clearMonsterList.containsKey(mon.getType().getTypeId()) && (!this.ske || !mon.getType().equals(EntityType.SKELETON) || !w.getEnvironment().equals(Environment.NETHER))) {
                     mon.remove();
                     it.remove();
                  }
               }
            }
         }
      } else if (this.tip) {
         this.server.broadcastMessage(this.get(1305) + this.get(1891));
      } else {
         Main.sendConsoleMessage(this.get(1305) + this.get(1891));
      }

      if (((Level)this.levelList.get(clearLevel)).isAnimal()) {
         if (this.tip) {
            this.server.broadcastMessage(this.get(1310) + this.get(1890));
         } else {
            Main.sendConsoleMessage(this.get(1310) + this.get(1890));
         }

         for(World w : this.server.getWorlds()) {
            HashMap<Integer, HashMap<Integer, Integer>> amountHash = new HashMap();
            HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> generateHash = new HashMap();
            HashMap<Integer, HashMap<Integer, Location>> locHash = new HashMap();
            if (!this.ignoreWorlds.containsKey(w.getName())) {
               Iterator<Animals> it = w.getEntitiesByClass(Animals.class).iterator();

               while(it.hasNext()) {
                  Animals animals = (Animals)it.next();
                  int id = animals.getType().getTypeId();
                  if (this.clearTypes.containsKey(id)) {
                     int x = animals.getLocation().getBlockX() / this.gridSize;
                     int z = animals.getLocation().getBlockZ() / this.gridSize;
                     if (!amountHash.containsKey(x)) {
                        amountHash.put(x, new HashMap());
                     }

                     if (!((HashMap)amountHash.get(x)).containsKey(z)) {
                        ((HashMap)amountHash.get(x)).put(z, 0);
                     }

                     int current;
                     if ((current = (Integer)((HashMap)amountHash.get(x)).get(z)) >= this.maxPerGrid) {
                        animals.remove();
                        it.remove();
                        if (this.r.nextInt(1000) < (Integer)this.clearTypes.get(id)) {
                           if (!generateHash.containsKey(x)) {
                              generateHash.put(x, new HashMap());
                           }

                           if (!((HashMap)generateHash.get(x)).containsKey(z)) {
                              ((HashMap)generateHash.get(x)).put(z, new HashMap());
                           }

                           if (!((HashMap)((HashMap)generateHash.get(x)).get(z)).containsKey(id)) {
                              ((HashMap)((HashMap)generateHash.get(x)).get(z)).put(id, 0);
                           }

                           ((HashMap)((HashMap)generateHash.get(x)).get(z)).put(id, (Integer)((HashMap)((HashMap)generateHash.get(x)).get(z)).get(id) + 1);
                           if (!locHash.containsKey(x)) {
                              locHash.put(x, new HashMap());
                           }

                           ((HashMap)locHash.get(x)).put(z, animals.getLocation());
                        }
                     } else {
                        ((HashMap)amountHash.get(x)).put(z, current + 1);
                     }
                  }
               }

               for(int x2 : generateHash.keySet()) {
                  for(int z2 : ((HashMap)generateHash.get(x2)).keySet()) {
                     try {
                        this.checkGenerateChest(w, x2, z2, (HashMap)((HashMap)generateHash.get(x2)).get(z2), (Location)((HashMap)locHash.get(x2)).get(z2));
                     } catch (Exception var22) {
                     }
                  }
               }
            }
         }
      } else if (this.tip) {
         this.server.broadcastMessage(this.get(1310) + this.get(1891));
      } else {
         Main.sendConsoleMessage(this.get(1310) + this.get(1891));
      }

      this.server.getScheduler().scheduleSyncDelayedTask(this.main, new DelayShow(startHash, startTotal), 35L);
   }

   public void loadConfig(YamlConfiguration config) {
      this.tip = config.getBoolean("clear.tip");
      this.ignoreWorlds = new HashMap();

      for(String s : config.getStringList("clear.ignoreWorlds")) {
         this.ignoreWorlds.put(s, true);
      }

      this.checkInterval = config.getInt("clear.checkInterval");
      this.startClearEntitys = config.getInt("clear.startClearEntitys");
      this.mustClearAmount = config.getInt("clear.mustClear.amount");
      this.mustClearLevel = config.getInt("clear.mustClear.level");
      this.levelList = new ArrayList();

      String[] var9;
      for(String s : var9 = new String[]{"unknown", "good", "fine", "bad"}) {
         String show = Main.convert(config.getString("clear.clear." + s + ".show"));
         boolean entity = config.getBoolean("clear.clear." + s + ".entity");
         boolean monster = config.getBoolean("clear.clear." + s + ".monster");
         boolean animal = config.getBoolean("clear.clear." + s + ".animal");
         Level level = new Level(show, entity, monster, animal);
         this.levelList.add(level);
      }

      this.clearList = new HashMap();

      for(int i : config.getIntegerList("clear.entity.clear")) {
         this.clearList.put(i, true);
      }

      this.ticksLived = config.getInt("clear.entity.items.ticksLived");
      this.clearMode = config.getInt("clear.entity.items.mode");
      this.clearWhite = new HashMap();
      this.clearBlack = new HashMap();

      for(int i : config.getIntegerList("clear.entity.items.white")) {
         this.clearWhite.put(i, true);
      }

      for(int i : config.getIntegerList("clear.entity.items.black")) {
         this.clearBlack.put(i, true);
      }

      this.ske = config.getBoolean("clear.monster.ske");
      this.clearMonsterList = new HashMap();

      for(int i : config.getIntegerList("clear.monster.clear")) {
         this.clearMonsterList.put((short)i, true);
      }

      this.gridSize = config.getInt("clear.animal.gridSize");
      this.maxPerGrid = config.getInt("clear.animal.maxPerGrid");
      this.firstAll = config.getBoolean("clear.animal.firstAll");
      this.heightMax = config.getInt("clear.animal.heightMax");
      this.heightMin = config.getInt("clear.animal.heightMin");
      this.clearTypes = new HashMap();

      for(String s : config.getStringList("clear.animal.clearTypes")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         int chance = Integer.parseInt(s.split(" ")[1]);
         this.clearTypes.put(id, chance);
      }

      this.airBlocks = new HashMap();

      for(int i : config.getIntegerList("clear.animal.airBlocks")) {
         this.airBlocks.put(i, true);
      }

   }

   private void checkGenerateChest(World w, int x, int z, HashMap hash, Location l) {
      if (this.firstAll) {
         for(int y2 = l.getBlockY() - this.heightMin; y2 <= l.getBlockY() + this.heightMax; ++y2) {
            for(int x2 = x * this.gridSize; x2 < x * (this.gridSize + 1); ++x2) {
               for(int z2 = z * this.gridSize; z2 < z * (this.gridSize + 1); ++z2) {
                  if (w.getBlockTypeIdAt(x2, y2, z2) == 54) {
                     Chest chest = (Chest)w.getBlockAt(x2, y2, z2).getState();
                     Inventory inventory = chest.getBlockInventory();

                     for(int id : hash.keySet()) {
                        short type = (short)id;
                        inventory.addItem(new ItemStack[]{new ItemStack(383, (Integer)hash.get(id), type)});
                     }

                     return;
                  }
               }
            }
         }
      }

      int xx = l.getBlockX();
      int zz = l.getBlockZ();
      if (this.airBlocks.containsKey(w.getBlockAt(xx, l.getBlockY(), zz).getTypeId())) {
         for(int yy = l.getBlockY() - 1; yy > 0; --yy) {
            if (!this.airBlocks.containsKey(w.getBlockAt(xx, yy, zz).getTypeId())) {
               if (w.getBlockAt(xx, yy, zz).getTypeId() != 54) {
                  ++yy;
                  w.getBlockAt(xx, yy, zz).setTypeId(54);
               }

               Chest chest = (Chest)w.getBlockAt(xx, yy, zz).getState();
               Inventory inventory = chest.getBlockInventory();

               for(int id : hash.keySet()) {
                  short type = (short)id;
                  inventory.addItem(new ItemStack[]{new ItemStack(383, (Integer)hash.get(id), type)});
               }

               return;
            }
         }

         for(int yy = 254; yy > l.getBlockY(); --yy) {
            if (!this.airBlocks.containsKey(w.getBlockAt(xx, yy, zz).getTypeId())) {
               if (w.getBlockAt(xx, yy, zz).getTypeId() != 54) {
                  ++yy;
                  w.getBlockAt(xx, yy, zz).setTypeId(54);
               }

               Chest chest = (Chest)w.getBlockAt(xx, yy, zz).getState();
               Inventory inventory = chest.getBlockInventory();

               for(int id : hash.keySet()) {
                  short type = (short)id;
                  inventory.addItem(new ItemStack[]{new ItemStack(383, (Integer)hash.get(id), type)});
               }

               return;
            }
         }
      } else {
         for(int yy = l.getBlockY() + 1; yy < 255; ++yy) {
            if (this.airBlocks.containsKey(w.getBlockAt(xx, yy, zz).getTypeId())) {
               if (w.getBlockAt(xx, yy - 1, zz).getTypeId() == 54) {
                  --yy;
               } else {
                  w.getBlockAt(xx, yy, zz).setTypeId(54);
               }

               Chest chest = (Chest)w.getBlockAt(xx, yy, zz).getState();
               Inventory inventory = chest.getBlockInventory();

               for(int id : hash.keySet()) {
                  short type = (short)id;
                  inventory.addItem(new ItemStack[]{new ItemStack(383, (Integer)hash.get(id), type)});
               }

               return;
            }
         }

         for(int yy = l.getBlockY() - 1; yy > 0; --yy) {
            if (this.airBlocks.containsKey(w.getBlockAt(xx, yy, zz).getTypeId())) {
               for(int yyy = yy - 1; yyy > 0; --yyy) {
                  if (!this.airBlocks.containsKey(w.getBlockAt(xx, yyy, zz).getTypeId())) {
                     if (w.getBlockAt(xx, yyy, zz).getTypeId() != 54) {
                        ++yyy;
                        w.getBlockAt(xx, yyy, zz).setTypeId(54);
                     }

                     Chest chest = (Chest)w.getBlockAt(xx, yyy, zz).getState();
                     Inventory inventory = chest.getBlockInventory();

                     for(int id : hash.keySet()) {
                        short type = (short)id;
                        inventory.addItem(new ItemStack[]{new ItemStack(383, (Integer)hash.get(id), type)});
                     }

                     return;
                  }
               }
            }
         }
      }

   }

   private String get(int id) {
      return this.main.get(id);
   }

   class Level {
      private String show;
      private boolean entity;
      private boolean monster;
      private boolean animal;

      public Level(String show, boolean entity, boolean monster, boolean animal) {
         this.show = show;
         this.entity = entity;
         this.monster = monster;
         this.animal = animal;
      }

      public String getShow() {
         return this.show;
      }

      public boolean isEntity() {
         return this.entity;
      }

      public boolean isMonster() {
         return this.monster;
      }

      public boolean isAnimal() {
         return this.animal;
      }
   }

   class ClearTimer implements Runnable {
      public void run() {
         Clear.this.clear(false, -1);
         Clear.this.main.getServer().getScheduler().scheduleSyncDelayedTask(Clear.this.main, Clear.this.clearTimer, (long)(Clear.this.checkInterval * 20));
      }
   }

   class DelayShow implements Runnable {
      HashMap startHash;
      int startTotal;

      public DelayShow(HashMap startHash, int startTotal) {
         this.startHash = startHash;
         this.startTotal = startTotal;
      }

      public void run() {
         HashMap<Short, Integer> endHash = new HashMap();
         int endTotal = 0;

         for(World w : Clear.this.server.getWorlds()) {
            List<Entity> list2 = w.getEntities();
            endTotal += list2.size();

            for(Entity e : list2) {
               if (!endHash.containsKey(e.getType().getTypeId())) {
                  endHash.put(e.getType().getTypeId(), 0);
               }

               endHash.put(e.getType().getTypeId(), (Integer)endHash.get(e.getType().getTypeId()) + 1);
            }
         }

         Clear.this.server.broadcastMessage(Clear.this.main.format("success", Clear.this.get(1210)));

         for(short s : this.startHash.keySet()) {
            int end;
            if (endHash.containsKey(s)) {
               end = (Integer)endHash.get(s);
            } else {
               end = 0;
            }

            String show = Names.getEntityName(s);
            if (Clear.this.tip) {
               Clear.this.server.broadcastMessage(Clear.this.main.format("clearInfo", show, this.startHash.get(s), end));
            } else {
               Main.sendConsoleMessage(Clear.this.main.format("clearInfo", show, this.startHash.get(s), end));
            }

            endHash.remove(s);
         }

         for(Short s : endHash.keySet()) {
            String show = Names.getEntityName(s);
            if (Clear.this.tip) {
               Clear.this.server.broadcastMessage(Clear.this.main.format("clearInfo", show, 0, endHash.get(s)));
            } else {
               Main.sendConsoleMessage(Clear.this.main.format("clearInfo", show, 0, endHash.get(s)));
            }
         }

         Clear.this.server.broadcastMessage(Clear.this.main.format("clearInfo3", this.startTotal, endTotal));
      }
   }
}
