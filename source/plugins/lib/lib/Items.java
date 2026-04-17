package lib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import lib.config.ReloadConfigEvent;
import lib.hashList.ChanceHashList;
import lib.hashList.ChanceHashListImpl;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.nbt.Attributes;
import lib.types.InvalidTypeException;
import lib.util.Util;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilNames;
import lib.util.UtilPer;
import org.bukkit.Location;
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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class Items implements Listener {
   private Random r = new Random();
   private Server server;
   private String pn;
   private String savePath;
   private Enchants enchants;
   private String adminPer;
   private String infoPer;
   private int showInterval;
   private int defaultCheckMode;
   private int defaultChance;
   private int maxChance;
   private int slot;
   private int pageSize;
   private HashMap itemsHash;
   private HashList tempHash;
   private HashMap invHash;
   private HashList openList;
   private HashMap lastOpenHash;

   public Items(Lib lib) {
      this.server = lib.getServer();
      this.pn = lib.getPn();
      this.savePath = lib.getPluginPath() + File.separator + this.pn + File.separator + "items";
      (new File(this.savePath)).mkdirs();
      this.enchants = lib.getEnchants();
      this.itemsHash = new HashMap();
      this.invHash = new HashMap();
      this.openList = new HashListImpl();
      this.lastOpenHash = new HashMap();
      this.loadConfig(lib.getCon().getConfig(this.pn));
      this.server.getPluginManager().registerEvents(this, lib);
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
               if (args[0].equalsIgnoreCase("open")) {
                  if (p == null) {
                     sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(127)));
                     return;
                  }

                  this.open(p, false);
                  return;
               }

               if (args[0].equalsIgnoreCase("list")) {
                  this.showPluginList(sender);
                  return;
               }

               if (args[0].equalsIgnoreCase("reload")) {
                  if (p != null && !UtilPer.checkPer(p, this.adminPer)) {
                     return;
                  }

                  if (!this.reloadItems(this.pn, this.savePath)) {
                     Util.sendConsoleMessage(UtilFormat.format(this.pn, "fail", this.get(195)));
                  }

                  sender.sendMessage(UtilFormat.format(this.pn, "success", this.get(220)));
                  return;
               }
            } else if (length == 2) {
               if (args[0].equalsIgnoreCase("open") && args[1].equalsIgnoreCase("-new")) {
                  if (p == null) {
                     sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(127)));
                     return;
                  }

                  this.open(p, true);
                  return;
               }

               if (args[0].equalsIgnoreCase("save")) {
                  if (p == null) {
                     sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(127)));
                     return;
                  }

                  this.save(p, args[1], false);
                  return;
               }

               if (args[0].equalsIgnoreCase("get")) {
                  if (p == null) {
                     sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(127)));
                     return;
                  }

                  this.get(p, this.pn, args[1], false);
                  return;
               }

               if (args[0].equalsIgnoreCase("list")) {
                  this.showItemsList(sender, args[1], 1);
                  return;
               }

               if (args[0].equalsIgnoreCase("info")) {
                  if (p == null) {
                     sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(127)));
                     return;
                  }

                  this.showType(p, (String)null, args[1], 1);
                  return;
               }
            } else if (length == 3) {
               if (args[0].equalsIgnoreCase("save") && args[2].equalsIgnoreCase("-f")) {
                  if (p == null) {
                     sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(127)));
                     return;
                  }

                  this.save(p, args[1], true);
                  return;
               }

               if (args[0].equalsIgnoreCase("get")) {
                  if (p == null) {
                     sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(127)));
                     return;
                  }

                  if (args[2].equalsIgnoreCase("-f")) {
                     this.get(p, this.pn, args[1], true);
                  } else {
                     this.get(p, args[1], args[2], false);
                  }

                  return;
               }

               if (args[0].equalsIgnoreCase("list")) {
                  this.showItemsList(sender, args[1], Integer.parseInt(args[2]));
                  return;
               }

               if (args[0].equalsIgnoreCase("info")) {
                  if (p == null) {
                     sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(127)));
                     return;
                  }

                  try {
                     int page = Integer.parseInt(args[2]);
                     this.showType(p, (String)null, args[1], page);
                     return;
                  } catch (NumberFormatException var8) {
                     this.showType(p, args[2], args[1], 1);
                     return;
                  }
               }
            } else if (length == 4) {
               if (args[0].equalsIgnoreCase("get") && args[3].equalsIgnoreCase("-f")) {
                  if (p == null) {
                     sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(127)));
                     return;
                  }

                  this.get(p, args[1], args[2], true);
                  return;
               }

               if (args[0].equalsIgnoreCase("info")) {
                  if (p == null) {
                     sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(127)));
                     return;
                  }

                  int page = Integer.parseInt(args[3]);
                  this.showType(p, args[2], args[1], page);
                  return;
               }
            }
         }

         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpHeader", this.get(130)));
         if (p == null || UtilPer.hasPer(p, this.adminPer)) {
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", this.get(135), this.get(140)));
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", this.get(145), this.get(150)));
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", this.get(165), this.get(170)));
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", this.get(172), this.get(173)));
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", this.get(210), this.get(215)));
         }

         if (p == null || UtilPer.hasPer(p, this.infoPer)) {
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", this.get(235), this.get(240)));
         }
      } catch (NumberFormatException var9) {
         sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(126)));
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void reloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onInventoryDrag(InventoryDragEvent e) {
      if (e.getWhoClicked() instanceof Player && this.openList.has((Player)e.getWhoClicked())) {
         ((Player)e.getWhoClicked()).sendMessage(UtilFormat.format(this.pn, "fail", this.get(230)));
         e.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onInventoryClick(InventoryClickEvent e) {
      if (e.getWhoClicked() instanceof Player && this.openList.has((Player)e.getWhoClicked())) {
         ((Player)e.getWhoClicked()).sendMessage(UtilFormat.format(this.pn, "fail", this.get(230)));
         e.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerQuit(PlayerQuitEvent e) {
      this.openList.remove(e.getPlayer());
      this.lastOpenHash.remove(e.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onInventoryClose(InventoryCloseEvent e) {
      this.openList.remove((Player)e.getPlayer());
   }

   public static String saveItem(ItemStack is) {
      if (is == null) {
         return null;
      } else {
         YamlConfiguration config = new YamlConfiguration();
         config.createSection("item", is.serialize());
         Attributes a = new Attributes(is);

         for(int index = 0; index < a.size(); ++index) {
            Attributes.Attribute at = a.get(index);
            config.set("item.attributes.attribute" + index + ".amount", at.getAmount());
            config.set("item.attributes.attribute" + index + ".type", at.getAttributeType().getMinecraftId());
            config.set("item.attributes.attribute" + index + ".name", at.getName());
            config.set("item.attributes.attribute" + index + ".operation", at.getOperation().getId());
            config.set("item.attributes.attribute" + index + ".uuid", at.getUUID().toString());
         }

         return config.saveToString();
      }
   }

   public static ItemStack loadItem(String s) {
      try {
         YamlConfiguration config = new YamlConfiguration();
         config.loadFromString(s);
         ItemStack is = ItemStack.deserialize(((MemorySection)config.get("item")).getValues(true));
         if (is != null) {
            String attrPath = "item.attributes";
            if (config.contains(attrPath)) {
               Attributes a = new Attributes(is);
               MemorySection ms = (MemorySection)config.get(attrPath);

               for(String key : ms.getValues(false).keySet()) {
                  Attributes.Attribute.Builder b = Attributes.Attribute.newBuilder();
                  if (ms.contains(key + ".uuid")) {
                     b.uuid(UUID.fromString(ms.getString(key + ".uuid")));
                  }

                  if (ms.contains(key + ".amount")) {
                     b.amount((double)ms.getInt(key + ".amount"));
                  }

                  if (ms.contains(key + ".name")) {
                     b.name(ms.getString(key + ".name"));
                  }

                  if (ms.contains(key + ".operation")) {
                     b.operation(Attributes.Operation.fromId(ms.getInt(key + ".operation")));
                  }

                  if (ms.contains(key + ".type")) {
                     b.type(Attributes.AttributeType.fromId(ms.getString(key + ".type")));
                  }

                  a.add(b.build());
               }

               is = a.getStack();
            }
         }

         return is;
      } catch (Exception var9) {
         return null;
      }
   }

   private void loadConfig(FileConfiguration config) {
      this.adminPer = config.getString("items.adminPer");
      this.infoPer = config.getString("items.infoPer");
      this.showInterval = config.getInt("items.showInterval");
      this.defaultCheckMode = config.getInt("items.defaultCheckMode");
      this.defaultChance = config.getInt("items.defaultChance");
      this.maxChance = config.getInt("items.maxChance");
      this.slot = config.getInt("items.slot");
      this.pageSize = config.getInt("items.pageSize");
      if (!this.reloadItems(this.pn, this.savePath)) {
         Util.sendConsoleMessage(UtilFormat.format(this.pn, "fail", this.get(195)));
      }

   }

   private boolean reloadItems(String plugin, String path) {
      try {
         File file = new File(path);
         if (file.isDirectory()) {
            File[] var7;
            for(File f : var7 = file.listFiles()) {
               YamlConfiguration config = new YamlConfiguration();
               config.load(f);
               if (!this.reloadItems(plugin, config)) {
                  return false;
               }
            }

            return true;
         }
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (InvalidConfigurationException e) {
         e.printStackTrace();
      }

      return false;
   }

   private ItemInfo loadItem(String plugin, FileConfiguration config, String type) {
      if (plugin == null) {
         plugin = this.pn;
      }

      if (this.tempHash.has(type)) {
         return null;
      } else {
         this.tempHash.add(type);
         if (!this.itemsHash.containsKey(plugin)) {
            this.itemsHash.put(plugin, new HashMap());
         }

         if (!((HashMap)this.itemsHash.get(plugin)).containsKey(type)) {
            ((HashMap)this.itemsHash.get(plugin)).put(type, new ItemInfo(type, new HashMap(), new ChanceHashListImpl()));
         }

         ItemInfo itemInfo = (ItemInfo)((HashMap)this.itemsHash.get(plugin)).get(type);
         int min = config.getInt("items." + type + ".min", 0);
         int max = config.getInt("items." + type + ".max", Integer.MAX_VALUE);
         itemInfo.setMin(min);
         itemInfo.setMax(max);

         try {
            ItemStack is = ItemStack.deserialize(((MemorySection)config.get("items." + type)).getValues(true));
            if (is != null) {
               String attrPath = "items." + type + ".attributes";
               if (config.contains(attrPath)) {
                  Attributes a = new Attributes(is);
                  MemorySection ms = (MemorySection)config.get(attrPath);

                  for(String key : ms.getValues(false).keySet()) {
                     Attributes.Attribute.Builder b = Attributes.Attribute.newBuilder();
                     if (ms.contains(key + ".uuid")) {
                        b.uuid(UUID.fromString(ms.getString(key + ".uuid")));
                     }

                     if (ms.contains(key + ".amount")) {
                        b.amount((double)ms.getInt(key + ".amount"));
                     }

                     if (ms.contains(key + ".name")) {
                        b.name(ms.getString(key + ".name"));
                     }

                     if (ms.contains(key + ".operation")) {
                        b.operation(Attributes.Operation.fromId(ms.getInt(key + ".operation")));
                     }

                     if (ms.contains(key + ".type")) {
                        b.type(Attributes.AttributeType.fromId(ms.getString(key + ".type")));
                     }

                     a.add(b.build());
                  }

                  is = a.getStack();
               }

               String chancePath = "items." + type + ".chance";
               int chance = config.getInt(chancePath, this.defaultChance);
               itemInfo.getItemsList().addChance(is, chance);
               itemInfo.getModeHash().put(is, config.getInt("items." + type + ".check", this.defaultCheckMode));
            }
         } catch (Exception var14) {
         }

         String inheritPath = "items." + type + ".inherit";
         if (config.contains(inheritPath)) {
            for(String s : config.getStringList(inheritPath)) {
               ItemInfo itemInfoInherit = this.loadItem(plugin, config, s);
               itemInfo.combine(itemInfoInherit);
            }
         }

         int fixChance = config.getInt("items." + type + ".fixChance");
         if (fixChance > 0) {
            itemInfo.getItemsList().updateTotalChance(fixChance);
         }

         return itemInfo;
      }
   }

   private void showItemsList(CommandSender sender, String plugin, int page) {
      if (!(sender instanceof Player) || UtilPer.checkPer((Player)sender, this.adminPer)) {
         if (!this.itemsHash.containsKey(plugin)) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(200)));
         } else if (((HashMap)this.itemsHash.get(plugin)).size() == 0) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(205)));
         } else {
            ChanceHashList<String> chanceHashList = new ChanceHashListImpl();
            Set<String> set = ((HashMap)this.itemsHash.get(plugin)).keySet();
            chanceHashList.convert(set, true);
            int maxPage = chanceHashList.getMaxPage(this.pageSize);
            if (page < 1) {
               page = 1;
            } else if (page > maxPage) {
               page = maxPage;
            }

            sender.sendMessage(UtilFormat.format(this.pn, "listHeader", UtilFormat.format(this.pn, "itemsShow", plugin), page, maxPage));

            for(String s : chanceHashList.getPage(page, this.pageSize)) {
               sender.sendMessage(UtilFormat.format(this.pn, "listItem", s));
            }

         }
      }
   }

   private void showPluginList(CommandSender sender) {
      if (!(sender instanceof Player) || UtilPer.checkPer((Player)sender, this.adminPer)) {
         String result = "";
         boolean first = true;

         for(String s : this.itemsHash.keySet()) {
            if (first) {
               first = false;
            } else {
               result = result + ",";
            }

            result = result + s;
         }

         sender.sendMessage(UtilFormat.format(this.pn, "itemsRegister", result));
      }
   }

   private void get(Player p, String plugin, String type, boolean force) {
      if (UtilPer.checkPer(p, this.adminPer)) {
         try {
            if (this.addItems(p, p.getInventory(), plugin, type, true, force).isEmpty()) {
               p.sendMessage(UtilFormat.format(this.pn, "itemsGetSuccess", type));
            } else {
               p.sendMessage(UtilFormat.format(this.pn, "itemsGetSuccess2", type));
            }
         } catch (FileNotFoundException var6) {
            p.sendMessage(UtilFormat.format(this.pn, "itemsExsitErr2", type));
         } catch (IOException var7) {
            p.sendMessage(UtilFormat.format(this.pn, "itemsExsitErr3", type));
         } catch (InvalidConfigurationException var8) {
            p.sendMessage(UtilFormat.format(this.pn, "itemsExsitErr4", type));
         } catch (InvalidTypeException var9) {
            p.sendMessage(UtilFormat.format(this.pn, "itemsExsitErr2", type));
         }

      }
   }

   private void open(Player p, boolean newInv) {
      if (UtilPer.checkPer(p, this.adminPer)) {
         String name = p.getName();
         if (!this.invHash.containsKey(name) || newInv) {
            this.invHash.put(name, this.server.createInventory((InventoryHolder)null, this.slot, UtilFormat.format(this.pn, "itemsName", name)));
         }

         Inventory inv = (Inventory)this.invHash.get(name);
         p.openInventory(inv);
      }
   }

   private void save(Player p, String type, boolean force) {
      if (UtilPer.checkPer(p, this.adminPer)) {
         String name = p.getName();
         if (this.invHash.containsKey(name) && UtilItems.getEmptySlots((Inventory)this.invHash.get(name)) != ((Inventory)this.invHash.get(name)).getSize()) {
            String saveFilePath = this.savePath + File.separator + type + ".yml";
            if (!force && (new File(saveFilePath)).exists()) {
               p.sendMessage(UtilFormat.format(this.pn, "itemsExsitErr", type));
            } else {
               YamlConfiguration saveConfig = new YamlConfiguration();
               Inventory inv = (Inventory)this.invHash.get(name);

               for(int i = 0; i < inv.getSize(); ++i) {
                  if (inv.getItem(i) != null && inv.getItem(i).getTypeId() != 0 && inv.getItem(i).getAmount() > 0) {
                     ItemStack is = inv.getItem(i);
                     saveConfig.createSection("items." + i, is.serialize());
                     Attributes a = new Attributes(is);

                     for(int index = 0; index < a.size(); ++index) {
                        Attributes.Attribute at = a.get(index);
                        saveConfig.set("items." + i + ".attributes.attribute" + index + ".amount", at.getAmount());
                        saveConfig.set("items." + i + ".attributes.attribute" + index + ".type", at.getAttributeType().getMinecraftId());
                        saveConfig.set("items." + i + ".attributes.attribute" + index + ".name", at.getName());
                        saveConfig.set("items." + i + ".attributes.attribute" + index + ".operation", at.getOperation().getId());
                        saveConfig.set("items." + i + ".attributes.attribute" + index + ".uuid", at.getUUID().toString());
                     }
                  }
               }

               try {
                  saveConfig.save(saveFilePath);
               } catch (IOException e) {
                  e.printStackTrace();
               }

               p.sendMessage(UtilFormat.format(this.pn, "tip", this.get(160)));
            }
         } else {
            p.sendMessage(UtilFormat.format(this.pn, "fail", this.get(155)));
         }
      }
   }

   private boolean isExsit(String plugin, String type) {
      if (plugin == null) {
         plugin = this.pn;
      }

      return this.itemsHash.containsKey(plugin) && ((HashMap)this.itemsHash.get(plugin)).containsKey(type);
   }

   private void showType(Player p, String plugin, String type, int page) {
      if (UtilPer.checkPer(p, this.infoPer)) {
         if (plugin == null) {
            plugin = this.pn;
         }

         if (this.lastOpenHash.containsKey(p) && System.currentTimeMillis() - (Long)this.lastOpenHash.get(p) < (long)this.showInterval) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", this.get(245)));
         } else if (!this.itemsHash.containsKey(plugin)) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", this.get(200)));
         } else if (!((HashMap)this.itemsHash.get(plugin)).containsKey(type)) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", this.get(225)));
         } else {
            ItemInfo itemInfo = (ItemInfo)((HashMap)this.itemsHash.get(plugin)).get(type);
            ChanceHashList<ItemStack> itemsList = itemInfo.getItemsList();
            int maxPage = itemsList.getMaxPage(54);
            if (page < 1) {
               page = 1;
            } else if (page > maxPage) {
               page = maxPage;
            }

            p.closeInventory();
            String msg = UtilFormat.format(this.pn, "itemsShowType", type, page, maxPage);
            Inventory inv = this.server.createInventory(p, 54, msg.substring(0, Math.min(32, msg.length())));
            int index = 0;

            for(ItemStack is : itemsList.getPage(page, 54)) {
               inv.setItem(index, is);
               ++index;
            }

            p.openInventory(inv);
            this.openList.add(p);
            this.lastOpenHash.put(p, System.currentTimeMillis());
         }
      }
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   public Inventory getInv(String name) {
      return (Inventory)this.invHash.get(name);
   }

   public HashList addItems(CommandSender sender, Inventory inv, String plugin, String type, boolean all, boolean force) throws FileNotFoundException, IOException, InvalidConfigurationException, InvalidTypeException {
      if (plugin == null) {
         plugin = this.pn;
      }

      HashList<HashMap<Integer, ItemStack>> result = new HashListImpl();
      if (!this.isExsit(plugin, type)) {
         throw new InvalidTypeException();
      } else {
         ItemInfo itemInfo = (ItemInfo)((HashMap)this.itemsHash.get(plugin)).get(type);
         ChanceHashList<ItemStack> chanceHashList = itemInfo.getItemsList();
         if (sender != null) {
            sender.sendMessage(UtilFormat.format(this.pn, "itemsGetTip2", type));
         }

         int count = 1;
         int left = chanceHashList.size();
         int leftToMin = itemInfo.getMin();

         for(ItemStack is : chanceHashList) {
            --left;
            if (leftToMin <= left) {
               int chance = chanceHashList.getChance(is);
               String name = UtilNames.getItemName(is.getTypeId(), is.getDurability());
               if (sender != null) {
                  sender.sendMessage(UtilFormat.format(this.pn, "itemsGetTip", name, chance, this.maxChance));
               }

               if (!force && this.r.nextInt(this.maxChance) >= chance) {
                  if (sender != null) {
                     sender.sendMessage(this.get(185));
                  }
                  continue;
               }
            }

            try {
               if (count++ > itemInfo.getMax()) {
                  if (sender != null) {
                     sender.sendMessage(this.get(176));
                  }
                  break;
               }

               --leftToMin;
               HashMap<Integer, ItemStack> tempMap = inv.addItem(new ItemStack[]{is.clone()});
               if (tempMap != null && tempMap.size() > 0) {
                  result.add(tempMap);
                  if (sender != null) {
                     sender.sendMessage(this.get(180));
                  }
               } else if (sender != null) {
                  sender.sendMessage(this.get(175));
               }
            } catch (IllegalArgumentException var17) {
               if (sender != null) {
                  sender.sendMessage(this.get(190));
               }
            }

            if (!all) {
               break;
            }
         }

         return result;
      }
   }

   public HashList getItems(String plugin, String type, boolean all, boolean force) throws InvalidTypeException {
      return this.getItems(plugin, type, all, force, (String)null, (String)null, true, true);
   }

   public HashList getItems(String itemsPlugin, String itemsType, boolean itemsAll, boolean itemsForce, String enchantsPlugin, String enchantsType, boolean enchantsAll, boolean enchantsForce) throws InvalidTypeException {
      if (itemsPlugin == null) {
         itemsPlugin = this.pn;
      }

      if (enchantsPlugin == null) {
         enchantsPlugin = this.pn;
      }

      HashList<ItemStack> result = new HashListImpl();
      if (!this.isExsit(itemsPlugin, itemsType)) {
         throw new InvalidTypeException();
      } else {
         boolean exsitEnchants = this.enchants.isEnchantExsit(enchantsPlugin, enchantsType);
         ItemInfo itemInfo = (ItemInfo)((HashMap)this.itemsHash.get(itemsPlugin)).get(itemsType);
         ChanceHashList<ItemStack> chanceHashList = itemInfo.getItemsList();
         int count = 1;
         int left = chanceHashList.size();
         int leftToMin = itemInfo.getMin();

         for(ItemStack is : chanceHashList) {
            --left;
            int chance = chanceHashList.getChance(is);
            if (leftToMin > left || itemsForce || this.r.nextInt(this.maxChance) < chance) {
               if (count++ > itemInfo.getMax()) {
                  break;
               }

               --leftToMin;
               ItemStack is2 = is.clone();
               if (exsitEnchants) {
                  this.enchants.addEnchant((CommandSender)null, enchantsPlugin, enchantsType, is2, false, enchantsForce, enchantsAll);
               }

               result.add(is2);
               if (!itemsAll) {
                  break;
               }
            }
         }

         return result;
      }
   }

   public boolean dropItems(CommandSender sender, Location l, boolean naturally, String plugin, String type, boolean all, boolean force) {
      try {
         HashList<ItemStack> list = this.getItems(plugin, type, all, force);
         if (list != null && !list.isEmpty()) {
            for(ItemStack is : list) {
               if (naturally) {
                  l.getWorld().dropItemNaturally(l, is.clone());
               } else {
                  l.getWorld().dropItem(l, is.clone());
               }
            }

            if (sender != null) {
               sender.sendMessage(this.get(192));
            }

            return true;
         } else {
            if (sender != null) {
               sender.sendMessage(this.get(185));
            }

            return false;
         }
      } catch (InvalidTypeException e) {
         e.printStackTrace();
         if (sender != null) {
            sender.sendMessage(this.get(190));
         }

         return false;
      }
   }

   public boolean reloadItems(String plugin, YamlConfiguration config) {
      if (plugin == null) {
         plugin = this.pn;
      }

      this.itemsHash.remove(plugin);

      try {
         Map<String, Object> map = ((MemorySection)config.get("items")).getValues(false);
         boolean result = true;

         for(String s : map.keySet()) {
            if (!this.itemsHash.containsKey(plugin) || !((HashMap)this.itemsHash.get(plugin)).containsKey(s)) {
               this.tempHash = new HashListImpl();
               if (this.loadItem(plugin, config, s) == null) {
                  Util.sendConsoleMessage(UtilFormat.format(this.pn, "itemsExsitErr5", s));
                  result = false;
                  break;
               }
            }
         }

         return result;
      } catch (Exception var7) {
         return false;
      }
   }

   public int getSize(String plugin, String type) {
      if (plugin == null) {
         plugin = this.pn;
      }

      return !this.isExsit(plugin, type) ? -1 : ((ItemInfo)((HashMap)this.itemsHash.get(plugin)).get(type)).getItemsList().size();
   }

   public ItemStack getItem(String plugin, String type) {
      try {
         if (plugin == null) {
            plugin = this.pn;
         }

         if (type == null) {
            return null;
         } else {
            return this.isExsit(plugin, type) && this.getSize(plugin, type) != 0 ? ((ItemStack)((ItemInfo)((HashMap)this.itemsHash.get(plugin)).get(type)).getItemsList().getRandom()).clone() : null;
         }
      } catch (Exception var4) {
         return null;
      }
   }

   public CheckResult check(Inventory inv, String plugin, String type) {
      try {
         HashMap<Integer, Integer> idNeedHash = new HashMap();
         HashMap<Integer, Integer> idLackHash = new HashMap();
         HashMap<ItemStack, Integer> itemNeedHash = new HashMap();
         HashMap<ItemStack, Integer> itemLackHash = new HashMap();
         CheckResult checkResult = new CheckResult(idNeedHash, idLackHash, itemNeedHash, itemLackHash);
         if (type != null && !type.trim().isEmpty()) {
            ItemInfo itemInfo = (ItemInfo)((HashMap)this.itemsHash.get(plugin)).get(type);

            for(ItemStack is : this.getItems(plugin, type, true, false)) {
               int mode;
               if (itemInfo.getModeHash().containsKey(is)) {
                  mode = (Integer)itemInfo.getModeHash().get(is);
               } else {
                  mode = this.defaultCheckMode;
               }

               int amount = is.getAmount();
               switch (mode) {
                  case 1:
                     int id = is.getTypeId();
                     int has = UtilItems.getAmount(inv, id);
                     int lack = amount - has;
                     idNeedHash.put(id, amount);
                     if (lack > 0) {
                        idLackHash.put(id, lack);
                     }
                     break;
                  case 2:
                     int has = UtilItems.getAmount(inv, is);
                     int lack = amount - has;
                     itemNeedHash.put(is, amount);
                     if (lack > 0) {
                        itemLackHash.put(is, lack);
                     }
               }
            }
         }

         return checkResult;
      } catch (InvalidTypeException e) {
         e.printStackTrace();
         return null;
      }
   }

   public class ItemInfo {
      private String type;
      private int min = 0;
      private int max = Integer.MAX_VALUE;
      private HashMap modeHash;
      private ChanceHashList itemsList;

      public ItemInfo(String type, HashMap modeHash, ChanceHashList itemsList) {
         this.type = type;
         this.modeHash = modeHash;
         this.itemsList = itemsList;
      }

      public ItemInfo(String type, HashMap modeHash, ChanceHashList itemsList, int min, int max) {
         this.type = type;
         this.modeHash = modeHash;
         this.itemsList = itemsList;
         this.min = min;
         this.max = max;
      }

      public String getType() {
         return this.type;
      }

      public ChanceHashList getItemsList() {
         return this.itemsList;
      }

      public HashMap getModeHash() {
         return this.modeHash;
      }

      public void setModeHash(HashMap modeHash) {
         this.modeHash = modeHash;
      }

      public void setType(String type) {
         this.type = type;
      }

      public void setItemsList(ChanceHashList itemsList) {
         this.itemsList = itemsList;
      }

      public void combine(ItemInfo itemInfo) {
         ChanceHashList<ItemStack> chanceHashList = itemInfo.getItemsList();
         if (chanceHashList != null) {
            this.itemsList.convert((HashList)chanceHashList, false);

            for(ItemStack is : itemInfo.getModeHash().keySet()) {
               this.modeHash.put(is, (Integer)itemInfo.getModeHash().get(is));
            }
         }

      }

      public int getMin() {
         return this.min;
      }

      public void setMin(int min) {
         this.min = min;
      }

      public int getMax() {
         return this.max;
      }

      public void setMax(int max) {
         this.max = max;
      }
   }

   public class CheckResult {
      private HashMap idNeedHash;
      private HashMap idLackHash;
      private HashMap itemNeedHash;
      private HashMap itemLackHash;

      public CheckResult(HashMap idNeedHash, HashMap idLackHash, HashMap itemNeedHash, HashMap itemLackHash) {
         this.idNeedHash = idNeedHash;
         this.idLackHash = idLackHash;
         this.itemNeedHash = itemNeedHash;
         this.itemLackHash = itemLackHash;
      }

      public HashMap getIdLackHash() {
         return this.idLackHash;
      }

      public HashMap getItemLackHash() {
         return this.itemLackHash;
      }

      public HashMap getIdNeedHash() {
         return this.idNeedHash;
      }

      public void setIdNeedHash(HashMap idNeedHash) {
         this.idNeedHash = idNeedHash;
      }

      public HashMap getItemNeedHash() {
         return this.itemNeedHash;
      }

      public void setItemNeedHash(HashMap itemNeedHash) {
         this.itemNeedHash = itemNeedHash;
      }

      public void setIdLackHash(HashMap idLackHash) {
         this.idLackHash = idLackHash;
      }

      public void setItemLackHash(HashMap itemLackHash) {
         this.itemLackHash = itemLackHash;
      }

      public boolean isSuccess() {
         return this.idLackHash.isEmpty() && this.itemLackHash.isEmpty();
      }
   }
}
