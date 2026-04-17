package lib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.types.InvalidTypeException;
import lib.util.Util;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilNames;
import lib.util.UtilPer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitScheduler;

public class Rewards implements Listener, IconMenu.OptionClickEventHandler {
   private static ItemMeta IM = (new ItemStack(1)).getItemMeta();
   private Random r = new Random();
   private Lib lib;
   private Server server;
   private String pn;
   private Eco eco;
   private Format f;
   private IconMenu icon;
   private BukkitScheduler scheduler;
   private String savePath;
   private String adminPer;
   private String usePer;
   private String infoOtherPer;
   private boolean tipRewards;
   private ItemStack pre;
   private ItemStack get;
   private ItemStack next;
   private ItemStack del;
   private int infoPos;
   private int prePos;
   private int getPos;
   private int nextPos;
   private int delPos;
   private int infoItem;
   private int infoItemSmallId;
   private String infoOwner;
   private String infoName;
   private String infoGold;
   private String infoExp;
   private String infoLevel;
   private String infoTip;
   private int delTimeLimit;
   private HashMap infoHash;
   private HashMap userHash;
   private HashMap rewardsHash;

   public Rewards(Lib lib) {
      this.lib = lib;
      this.server = lib.getServer();
      this.pn = lib.getPn();
      this.eco = lib.getEco();
      this.f = lib.getFormat();
      this.icon = lib.getIcon();
      this.scheduler = this.server.getScheduler();
      this.savePath = lib.getPluginPath() + File.separator + this.pn + File.separator + "rewards";
      (new File(this.savePath)).mkdirs();
      this.rewardsHash = new HashMap();
      this.loadConfig(lib.getCon().getConfig(this.pn));
      this.server.getPluginManager().registerEvents(this, lib);
   }

   public void init(Lib lib) {
      this.icon = lib.getIcon();
      this.loadRewards();
   }

   public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      try {
         Player p = null;
         if (sender instanceof Player) {
            p = (Player)sender;
         }

         int length = args.length;
         if (length != 1 || !args[0].equalsIgnoreCase("?")) {
            if (length == 1) {
               if (args[0].equalsIgnoreCase("reload")) {
                  if (p != null && !UtilPer.checkPer(p, this.adminPer)) {
                     return;
                  }

                  this.loadConfig(this.lib.getCon().getConfig(this.pn));
                  sender.sendMessage(UtilFormat.format(this.pn, "success", this.get(698)));
                  return;
               }

               if (args[0].equalsIgnoreCase("list")) {
                  this.showList(sender, p.getName(), 1);
                  return;
               }
            } else if (length == 2) {
               if (args[0].equalsIgnoreCase("list")) {
                  try {
                     int page = Integer.parseInt(args[1]);
                     this.showList(sender, p.getName(), page);
                     return;
                  } catch (Exception var8) {
                     this.showList(sender, args[1], 1);
                     return;
                  }
               }
            } else if (length == 3) {
               if (args[0].equalsIgnoreCase("list")) {
                  int page = Integer.parseInt(args[1]);
                  this.showList(sender, args[2], page);
                  return;
               }
            } else if (length == 4) {
               if (args[0].equalsIgnoreCase("add")) {
                  this.addRewards(sender, args[1], args[2], args[3]);
                  return;
               }
            } else if (length == 5) {
               if (args[0].equalsIgnoreCase("give")) {
                  if (p == null) {
                     sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(127)));
                  } else {
                     this.give(p, args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), (String)null);
                  }

                  return;
               }
            } else if (length == 6 && args[0].equalsIgnoreCase("give")) {
               if (p == null) {
                  sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(127)));
               } else {
                  this.give(p, args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), args[5]);
               }

               return;
            }
         }

         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpHeader", this.get(600)));
         if (p == null || UtilPer.hasPer(p, this.adminPer)) {
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", this.get(602), this.get(603)));
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", this.get(605), this.get(610)));
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", this.get(675), this.get(680)));
         }

         if (p == null || UtilPer.hasPer(p, this.usePer)) {
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", this.get(615), this.get(620)));
         }
      } catch (NumberFormatException var9) {
         sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(126)));
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
         this.loadRewards();
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerJoin(PlayerJoinEvent e) {
      if (this.tipRewards && this.userHash.containsKey(e.getPlayer().getName()) && ((HashMap)this.userHash.get(e.getPlayer().getName())).size() > 0) {
         e.getPlayer().sendMessage(UtilFormat.format(this.pn, "tip", this.get(660)));
      }

   }

   public void onOptionClick(IconMenu.OptionClickEvent e) {
      try {
         IconMenu.Info info = e.getInfo();
         int cmd = e.getPos() - info.getInv().getSize();
         if (cmd >= 0) {
            if (cmd == this.prePos) {
               Player p = e.getP();
               Inventory inv = info.getInv(p);
               int size = info.getInv().getSize();
               ItemStack infoItem = inv.getItem(size);
               String tar = infoItem.getItemMeta().getDisplayName().substring(this.infoOwner.length());
               int page = infoItem.getAmount() - 1;
               ShowList sl = new ShowList(p, tar, page);
               this.scheduler.scheduleSyncDelayedTask(this.lib, sl);
               e.setWillClose(true);
            } else if (cmd == this.getPos) {
               Player p = e.getP();
               Inventory inv = info.getInv(p);
               int size = info.getInv().getSize();
               ItemStack infoItem = inv.getItem(size);
               String type = ((String)infoItem.getItemMeta().getLore().get(0)).substring(this.infoName.length());
               Get get = new Get(p, type);
               this.scheduler.scheduleSyncDelayedTask(this.lib, get);
               e.setWillClose(true);
            } else if (cmd == this.nextPos) {
               Player p = e.getP();
               Inventory inv = info.getInv(p);
               int size = info.getInv().getSize();
               ItemStack infoItem = inv.getItem(size);
               String tar = infoItem.getItemMeta().getDisplayName().substring(this.infoOwner.length());
               int page = infoItem.getAmount() + 1;
               ShowList sl = new ShowList(p, tar, page);
               this.scheduler.scheduleSyncDelayedTask(this.lib, sl);
               e.setWillClose(true);
            } else if (cmd == this.delPos) {
               Player p = e.getP();
               Inventory inv = info.getInv(p);
               int size = info.getInv().getSize();
               ItemStack infoItem = inv.getItem(size);
               String tar = infoItem.getItemMeta().getDisplayName().substring(this.infoOwner.length());
               String type = ((String)infoItem.getItemMeta().getLore().get(0)).substring(this.infoName.length());
               DelConfirm delConfirm = new DelConfirm(p, tar, type);
               this.icon.openSession(p, this.get(697), (List)null, delConfirm, this.delTimeLimit);
               e.setWillClose(true);
            }
         }
      } catch (Exception var11) {
      }

   }

   public void reloadRewards(String plugin, YamlConfiguration config) {
      this.rewardsHash.put(plugin, new HashMap());
      MemorySection ms = (MemorySection)config.get("rewards");
      Map<String, Object> map = ms.getValues(false);

      for(String type : map.keySet()) {
         int minMoney = ms.getInt(type + ".money.min");
         int maxMoney = ms.getInt(type + ".money.max");
         int minExp = ms.getInt(type + ".exp.min");
         int maxExp = ms.getInt(type + ".exp.max");
         int minLevel = ms.getInt(type + ".level.min");
         int maxLevel = ms.getInt(type + ".level.max");
         String tip = ms.getString(type + ".tip");
         String s = ms.getString(type + ".itemsType");
         String itemsPlugin = plugin;
         String itemsType = s;
         if (s != null && s.indexOf(":") != -1) {
            itemsPlugin = s.split(":")[0];
            itemsType = s.split(":")[1];
         }

         s = ms.getString(type + ".enchantsType");
         if (s != null && s.indexOf(":") != -1) {
            itemsPlugin = s.split(":")[0];
            itemsType = s.split(":")[1];
         }

         RewardsInfo rewardsInfo = new RewardsInfo(type, minMoney, maxMoney, minExp, maxExp, minLevel, maxLevel, tip, itemsPlugin, itemsType, plugin, s);
         ((HashMap)this.rewardsHash.get(plugin)).put(type, rewardsInfo);
      }

   }

   public boolean addRewards(String plugin, String type, String tar, int money, int exp, int level, String tip, HashMap itemsHash, boolean force) {
      if (type == null) {
         type = this.getNextName(plugin, tar);
      }

      if (tip == null) {
         tip = this.get(645);
      }

      RewardsUser rewardsInfo = new RewardsUser(tar, plugin + "-" + type, money, exp, level, tip, itemsHash);
      if (!this.save(rewardsInfo, force)) {
         return false;
      } else {
         if (!this.userHash.containsKey(tar)) {
            this.userHash.put(tar, new HashMap());
         }

         if (!this.infoHash.containsKey(tar)) {
            this.infoHash.put(tar, new HashMap());
         }

         ((HashMap)this.userHash.get(tar)).put(plugin + "-" + type, rewardsInfo);
         String name = UtilFormat.format(this.pn, "showRewards1", tar);
         int size = 45;
         boolean emptyDestroy = false;
         IconMenu.Info info = this.icon.register(name, size, emptyDestroy, this);
         ((HashMap)this.infoHash.get(tar)).put(plugin + "-" + type, info);

         for(int slot : itemsHash.keySet()) {
            if (slot >= 0 && slot < size) {
               ItemStack is = (ItemStack)itemsHash.get(slot);
               info.setItem(slot, is);
            }
         }

         if (this.server.getPlayer(tar) != null) {
            this.server.getPlayer(tar).sendMessage(UtilFormat.format(this.pn, "success", this.get(695)));
         }

         return true;
      }
   }

   public boolean addRewards(CommandSender sender, String tar, String plugin, String type) {
      if (sender instanceof Player && !UtilPer.checkPer((Player)sender, this.adminPer)) {
         return false;
      } else {
         tar = Util.getRealName(sender, tar);
         if (tar == null) {
            return false;
         } else if (this.rewardsHash.containsKey(plugin) && ((HashMap)this.rewardsHash.get(plugin)).containsKey(type)) {
            RewardsInfo rewardsInfo = (RewardsInfo)((HashMap)this.rewardsHash.get(plugin)).get(type);
            int money = this.r.nextInt(rewardsInfo.getMaxMoney() - rewardsInfo.getMinMoney() + 1) + rewardsInfo.getMinMoney();
            int exp = this.r.nextInt(rewardsInfo.getMaxExp() - rewardsInfo.getMinExp() + 1) + rewardsInfo.getMinExp();
            int level = this.r.nextInt(rewardsInfo.getMaxLevel() - rewardsInfo.getMinLevel() + 1) + rewardsInfo.getMinLevel();
            String tip = rewardsInfo.getTip();
            String itemsType = rewardsInfo.getItemsType();
            String enchantsType = rewardsInfo.getEnchantsType();
            HashMap<Integer, ItemStack> itemsHash = new HashMap();
            if (itemsType != null && !itemsType.trim().isEmpty()) {
               Inventory inv = this.server.createInventory((InventoryHolder)null, 36);

               HashList<ItemStack> itemsList;
               try {
                  itemsList = UtilItems.getItems(rewardsInfo.getItemsPlugin(), itemsType, true, false, rewardsInfo.getEnchantsPlugin(), enchantsType, true, false);
               } catch (InvalidTypeException e) {
                  e.printStackTrace();
                  return false;
               }

               int index = 0;

               for(ItemStack is : itemsList) {
                  inv.setItem(index, is);
                  ++index;
                  if (index > 35) {
                     break;
                  }
               }

               for(int i = 0; i < 36; ++i) {
                  ItemStack is = inv.getItem(i);
                  if (is != null && is.getTypeId() != 0) {
                     itemsHash.put(i, is);
                  }
               }
            }

            if (money <= 0 && exp <= 0 && level <= 0 && itemsHash.size() <= 0) {
               return false;
            } else if (this.addRewards(plugin, (String)null, tar, money, exp, level, tip, itemsHash, false)) {
               sender.sendMessage(UtilFormat.format(this.pn, "success", this.get(685)));
               return true;
            } else {
               sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(690)));
               return false;
            }
         } else {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(650)));
            return false;
         }
      }
   }

   public RewardsInfo getRewardsInfo(String plugin, String type) {
      return (RewardsInfo)((HashMap)this.rewardsHash.get(plugin)).get(type);
   }

   public boolean showList(CommandSender sender, String tar, int page) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null) {
         sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(127)));
         return false;
      } else if (!UtilPer.checkPer(p, this.usePer)) {
         return false;
      } else {
         tar = Util.getRealName(p, tar);
         if (tar == null) {
            return false;
         } else if (!p.getName().equals(tar) && !UtilPer.checkPer(p, this.infoOtherPer)) {
            return false;
         } else if (this.infoHash.containsKey(tar)) {
            HashMap<String, IconMenu.Info> hash = (HashMap)this.infoHash.get(tar);
            int maxPage = hash.size();
            if (maxPage == 0) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", this.get(655)));
               return false;
            } else if (page >= 1 && page <= maxPage) {
               IconMenu.Info info = this.getInfo(page - 1, hash);
               if (info != null) {
                  String type = this.getKey(info, hash);
                  if (type != null) {
                     try {
                        RewardsUser ru = (RewardsUser)((HashMap)this.userHash.get(tar)).get(type);
                        Inventory inv = this.server.createInventory(p, 9, "none");
                        ItemStack infoItem = new ItemStack(this.infoItem, page, (short)this.infoItemSmallId);
                        ItemMeta im = infoItem.getItemMeta();
                        im.setDisplayName(this.infoOwner + tar);
                        List<String> lore = new ArrayList();
                        lore.add(this.infoName + type);
                        lore.add(this.infoGold + ru.getMoney());
                        lore.add(this.infoExp + ru.getExp());
                        lore.add(this.infoLevel + ru.getLevel());
                        lore.add(this.infoTip + ru.getTip());
                        im.setLore(lore);
                        infoItem.setItemMeta(im);
                        inv.setItem(this.infoPos, infoItem);
                        if (page > 1) {
                           ItemStack preItem = this.pre.clone();
                           inv.setItem(this.prePos, preItem);
                        }

                        if (tar.equals(p.getName())) {
                           ItemStack getItem = this.get.clone();
                           inv.setItem(this.getPos, getItem);
                        }

                        if (page < maxPage) {
                           ItemStack nextItem = this.next.clone();
                           inv.setItem(this.nextPos, nextItem);
                        }

                        if (UtilPer.hasPer(p, this.adminPer)) {
                           ItemStack delItem = this.del.clone();
                           inv.setItem(this.delPos, delItem);
                        }

                        this.icon.open(p, info, (String)null, inv);
                     } catch (Exception var15) {
                     }
                  }
               }

               return true;
            } else {
               p.sendMessage(UtilFormat.format(this.pn, "showRewards3", maxPage));
               return false;
            }
         } else {
            p.sendMessage(UtilFormat.format(this.pn, "fail", this.get(655)));
            return false;
         }
      }
   }

   private void give(Player p, String tar, int money, int exp, int level, String tip) {
      if (UtilPer.checkPer(p, this.adminPer)) {
         Inventory result = this.server.createInventory((InventoryHolder)null, 36);
         Inventory inv = UtilItems.getInv(p.getName());
         if (inv != null) {
            int index = 0;

            for(int i = 0; i < inv.getSize(); ++i) {
               ItemStack is = inv.getItem(i);
               if (is != null && is.getTypeId() != 0) {
                  result.setItem(index, is.clone());
                  ++index;
                  if (index > 35) {
                     break;
                  }
               }
            }
         }

         HashMap<Integer, ItemStack> itemsHash = new HashMap();

         for(int i = 0; i < 36; ++i) {
            ItemStack is = result.getItem(i);
            if (is != null && is.getTypeId() != 0) {
               itemsHash.put(i, is);
            }
         }

         if (this.addRewards(this.pn, (String)null, tar, money, exp, level, tip, itemsHash, true)) {
            p.sendMessage(UtilFormat.format(this.pn, "success", this.get(685)));
         } else {
            p.sendMessage(UtilFormat.format(this.pn, "fail", this.get(690)));
         }

      }
   }

   private void loadConfig(FileConfiguration config) {
      this.adminPer = config.getString("rewards.adminPer");
      this.usePer = config.getString("rewards.usePer");
      this.infoOtherPer = config.getString("rewards.infoOtherPer");
      this.tipRewards = config.getBoolean("rewards.tip");
      String path = config.getString("rewards.path");
      YamlConfiguration saveConfig = new YamlConfiguration();

      try {
         saveConfig.load((new File(path)).getCanonicalPath());
         this.reloadRewards(this.pn, saveConfig);
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (InvalidConfigurationException e) {
         e.printStackTrace();
      }

      this.prePos = config.getInt("rewards.showRewards.pre.pos");
      String[] temp = config.getString("rewards.showRewards.pre.item").split(":");
      int id;
      int smallId;
      if (temp.length == 2) {
         id = Integer.parseInt(temp[0]);
         smallId = Integer.parseInt(temp[1]);
      } else {
         id = Integer.parseInt(temp[0]);
         smallId = 0;
      }

      String name = Util.convert(config.getString("rewards.showRewards.pre.name"));
      List<String> lore = config.getStringList("rewards.showRewards.pre.lore");

      for(int i = 0; i < lore.size(); ++i) {
         lore.set(i, Util.convert((String)lore.get(i)));
      }

      this.pre = new ItemStack(id, 1, (short)smallId);
      ItemMeta im = IM.clone();
      im.setDisplayName(name);
      im.setLore(lore);
      this.pre.setItemMeta(im);
      this.getPos = config.getInt("rewards.showRewards.get.pos");
      temp = config.getString("rewards.showRewards.get.item").split(":");
      if (temp.length == 2) {
         id = Integer.parseInt(temp[0]);
         smallId = Integer.parseInt(temp[1]);
      } else {
         id = Integer.parseInt(temp[0]);
         smallId = 0;
      }

      name = Util.convert(config.getString("rewards.showRewards.get.name"));
      lore = config.getStringList("rewards.showRewards.get.lore");

      for(int i = 0; i < lore.size(); ++i) {
         lore.set(i, Util.convert((String)lore.get(i)));
      }

      this.get = new ItemStack(id, 1, (short)smallId);
      im = IM.clone();
      im.setDisplayName(name);
      im.setLore(lore);
      this.get.setItemMeta(im);
      this.nextPos = config.getInt("rewards.showRewards.next.pos");
      temp = config.getString("rewards.showRewards.next.item").split(":");
      if (temp.length == 2) {
         id = Integer.parseInt(temp[0]);
         smallId = Integer.parseInt(temp[1]);
      } else {
         id = Integer.parseInt(temp[0]);
         smallId = 0;
      }

      name = Util.convert(config.getString("rewards.showRewards.next.name"));
      lore = config.getStringList("rewards.showRewards.next.lore");

      for(int i = 0; i < lore.size(); ++i) {
         lore.set(i, Util.convert((String)lore.get(i)));
      }

      this.next = new ItemStack(id, 1, (short)smallId);
      im = IM.clone();
      im.setDisplayName(name);
      im.setLore(lore);
      this.next.setItemMeta(im);
      this.delPos = config.getInt("rewards.showRewards.del.pos");
      temp = config.getString("rewards.showRewards.del.item").split(":");
      if (temp.length == 2) {
         id = Integer.parseInt(temp[0]);
         smallId = Integer.parseInt(temp[1]);
      } else {
         id = Integer.parseInt(temp[0]);
         smallId = 0;
      }

      name = Util.convert(config.getString("rewards.showRewards.del.name"));
      lore = config.getStringList("rewards.showRewards.del.lore");

      for(int i = 0; i < lore.size(); ++i) {
         lore.set(i, Util.convert((String)lore.get(i)));
      }

      this.del = new ItemStack(id, 1, (short)smallId);
      im = IM.clone();
      im.setDisplayName(name);
      im.setLore(lore);
      this.del.setItemMeta(im);
      this.infoPos = config.getInt("rewards.showRewards.info.pos");
      this.infoItem = config.getInt("rewards.showRewards.info.item");
      this.infoItemSmallId = config.getInt("rewards.showRewards.info.smallId");
      this.infoOwner = Util.convert(config.getString("rewards.showRewards.info.owner"));
      this.infoName = Util.convert(config.getString("rewards.showRewards.info.name"));
      this.infoGold = Util.convert(config.getString("rewards.showRewards.info.gold"));
      this.infoExp = Util.convert(config.getString("rewards.showRewards.info.exp"));
      this.infoLevel = Util.convert(config.getString("rewards.showRewards.info.level"));
      this.infoTip = Util.convert(config.getString("rewards.showRewards.info.tip"));
      this.delTimeLimit = config.getInt("rewards.showRewards.del.timeLimit");
   }

   private void remove(CommandSender sender, String tar, String type) {
      if (!(sender instanceof Player) || UtilPer.checkPer((Player)sender, this.adminPer)) {
         tar = Util.getRealName(sender, tar);
         if (tar != null) {
            if (this.remove(tar, type)) {
               sender.sendMessage(UtilFormat.format(this.pn, "success", this.get(665)));
            } else {
               sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(670)));
            }

         }
      }
   }

   private String get(int id) {
      return this.f.format(this.pn, id);
   }

   private void get(Player p, String type) {
      if (UtilPer.checkPer(p, this.usePer)) {
         String name = p.getName();
         if (this.userHash.containsKey(name) && ((HashMap)this.userHash.get(name)).containsKey(type)) {
            RewardsUser rewardsInfo = (RewardsUser)((HashMap)this.userHash.get(name)).get(type);
            int money = rewardsInfo.getMoney();
            int exp = rewardsInfo.getExp();
            int level = rewardsInfo.getLevel();
            HashMap<Integer, ItemStack> itemsHash = rewardsInfo.getItemsHash();
            PlayerInventory inv = p.getInventory();
            int emptySlots = UtilItems.getEmptySlots(inv);
            if (emptySlots < itemsHash.size()) {
               p.sendMessage(UtilFormat.format(this.pn, "emptySlots", itemsHash.size()));
            } else {
               this.remove(name, type);
               if (money > 0) {
                  this.eco.add(p, money);
                  p.sendMessage(UtilFormat.format(this.pn, "addGold", money));
               }

               if (exp > 0) {
                  Util.setTotalExperience(p, Util.getTotalExperience(p) + exp);
                  p.sendMessage(UtilFormat.format(this.pn, "addExp", exp));
               }

               if (level > 0) {
                  p.setLevel(p.getLevel() + level);
                  p.sendMessage(UtilFormat.format(this.pn, "addLevel", level));
               }

               for(int i : itemsHash.keySet()) {
                  ItemStack is = (ItemStack)itemsHash.get(i);
                  inv.addItem(new ItemStack[]{is});
                  p.sendMessage(UtilFormat.format(this.pn, "addItem", is.getAmount(), UtilNames.getItemName(is.getTypeId(), is.getDurability())));
               }

               p.updateInventory();
               p.sendMessage(UtilFormat.format(this.pn, "success", this.get(640)));
               if (this.userHash.containsKey(name) && ((HashMap)this.userHash.get(name)).size() > 0) {
                  this.showList(p, name, 1);
               }

            }
         } else {
            p.sendMessage(UtilFormat.format(this.pn, "fail", this.get(635)));
         }
      }
   }

   private boolean remove(String name, String type) {
      if (this.userHash.containsKey(name) && ((HashMap)this.userHash.get(name)).containsKey(type)) {
         ((HashMap)this.userHash.get(name)).remove(type);

         try {
            ((HashMap)this.infoHash.get(name)).remove(type);
         } catch (Exception var4) {
         }

         (new File(this.savePath + File.separator + name + File.separator + type + ".yml")).delete();
         return true;
      } else {
         return false;
      }
   }

   private String getKey(IconMenu.Info info, HashMap hash) {
      for(String key : hash.keySet()) {
         if (((IconMenu.Info)hash.get(key)).equals(info)) {
            return key;
         }
      }

      return null;
   }

   private IconMenu.Info getInfo(int pos, HashMap hash) {
      if (hash.size() <= 0) {
         return null;
      } else {
         if (pos < 0) {
            pos = 0;
         }

         if (pos >= hash.size()) {
            pos = hash.size() - 1;
         }

         String key = (String)hash.keySet().toArray()[pos];
         return (IconMenu.Info)hash.get(key);
      }
   }

   private boolean save(RewardsUser rewardsInfo, boolean force) {
      try {
         String saveFile = this.savePath + File.separator + rewardsInfo.getName() + File.separator + rewardsInfo.getType() + ".yml";
         if ((new File(saveFile)).exists() && !force) {
            return false;
         } else {
            YamlConfiguration config = new YamlConfiguration();
            config.set("save-method", 1);
            config.set("money", rewardsInfo.getMoney());
            config.set("exp", rewardsInfo.getExp());
            config.set("level", rewardsInfo.getLevel());
            config.set("tip", rewardsInfo.getTip());

            for(int i : rewardsInfo.getItemsHash().keySet()) {
               config.set("items." + i, Util.strToU(UtilItems.saveItem((ItemStack)rewardsInfo.getItemsHash().get(i))));
            }

            config.save(saveFile);
            return true;
         }
      } catch (IOException e) {
         e.printStackTrace();
         return false;
      }
   }

   private boolean loadRewards() {
      (new File(this.savePath)).mkdirs();
      this.userHash = new HashMap();
      this.infoHash = new HashMap();
      File[] nameFileList = (new File(this.savePath)).listFiles();

      for(File file : nameFileList) {
         if (file.isDirectory()) {
            String name = file.getName();
            if (!this.userHash.containsKey(name)) {
               this.userHash.put(name, new HashMap());
            }

            if (!this.infoHash.containsKey(name)) {
               this.infoHash.put(name, new HashMap());
            }

            File[] fileList = (new File(this.savePath + File.separator + name)).listFiles();

            for(File file2 : fileList) {
               if (file2.isFile() && file2.getName().endsWith(".yml")) {
                  YamlConfiguration config = new YamlConfiguration();

                  try {
                     config.load(file2);
                  } catch (FileNotFoundException e) {
                     e.printStackTrace();
                     return false;
                  } catch (IOException e) {
                     e.printStackTrace();
                     return false;
                  } catch (InvalidConfigurationException e) {
                     e.printStackTrace();
                     return false;
                  }

                  String s = file2.getName().substring(0, file2.getName().length() - 4);
                  int saveMethod = config.getInt("save-method", 0);
                  int money = config.getInt("money");
                  int exp = config.getInt("exp");
                  int level = config.getInt("level");
                  String tip = Util.convert(config.getString("tip", this.get(645)));
                  HashMap<Integer, ItemStack> itemsHash = new HashMap();

                  for(int i = 0; i < 36; ++i) {
                     if (config.contains("items." + i)) {
                        ItemStack is = null;

                        try {
                           if (saveMethod == 0) {
                              is = ItemStack.deserialize(((MemorySection)config.get("items." + i)).getValues(true));
                           } else {
                              is = UtilItems.loadItem(Util.uToStr(config.getString("items." + i)));
                           }
                        } catch (Exception var28) {
                        }

                        if (is != null) {
                           itemsHash.put(i, is);
                        }
                     }
                  }

                  RewardsUser rewardsInfo = new RewardsUser(name, s, money, exp, level, tip, itemsHash);
                  ((HashMap)this.userHash.get(name)).put(s, rewardsInfo);
                  String show = this.lib.getFormat().format(this.pn, "showRewards1", name);
                  int size = 45;
                  boolean emptyDestroy = false;
                  IconMenu.Info info = this.icon.register(show, size, emptyDestroy, this);

                  for(int slot : itemsHash.keySet()) {
                     ItemStack is = (ItemStack)itemsHash.get(slot);
                     info.setItem(slot, is);
                  }

                  ((HashMap)this.infoHash.get(name)).put(s, info);
               }
            }
         }
      }

      return true;
   }

   private String getNextName(String plugin, String tar) {
      String path = this.savePath + File.separator + tar;

      int index;
      for(index = 1; (new File(path + File.separator + plugin + "-" + index + ".yml")).exists(); ++index) {
      }

      return String.valueOf(index);
   }

   class RewardsUser {
      private String name;
      private String type;
      private int money;
      private int exp;
      private int level;
      private String tip;
      private HashMap itemsHash;

      public RewardsUser(String name, String type, int money, int exp, int level, String tip, HashMap itemsHash) {
         this.name = name;
         this.type = type;
         this.money = money;
         this.exp = exp;
         this.level = level;
         this.tip = tip;
         this.itemsHash = itemsHash;
      }

      public String getName() {
         return this.name;
      }

      public String getType() {
         return this.type;
      }

      public int getMoney() {
         return this.money;
      }

      public int getExp() {
         return this.exp;
      }

      public int getLevel() {
         return this.level;
      }

      public String getTip() {
         return this.tip;
      }

      public HashMap getItemsHash() {
         return this.itemsHash;
      }
   }

   public class RewardsInfo {
      private String type;
      private int minMoney;
      private int maxMoney;
      private int minExp;
      private int maxExp;
      private int minLevel;
      private int maxLevel;
      private String tip;
      private String itemsPlugin;
      private String itemsType;
      private String enchantsPlugin;
      private String enchantsType;

      public RewardsInfo(String type, int minMoney, int maxMoney, int minExp, int maxExp, int minLevel, int maxLevel, String tip, String itemsPlugin, String itemsType, String enchantsPlugin, String enchantsType) {
         this.type = type;
         this.minMoney = minMoney;
         this.maxMoney = maxMoney;
         this.minExp = minExp;
         this.maxExp = maxExp;
         this.minLevel = minLevel;
         this.maxLevel = maxLevel;
         this.tip = tip;
         this.itemsPlugin = itemsPlugin;
         this.itemsType = itemsType;
         this.enchantsPlugin = enchantsPlugin;
         this.enchantsType = enchantsType;
      }

      public String getType() {
         return this.type;
      }

      public int getMinMoney() {
         return this.minMoney;
      }

      public int getMaxMoney() {
         return this.maxMoney;
      }

      public int getMinExp() {
         return this.minExp;
      }

      public int getMaxExp() {
         return this.maxExp;
      }

      public int getMinLevel() {
         return this.minLevel;
      }

      public int getMaxLevel() {
         return this.maxLevel;
      }

      public String getTip() {
         return this.tip;
      }

      public String getItemsType() {
         return this.itemsType;
      }

      public String getEnchantsType() {
         return this.enchantsType;
      }

      public String getItemsPlugin() {
         return this.itemsPlugin;
      }

      public String getEnchantsPlugin() {
         return this.enchantsPlugin;
      }
   }

   class ShowList implements Runnable {
      private Player p;
      private String tar;
      private int page;

      public ShowList(Player p, String tar, int page) {
         this.p = p;
         this.tar = tar;
         this.page = page;
      }

      public void run() {
         if (this.p.isOnline()) {
            Rewards.this.showList(this.p, this.tar, this.page);
         }

      }
   }

   class Get implements Runnable {
      private Player p;
      private String type;

      public Get(Player p, String type) {
         this.p = p;
         this.type = type;
      }

      public void run() {
         if (this.p.isOnline()) {
            Rewards.this.get(this.p, this.type);
         }

      }
   }

   class DelConfirm implements IconMenu.Session {
      private Player p;
      private String tar;
      private String type;

      public DelConfirm(Player p, String tar, String type) {
         this.p = p;
         this.tar = tar;
         this.type = type;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(IconMenu.Session.Result.YES)) {
            Rewards.this.remove(this.p, this.tar, this.type);
         }

      }
   }
}
