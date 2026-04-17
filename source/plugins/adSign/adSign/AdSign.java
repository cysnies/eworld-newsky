package adSign;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import land.Pos;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.time.TimeEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilEco;
import lib.util.UtilFormat;
import lib.util.UtilNames;
import lib.util.UtilPer;
import lib.util.UtilSpeed;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class AdSign extends JavaPlugin implements Listener {
   private static final String SPEED_CLICK = "click";
   private Random r = new Random();
   private String pn;
   private String pluginPath;
   private String dataFolder;
   private String pluginVersion;
   private String dataPath;
   private String userPath;
   private String per_adSign_admin;
   private String per_adSign_rent;
   private int setItem;
   private String flag;
   private int interval;
   private int saveInterval;
   private int checkInterval;
   private int checkChance;
   private int rent;
   private int last;
   private String line2;
   private String line3;
   private String line4;
   private String line2start;
   private String line3start;
   private String line4start;
   private HashMap posHash;
   private HashMap userHash;
   private HashMap editHash = new HashMap();

   public void onEnable() {
      this.initBasic();
      this.initConfig();
      this.loadConfig0(UtilConfig.getConfig(this.pn));
      this.loadData();
      this.loadUsers();
      UtilSpeed.register(this.pn, "click");
      Bukkit.getPluginManager().registerEvents(this, this);
      Util.sendConsoleMessage(UtilFormat.format(this.pn, "pluginEnabled", new Object[]{this.pn, this.pluginVersion}));
   }

   public void onDisable() {
      this.saveData();
      Util.sendConsoleMessage(UtilFormat.format(this.pn, "pluginDisabled", new Object[]{this.pn, this.pluginVersion}));
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      try {
         Player p = null;
         if (sender instanceof Player) {
            p = (Player)sender;
         }

         int length = args.length;
         if (cmd.getName().equalsIgnoreCase("adSign")) {
            if (length != 1 || !args[0].equalsIgnoreCase("?")) {
               if (length == 1) {
                  if (args[0].equalsIgnoreCase("reload")) {
                     this.reloadConfig(sender);
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("info")) {
                     if (p == null) {
                        sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(30)}));
                     } else {
                        this.info(p, p.getName());
                     }

                     return true;
                  }

                  if (p == null) {
                     sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(30)}));
                     return true;
                  }

                  int price = Integer.parseInt(args[0]);
                  this.editHash.put(p, "2 " + price);
                  p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(240)}));
                  return true;
               }

               if (length >= 2) {
                  if (length == 2 && args[0].equalsIgnoreCase("info")) {
                     this.info(sender, args[1]);
                     return true;
                  }

                  if (p == null) {
                     sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(30)}));
                     return true;
                  }

                  if (length == 2 && args[0].equalsIgnoreCase("set")) {
                     if (!UtilPer.checkPer(p, this.per_adSign_rent)) {
                        return true;
                     }

                     int line = Integer.parseInt(args[1]);
                     if (line >= 2 && line <= 4) {
                        List<String> list = (List)this.userHash.get(p.getName());
                        if (list == null || list.size() != 3) {
                           list = new ArrayList();
                           list.add("");
                           list.add("");
                           list.add("");
                        }

                        list.set(line - 2, "");
                        this.userHash.put(p.getName(), list);
                        this.saveUser(p.getName(), list);
                        p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(135)}));
                        this.info(sender, p.getName());
                        return true;
                     }

                     p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(235)}));
                     return true;
                  }

                  if (length >= 3 && args[0].equalsIgnoreCase("set")) {
                     if (!UtilPer.checkPer(p, this.per_adSign_rent)) {
                        return true;
                     }

                     int line = Integer.parseInt(args[1]);
                     if (line >= 2 && line <= 4) {
                        String content = Util.combine(args, " ", 2, length).trim();
                        if (content.isEmpty()) {
                           p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(236)}));
                           return true;
                        }

                        content = Util.convert(content.substring(0, Math.min(15, content.length())));
                        List<String> list = (List)this.userHash.get(p.getName());
                        if (list == null || list.size() != 3) {
                           list = new ArrayList();
                           list.add("");
                           list.add("");
                           list.add("");
                        }

                        list.set(line - 2, content);
                        this.userHash.put(p.getName(), list);
                        this.saveUser(p.getName(), list);
                        p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(135)}));
                        this.info(sender, p.getName());
                        return true;
                     }

                     p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(235)}));
                     return true;
                  }

                  int line = Integer.parseInt(args[0]);
                  if (line >= 2 && line <= 4) {
                     String content = Util.combine(args, " ", 1, length).trim();
                     if (content.isEmpty()) {
                        p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(236)}));
                        return true;
                     }

                     content = Util.convert(content.substring(0, Math.min(15, content.length())));
                     this.editHash.put(p, "1 " + line + " " + content);
                     p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(238)}));
                     return true;
                  }

                  p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(235)}));
                  return true;
               }
            }

            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpHeader", new Object[]{this.get(10)}));
            if (p == null || UtilPer.hasPer(p, this.per_adSign_admin)) {
               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(15), this.get(20)}));
            }

            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(35), this.get(40)}));
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(85), this.get(90)}));
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(115), this.get(120)}));
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(125), this.get(130)}));
         }

         return true;
      } catch (NumberFormatException var10) {
         sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(105)}));
         return true;
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

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onTime(TimeEvent e) {
      if (TimeEvent.getTime() % (long)this.saveInterval == 0L) {
         this.saveData();
      }

      if (TimeEvent.getTime() % (long)this.checkInterval == 0L) {
         long now = System.currentTimeMillis();

         for(Pos pos : this.posHash.keySet()) {
            if (this.r.nextInt(100) < this.checkChance) {
               SignInfo si = (SignInfo)this.posHash.get(pos);
               if (now - si.getStart() >= (long)(si.getLast() * 1000)) {
                  if (si.getNext() != null) {
                     Util.sendMsg(si.getOwner(), UtilFormat.format(this.pn, "dead", new Object[]{UtilNames.getWorldName(pos.getWorld()), pos.getX(), pos.getY(), pos.getZ()}));
                     Util.sendMsg(si.getNext(), UtilFormat.format(this.pn, "start", new Object[]{UtilNames.getWorldName(pos.getWorld()), pos.getX(), pos.getY(), pos.getZ()}));
                     boolean same = si.getOwner().equals(si.getNext());
                     si.setStart(now);
                     si.setOwner(si.getNext());
                     si.setLast(this.last);
                     si.setNext((String)null);
                     si.setPrice(0);
                     if (!same) {
                        try {
                           Location l = Pos.toLoc(pos);
                           if (l != null) {
                              Sign sign = (Sign)l.getBlock().getState();
                              this.reset(sign, si.getOwner());
                              sign.update(true);
                           }
                        } catch (Exception var11) {
                        }
                     }
                  } else {
                     si.setStart(0L);
                     si.setOwner((String)null);
                     si.setLast(0);
                     si.setNext((String)null);
                     si.setPrice(0);

                     try {
                        Location l = Pos.toLoc(pos);
                        if (l != null) {
                           Sign sign = (Sign)l.getBlock().getState();
                           this.reset(sign);
                           sign.update(true);
                        }
                     } catch (Exception var10) {
                     }
                  }
               }
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerJoin(PlayerJoinEvent e) {
      this.editHash.remove(e.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerQuit(PlayerQuitEvent e) {
      this.editHash.remove(e.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPlayerAnimation(PlayerAnimationEvent e) {
      if (this.animation(e.getPlayer())) {
         e.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerInteract(PlayerInteractEvent e) {
      if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.hasBlock() && (e.getClickedBlock().getTypeId() == 63 || e.getClickedBlock().getTypeId() == 68)) {
         Location l = e.getClickedBlock().getLocation();
         Pos pos = Pos.getPos(l);
         SignInfo si = (SignInfo)this.posHash.get(pos);
         if (si != null) {
            e.setCancelled(true);
            if (!UtilSpeed.check(e.getPlayer(), this.pn, "click", this.interval)) {
               return;
            }

            String edit = (String)this.editHash.get(e.getPlayer());
            if (edit != null) {
               String[] ss = edit.split(" ");
               this.editHash.remove(e.getPlayer());
               int check = Integer.parseInt(ss[0]);
               switch (check) {
                  case 1:
                     if (!UtilPer.checkPer(e.getPlayer(), this.per_adSign_rent)) {
                        return;
                     }

                     if (si.getOwner() == null || !si.getOwner().equals(e.getPlayer().getName())) {
                        e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(60)}));
                        return;
                     }

                     Sign sign = (Sign)e.getClickedBlock().getState();
                     sign.setLine(0, this.flag);
                     int line = Integer.parseInt(ss[1]);
                     String content = Util.combine(ss, " ", 2, ss.length);
                     sign.setLine(line - 1, content.substring(0, Math.min(15, content.length())));
                     sign.update(true);
                     e.getPlayer().sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(230)}));
                     break;
                  case 2:
                     if (!UtilPer.checkPer(e.getPlayer(), this.per_adSign_rent)) {
                        return;
                     }

                     if (si.getOwner() == null) {
                        e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(65)}));
                        return;
                     }

                     int pay = Integer.parseInt(ss[1]);
                     if (pay <= 0) {
                        e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(70)}));
                        return;
                     }

                     if (UtilEco.get(e.getPlayer().getName()) < (double)pay) {
                        e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(75)}));
                        return;
                     }

                     if (pay < this.rent) {
                        e.getPlayer().sendMessage(UtilFormat.format(this.pn, "moreThanRent", new Object[]{this.rent}));
                        return;
                     }

                     if (pay <= si.getPrice()) {
                        e.getPlayer().sendMessage(UtilFormat.format(this.pn, "more", new Object[]{si.getPrice()}));
                        return;
                     }

                     String next = si.getNext();
                     if (next != null) {
                        UtilEco.add(next, (double)si.getPrice());
                        Util.sendMsg(next, UtilFormat.format(this.pn, "payback", new Object[]{UtilNames.getWorldName(pos.getWorld()), pos.getX(), pos.getY(), pos.getZ(), si.getPrice()}));
                     }

                     UtilEco.del(e.getPlayer().getName(), (double)pay);
                     si.setNext(e.getPlayer().getName());
                     si.setPrice(pay);
                     e.getPlayer().sendMessage(UtilFormat.format(this.pn, "payYes", new Object[]{pay}));
               }
            } else if (si.getOwner() == null) {
               if (!UtilPer.checkPer(e.getPlayer(), this.per_adSign_rent)) {
                  return;
               }

               if (!e.getPlayer().isSneaking()) {
                  e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(110)}));
                  return;
               }

               if (UtilEco.get(e.getPlayer().getName()) < (double)this.rent) {
                  e.getPlayer().sendMessage(UtilFormat.format(this.pn, "noMoney", new Object[]{this.rent}));
                  return;
               }

               UtilEco.del(e.getPlayer().getName(), (double)this.rent);
               si.setOwner(e.getPlayer().getName());
               si.setStart(System.currentTimeMillis());
               si.setLast(this.last);
               si.setNext((String)null);
               si.setPrice(0);
               Sign sign = (Sign)e.getClickedBlock().getState();
               this.reset(sign, si.getOwner());
               sign.update(true);
               e.getPlayer().sendMessage(UtilFormat.format(this.pn, "rentYes", new Object[]{this.rent}));
            } else {
               String owner = si.getOwner();
               if (owner == null) {
                  owner = this.get(95);
               }

               String deadTime = Util.getDateTime(new Date(si.getStart()), 0, 0, si.getLast() / 60);
               String next = si.getNext();
               if (next == null) {
                  next = this.get(95);
               }

               int price = si.getPrice();
               e.getPlayer().sendMessage(UtilFormat.format(this.pn, "showInfo", new Object[]{owner, deadTime, next, price}));
            }
         } else if (UtilPer.hasPer(e.getPlayer(), this.per_adSign_admin)) {
            ItemStack is = e.getPlayer().getItemInHand();
            if (is != null && is.getTypeId() == this.setItem) {
               si = new SignInfo();
               this.posHash.put(pos, si);
               Sign sign = (Sign)e.getClickedBlock().getState();
               this.reset(sign);
               sign.update(true);
               e.getPlayer().sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(100)}));
               return;
            }
         }

      } else {
         if (this.animation(e.getPlayer())) {
            e.setCancelled(true);
         }

      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockBreak(BlockBreakEvent e) {
      if (e.getBlock().getTypeId() == 63 || e.getBlock().getTypeId() == 68) {
         Pos pos = Pos.getPos(e.getBlock().getLocation());
         SignInfo si = (SignInfo)this.posHash.get(pos);
         if (si != null) {
            if (!UtilPer.hasPer(e.getPlayer(), this.per_adSign_admin)) {
               e.setCancelled(true);
               e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(55)}));
            } else {
               if (si.getNext() != null) {
                  UtilEco.add(si.getNext(), (double)si.getPrice());
                  Util.sendMsg(si.getNext(), UtilFormat.format(this.pn, "payback2", new Object[]{UtilNames.getWorldName(pos.getWorld()), pos.getX(), pos.getY(), pos.getZ(), si.getPrice()}));
               }

               this.posHash.remove(pos);
               e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(80)}));
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockPhysics(BlockPhysicsEvent e) {
      if (e.getBlock().getTypeId() == 63 || e.getBlock().getTypeId() == 68) {
         Pos pos = Pos.getPos(e.getBlock().getLocation());
         SignInfo si = (SignInfo)this.posHash.get(pos);
         if (si != null) {
            e.setCancelled(true);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onSignChange(SignChangeEvent e) {
      if (Util.convert(e.getLines()[0].trim()).equalsIgnoreCase(this.flag)) {
         e.setCancelled(true);
         e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(50)}));
      }

   }

   private void info(CommandSender sender, String tar) {
      List<String> list = (List)this.userHash.get(tar);
      if (list != null && list.size() == 3) {
         sender.sendMessage(UtilFormat.format(this.pn, "infoTar", new Object[]{tar, list.get(0), list.get(1), list.get(2)}));
      } else {
         sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(140)}));
      }

   }

   private void reset(Sign sign, String owner) {
      sign.setLine(0, this.flag);
      List<String> list = (List)this.userHash.get(owner);
      String line2;
      String line3;
      String line4;
      if (list != null && list.size() == 3) {
         line2 = (String)list.get(0);
         line3 = (String)list.get(1);
         line4 = (String)list.get(2);
      } else {
         line2 = this.line2start;
         line3 = this.line3start.replace("{0}", owner);
         line4 = this.line4start;
      }

      sign.setLine(1, line2);
      sign.setLine(2, line3);
      sign.setLine(3, line4);
   }

   private void reset(Sign sign) {
      sign.setLine(0, this.flag);
      sign.setLine(1, this.line2.replace("{0}", String.valueOf(this.rent)));
      int hour = this.last / 3600;
      int minute = this.last % 3600 / 60;
      int seconds = this.last % 60;
      sign.setLine(2, this.line3.replace("{0}", String.valueOf(hour)).replace("{1}", String.valueOf(minute)).replace("{2}", String.valueOf(seconds)));
      sign.setLine(3, this.line4);
   }

   private void loadUsers() {
      this.userHash = new HashMap();
      File path = new File(this.userPath);

      File[] var5;
      for(File file : var5 = path.listFiles()) {
         try {
            if (file.getName().endsWith(".yml")) {
               String name = file.getName().split("\\.")[0].trim();
               if (!name.isEmpty()) {
                  YamlConfiguration config = new YamlConfiguration();
                  config.load(file);
                  List<String> list = config.getStringList("list");
                  if (list != null && list.size() == 3) {
                     this.userHash.put(name, list);
                  }
               }
            }
         } catch (Exception var9) {
         }
      }

   }

   private void saveUser(String user, List list) {
      try {
         File file = new File(this.userPath + File.separator + user + ".yml");
         file.createNewFile();
         YamlConfiguration config = new YamlConfiguration();
         config.load(file);
         config.set("list", list);
         config.save(file);
      } catch (Exception var5) {
      }

   }

   private void loadData() {
      this.posHash = new HashMap();
      YamlConfiguration adConfig = new YamlConfiguration();

      try {
         adConfig.load(this.dataPath);

         for(String s : adConfig.getStringList("data")) {
            String[] ss = s.split(" ");
            String world = ss[0];
            int x = Integer.parseInt(ss[1]);
            int y = Integer.parseInt(ss[2]);
            int z = Integer.parseInt(ss[3]);
            Pos pos = new Pos(world, x, y, z);
            String data = Util.combine(ss, " ", 4, ss.length);
            SignInfo si = SignInfo.load(data);
            if (si != null) {
               this.posHash.put(pos, si);
            }
         }
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (InvalidConfigurationException e) {
         e.printStackTrace();
      }

   }

   private void saveData() {
      List<String> result = new ArrayList();

      for(Pos pos : this.posHash.keySet()) {
         SignInfo si = (SignInfo)this.posHash.get(pos);
         String data = pos.getWorld() + " " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " " + SignInfo.save(si);
         result.add(data);
      }

      YamlConfiguration adConfig = new YamlConfiguration();
      adConfig.set("data", result);

      try {
         adConfig.save(this.dataPath);
      } catch (IOException e) {
         e.printStackTrace();
      }

   }

   private boolean animation(Player p) {
      if (this.editHash.containsKey(p)) {
         this.editHash.remove(p);
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(225)}));
         return true;
      } else {
         return false;
      }
   }

   private void loadConfig0(FileConfiguration config) {
      this.per_adSign_admin = config.getString("per_adSign_admin");
      this.per_adSign_rent = config.getString("per_adSign_rent");
      this.setItem = config.getInt("setItem");
      this.flag = Util.convert(config.getString("flag"));
      this.interval = config.getInt("interval");
      this.saveInterval = config.getInt("saveInterval");
      this.checkInterval = config.getInt("checkInterval");
      this.checkChance = config.getInt("checkChance");
      this.rent = config.getInt("rent");
      this.last = config.getInt("last");
      this.line2 = Util.convert(config.getString("line2"));
      this.line3 = Util.convert(config.getString("line3"));
      this.line4 = Util.convert(config.getString("line4"));
      this.line2start = Util.convert(config.getString("line2start"));
      this.line3start = Util.convert(config.getString("line3start"));
      this.line4start = Util.convert(config.getString("line4start"));
   }

   private void initBasic() {
      this.pn = this.getName();
      this.pluginPath = this.getFile().getParentFile().getAbsolutePath();
      this.dataFolder = this.pluginPath + File.separator + this.pn;
      this.pluginVersion = Util.getPluginVersion(this.getFile());
      this.dataPath = this.pluginPath + File.separator + this.pn + File.separator + "data.yml";

      try {
         (new File(this.pluginPath + File.separator + this.pn)).mkdirs();
         (new File(this.dataPath)).createNewFile();
      } catch (IOException e) {
         e.printStackTrace();
      }

      this.userPath = this.pluginPath + File.separator + this.pn + File.separator + "user";
      (new File(this.userPath)).mkdirs();
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

      if (p == null || UtilPer.checkPer(p, this.per_adSign_admin)) {
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
            sender = Bukkit.getConsoleSender();
         }

         if (sender != null) {
            sender.sendMessage(e.getMessage());
         } else {
            Bukkit.getLogger().info(e.getMessage());
         }

         return false;
      }
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
