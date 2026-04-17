package house;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Pattern;
import land.Land;
import land.Range;
import landMain.LandMain;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilEco;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class House extends JavaPlugin implements Listener {
   static final String FLAG_HOUSE = "house";
   private String pn;
   private String pluginPath;
   private String dataFolder;
   private String pluginVersion;
   private Dao dao;
   private String per_house_admin;
   private int upgradeCost;
   private int upgradeAmount;
   private int upgradeMax;
   private HashList houseList;
   private HashList userList;
   private HashMap hash;
   private HouseGenerater houseGenerater;
   private HashList openRepairList;
   private int taskId = -1;

   public void onEnable() {
      this.initBasic();
      this.initConfig();
      this.loadConfig0(UtilConfig.getConfig(this.pn));
      this.dao = new Dao(this);
      this.loadData();
      Bukkit.getPluginManager().registerEvents(this, this);
      this.houseGenerater = new HouseGenerater(this);
      this.openRepairList = new HashListImpl();
      Util.sendConsoleMessage(UtilFormat.format((String)null, "pluginEnabled", new Object[]{this.pn, this.pluginVersion}));
   }

   public void onDisable() {
      this.dao.close();
      Util.sendConsoleMessage(UtilFormat.format((String)null, "pluginDisabled", new Object[]{this.pn, this.pluginVersion}));
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null) {
         sender.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(35)}));
         return true;
      } else {
         int length = args.length;
         if (cmd.getName().equalsIgnoreCase("house")) {
            if (length != 1 || !args[0].equalsIgnoreCase("?")) {
               if (length == 0) {
                  this.spawn(p);
                  return true;
               }

               if (length == 1) {
                  if (args[0].equalsIgnoreCase("reloadConfig") || args[0].equalsIgnoreCase("rc")) {
                     this.reloadConfig(sender);
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("set")) {
                     this.setSpawn(p);
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("repair")) {
                     if (!p.isOp()) {
                        p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(85)}));
                        return true;
                     }

                     this.houseGenerater.repair(p);
                     p.sendMessage(UtilFormat.format((String)null, "success", new Object[]{this.get(140)}));
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("stop")) {
                     if (!p.isOp()) {
                        p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(85)}));
                        return true;
                     }

                     if (this.taskId == -1) {
                        p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(120)}));
                     } else {
                        Bukkit.getScheduler().cancelTask(this.taskId);
                        this.taskId = -1;
                        p.sendMessage(UtilFormat.format((String)null, "success", new Object[]{this.get(125)}));
                     }

                     return true;
                  }
               } else if (length == 2) {
                  if (args[0].equalsIgnoreCase("repair")) {
                     if (!p.isOp()) {
                        p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(85)}));
                        return true;
                     }

                     if (args[1].equalsIgnoreCase("on")) {
                        this.openRepairList.add(p.getName());
                        p.sendMessage(UtilFormat.format((String)null, "success", new Object[]{this.get(90)}));
                        return true;
                     }

                     if (args[1].equalsIgnoreCase("off")) {
                        this.openRepairList.remove(p.getName());
                        p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(95)}));
                        return true;
                     }
                  }
               } else if (length == 6 && args[0].equalsIgnoreCase("auto")) {
                  if (!p.isOp()) {
                     p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(85)}));
                     return true;
                  }

                  if (this.taskId != -1) {
                     p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(135)}));
                     return true;
                  }

                  int tick = Integer.parseInt(args[1]);
                  int xMin = Integer.parseInt(args[2]);
                  int xMax = Integer.parseInt(args[3]);
                  int zMin = Integer.parseInt(args[4]);
                  int zMax = Integer.parseInt(args[5]);
                  this.taskId = Bukkit.getScheduler().runTaskTimer(this, new Auto(xMin, xMax, zMin, zMax), (long)tick, (long)tick).getTaskId();
                  p.sendMessage(UtilFormat.format((String)null, "success", new Object[]{this.get(130)}));
                  return true;
               }
            }

            sender.sendMessage(UtilFormat.format((String)null, "cmdHelpHeader", new Object[]{this.get(10)}));
            if (p == null || UtilPer.hasPer(p, this.per_house_admin)) {
               sender.sendMessage(UtilFormat.format((String)null, "cmdHelpItem", new Object[]{this.get(15), this.get(20)}));
            }

            sender.sendMessage(UtilFormat.format((String)null, "cmdHelpItem", new Object[]{this.get(45), this.get(50)}));
            sender.sendMessage(UtilFormat.format((String)null, "cmdHelpItem", new Object[]{this.get(55), this.get(60)}));
            if (p == null || p.isOp()) {
               sender.sendMessage(UtilFormat.format((String)null, "cmdHelpItem", new Object[]{this.get(75), this.get(80)}));
               sender.sendMessage(UtilFormat.format((String)null, "cmdHelpItem", new Object[]{this.get(100), this.get(105)}));
               sender.sendMessage(UtilFormat.format((String)null, "cmdHelpItem", new Object[]{this.get(110), this.get(115)}));
            }
         }

         return true;
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerJoin(PlayerJoinEvent e) {
      if (!this.userList.has(e.getPlayer().getName())) {
         this.houseGenerater.addUser(e.getPlayer());
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerInteract(PlayerInteractEvent e) {
      if (this.openRepairList.has(e.getPlayer().getName()) && e.hasItem() && e.getItem().getTypeId() == 268) {
         this.houseGenerater.repair(e.getPlayer());
         e.getPlayer().sendMessage(UtilFormat.format((String)null, "success", new Object[]{this.get(140)}));
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig0(e.getConfig());
      }

   }

   public boolean spawn(Player p) {
      for(Land land : LandMain.getLandManager().getUserLands(p.getName())) {
         if (land.hasFlag("house")) {
            LandMain.getLandManager().getTpHandler().tp(p, land.getName());
            return true;
         }
      }

      p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(40)}));
      return false;
   }

   public void upgrade(Player p) {
      for(Land land : LandMain.getLandManager().getUserLands(p.getName())) {
         if (land.hasFlag("house")) {
            Range r = land.getRange();
            int y = Math.max(r.getP1().getY(), r.getP2().getY()) + this.upgradeAmount;
            if (y > this.upgradeMax) {
               p.sendMessage(UtilFormat.format(this.pn, "upgradeMaxTip", new Object[]{this.upgradeMax}));
               return;
            }

            if (UtilEco.get(p.getName()) < (double)this.upgradeCost) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(70)}));
               return;
            }

            UtilEco.del(p.getName(), (double)this.upgradeCost);
            p.sendMessage(UtilFormat.format(this.pn, "upgradeDel", new Object[]{this.upgradeCost}));
            r.expand(0, this.upgradeAmount, 0);
            LandMain.getLandManager().getSetHandler().setRange(land, r);
            p.sendMessage(UtilFormat.format(this.pn, "upgradeSuccess", new Object[]{y}));
            return;
         }
      }

      p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(40)}));
   }

   public Dao getDao() {
      return this.dao;
   }

   public String getPn() {
      return this.pn;
   }

   public HashList getHouseList() {
      return this.houseList;
   }

   public HashMap getHash() {
      return this.hash;
   }

   public void addHouseUser(HouseUser houseUser) {
      this.dao.addHouseUser(houseUser);
      this.houseList.add(houseUser);
      this.userList.add(houseUser.getName());
      if (!this.hash.containsKey(houseUser.getX())) {
         this.hash.put(houseUser.getX(), new HashMap());
      }

      ((HashMap)this.hash.get(houseUser.getX())).put(houseUser.getZ(), houseUser);
   }

   public static String getFlagHouse() {
      return "house";
   }

   private void loadData() {
      this.houseList = new HashListImpl();
      this.userList = new HashListImpl();
      this.hash = new HashMap();

      for(HouseUser houseUser : this.dao.getAllHouseUsers()) {
         this.houseList.add(houseUser);
         this.userList.add(houseUser.getName());
         if (!this.hash.containsKey(houseUser.getX())) {
            this.hash.put(houseUser.getX(), new HashMap());
         }

         ((HashMap)this.hash.get(houseUser.getX())).put(houseUser.getZ(), houseUser);
      }

   }

   private void setSpawn(Player p) {
      for(Land land : LandMain.getLandManager().getUserLands(p.getName())) {
         if (land.hasFlag("house")) {
            LandMain.getLandManager().getTpHandler().setTp(p, land.getName());
            return;
         }
      }

      p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(40)}));
   }

   private void loadConfig0(YamlConfiguration config) {
      this.per_house_admin = config.getString("per_house_admin");
      this.upgradeCost = config.getInt("upgrade.cost");
      this.upgradeAmount = config.getInt("upgrade.amount");
      this.upgradeMax = config.getInt("upgrade.max");
   }

   private void initBasic() {
      this.pn = this.getName();
      this.pluginPath = this.getFile().getParentFile().getAbsolutePath();
      this.dataFolder = this.pluginPath + File.separator + this.pn;
      this.pluginVersion = Util.getPluginVersion(this.getFile());
   }

   private void initConfig() {
      HashList<Pattern> filter = UtilConfig.getDefaultFilter();
      filter.add(Pattern.compile("house.schematic"));
      filter.add(Pattern.compile("nowSize.yml"));
      UtilConfig.register(new File(this.pluginPath + File.separator + "house.jar"), this.dataFolder, filter, this.pn);
      this.loadConfig((CommandSender)null);
   }

   private void reloadConfig(CommandSender sender) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null || UtilPer.checkPer(p, this.per_house_admin)) {
         if (this.loadConfig(sender)) {
            sender.sendMessage(UtilFormat.format((String)null, "success", new Object[]{this.get(25)}));
         } else {
            sender.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(30)}));
         }
      }

   }

   private boolean loadConfig(CommandSender sender) {
      try {
         return UtilConfig.loadConfig(this.pn);
      } catch (InvalidConfigurationException e) {
         Util.sendConsoleMessage(e.getMessage());
         return false;
      }
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   class Auto implements Runnable {
      int xMin;
      int xMax;
      int zMin;
      int zMax;
      int xNow;
      int zNow;

      public Auto(int xMin, int xMax, int zMin, int zMax) {
         this.xMin = xMin;
         this.xMax = xMax;
         this.zMin = zMin;
         this.zMax = zMax;
         this.xNow = xMin;
         this.zNow = zMin;
      }

      public void run() {
         if (this.zNow > this.zMax) {
            Bukkit.getScheduler().cancelTask(House.this.taskId);
            House.this.taskId = -1;
         } else {
            House.this.houseGenerater.generateHouse(this.xNow, this.zNow, false);
            ++this.xNow;
            if (this.xNow > this.xMax) {
               this.xNow = this.xMin;
               ++this.zNow;
            }

         }
      }
   }
}
