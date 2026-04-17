package org.maxgamer.QuickShop;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.maxgamer.QuickShop.Command.QS;
import org.maxgamer.QuickShop.Database.Database;
import org.maxgamer.QuickShop.Database.DatabaseCore;
import org.maxgamer.QuickShop.Database.DatabaseHelper;
import org.maxgamer.QuickShop.Database.MySQLCore;
import org.maxgamer.QuickShop.Database.SQLiteCore;
import org.maxgamer.QuickShop.Economy.Economy;
import org.maxgamer.QuickShop.Economy.EconomyCore;
import org.maxgamer.QuickShop.Economy.Economy_Vault;
import org.maxgamer.QuickShop.Listeners.BlockListener;
import org.maxgamer.QuickShop.Listeners.ChatListener;
import org.maxgamer.QuickShop.Listeners.ChunkListener;
import org.maxgamer.QuickShop.Listeners.HeroChatListener;
import org.maxgamer.QuickShop.Listeners.LockListener;
import org.maxgamer.QuickShop.Listeners.PlayerListener;
import org.maxgamer.QuickShop.Listeners.WorldListener;
import org.maxgamer.QuickShop.Metrics.Metrics;
import org.maxgamer.QuickShop.Metrics.ShopListener;
import org.maxgamer.QuickShop.Shop.ContainerShop;
import org.maxgamer.QuickShop.Shop.Shop;
import org.maxgamer.QuickShop.Shop.ShopManager;
import org.maxgamer.QuickShop.Shop.ShopType;
import org.maxgamer.QuickShop.Util.Converter;
import org.maxgamer.QuickShop.Util.MsgUtil;
import org.maxgamer.QuickShop.Util.Util;
import org.maxgamer.QuickShop.Watcher.ItemWatcher;
import org.maxgamer.QuickShop.Watcher.LogWatcher;

public class QuickShop extends JavaPlugin {
   public static QuickShop instance;
   private Economy economy;
   private ShopManager shopManager;
   public HashSet warnings = new HashSet(10);
   private Database database;
   private ChatListener chatListener;
   private HeroChatListener heroChatListener;
   private BlockListener blockListener = new BlockListener(this);
   private PlayerListener playerListener = new PlayerListener(this);
   private ChunkListener chunkListener = new ChunkListener(this);
   private WorldListener worldListener = new WorldListener(this);
   private BukkitTask itemWatcherTask;
   private LogWatcher logWatcher;
   public boolean sneak;
   public boolean display = true;
   public boolean priceChangeRequiresFee = false;
   public boolean limit = false;
   private HashMap limits = new HashMap();
   public boolean useSpout = false;
   private Metrics metrics;
   public boolean debug = false;

   public int getShopLimit(Player p) {
      int max = this.getConfig().getInt("limits.default");

      for(Map.Entry entry : this.limits.entrySet()) {
         if ((Integer)entry.getValue() > max && p.hasPermission((String)entry.getKey())) {
            max = (Integer)entry.getValue();
         }
      }

      return max;
   }

   public Metrics getMetrics() {
      return this.metrics;
   }

   public void onEnable() {
      instance = this;
      this.saveDefaultConfig();
      this.reloadConfig();
      this.getConfig().options().copyDefaults(true);
      if (this.loadEcon()) {
         this.shopManager = new ShopManager(this);
         if (this.display) {
            this.getLogger().info("Starting item scheduler");
            ItemWatcher itemWatcher = new ItemWatcher(this);
            this.itemWatcherTask = Bukkit.getScheduler().runTaskTimer(this, itemWatcher, 600L, 600L);
         }

         if (this.getConfig().getBoolean("log-actions")) {
            this.logWatcher = new LogWatcher(this, new File(this.getDataFolder(), "qs.log"));
            this.logWatcher.task = Bukkit.getScheduler().runTaskTimerAsynchronously(this, this.logWatcher, 150L, 150L);
         }

         if (this.getConfig().getBoolean("shop.lock")) {
            LockListener ll = new LockListener(this);
            this.getServer().getPluginManager().registerEvents(ll, this);
         }

         ConfigurationSection limitCfg = this.getConfig().getConfigurationSection("limits");
         if (limitCfg != null) {
            this.getLogger().info("Limit cfg found...");
            this.limit = limitCfg.getBoolean("use", false);
            this.getLogger().info("Limits.use: " + this.limit);
            limitCfg = limitCfg.getConfigurationSection("ranks");

            for(String key : limitCfg.getKeys(true)) {
               this.limits.put(key, limitCfg.getInt(key));
            }

            this.getLogger().info(this.limits.toString());
         }

         try {
            ConfigurationSection dbCfg = this.getConfig().getConfigurationSection("database");
            if (dbCfg.getBoolean("mysql")) {
               String user = dbCfg.getString("user");
               String pass = dbCfg.getString("password");
               String host = dbCfg.getString("host");
               String port = dbCfg.getString("port");
               String database = dbCfg.getString("database");
               DatabaseCore dbCore = new MySQLCore(host, user, pass, database, port);
               this.database = new Database(dbCore);
            } else {
               DatabaseCore dbCore = new SQLiteCore(new File(this.getDataFolder(), "shops.db"));
               this.database = new Database(dbCore);
            }

            DatabaseHelper.setup(this.getDB());
         } catch (Database.ConnectionException e) {
            e.printStackTrace();
            this.getLogger().severe("Error connecting to database. Aborting plugin load.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
         } catch (SQLException e) {
            e.printStackTrace();
            this.getLogger().severe("Error setting up database. Aborting plugin load.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
         }

         int count = 0;

         try {
            this.getLogger().info("Loading shops from database...");
            int res = Converter.convert();
            if (res < 0) {
               System.out.println("Could not convert shops. Exitting.");
               return;
            }

            if (res > 0) {
               System.out.println("Conversion success. Continuing...");
            }

            Connection con = this.database.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM shops");
            ResultSet rs = ps.executeQuery();
            int errors = 0;

            while(rs.next()) {
               int x = 0;
               int y = 0;
               int z = 0;
               String worldName = null;

               try {
                  x = rs.getInt("x");
                  y = rs.getInt("y");
                  z = rs.getInt("z");
                  worldName = rs.getString("world");
                  World world = Bukkit.getWorld(worldName);
                  ItemStack item = Util.deserialize(rs.getString("itemConfig"));
                  String owner = rs.getString("owner");
                  double price = rs.getDouble("price");
                  Location loc = new Location(world, (double)x, (double)y, (double)z);
                  if (world != null && !(loc.getBlock().getState() instanceof InventoryHolder)) {
                     this.getLogger().info("Shop is not an InventoryHolder in " + rs.getString("world") + " at: " + x + ", " + y + ", " + z + ".  Deleting.");
                     PreparedStatement delps = this.getDB().getConnection().prepareStatement("DELETE FROM shops WHERE x = ? AND y = ? and z = ? and world = ?");
                     delps.setInt(1, x);
                     delps.setInt(2, y);
                     delps.setInt(3, z);
                     delps.setString(4, worldName);
                     delps.execute();
                  } else {
                     int type = rs.getInt("type");
                     Shop shop = new ContainerShop(loc, price, item, owner);
                     shop.setUnlimited(rs.getBoolean("unlimited"));
                     shop.setShopType(ShopType.fromID(type));
                     this.shopManager.loadShop(rs.getString("world"), shop);
                     if (loc.getWorld() != null && loc.getChunk().isLoaded()) {
                        shop.onLoad();
                     }

                     ++count;
                  }
               } catch (Exception e) {
                  ++errors;
                  e.printStackTrace();
                  this.getLogger().severe("Error loading a shop! Coords: " + worldName + " (" + x + ", " + y + ", " + z + ")...");
                  if (errors < 3) {
                     this.getLogger().info("Deleting the shop...");
                     PreparedStatement delps = this.getDB().getConnection().prepareStatement("DELETE FROM shops WHERE x = ? AND y = ? and z = ? and world = ?");
                     delps.setInt(1, x);
                     delps.setInt(2, y);
                     delps.setInt(3, z);
                     delps.setString(4, worldName);
                     delps.execute();
                  } else {
                     this.getLogger().severe("Multiple errors in shops - Something seems to be wrong with your shops database! Please check it out immediately!");
                     e.printStackTrace();
                  }
               }
            }
         } catch (SQLException e) {
            e.printStackTrace();
            this.getLogger().severe("Could not load shops.");
         }

         this.getLogger().info("Loaded " + count + " shops.");
         MsgUtil.loadTransactionMessages();
         MsgUtil.clean();
         this.getLogger().info("Registering Listeners");
         Bukkit.getServer().getPluginManager().registerEvents(this.blockListener, this);
         Bukkit.getServer().getPluginManager().registerEvents(this.playerListener, this);
         if (this.display) {
            Bukkit.getServer().getPluginManager().registerEvents(this.chunkListener, this);
         }

         Bukkit.getServer().getPluginManager().registerEvents(this.worldListener, this);
         if (Bukkit.getPluginManager().getPlugin("Herochat") != null) {
            this.getLogger().info("Found Herochat... Hooking!");
            this.heroChatListener = new HeroChatListener(this);
            Bukkit.getServer().getPluginManager().registerEvents(this.heroChatListener, this);
         } else {
            this.chatListener = new ChatListener(this);
            Bukkit.getServer().getPluginManager().registerEvents(this.chatListener, this);
         }

         QS commandExecutor = new QS(this);
         this.getCommand("qs").setExecutor(commandExecutor);
         if (this.getConfig().getInt("shop.find-distance") > 100) {
            this.getLogger().severe("Shop.find-distance is too high! Pick a number under 100!");
         }

         if (Bukkit.getPluginManager().getPlugin("Spout") != null) {
            this.getLogger().info("Found Spout...");
            this.useSpout = true;
         } else {
            this.useSpout = false;
         }

         try {
            this.metrics = new Metrics(this);
            if (!this.metrics.isOptOut()) {
               this.getServer().getPluginManager().registerEvents(new ShopListener(), this);
               if (this.metrics.start()) {
                  this.getLogger().info("Metrics started.");
               }
            }
         } catch (IOException var20) {
            this.getLogger().info("Could not start metrics.");
         }

         this.getLogger().info("QuickShop loaded!");
      }
   }

   public void reloadConfig() {
      super.reloadConfig();
      this.display = this.getConfig().getBoolean("shop.display-items");
      this.sneak = this.getConfig().getBoolean("shop.sneak-only");
      this.priceChangeRequiresFee = this.getConfig().getBoolean("shop.price-change-requires-fee");
      MsgUtil.loadCfgMessages();
   }

   public boolean loadEcon() {
      String econ = this.getConfig().getString("economy");
      if (econ == null || econ.isEmpty()) {
         econ = "Vault";
      }

      econ = econ.substring(0, 1).toUpperCase() + econ.substring(1).toLowerCase();
      EconomyCore core = null;

      try {
         this.getLogger().info("Hooking " + econ);
         Class<? extends EconomyCore> ecoClass = Class.forName("org.maxgamer.QuickShop.Economy.Economy_" + econ).asSubclass(EconomyCore.class);
         core = (EconomyCore)ecoClass.newInstance();
      } catch (NoClassDefFoundError e) {
         e.printStackTrace();
         System.out.println("Could not find economy called " + econ + "... Is it installed? Using Vault instead!");
         core = new Economy_Vault();
      } catch (ClassNotFoundException e) {
         e.printStackTrace();
         System.out.println("QuickShop does not know how to hook into " + econ + "! Using Vault instead!");
         core = new Economy_Vault();
      } catch (InstantiationException e) {
         e.printStackTrace();
         System.out.println("Invalid Economy Core! " + econ);
         return false;
      } catch (IllegalAccessException e) {
         e.printStackTrace();
         System.out.println("Invalid Economy Core! " + econ);
         return false;
      }

      if (core != null && core.isValid()) {
         this.economy = new Economy(core);
         return true;
      } else {
         this.getLogger().severe("Economy is not valid!");
         this.getLogger().severe("QuickShop could not hook an economy!");
         this.getLogger().severe("QuickShop CANNOT start!");
         if (econ.equals("Vault")) {
            this.getLogger().severe("(Does Vault have an Economy to hook into?!)");
         }

         return false;
      }
   }

   public void onDisable() {
      if (this.itemWatcherTask != null) {
         this.itemWatcherTask.cancel();
      }

      if (this.logWatcher != null) {
         this.logWatcher.task.cancel();
         this.logWatcher.close();
      }

      this.shopManager.clear();
      this.database.close();

      try {
         this.database.getConnection().close();
      } catch (SQLException e) {
         e.printStackTrace();
      }

      this.warnings.clear();
      this.reloadConfig();
   }

   public EconomyCore getEcon() {
      return this.economy;
   }

   public void log(String s) {
      if (this.logWatcher != null) {
         Date date = Calendar.getInstance().getTime();
         Timestamp time = new Timestamp(date.getTime());
         this.logWatcher.add("[" + time.toString() + "] " + s);
      }
   }

   public Database getDB() {
      return this.database;
   }

   public void debug(String s) {
      if (this.debug) {
         this.getLogger().info(ChatColor.YELLOW + "[Debug] " + s);
      }
   }

   public ShopManager getShopManager() {
      return this.shopManager;
   }
}
