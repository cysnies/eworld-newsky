package lib;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;
import lib.barapi.BarAPI;
import lib.config.Config;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.realDamage.RealDamage;
import lib.tab.Near;
import lib.tab.Online;
import lib.tab.Tab;
import lib.tab.TabAPI;
import lib.time.Day;
import lib.time.Time;
import lib.types.Types;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilCosts;
import lib.util.UtilEco;
import lib.util.UtilEnchants;
import lib.util.UtilFormat;
import lib.util.UtilIconMenu;
import lib.util.UtilItems;
import lib.util.UtilNames;
import lib.util.UtilPer;
import lib.util.UtilPotions;
import lib.util.UtilRewards;
import lib.util.UtilScoreboard;
import lib.util.UtilSpeed;
import lib.util.UtilTypes;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Lib extends JavaPlugin implements Listener {
   private String pn;
   private Server server;
   private PluginManager pm;
   private BukkitScheduler scheduler;
   private String mainPath;
   private String pluginPath;
   private String dataFolder;
   private String pluginVersion;
   private Per per;
   private Eco eco;
   private Format f;
   private Dao dao;
   private Config con;
   private Time time;
   private Day day;
   private Names names;
   private Tps tps;
   private Types types;
   private Enchants enchants;
   private Costs costs;
   private Items items;
   private Potions potions;
   private Durability durability;
   private Copy copy;
   private Stack stack;
   private Speed speed;
   private Rewards rewards;
   private ItemMessage im;
   private Msg msg;
   private IconMenu icon;
   private RealDamage realDamage;
   private static InputManager inputManager;
   private RealName realName;
   private BarAPI barApi;
   private Bar bar;
   private TabAPI tabAPI;
   private Tab tab;
   private Near near;
   private Online online;
   private Debt debt;
   private String per_lib_admin;

   public void onLoad() {
   }

   public void onEnable() {
      this.initBasic();
      this.initNeed();
      this.initConfig();
      this.initDatabase();
      this.loadData();
      this.time = new Time(this);
      this.day = new Day(this);
      this.names = new Names(this);
      this.tps = new Tps(this);
      this.types = new Types(this);
      this.enchants = new Enchants(this);
      this.costs = new Costs(this);
      this.items = new Items(this);
      this.potions = new Potions(this);
      this.durability = new Durability(this);
      this.copy = new Copy(this);
      this.stack = new Stack(this);
      this.speed = new Speed(this);
      this.rewards = new Rewards(this);
      this.im = new ItemMessage(this);
      this.msg = new Msg(this);
      this.icon = new IconMenu(this);
      this.rewards.init(this);
      Util.init(this);
      UtilConfig.init(this);
      UtilEco.init(this);
      UtilEnchants.init(this);
      UtilCosts.init(this);
      UtilFormat.init(this);
      UtilItems.init(this);
      UtilNames.init(this);
      UtilPer.init(this);
      UtilPotions.init(this);
      UtilRewards.init(this);
      UtilScoreboard.init(this);
      UtilSpeed.init(this);
      UtilTypes.init(this);
      UtilIconMenu.init(this);
      this.realDamage = new RealDamage(this);
      inputManager = new InputManager(this);
      this.realName = new RealName(this);
      this.barApi = new BarAPI(this);
      this.bar = new Bar(this);
      this.tabAPI = new TabAPI();
      this.tabAPI.onEnable(this);
      this.tab = new Tab(this);
      this.near = new Near(this);
      this.online = new Online(this);
      this.debt = new Debt(this);
      this.loadConfig0(this.con.getConfig(this.pn));
      this.pm.registerEvents(this, this);

      try {
         Metrics metrics = new Metrics(this);
         metrics.start();
      } catch (IOException var2) {
      }

      Util.sendConsoleMessage(UtilFormat.format(this.pn, "pluginEnabled", this.pn, this.pluginVersion));
   }

   public void onDisable() {
      this.scheduler.cancelAllTasks();
      Util.sendConsoleMessage(UtilFormat.format(this.pn, "pluginDisabled", this.pn, this.pluginVersion));
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      Player p = null;
      if (!(sender instanceof ConsoleCommandSender)) {
         p = (Player)sender;
      }

      String cmdName = cmd.getName();
      int length = args.length;
      if (cmdName.equalsIgnoreCase("lib")) {
         if ((length != 1 || !args[0].equalsIgnoreCase("?")) && length == 1 && args[0].equalsIgnoreCase("reload")) {
            this.reloadConfig(sender);
            return true;
         }

         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpHeader", this.get(110)));
         if (p == null || UtilPer.hasPer(p, this.per_lib_admin)) {
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", this.get(115), this.get(116)));
         }

         sender.sendMessage(UtilFormat.format(this.pn, "ver", "lib", this.pluginVersion, "fyxridd", "http://www.minecraft001.com", this.get(128)));
      } else if (!cmdName.equalsIgnoreCase("items") && !cmdName.equalsIgnoreCase("is")) {
         if (cmdName.equalsIgnoreCase("side")) {
            this.msg.onCommand(sender, cmd, label, args);
         } else if (cmdName.equalsIgnoreCase("enchants")) {
            this.enchants.onCommand(sender, cmd, label, args);
         } else if (cmdName.equalsIgnoreCase("costs")) {
            this.costs.onCommand(sender, cmd, label, args);
         } else if (cmdName.equalsIgnoreCase("potions")) {
            this.potions.onCommand(sender, cmd, label, args);
         } else if (cmdName.equalsIgnoreCase("d")) {
            this.durability.onCommand(sender, cmd, label, args);
         } else if (!cmdName.equalsIgnoreCase("rewards") && !cmdName.equalsIgnoreCase("re")) {
            if (cmdName.equalsIgnoreCase("copy")) {
               this.copy.onCommand(sender, cmd, label, args);
            } else if (cmdName.equalsIgnoreCase("s")) {
               inputManager.onCommand(sender, cmd, label, args);
            } else if (cmdName.equalsIgnoreCase("debt")) {
               this.debt.onCommand(sender, cmd, label, args);
            }
         } else {
            this.rewards.onCommand(sender, cmd, label, args);
         }
      } else {
         this.items.onCommand(sender, cmd, label, args);
      }

      return true;
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig0(e.getConfig());
      }

   }

   private void initBasic() {
      this.pn = this.getName();
      this.server = this.getServer();
      this.pm = this.server.getPluginManager();
      this.scheduler = this.server.getScheduler();
      this.mainPath = System.getProperty("user.dir");
      this.pluginPath = this.getFile().getParentFile().getAbsolutePath();
      this.dataFolder = this.pluginPath + File.separator + this.pn;
      this.pluginVersion = Util.getPluginVersion(this.getFile());
   }

   private void initNeed() {
      this.per = new Per(this);
      this.eco = new Eco(this);
      this.f = new Format(this);
   }

   private void initConfig() {
      this.con = new Config(this);
      HashList<Pattern> filter = this.con.getDefaultFilter().clone();
      filter.add(Pattern.compile("hibernate.cfg.xml"));
      filter.add(Pattern.compile("names.yml"));
      filter.add(Pattern.compile("types.yml"));
      filter.add(Pattern.compile("enchants.yml"));
      filter.add(Pattern.compile("rewards.yml"));
      filter.add(Pattern.compile("costs.yml"));
      filter.add(Pattern.compile("potions.yml"));
      this.con.register(new File(this.pluginPath + File.separator + "lib.jar"), this.dataFolder, filter, this.pn);
      this.loadConfig((CommandSender)null);
   }

   private void initDatabase() {
      this.dao = new Dao(this);
   }

   private void loadData() {
   }

   private void reloadConfig(CommandSender sender) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null || UtilPer.checkPer(p, this.per_lib_admin)) {
         if (this.loadConfig(sender)) {
            sender.sendMessage(UtilFormat.format(this.pn, "success", this.get(120)));
         } else {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(121)));
         }

      }
   }

   private boolean loadConfig(CommandSender sender) {
      try {
         return this.con.loadConfig(this.pn);
      } catch (InvalidConfigurationException e) {
         if (sender == null) {
            sender = this.server.getConsoleSender();
         }

         if (sender != null) {
            sender.sendMessage(e.getMessage());
         } else {
            this.server.getLogger().info(e.getMessage());
         }

         return false;
      }
   }

   private String get(int id) {
      return this.f.format(this.pn, id);
   }

   private void loadConfig0(YamlConfiguration config) {
      this.per_lib_admin = config.getString("per_lib_admin");
   }

   public String getMainPath() {
      return this.mainPath;
   }

   public String getPn() {
      return this.pn;
   }

   public String getPluginPath() {
      return this.pluginPath;
   }

   public PluginManager getPm() {
      return this.pm;
   }

   public Tps getTps() {
      return this.tps;
   }

   public Format getFormat() {
      return this.f;
   }

   public Per getPer() {
      return this.per;
   }

   public Eco getEco() {
      return this.eco;
   }

   public Config getCon() {
      return this.con;
   }

   public Names getNames() {
      return this.names;
   }

   public Types getTypes() {
      return this.types;
   }

   public Items getItems() {
      return this.items;
   }

   public Time getTime() {
      return this.time;
   }

   public Day getDay() {
      return this.day;
   }

   public Enchants getEnchants() {
      return this.enchants;
   }

   public Potions getPotions() {
      return this.potions;
   }

   public Durability getDurability() {
      return this.durability;
   }

   public Stack getStack() {
      return this.stack;
   }

   public Speed getSpeed() {
      return this.speed;
   }

   public Rewards getRewards() {
      return this.rewards;
   }

   public ItemMessage getIm() {
      return this.im;
   }

   public Msg getMsg() {
      return this.msg;
   }

   public IconMenu getIcon() {
      return this.icon;
   }

   public RealDamage getRealDamage() {
      return this.realDamage;
   }

   public Copy getCopy() {
      return this.copy;
   }

   public Costs getCosts() {
      return this.costs;
   }

   public static InputManager getInputManager() {
      return inputManager;
   }

   public RealName getRealName() {
      return this.realName;
   }

   public Dao getDao() {
      return this.dao;
   }

   public Bar getBar() {
      return this.bar;
   }

   public Tab getTab() {
      return this.tab;
   }

   public Near getNear() {
      return this.near;
   }

   public Online getOnline() {
      return this.online;
   }

   public Debt getDebt() {
      return this.debt;
   }

   public BarAPI getBarApi() {
      return this.barApi;
   }
}
