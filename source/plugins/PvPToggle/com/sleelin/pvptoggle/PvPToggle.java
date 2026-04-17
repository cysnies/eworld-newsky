package com.sleelin.pvptoggle;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sleelin.pvptoggle.handlers.CommandHandler;
import com.sleelin.pvptoggle.handlers.RegionHandler;
import com.sleelin.pvptoggle.listeners.EntityListener;
import com.sleelin.pvptoggle.listeners.PlayerListener;
import com.sleelin.pvptoggle.listeners.RegionListener;
import com.sleelin.pvptoggle.listeners.WorldListener;
import java.net.URL;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PvPToggle extends JavaPlugin {
   public Logger log = Logger.getLogger("Minecraft");
   private Runnable updateThread;
   private int updateId = -1;
   private static final String RSS_URL = "http://dev.bukkit.org/server-mods/PvPToggle/files.rss";
   private static String version;
   private static String name;
   private static PermissionHandler permissionHandler;
   private final PlayerListener playerListener = new PlayerListener(this);
   private final EntityListener entityListener = new EntityListener(this);
   private final WorldListener worldListener = new WorldListener(this);
   public RegionListener regionListener;
   private HashMap globalsettings = new HashMap();
   protected HashMap worlds = new HashMap();
   private HashMap lastaction = new HashMap();

   public void onEnable() {
      this.log.info("[" + this.getDescription().getName() + "] Loading...");
      PvPLocalisation.loadProcedure(this);
      this.loadProcedure();
      this.getServer().getPluginManager().registerEvents(this.playerListener, this);
      this.getServer().getPluginManager().registerEvents(this.entityListener, this);
      this.getServer().getPluginManager().registerEvents(this.worldListener, this);
      version = this.getDescription().getVersion();
      name = this.getDescription().getName();
      this.startUpdateThread();
      if (!((String)this.globalsettings.get("command")).equalsIgnoreCase("tpvp") && !((String)this.globalsettings.get("command")).equalsIgnoreCase("pvpt")) {
         this.getCommand("pvp").setExecutor(new CommandHandler(this));
      } else {
         this.getCommand((String)this.globalsettings.get("command")).setExecutor(new CommandHandler(this));
      }

      if ((Boolean)this.globalsettings.get("worldguard")) {
         RegionHandler.loadProcedure(this);
         this.regionListener = new RegionListener(this);
         this.getServer().getPluginManager().registerEvents(this.regionListener, this);
      }

      System.out.println("[" + this.getDescription().getName() + "] v" + this.getDescription().getVersion() + " enabled!");
   }

   public void onDisable() {
      this.stopUpdateThread();
      this.worlds.clear();
      this.globalsettings.clear();
      this.lastaction.clear();
      this.log.info("[PvPToggle] Disabled");
   }

   public PvPToggle getHandler() {
      return this;
   }

   private void loadProcedure() {
      if (!this.getConfig().isSet("plugin.enabled")) {
         this.getConfig().set("plugin.enabled", !this.getConfig().getBoolean("globalDisabled", false));
      }

      if (!this.getConfig().isSet("plugin.debug")) {
         this.getConfig().set("plugin.debug", this.getConfig().getBoolean("debug", false));
      }

      if (!this.getConfig().isSet("plugin.updateinterval")) {
         this.getConfig().set("plugin.updateinterval", this.getConfig().getInt("updateinterval", 21600));
      }

      if (!this.getConfig().isSet("plugin.command")) {
         this.getConfig().set("plugin.command", "pvp");
      }

      if (!this.getConfig().isSet("plugin.worldguard-integration")) {
         this.getConfig().set("plugin.worldguard-integration", false);
      }

      this.getConfig().set("cooldown", (Object)null);
      this.getConfig().set("warmup", (Object)null);
      this.getConfig().set("globalDisabled", (Object)null);
      this.getConfig().set("debug", (Object)null);
      this.getConfig().set("updateinterval", (Object)null);
      this.saveConfig();
      this.globalsettings.put("enabled", this.getConfig().getBoolean("plugin.enabled", true));
      this.globalsettings.put("debug", this.getConfig().getBoolean("plugin.debug", false));
      this.globalsettings.put("updateinterval", this.getConfig().getInt("plugin.updateinterval", 21600));
      this.globalsettings.put("command", this.getConfig().getString("plugin.command", "pvp"));
      this.globalsettings.put("citizens", false);
      this.globalsettings.put("worldguard", this.getConfig().getBoolean("plugin.worldguard-integration", false));

      for(World world : this.getServer().getWorlds()) {
         this.loadWorld(world);
      }

      Player[] var4;
      for(Player player : var4 = this.getServer().getOnlinePlayers()) {
         this.lastaction.put(player, new PvPAction(0L, "login"));
      }

      if (this.getServer().getPluginManager().getPlugin("Permissions") != null) {
         if (!this.getServer().getPluginManager().getPlugin("Permissions").getDescription().getVersion().equalsIgnoreCase("2.7.7")) {
            permissionHandler = ((Permissions)this.getServer().getPluginManager().getPlugin("Permissions")).getHandler();
            this.log.info("[" + this.getDescription().getName() + "] Legacy Permissions " + this.getServer().getPluginManager().getPlugin("Permissions").getDescription().getVersion() + " detected");
         } else {
            this.log.info("[" + this.getDescription().getName() + "] Permissions bridge detected, using SuperPerms instead!");
         }
      } else {
         this.log.info("[" + this.getDescription().getName() + "] Using SuperPerms for permissions checking");
      }

      if (this.getServer().getPluginManager().getPlugin("Citizens") != null) {
         this.globalsettings.put("citizens", true);
         this.log.info("[" + this.getDescription().getName() + "] Citizens Plugin detected");
      }

      if (this.getServer().getPluginManager().getPlugin("WorldGuard") != null && this.getServer().getPluginManager().getPlugin("WorldGuard") instanceof WorldGuardPlugin) {
         this.log.info("[" + this.getDescription().getName() + "] WorldGuard Plugin detected...");
         if ((Boolean)this.globalsettings.get("worldguard")) {
            this.log.info("[" + this.getDescription().getName() + "] WorldGuard integration enabled!");
         } else {
            this.log.info("[" + this.getDescription().getName() + "] WorldGuard integration disabled via options!");
         }
      }

   }

   public void loadWorld(World world) {
      PvPWorld pvpworld = new PvPWorld();
      if (!this.getConfig().isSet("worlds." + world.getName() + ".enabled")) {
         this.getConfig().set("worlds." + world.getName() + ".enabled", this.getConfig().getBoolean("worlds." + world.getName() + ".pvpenabled", true));
      }

      if (!this.getConfig().isSet("worlds." + world.getName() + ".default")) {
         this.getConfig().set("worlds." + world.getName() + ".default", this.getConfig().getBoolean("worlds." + world.getName() + ".logindefault", true));
      }

      if (!this.getConfig().isSet("worlds." + world.getName() + ".cooldown")) {
         this.getConfig().set("worlds." + world.getName() + ".cooldown", this.getConfig().getInt("cooldown", 0));
      }

      if (!this.getConfig().isSet("worlds." + world.getName() + ".warmup")) {
         this.getConfig().set("worlds." + world.getName() + ".warmup", this.getConfig().getInt("warmup", 0));
      }

      this.getConfig().set("worlds." + world.getName().toString() + ".pvpenabled", (Object)null);
      this.getConfig().set("worlds." + world.getName().toString() + ".logindefault", (Object)null);
      this.saveConfig();
      pvpworld.cooldown = this.getConfig().getInt("worlds." + world.getName() + ".cooldown", 0);
      pvpworld.warmup = this.getConfig().getInt("worlds." + world.getName() + ".warmup", 0);
      pvpworld.enabled = this.getConfig().getBoolean("worlds." + world.getName() + ".enabled", true);
      pvpworld.logindefault = this.getConfig().getBoolean("worlds." + world.getName() + ".default", true);

      Player[] var6;
      for(Player player : var6 = this.getServer().getOnlinePlayers()) {
         pvpworld.players.put(player, pvpworld.logindefault);
      }

      this.worlds.put(world.getName(), pvpworld);
      this.log.info("[" + this.getDescription().getName() + "] found and loaded world " + world.getName());
   }

   protected void setWorldStatus(String world, boolean enabled) {
      ((PvPWorld)this.worlds.get(world)).enabled = enabled;
   }

   public boolean getWorldStatus(String world) {
      return world != null ? true : ((PvPWorld)this.worlds.get(world)).enabled;
   }

   public boolean getWorldDefault(String world) {
      return world != null ? ((PvPWorld)this.worlds.get(world)).logindefault : true;
   }

   public PvPWorld getWorld(String world) {
      return (PvPWorld)this.worlds.get(world);
   }

   public String checkWorldName(String targetworld) {
      String output = null;

      for(World world : this.getServer().getWorlds()) {
         if (world.getName().toLowerCase().contains(targetworld.toLowerCase())) {
            output = world.getName();
            break;
         }
      }

      return output;
   }

   public void setPlayerStatus(Player player, String world, boolean status) {
      if (this.checkWorldName(world) != null && player != null) {
         ((PvPWorld)this.worlds.get(this.checkWorldName(world))).players.put(player, status);
      }

   }

   public boolean checkPlayerStatus(Player player, String world) {
      if (!((PvPWorld)this.worlds.get(world)).players.containsKey(player)) {
         this.lastaction.put(player, new PvPAction(0L, "login"));
         ((PvPWorld)this.worlds.get(world)).players.put(player, ((PvPWorld)this.worlds.get(world)).logindefault);
      }

      if (this.permissionsCheck(player, "pvptoggle.pvp.force", false)) {
         return true;
      } else {
         return this.permissionsCheck(player, "pvptoggle.pvp.deny", false) ? false : (Boolean)((PvPWorld)this.worlds.get(world)).players.get(player);
      }
   }

   public Object getGlobalSetting(String setting) {
      return this.globalsettings.get(setting);
   }

   protected void setGlobalSetting(String setting, Object value) {
      this.globalsettings.put(setting, value);
   }

   protected void toggleGlobalStatus(Boolean newval) {
      this.setGlobalSetting("enabled", newval);
   }

   public Boolean checkGlobalStatus() {
      return (Boolean)this.globalsettings.get("enabled");
   }

   public void setLastAction(Player player, String action) {
      this.lastaction.put(player, new PvPAction((new GregorianCalendar()).getTime().getTime(), action));
   }

   public boolean checkLastAction(Player player, String action, String world) {
      GregorianCalendar cal = new GregorianCalendar();
      Long difference = cal.getTime().getTime() - ((PvPAction)this.lastaction.get(player)).time;
      int before = 0;
      if (action.equalsIgnoreCase("combat")) {
         if (((PvPAction)this.lastaction.get(player)).action.equalsIgnoreCase("toggle")) {
            before = difference.compareTo((long)((PvPWorld)this.worlds.get(world)).warmup * 1000L);
         }
      } else if (action.equalsIgnoreCase("toggle") && ((PvPAction)this.lastaction.get(player)).action.equalsIgnoreCase("combat")) {
         before = difference.compareTo((long)((PvPWorld)this.worlds.get(world)).cooldown * 1000L);
      }

      return before >= 0;
   }

   public boolean permissionsCheck(CommandSender sender, String permissions, boolean opdefault) {
      if (sender instanceof Player) {
         Player player = (Player)sender;
         if ((Boolean)this.globalsettings.get("debug")) {
            this.log.info(player.getName().toString() + "/" + permissions + "/Start: " + opdefault);
         }

         boolean haspermissions;
         if (permissionHandler != null) {
            haspermissions = permissionHandler.has(player, permissions);
            if ((Boolean)this.globalsettings.get("debug")) {
               this.log.info(player.getName().toString() + "/" + permissions + "/LegPerms: " + haspermissions);
            }

            if (permissionHandler.has(player, "*")) {
               haspermissions = opdefault;
            }
         } else {
            haspermissions = player.hasPermission(permissions);
            if ((Boolean)this.globalsettings.get("debug")) {
               this.log.info(player.getName().toString() + "/" + permissions + "/Before*: " + haspermissions);
            }

            if (player.hasPermission("*")) {
               haspermissions = opdefault;
            }

            if ((Boolean)this.globalsettings.get("debug")) {
               this.log.info(player.getName().toString() + "/" + permissions + "/After*: " + haspermissions);
            }
         }

         if ((Boolean)this.globalsettings.get("debug")) {
            this.log.info(player.getName().toString() + "/" + permissions + "/Final: " + haspermissions);
         }

         return haspermissions;
      } else {
         return true;
      }
   }

   private void startUpdateThread() {
      if ((Integer)this.globalsettings.get("updateinterval") != 0) {
         if (this.updateThread == null) {
            this.updateThread = new Runnable() {
               public void run() {
                  String checkVersion = PvPToggle.this.updateCheck(PvPToggle.version);
                  if (!checkVersion.equalsIgnoreCase("[v" + PvPToggle.version + "]")) {
                     PvPToggle.this.log.info("[" + PvPToggle.name + "] Found new version: " + checkVersion + " (you have [v" + PvPToggle.version + "])");
                     PvPToggle.this.log.info("[" + PvPToggle.name + "] Visit http://dev.bukkit.org/server-mods/" + PvPToggle.name + "/ to download!");
                  }

               }
            };
         }

         this.updateId = this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, this.updateThread, 100L, (long)((Integer)this.globalsettings.get("updateinterval") * 20));
      }
   }

   private void stopUpdateThread() {
      if (this.updateId != -1) {
         this.getServer().getScheduler().cancelTask(this.updateId);
         this.updateId = -1;
      }

   }

   public String updateCheck(String currentVersion) {
      try {
         URL url = new URL("http://dev.bukkit.org/server-mods/PvPToggle/files.rss");
         Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openConnection().getInputStream());
         doc.getDocumentElement().normalize();
         NodeList nodes = doc.getElementsByTagName("item");
         Node firstNode = nodes.item(0);
         if (firstNode.getNodeType() == 1) {
            Element firstElement = (Element)firstNode;
            NodeList firstElementTagName = firstElement.getElementsByTagName("title");
            Element firstNameElement = (Element)firstElementTagName.item(0);
            NodeList firstNodes = firstNameElement.getChildNodes();
            return firstNodes.item(0).getNodeValue();
         } else {
            return currentVersion;
         }
      } catch (Exception var10) {
         return currentVersion;
      }
   }

   public class PvPWorld {
      int cooldown;
      int warmup;
      boolean enabled;
      boolean logindefault;
      HashMap players = new HashMap();
   }

   public class PvPAction {
      Long time;
      String action;

      public PvPAction(Long itime, String iaction) {
         this.time = itime;
         this.action = iaction;
      }
   }
}
