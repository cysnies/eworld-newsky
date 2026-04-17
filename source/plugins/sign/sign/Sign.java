package sign;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Pattern;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import lib.util.UtilSpeed;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Sign extends JavaPlugin implements Listener {
   private static final String COLOR_CHARS = "0123456789abcdeflmn";
   private static final String SPEED_CLICK = "click";
   private Server server;
   private String pn;
   private PluginManager pm;
   private String pluginPath;
   private String dataFolder;
   private String pluginVersion;
   private String per_sign_admin;
   private String flag;
   private String show;
   private int cmdInterval;
   private double distance;
   private SignManager signManager;
   private HashMap intervalHash;
   private HashMap editLineHash;

   public void onEnable() {
      this.initBasic();
      this.initConfig();
      this.loadConfig0(UtilConfig.getConfig(this.pn));
      this.server.getPluginManager().registerEvents(this, this);
      this.editLineHash = new HashMap();
      this.signManager = new SignManager(this);
      this.intervalHash = new HashMap();
      UtilSpeed.register(this.pn, "click");
      this.sendConsoleMessage(UtilFormat.format(this.pn, "pluginEnabled", new Object[]{this.pn, this.pluginVersion}));
   }

   public void onDisable() {
      this.sendConsoleMessage(UtilFormat.format(this.pn, "pluginDisabled", new Object[]{this.pn, this.pluginVersion}));
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      int length = args.length;
      if (cmd.getName().equalsIgnoreCase("sign")) {
         if ((length != 1 || !args[0].equalsIgnoreCase("?")) && length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
               this.reloadConfig(sender);
               return true;
            }

            if (args[0].equalsIgnoreCase("list")) {
               if (p != null && !p.isOp()) {
                  return true;
               }

               this.signManager.showList(sender);
               return true;
            }
         }

         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpHeader", new Object[]{this.get(10)}));
         if (p == null || UtilPer.hasPer(p, this.per_sign_admin)) {
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(15), this.get(20)}));
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(65), this.get(70)}));
         }
      } else if (cmd.getName().equalsIgnoreCase("line")) {
         if (p == null) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(145)}));
            return true;
         }

         if (!UtilPer.checkPer(p, this.per_sign_admin)) {
            return true;
         }

         if (length != 1 || !args[0].equalsIgnoreCase("?")) {
            if (length == 2) {
               int line = Integer.parseInt(args[0]);
               if (line >= 1 && line <= 4) {
                  if (args[1].length() == 1 && "0123456789abcdeflmn".indexOf(args[1]) != -1) {
                     this.editLineHash.put(p, "1 " + line + " " + args[1]);
                     p.sendMessage(UtilFormat.format(this.pn, "tip", new Object[]{this.get(215)}));
                  } else {
                     String result = Util.convert(args[1]);
                     if (result.length() > 15) {
                        result = result.substring(0, 15);
                     }

                     this.editLineHash.put(p, "2 " + line + " " + result);
                     p.sendMessage(UtilFormat.format(this.pn, "tip", new Object[]{this.get(220)}));
                  }

                  return true;
               }

               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(235)}));
               return true;
            }

            if (length > 2) {
               int line = Integer.parseInt(args[0]);
               if (line >= 1 && line <= 4) {
                  String result = "";

                  for(int i = 0; i <= length - 2; ++i) {
                     if (i != 0) {
                        result = result + " ";
                     }

                     result = result + args[i + 1];
                  }

                  result = Util.convert(result);
                  if (result.length() > 15) {
                     result = result.substring(0, 15);
                  }

                  this.editLineHash.put(p, "2 " + line + " " + result);
                  p.sendMessage(UtilFormat.format(this.pn, "tip", new Object[]{this.get(220)}));
                  return true;
               }

               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(235)}));
               return true;
            }
         }

         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpHeader", new Object[]{this.get(200)}));
         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(205), this.get(210)}));
      }

      return true;
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void reloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig0(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void playerInteract(PlayerInteractEvent e) {
      if (this.interact(e.getPlayer(), e.getClickedBlock())) {
         e.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void playerAnimation(PlayerAnimationEvent e) {
      this.animation(e.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPlayerJoin(PlayerJoinEvent e) {
      this.join(e.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPlayerQuit(PlayerQuitEvent e) {
      this.quit(e.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onSignChange(SignChangeEvent e) {
      if (this.isCmdSign(e.getLines()[0])) {
         this.show(e.getPlayer(), "fail", this.get(30));
         e.setCancelled(true);
      } else if (e.getLines()[0].split(" ").length == 2 && e.getLines()[0].split(" ")[0].equalsIgnoreCase(this.flag)) {
         String check = this.getCreateFlag(e.getLines()[0]);
         SignHandler signHandler = this.signManager.getSignHandler(check);
         if (signHandler == null) {
            this.show(e.getPlayer(), "fail", this.get(35));
            e.setCancelled(true);
         } else {
            Player p = e.getPlayer();
            if (!signHandler.checkPer(p, e.getBlock(), e.getLines())) {
               this.show(p, "fail", this.get(50));
               e.setCancelled(true);
            } else {
               e.setLine(0, this.show + signHandler.getCheckFlag());
               String[] show = signHandler.getShow(p, e.getBlock(), e.getLines());
               if (!show[0].isEmpty()) {
                  e.setLine(1, show[0]);
               }

               if (!show[1].isEmpty()) {
                  e.setLine(2, show[1]);
               }

               if (!show[2].isEmpty()) {
                  e.setLine(3, show[2]);
               }

               this.show(p, "success", this.get(55));
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerInteract(PlayerInteractEvent e) {
      if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.hasBlock() && (e.getClickedBlock().getTypeId() == 63 || e.getClickedBlock().getTypeId() == 68)) {
         org.bukkit.block.Sign sign = (org.bukkit.block.Sign)e.getClickedBlock().getState();
         if (this.isCmdSign(sign.getLine(0))) {
            e.setCancelled(true);
            if (UtilSpeed.check(e.getPlayer(), this.pn, "click", this.cmdInterval)) {
               if (e.getPlayer().getEyeLocation().distance(this.getCenter(sign.getLocation())) > this.distance) {
                  e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(240)}));
               } else {
                  this.signManager.onClick(e.getPlayer(), e.getClickedBlock(), sign.getLines());
               }
            }
         }
      }
   }

   private Location getCenter(Location l) {
      Location result = l.clone();
      result.setX(l.getX() + (double)0.5F);
      result.setY(l.getY() + (double)0.5F);
      result.setZ(l.getZ() + (double)0.5F);
      return result;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockBreak(BlockBreakEvent e) {
      if (e.getBlock().getTypeId() == 63 || e.getBlock().getTypeId() == 68) {
         org.bukkit.block.Sign sign = (org.bukkit.block.Sign)e.getBlock().getState();
         if (!this.isCmdSign(sign.getLine(0))) {
            return;
         }

         if (this.signManager.onBreak(e.getPlayer(), sign.getLines())) {
            e.setCancelled(true);
         }
      }

   }

   public String getCreateFlag(String s) {
      String result = s.split(" ")[1];
      return result;
   }

   public String getCheckFlag(String s) {
      String result = s.substring(this.show.length(), s.length());
      return result;
   }

   public boolean isCmdSign(String s) {
      return s.length() >= this.show.length() && s.substring(0, this.show.length()).equalsIgnoreCase(this.show);
   }

   public String getPn() {
      return this.pn;
   }

   public String getPer_sign_admin() {
      return this.per_sign_admin;
   }

   public PluginManager getPm() {
      return this.pm;
   }

   private boolean interact(Player p, Block b) {
      try {
         if (!this.editLineHash.containsKey(p)) {
            return false;
         }

         if (b.getTypeId() != 63 && b.getTypeId() != 68) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(225)}));
         } else {
            int type = Integer.parseInt(((String)this.editLineHash.get(p)).split(" ")[0]);
            int line = Integer.parseInt(((String)this.editLineHash.get(p)).split(" ")[1]);
            String content = ((String)this.editLineHash.get(p)).substring(4, ((String)this.editLineHash.get(p)).length());
            org.bukkit.block.Sign sign = (org.bukkit.block.Sign)b.getState();
            if (type == 1) {
               String result = sign.getLine(line - 1);
               if (result == null) {
                  result = "";
               }

               if (result.charAt(0) == 167) {
                  result = '§' + content + result.substring(2, result.length());
               } else {
                  result = '§' + content + result;
               }

               if (result.length() > 15) {
                  result = result.substring(0, 15);
               }

               sign.setLine(line - 1, result);
               sign.update();
            } else {
               sign.setLine(line - 1, content);
               sign.update();
            }

            p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(230)}));
         }

         this.editLineHash.remove(p);
         return true;
      } catch (NumberFormatException var8) {
      } catch (IndexOutOfBoundsException var9) {
      }

      return true;
   }

   private void animation(Player p) {
      if (this.editLineHash.containsKey(p)) {
         this.editLineHash.remove(p);
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(225)}));
      }

   }

   private void join(Player p) {
      this.editLineHash.remove(p);
   }

   private void quit(Player p) {
      this.intervalHash.remove(p);
      this.editLineHash.remove(p);
   }

   private void show(Player p, String type, Object args) {
      p.sendMessage(UtilFormat.format(this.pn, type, new Object[]{args}));
   }

   private void loadConfig0(FileConfiguration config) {
      this.per_sign_admin = config.getString("per_sign_admin");
      this.flag = config.getString("flag");
      this.show = Util.convert(config.getString("show"));
      this.cmdInterval = config.getInt("cmdInterval");
      this.distance = config.getDouble("distance");
   }

   private void sendConsoleMessage(String msg) {
      if (this.server.getConsoleSender() != null) {
         this.server.getConsoleSender().sendMessage(msg);
      } else {
         this.server.getLogger().info(msg);
      }

   }

   private void initBasic() {
      this.server = this.getServer();
      this.pn = this.getName();
      this.pm = this.server.getPluginManager();
      this.pluginPath = this.getFile().getParentFile().getAbsolutePath();
      this.dataFolder = this.pluginPath + File.separator + this.pn;
      this.pluginVersion = Util.getPluginVersion(this.getFile());
   }

   private void initConfig() {
      HashList<Pattern> filter = UtilConfig.getDefaultFilter();
      UtilConfig.register(new File(this.pluginPath + File.separator + this.pn + ".jar"), this.dataFolder, filter, this.pn);
      this.loadConfig((CommandSender)null);
   }

   private void reloadConfig(CommandSender sender) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null || UtilPer.checkPer(p, this.per_sign_admin)) {
         if (this.loadConfig(sender)) {
            sender.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(25)}));
         } else {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(30)}));
         }

      }
   }

   private boolean loadConfig(CommandSender sender) {
      try {
         return UtilConfig.loadConfig(this.pn);
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
      return UtilFormat.format(this.pn, id);
   }
}
