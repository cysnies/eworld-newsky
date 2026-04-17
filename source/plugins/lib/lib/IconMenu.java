package lib;

import java.util.HashMap;
import java.util.List;
import lib.config.ReloadConfigEvent;
import lib.util.Util;
import lib.util.UtilSpeed;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitScheduler;

public class IconMenu implements Listener {
   private static ItemMeta IM = (new ItemStack(1)).getItemMeta();
   private int id = 1;
   private Lib lib;
   private Server server;
   private BukkitScheduler scheduler;
   private String pn;
   private String speedName;
   private int interval;
   private String tipSession;
   private int sessionSize;
   private ItemStack timeIcon;
   private ItemStack tipIcon;
   private ItemStack yesIcon;
   private ItemStack noIcon;
   private ItemStack cancelIcon;
   private int timePos;
   private int tipPos;
   private int yesPos;
   private int noPos;
   private int cancelPos;
   private HashMap infoHash;
   private HashMap playerHash;

   public IconMenu(Lib lib) {
      this.lib = lib;
      this.server = lib.getServer();
      this.scheduler = this.server.getScheduler();
      this.pn = lib.getPn();
      this.infoHash = new HashMap();
      this.playerHash = new HashMap();
      this.loadConfig(lib.getCon().getConfig(this.pn));
      lib.getPm().registerEvents(this, lib);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onInventoryClick(InventoryClickEvent event) {
      if (event.getWhoClicked() instanceof Player) {
         Player p = (Player)event.getWhoClicked();
         Info info = (Info)this.playerHash.get(p);
         if (info != null) {
            event.setCancelled(true);
            ItemStack is = event.getCurrentItem();
            ItemStack is2 = event.getCursor();
            if ((is != null && is.getTypeId() != 0 || is2 != null && is2.getTypeId() != 0) && !UtilSpeed.check(p, this.pn, this.speedName, this.interval)) {
               return;
            }

            InventoryClickCheck icc = new InventoryClickCheck(event, info, p);
            this.scheduler.scheduleSyncDelayedTask(this.lib, icc);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onInventoryClose(InventoryCloseEvent event) {
      if (event.getPlayer() instanceof Player) {
         Player p = (Player)event.getPlayer();
         Info info = (Info)this.playerHash.get(p);
         if (info != null) {
            info.removePlayer(p);
            this.playerHash.remove(p);
         }
      }

   }

   public Info register(String name, int size, boolean emptyDestroy, OptionClickEventHandler handler) {
      int id = this.getNextId();
      Info info = new Info(id, name, size, emptyDestroy, handler);
      this.infoHash.put(id, info);
      return info;
   }

   public void unregister(Info info) {
      info.closeAll();
      this.infoHash.remove(info.getId());
   }

   public void open(Player p, Info info, String title, Inventory handle) {
      p.closeInventory();
      this.playerHash.put(p, info);
      info.open(p, title, handle);
   }

   public Info getInfo(int id) {
      return (Info)this.infoHash.get(id);
   }

   public void openSession(Player p, String name, List lore, Session session, int timeLimit) {
      this.scheduler.scheduleSyncDelayedTask(this.lib, new OpenSession(p, name, lore, session, timeLimit));
   }

   private void loadConfig(FileConfiguration config) {
      this.speedName = config.getString("icon.speedName");
      this.interval = config.getInt("icon.interval");
      this.tipSession = Util.convert(config.getString("icon.session.tip"));
      this.sessionSize = config.getInt("icon.session.size");
      this.timePos = config.getInt("icon.session.timeIcon.pos");
      String[] temp = config.getString("icon.session.timeIcon.item").split(":");
      int id;
      int smallId;
      if (temp.length == 2) {
         id = Integer.parseInt(temp[0]);
         smallId = Integer.parseInt(temp[1]);
      } else {
         id = Integer.parseInt(temp[0]);
         smallId = 0;
      }

      String name = Util.convert(config.getString("icon.session.timeIcon.name"));
      List<String> lore = config.getStringList("icon.session.timeIcon.lore");

      for(int i = 0; i < lore.size(); ++i) {
         lore.set(i, Util.convert((String)lore.get(i)));
      }

      this.timeIcon = new ItemStack(id, 1, (short)smallId);
      ItemMeta im = IM.clone();
      im.setDisplayName(name);
      im.setLore(lore);
      this.timeIcon.setItemMeta(im);
      this.tipPos = config.getInt("icon.session.tipIcon.pos");
      temp = config.getString("icon.session.tipIcon.item").split(":");
      if (temp.length == 2) {
         id = Integer.parseInt(temp[0]);
         smallId = Integer.parseInt(temp[1]);
      } else {
         id = Integer.parseInt(temp[0]);
         smallId = 0;
      }

      this.tipIcon = new ItemStack(id, 1, (short)smallId);
      this.yesPos = config.getInt("icon.session.yesIcon.pos");
      temp = config.getString("icon.session.yesIcon.item").split(":");
      if (temp.length == 2) {
         id = Integer.parseInt(temp[0]);
         smallId = Integer.parseInt(temp[1]);
      } else {
         id = Integer.parseInt(temp[0]);
         smallId = 0;
      }

      name = Util.convert(config.getString("icon.session.yesIcon.name"));
      lore = config.getStringList("icon.session.yesIcon.lore");

      for(int i = 0; i < lore.size(); ++i) {
         lore.set(i, Util.convert((String)lore.get(i)));
      }

      this.yesIcon = new ItemStack(id, 1, (short)smallId);
      im = IM.clone();
      im.setDisplayName(name);
      im.setLore(lore);
      this.yesIcon.setItemMeta(im);
      this.noPos = config.getInt("icon.session.noIcon.pos");
      temp = config.getString("icon.session.noIcon.item").split(":");
      if (temp.length == 2) {
         id = Integer.parseInt(temp[0]);
         smallId = Integer.parseInt(temp[1]);
      } else {
         id = Integer.parseInt(temp[0]);
         smallId = 0;
      }

      name = Util.convert(config.getString("icon.session.noIcon.name"));
      lore = config.getStringList("icon.session.noIcon.lore");

      for(int i = 0; i < lore.size(); ++i) {
         lore.set(i, Util.convert((String)lore.get(i)));
      }

      this.noIcon = new ItemStack(id, 1, (short)smallId);
      im = IM.clone();
      im.setDisplayName(name);
      im.setLore(lore);
      this.noIcon.setItemMeta(im);
      this.cancelPos = config.getInt("icon.session.cancelIcon.pos");
      temp = config.getString("icon.session.cancelIcon.item").split(":");
      if (temp.length == 2) {
         id = Integer.parseInt(temp[0]);
         smallId = Integer.parseInt(temp[1]);
      } else {
         id = Integer.parseInt(temp[0]);
         smallId = 0;
      }

      name = Util.convert(config.getString("icon.session.cancelIcon.name"));
      lore = config.getStringList("icon.session.cancelIcon.lore");

      for(int i = 0; i < lore.size(); ++i) {
         lore.set(i, Util.convert((String)lore.get(i)));
      }

      this.cancelIcon = new ItemStack(id, 1, (short)smallId);
      im = IM.clone();
      im.setDisplayName(name);
      im.setLore(lore);
      this.cancelIcon.setItemMeta(im);
      this.lib.getSpeed().register(this.pn, this.speedName);
   }

   private int getNextId() {
      return this.id++;
   }

   public class OptionClickEvent {
      private Player p;
      private Info info;
      private int pos;
      private boolean close;
      private boolean left;
      private boolean right;
      private boolean shift;

      public OptionClickEvent(Player p, Info info, int pos, boolean left, boolean right, boolean shift) {
         this.p = p;
         this.info = info;
         this.pos = pos;
         this.left = left;
         this.right = right;
         this.shift = shift;
      }

      public Player getP() {
         return this.p;
      }

      public Info getInfo() {
         return this.info;
      }

      public int getPos() {
         return this.pos;
      }

      public boolean isWillClose() {
         return this.close;
      }

      public void setWillClose(boolean close) {
         this.close = close;
      }

      public boolean isLeft() {
         return this.left;
      }

      public boolean isRight() {
         return this.right;
      }

      public boolean isShift() {
         return this.shift;
      }
   }

   public class Info {
      private int id;
      private String name;
      private Inventory inv;
      private int emptySlots;
      private OptionClickEventHandler handler;
      private boolean emptyDestroy;
      private HashMap invHash;

      public Info(int id, String name, int size, boolean emptyDestroy, OptionClickEventHandler handler) {
         this.id = id;
         this.name = name;
         String title = name.substring(0, Math.min(name.length(), 32));
         this.inv = IconMenu.this.server.createInventory((InventoryHolder)null, size, title);
         this.emptySlots = size;
         this.emptyDestroy = emptyDestroy;
         this.handler = handler;
         this.invHash = new HashMap();
      }

      public int getId() {
         return this.id;
      }

      public String getName() {
         return this.name;
      }

      public Inventory getInv() {
         return this.inv;
      }

      public Inventory getInv(Player p) {
         return (Inventory)this.invHash.get(p);
      }

      public int getEmptySlots() {
         return this.emptySlots;
      }

      public void setItem(int slot, ItemStack is) {
         if (this.inv.getItem(slot) != null && this.inv.getItem(slot).getTypeId() != 0) {
            if (is == null) {
               --this.emptySlots;
            }
         } else if (is != null) {
            ++this.emptySlots;
         }

         this.inv.setItem(slot, is);

         for(Player p : this.invHash.keySet()) {
            Inventory inv = (Inventory)this.invHash.get(p);
            inv.setItem(slot, is);
         }

      }

      public void update() {
         for(Player p : this.invHash.keySet()) {
            try {
               p.updateInventory();
            } catch (Exception var4) {
            }
         }

      }

      public void closeAll() {
         for(Player p : this.invHash.keySet()) {
            p.closeInventory();
         }

      }

      public boolean isEmptyDestroy() {
         return this.emptyDestroy;
      }

      public void setEmptyDestroy(boolean emptyDestroy) {
         this.emptyDestroy = emptyDestroy;
      }

      public boolean equals(Object obj) {
         return ((Info)obj).getId() == this.id;
      }

      private OptionClickEventHandler getHandler() {
         return this.handler;
      }

      private void removePlayer(Player p) {
         this.invHash.remove(p);
         if (this.emptyDestroy && this.invHash.isEmpty()) {
            IconMenu.this.unregister(this);
         }

      }

      private void open(Player p, String title, Inventory handle) {
         int size = this.inv.getSize();
         if (handle != null) {
            size += 9;
         }

         if (title == null) {
            title = this.inv.getTitle();
         }

         Inventory result = IconMenu.this.server.createInventory(p, size, title);
         this.invHash.put(p, result);
         if (handle != null) {
            for(int i = 0; i < size - 9; ++i) {
               result.setItem(i, this.inv.getItem(i));
            }

            for(int i = 0; i < 9; ++i) {
               result.setItem(size - 9 + i, handle.getItem(i));
            }
         } else {
            for(int i = 0; i < size; ++i) {
               result.setItem(i, this.inv.getItem(i));
            }
         }

         p.openInventory(result);
      }
   }

   private class OpenSession implements Runnable, OptionClickEventHandler {
      private static final int TIME_LIMIT = 64;
      private Player p;
      private String name;
      private List lore;
      private Session session;
      private int timeLimit;
      private Info info;
      private int taskId;

      public OpenSession(Player p, String name, List lore, Session session, int timeLimit) {
         if (timeLimit > 64) {
            timeLimit = 64;
         }

         this.p = p;
         this.name = name;
         this.lore = lore;
         this.session = session;
         this.timeLimit = timeLimit;
      }

      public void run() {
         if (this.p.isOnline()) {
            this.p.closeInventory();
            ItemStack tipItem = IconMenu.this.tipIcon.clone();
            ItemMeta im = tipItem.getItemMeta();
            im.setDisplayName(this.name);
            if (this.lore != null && this.lore.size() > 0) {
               im.setLore(this.lore);
            } else {
               im.setLore((List)null);
            }

            tipItem.setItemMeta(im);
            ItemStack yesItem = IconMenu.this.yesIcon.clone();
            ItemStack noItem = IconMenu.this.noIcon.clone();
            ItemStack cancelItem = IconMenu.this.cancelIcon.clone();
            Info info = IconMenu.this.register(IconMenu.this.tipSession, IconMenu.this.sessionSize, true, this);
            this.info = info;
            info.setItem(IconMenu.this.tipPos, tipItem);
            info.setItem(IconMenu.this.yesPos, yesItem);
            info.setItem(IconMenu.this.noPos, noItem);
            info.setItem(IconMenu.this.cancelPos, cancelItem);
            if (this.timeLimit > 0) {
               ItemStack timeItem = IconMenu.this.timeIcon.clone();
               timeItem.setAmount(this.timeLimit);
               info.setItem(IconMenu.this.timePos, timeItem);
               Timer timer = new Timer();
               this.taskId = IconMenu.this.scheduler.scheduleSyncRepeatingTask(IconMenu.this.lib, timer, 20L, 20L);
            }

            IconMenu.this.open(this.p, info, (String)null, (Inventory)null);
         }

      }

      public void onOptionClick(OptionClickEvent e) {
         e.setWillClose(true);
         if (e.getPos() == IconMenu.this.yesPos) {
            this.session.onSelect(IconMenu.Session.Result.YES);
         } else if (e.getPos() == IconMenu.this.noPos) {
            this.session.onSelect(IconMenu.Session.Result.NO);
         } else if (e.getPos() == IconMenu.this.cancelPos) {
            this.session.onSelect(IconMenu.Session.Result.CANCEL);
         } else {
            e.setWillClose(false);
         }

      }

      class Timer implements Runnable {
         public void run() {
            if (IconMenu.this.playerHash.containsKey(OpenSession.this.p) && ((Info)IconMenu.this.playerHash.get(OpenSession.this.p)).equals(OpenSession.this.info)) {
               OpenSession var10000 = OpenSession.this;
               var10000.timeLimit = var10000.timeLimit - 1;
               if (OpenSession.this.timeLimit <= 0) {
                  try {
                     IconMenu.this.scheduler.cancelTask(OpenSession.this.taskId);
                  } catch (Exception var4) {
                  }

                  OpenSession.this.session.onSelect(IconMenu.Session.Result.CANCEL);
                  OpenSession.this.p.closeInventory();
               } else {
                  try {
                     Inventory inv = OpenSession.this.info.getInv(OpenSession.this.p);
                     ItemStack result = inv.getItem(IconMenu.this.timePos);
                     result.setAmount(result.getAmount() - 1);
                     OpenSession.this.info.setItem(IconMenu.this.timePos, result);
                     OpenSession.this.info.update();
                  } catch (Exception var3) {
                  }
               }
            } else {
               IconMenu.this.scheduler.cancelTask(OpenSession.this.taskId);
            }

         }
      }
   }

   private class InventoryClickCheck implements Runnable {
      private InventoryClickEvent e;
      private Info info;
      private Player p;

      public InventoryClickCheck(InventoryClickEvent e, Info info, Player p) {
         this.e = e;
         this.info = info;
         this.p = p;
      }

      public void run() {
         if (this.p.isOnline()) {
            int slot = this.e.getRawSlot();
            Inventory inv = this.info.getInv(this.p);
            if (inv != null) {
               int size = inv.getSize();
               if (slot >= 0 && slot < size) {
                  ItemStack result = inv.getItem(slot);
                  if (result != null && result.getTypeId() != 0) {
                     OptionClickEvent e = IconMenu.this.new OptionClickEvent(this.p, this.info, slot, this.e.isLeftClick(), this.e.isRightClick(), this.e.isShiftClick());
                     this.info.getHandler().onOptionClick(e);
                     if (e.isWillClose()) {
                        Info currentSee = (Info)IconMenu.this.playerHash.get(this.p);
                        if (currentSee != null && currentSee.equals(this.info)) {
                           this.p.closeInventory();
                        }
                     }
                  }
               }

            }
         }
      }
   }

   public interface OptionClickEventHandler {
      void onOptionClick(OptionClickEvent var1);
   }

   public interface Session {
      void onSelect(Result var1);

      public static enum Result {
         YES,
         NO,
         CANCEL;
      }
   }
}
