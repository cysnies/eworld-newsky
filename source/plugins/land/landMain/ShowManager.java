package landMain;

import event.LandCreateEvent;
import event.LandRemoveEvent;
import event.NameChangeEvent;
import event.OwnerChangeEvent;
import event.SellChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import land.Land;
import land.LandUser;
import land.Range;
import landHandler.AdminHandler;
import landHandler.FlagHandler;
import landHandler.InfoHandler;
import landHandler.PersHandler;
import lib.IconMenu;
import lib.InputManager;
import lib.Lib;
import lib.IconMenu.Session.Result;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilIconMenu;
import lib.util.UtilItems;
import lib.util.UtilNames;
import lib.util.UtilPer;
import lib.util.UtilSpeed;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ShowManager implements Listener {
   private static final int SIZE = 45;
   private static final String SHOW_SPEED = "show";
   private static final ItemMeta IM = (new ItemStack(1)).getItemMeta();
   private LandManager landManager;
   private String pn;
   private HashMap landShowHash1;
   private HashMap landShowHash2;
   private HashMap landShowHash3;
   private HashList landShowList1;
   private HashList landShowList2;
   private HashList landShowList3;
   private HashList sellLandList;
   private HashMap player1Hash;
   private HashMap player2Hash;
   private HashMap player3Hash;
   private HashMap flagShowHash;
   private HashList flagShowListPlayer;
   private HashList flagShowListOp;
   private String per_land_admin;
   private String fixPer;
   private String fixName;
   private List fixLore;
   private String deleteName;
   private List deleteLore;
   private String sellName;
   private List sellLore;
   private String buyName;
   private List buyLore;
   private String addAllName;
   private List addAllLore;
   private String delAllName;
   private List delAllLore;
   private String removeFlagName;
   private List removeFlagLore;
   private String showMainMenuPer;
   private String showSeeAllPer;
   private int flagNameCheckFrom;
   private int confirmTimeLimit;
   private int showInterval;
   private int lineMaxLength;
   private int item;
   private String displayName;
   private String checkFrom;
   private String checkTo;
   private int flagItem;
   private String flagDisplayName;
   private HashMap mainMenuHash;
   private HashMap seeNowHash;
   private HashMap landHash;
   private HashMap selHash;
   private HashMap setFlagHash;
   private HashMap selFlagHash;
   private HashMap handleFlagHash;
   private String checkPer;
   private List checkList;
   private HashList list1 = new HashListImpl();

   public ShowManager(LandMain landMain) {
      this.list1.add("setLevel");
      this.list1.add("toggleOverlap");
      this.list1.add("fix");
      this.list1.add("setOwner");
      this.landManager = LandMain.getLandManager();
      this.pn = landMain.getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      this.landManager.registerEvents(this);
      UtilSpeed.register(this.pn, "show");
      this.loadData();
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
      priority = EventPriority.LOWEST,
      ignoreCancelled = false
   )
   public void onLandCreate(LandCreateEvent e) {
      Land land = e.getLand();
      ItemStack is = this.getShowItem(land);
      switch (land.getType()) {
         case 1:
            this.landShowHash1.put(land.getId(), is);
            this.landShowList1.add(is);
            if (!this.player1Hash.containsKey(land.getOwner())) {
               this.player1Hash.put(land.getOwner(), new HashListImpl());
            }

            ((HashList)this.player1Hash.get(land.getOwner())).add(land.getId());
            break;
         case 2:
            this.landShowHash2.put(land.getId(), is);
            this.landShowList2.add(is);
            if (!this.player2Hash.containsKey(land.getOwner())) {
               this.player2Hash.put(land.getOwner(), new HashListImpl());
            }

            ((HashList)this.player2Hash.get(land.getOwner())).add(land.getId());
            break;
         case 3:
            this.landShowHash3.put(land.getId(), is);
            this.landShowList3.add(is);
            if (!this.player3Hash.containsKey(land.getOwner())) {
               this.player3Hash.put(land.getOwner(), new HashListImpl());
            }

            ((HashList)this.player3Hash.get(land.getOwner())).add(land.getId());
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = false
   )
   public void onNameChange(NameChangeEvent e) {
      Land land = e.getLand();
      ItemStack is = this.getShowItem(land);
      switch (land.getType()) {
         case 1:
            ItemStack old = (ItemStack)this.landShowHash1.put(land.getId(), is);
            if (old != null) {
               this.landShowList1.remove(old);
            }

            this.landShowList1.add(is);
            break;
         case 2:
            ItemStack old = (ItemStack)this.landShowHash2.put(land.getId(), is);
            if (old != null) {
               this.landShowList2.remove(old);
            }

            this.landShowList2.add(is);
            break;
         case 3:
            ItemStack old = (ItemStack)this.landShowHash3.put(land.getId(), is);
            if (old != null) {
               this.landShowList3.remove(old);
            }

            this.landShowList3.add(is);
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onOwnerChange(OwnerChangeEvent e) {
      Land land = e.getLand();
      switch (land.getType()) {
         case 1:
            ((HashList)this.player1Hash.get(e.getOldOwner())).remove(land.getId());
            if (!this.player1Hash.containsKey(land.getOwner())) {
               this.player1Hash.put(land.getOwner(), new HashListImpl());
            }

            ((HashList)this.player1Hash.get(land.getOwner())).add(land.getId());
            break;
         case 2:
            ((HashList)this.player2Hash.get(e.getOldOwner())).remove(land.getId());
            if (!this.player2Hash.containsKey(land.getOwner())) {
               this.player2Hash.put(land.getOwner(), new HashListImpl());
            }

            ((HashList)this.player2Hash.get(land.getOwner())).add(land.getId());
            break;
         case 3:
            ((HashList)this.player3Hash.get(e.getOldOwner())).remove(land.getId());
            if (!this.player3Hash.containsKey(land.getOwner())) {
               this.player3Hash.put(land.getOwner(), new HashListImpl());
            }

            ((HashList)this.player3Hash.get(land.getOwner())).add(land.getId());
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onSellChange(SellChangeEvent e) {
      Land land = e.getLand();
      if (land.getPrice() >= 0) {
         this.sellLandList.add(land.getId());
      } else {
         this.sellLandList.remove(land.getId());
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = false
   )
   public void onLandRemove(LandRemoveEvent e) {
      Land land = e.getRemovedLand();
      switch (land.getType()) {
         case 1:
            ItemStack is = (ItemStack)this.landShowHash1.remove(land.getId());
            this.landShowList1.remove(is);
            ((HashList)this.player1Hash.get(land.getOwner())).remove(land.getId());
            break;
         case 2:
            ItemStack is = (ItemStack)this.landShowHash2.remove(land.getId());
            this.landShowList2.remove(is);
            ((HashList)this.player2Hash.get(land.getOwner())).remove(land.getId());
            break;
         case 3:
            ItemStack is = (ItemStack)this.landShowHash3.remove(land.getId());
            this.landShowList3.remove(is);
            ((HashList)this.player3Hash.get(land.getOwner())).remove(land.getId());
            if (land.getPrice() >= 0) {
               this.sellLandList.remove(land.getId());
            }
      }

   }

   public String getFriendPer(boolean friendPer) {
      return friendPer ? this.get(1005) : this.get(1010);
   }

   public String getType(int type) {
      return this.get(999 + type);
   }

   public boolean showMainMenu(Player p) {
      if (!UtilPer.checkPer(p, this.showMainMenuPer)) {
         return false;
      } else if (!UtilSpeed.check(p, this.pn, "show", this.showInterval)) {
         return false;
      } else {
         String name = UtilFormat.format(this.pn, 1115);
         IconMenu.OptionClickEventHandler handler = new ShowMainMenu(p);
         IconMenu.Info info = UtilIconMenu.register(name, 45, true, handler);

         for(ShowInfo showInfo : this.mainMenuHash.values()) {
            int pos = showInfo.getPos();
            String type = showInfo.getType();
            String name2 = showInfo.getName();
            ItemStack is = UtilItems.getItem(this.pn, type);
            if (p.isOp() || !name2.equals("info") && !name2.equals("seeAll1") && !name2.equals("seeYou1")) {
               if (name2.equals("info")) {
                  is = is.clone();
                  LandUser landUser = (LandUser)this.landManager.getLandUserHandler().getUserHash().get(p.getName());
                  int current = 0;

                  try {
                     HashList<Land> landList = this.landManager.getUserLands(p.getName());
                     if (landList != null) {
                        for(Land land : landList) {
                           if (land.getType() != 3) {
                              ++current;
                           }
                        }
                     }
                  } catch (Exception var16) {
                  }

                  int max = landUser.getMaxLands();
                  ItemMeta im = is.getItemMeta();
                  List<String> lore = im.getLore();
                  lore.set(0, ((String)lore.get(0)).replace("{0}", String.valueOf(current)));
                  lore.set(1, ((String)lore.get(1)).replace("{1}", String.valueOf(max)));
                  im.setLore(lore);
                  is.setItemMeta(im);
               }

               info.setItem(pos, is);
            }
         }

         Inventory inv = Bukkit.getServer().createInventory((InventoryHolder)null, 9, "");
         inv.setItem(4, UtilItems.getItem(this.pn, "toSel"));
         UtilIconMenu.open(p, info, (String)null, inv);
         return true;
      }
   }

   public boolean seeNow(Player p) {
      if (!UtilPer.checkPer(p, this.landManager.getInfoHandler().getPerInfoNow())) {
         return false;
      } else if (!UtilSpeed.check(p, this.pn, "show", this.showInterval)) {
         return false;
      } else {
         String name = UtilFormat.format(this.pn, 1120);
         IconMenu.OptionClickEventHandler handler = new ShowSeeNow(p);
         IconMenu.Info info = UtilIconMenu.register(name, 45, true, handler);
         Location l = p.getLocation();
         Land highestLand = this.landManager.getHighestPriorityLand(l);
         if (highestLand != null) {
            ItemStack highestItem = this.getExsitShowItem(highestLand).clone();
            ItemMeta im = highestItem.getItemMeta();
            List<String> lore = new ArrayList();
            lore.add(UtilFormat.format(this.pn, "landShow3", new Object[]{highestLand.getLevel()}));
            lore.add(this.get(1125));
            im.setLore(lore);
            highestItem.setItemMeta(im);
            info.setItem(0, highestItem);
            HashList<Land> list = this.landManager.getLands(l);
            int index = 1;

            for(Land land : list) {
               if (index > 44) {
                  break;
               }

               if (!land.equals(highestLand)) {
                  ItemStack is = this.getExsitShowItem(land);
                  info.setItem(index, is);
                  ++index;
               }
            }
         }

         Inventory inv = Bukkit.getServer().createInventory((InventoryHolder)null, 9, "");

         for(ShowInfo showInfo : this.seeNowHash.values()) {
            int pos = showInfo.getPos();
            String type = showInfo.getType();
            ItemStack is = UtilItems.getItem(this.pn, type);
            inv.setItem(pos, is);
         }

         UtilIconMenu.open(p, info, (String)null, inv);
         return true;
      }
   }

   public boolean seeAll(int type, Player p, int page) {
      if (!UtilPer.checkPer(p, this.showSeeAllPer)) {
         return false;
      } else if (!UtilSpeed.check(p, this.pn, "show", this.showInterval)) {
         return false;
      } else {
         HashMap<Long, ItemStack> hash;
         HashList<ItemStack> list0;
         String s;
         if (type == 1) {
            hash = this.landShowHash1;
            list0 = this.landShowList1;
            s = this.get(1145);
         } else if (type == 2) {
            hash = this.landShowHash2;
            list0 = this.landShowList2;
            s = this.get(1150);
         } else {
            hash = this.landShowHash3;
            list0 = this.landShowList3;
            s = this.get(1155);
         }

         if (hash.isEmpty()) {
            p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1134 + type)}));
            return false;
         } else {
            int maxPage = list0.getMaxPage(45);
            if (page == -1000) {
               page = maxPage;
            }

            if (page >= 1 && page <= maxPage) {
               String name = UtilFormat.format(this.pn, "landShow4", new Object[]{s, this.get(1130)});
               IconMenu.OptionClickEventHandler handler = new ShowSee(type, p, (String)null, page, 1);
               IconMenu.Info info = UtilIconMenu.register(name, 45, true, handler);
               List<ItemStack> list = list0.getPage(page, 45);
               int index = 0;

               for(ItemStack is : list) {
                  info.setItem(index, is);
                  ++index;
               }

               Inventory inv = Bukkit.getServer().createInventory((InventoryHolder)null, 9, "");
               ItemStack is = UtilItems.getItem(this.pn, "see_first").clone();
               ItemMeta im = is.getItemMeta();
               List<String> lore = im.getLore();
               lore.set(0, ((String)lore.get(0)).replace("{0}", String.valueOf(page)));
               lore.set(1, ((String)lore.get(1)).replace("{1}", String.valueOf(maxPage)));
               im.setLore(lore);
               is.setItemMeta(im);
               inv.setItem(0, is);
               if (page - 10 >= 1) {
                  is = UtilItems.getItem(this.pn, "see_pre10");
                  inv.setItem(1, is);
               }

               if (page - 5 >= 1) {
                  is = UtilItems.getItem(this.pn, "see_pre5");
                  inv.setItem(2, is);
               }

               if (page - 1 >= 1) {
                  is = UtilItems.getItem(this.pn, "see_pre1");
                  inv.setItem(3, is);
               }

               is = UtilItems.getItem(this.pn, "see_main");
               inv.setItem(4, is);
               if (page + 1 <= maxPage) {
                  is = UtilItems.getItem(this.pn, "see_next1");
                  inv.setItem(5, is);
               }

               if (page + 5 <= maxPage) {
                  is = UtilItems.getItem(this.pn, "see_next5");
                  inv.setItem(6, is);
               }

               if (page + 10 <= maxPage) {
                  is = UtilItems.getItem(this.pn, "see_next10");
                  inv.setItem(7, is);
               }

               if (page < maxPage) {
                  is = UtilItems.getItem(this.pn, "see_end");
                  inv.setItem(8, is);
               }

               UtilIconMenu.open(p, info, (String)null, inv);
               return true;
            } else {
               p.sendMessage(UtilFormat.format(this.pn, "landErr2", new Object[]{maxPage}));
               return false;
            }
         }
      }
   }

   public boolean seeYou(int type, Player p, String tar, int page, int sell) {
      if (sell == 1 && !UtilPer.checkPer(p, this.showSeeAllPer)) {
         return false;
      } else if (!UtilSpeed.check(p, this.pn, "show", this.showInterval)) {
         return false;
      } else {
         HashMap<Long, ItemStack> hash;
         HashList<Long> list;
         String s;
         if (sell == 1) {
            hash = this.landShowHash3;
            list = this.sellLandList;
            s = this.get(1160);
         } else if (sell == 2) {
            hash = this.landShowHash3;
            list = new HashListImpl();

            for(long id : this.sellLandList) {
               Land land = this.landManager.getLand(id);
               if (land != null && land.getOwner().equals(tar)) {
                  list.add(id);
               }
            }

            s = this.get(1160);
         } else if (type == 1) {
            hash = this.landShowHash1;
            list = (HashList)this.player1Hash.get(tar);
            s = this.get(1145);
         } else if (type == 2) {
            hash = this.landShowHash2;
            list = (HashList)this.player2Hash.get(tar);
            s = this.get(1150);
         } else {
            hash = this.landShowHash3;
            list = (HashList)this.player3Hash.get(tar);
            s = this.get(1155);
         }

         if (list != null && !list.isEmpty()) {
            int maxPage = list.getMaxPage(45);
            if (page == -1000) {
               page = maxPage;
            }

            if (page >= 1 && page <= maxPage) {
               String name = UtilFormat.format(this.pn, "landShow4", new Object[]{s, tar});
               int i;
               if (sell != 0) {
                  i = 3;
               } else {
                  i = 2;
               }

               IconMenu.OptionClickEventHandler handler = new ShowSee(type, p, tar, page, i);
               IconMenu.Info info = UtilIconMenu.register(name, 45, true, handler);
               List<Long> list2 = list.getPage(page, 45);
               int index = 0;

               for(long id : list2) {
                  ItemStack is = (ItemStack)hash.get(id);
                  info.setItem(index, is);
                  ++index;
               }

               Inventory inv = Bukkit.getServer().createInventory((InventoryHolder)null, 9, "");
               ItemStack is = UtilItems.getItem(this.pn, "see_first").clone();
               ItemMeta im = is.getItemMeta();
               List<String> lore = im.getLore();
               lore.set(0, ((String)lore.get(0)).replace("{0}", String.valueOf(page)));
               lore.set(1, ((String)lore.get(1)).replace("{1}", String.valueOf(maxPage)));
               im.setLore(lore);
               is.setItemMeta(im);
               inv.setItem(0, is);
               if (page - 10 >= 1) {
                  is = UtilItems.getItem(this.pn, "see_pre10");
                  inv.setItem(1, is);
               }

               if (page - 5 >= 1) {
                  is = UtilItems.getItem(this.pn, "see_pre5");
                  inv.setItem(2, is);
               }

               if (page - 1 >= 1) {
                  is = UtilItems.getItem(this.pn, "see_pre1");
                  inv.setItem(3, is);
               }

               is = UtilItems.getItem(this.pn, "see_main");
               inv.setItem(4, is);
               if (page + 1 <= maxPage) {
                  is = UtilItems.getItem(this.pn, "see_next1");
                  inv.setItem(5, is);
               }

               if (page + 5 <= maxPage) {
                  is = UtilItems.getItem(this.pn, "see_next5");
                  inv.setItem(6, is);
               }

               if (page + 10 <= maxPage) {
                  is = UtilItems.getItem(this.pn, "see_next10");
                  inv.setItem(7, is);
               }

               if (page < maxPage) {
                  is = UtilItems.getItem(this.pn, "see_end");
                  inv.setItem(8, is);
               }

               UtilIconMenu.open(p, info, (String)null, inv);
               return true;
            } else {
               p.sendMessage(UtilFormat.format(this.pn, "landErr2", new Object[]{maxPage}));
               return false;
            }
         } else {
            if (sell == 1) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1165)}));
            } else if (sell == 2) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1167)}));
            } else {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1139 + type)}));
            }

            return false;
         }
      }
   }

   public boolean showLandInfo(Player p, String s) {
      if (!UtilPer.checkPer(p, this.landManager.getInfoHandler().getPerInfoLand())) {
         return true;
      } else if (!UtilSpeed.check(p, this.pn, InfoHandler.getInfo(), this.landManager.getInfoHandler().getInterval())) {
         return false;
      } else {
         Land land = this.landManager.getLand(p, s);
         if (land == null) {
            return false;
         } else {
            this.showLandInfo(p, land);
            return true;
         }
      }
   }

   public boolean showSetFlag(Player p, long landId, int page) {
      if (!UtilPer.checkPer(p, this.landManager.getFlagHandler().getPer())) {
         return false;
      } else {
         Land land = this.landManager.getLand(p, String.valueOf(landId));
         if (land == null) {
            return false;
         } else {
            HashMap<String, Integer> flagHash = land.getFlags();
            int maxPage = (flagHash.size() - 1) / 36 + 1;
            if (flagHash.size() == 0) {
               maxPage = 1;
            }

            if (page >= 1 && page <= maxPage) {
               String name = UtilFormat.format(this.pn, 1295);
               IconMenu.OptionClickEventHandler handler = new ShowSetFlag(p, landId, page);
               IconMenu.Info info = UtilIconMenu.register(name, 45, true, handler);
               info.setItem(4, this.getShowItemDetail(land));
               Iterator<String> it = flagHash.keySet().iterator();
               int from = 0;
               int tar = 36 * (page - 1);
               int counter = 8;

               while(it.hasNext()) {
                  String flagName = (String)it.next();
                  ++from;
                  if (from > tar) {
                     ++counter;
                     if (counter > 44) {
                        break;
                     }

                     FlagHandler.Flag flag = (FlagHandler.Flag)this.landManager.getFlagHandler().getFlagHash().get(flagName);
                     if (flag != null) {
                        ItemStack is = this.getFlagShowItem(flag);
                        info.setItem(counter, is);
                     }
                  }
               }

               Inventory inv = Bukkit.getServer().createInventory((InventoryHolder)null, 9, "");
               inv.setItem(0, UtilItems.getItem(this.pn, "setFlag_add"));
               inv.setItem(1, UtilItems.getItem(this.pn, "setFlag_tip"));
               inv.setItem(4, UtilItems.getItem(this.pn, "setFlag_back"));
               if (page > 1) {
                  inv.setItem(3, UtilItems.getItem(this.pn, "setFlag_pre"));
               }

               if (page < maxPage) {
                  inv.setItem(5, UtilItems.getItem(this.pn, "setFlag_next"));
               }

               UtilIconMenu.open(p, info, (String)null, inv);
               return true;
            } else {
               p.sendMessage(UtilFormat.format(this.pn, "landShow20", new Object[]{maxPage}));
               return false;
            }
         }
      }
   }

   public boolean showSelFlag(Player p, long landId, int flagPage, int page, boolean player) {
      if (!UtilPer.checkPer(p, this.landManager.getFlagHandler().getPer())) {
         return false;
      } else {
         Land land = this.landManager.getLand(p, String.valueOf(landId));
         if (land == null) {
            return false;
         } else if (land.isFix()) {
            p.sendMessage(this.get(1220));
            return false;
         } else {
            HashList<ItemStack> flagList;
            if (player) {
               flagList = this.flagShowListPlayer;
            } else {
               flagList = this.flagShowListOp;
            }

            int maxPage = flagList.getMaxPage(45);
            if (page >= 1 && page <= maxPage) {
               String type;
               if (player) {
                  type = this.get(1820);
               } else {
                  type = this.get(1825);
               }

               String name = UtilFormat.format(this.pn, "selFlagTip", new Object[]{type});
               IconMenu.OptionClickEventHandler handler = new ShowSelFlag(p, landId, flagPage, page, player);
               IconMenu.Info info = UtilIconMenu.register(name, 45, true, handler);
               int index = 0;

               for(ItemStack is : flagList.getPage(page, 45)) {
                  String flagName = this.getFlagName(is);
                  if (!land.getFlags().containsKey(flagName)) {
                     info.setItem(index, is);
                  }

                  ++index;
               }

               Inventory inv = Bukkit.getServer().createInventory((InventoryHolder)null, 9, "");
               inv.setItem(0, UtilItems.getItem(this.pn, "selFlag_player"));
               inv.setItem(1, UtilItems.getItem(this.pn, "selFlag_op"));
               inv.setItem(4, UtilItems.getItem(this.pn, "selFlag_back"));
               if (page > 1) {
                  inv.setItem(3, UtilItems.getItem(this.pn, "selFlag_pre"));
               }

               if (page < maxPage) {
                  inv.setItem(5, UtilItems.getItem(this.pn, "selFlag_next"));
               }

               UtilIconMenu.open(p, info, (String)null, inv);
               return true;
            } else {
               p.sendMessage(UtilFormat.format(this.pn, "landShow21", new Object[]{maxPage}));
               return false;
            }
         }
      }
   }

   public boolean showHandleFlag(Player p, long landId, int page, String flagName) {
      if (!UtilPer.checkPer(p, this.landManager.getFlagHandler().getPer())) {
         return false;
      } else {
         Land land = this.landManager.getLand(p, String.valueOf(landId));
         if (land == null) {
            return false;
         } else if (!land.getFlags().containsKey(flagName)) {
            p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1095)}));
            return false;
         } else {
            FlagHandler.Flag flag = (FlagHandler.Flag)this.landManager.getFlagHandler().getFlagHash().get(flagName);
            if (flag == null) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(270)}));
               return false;
            } else {
               ItemStack is = this.getLandFlagShowItem(land, flag);
               if (is == null) {
                  p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(270)}));
                  return false;
               } else if (!UtilPer.hasPer(p, flag.getPer())) {
                  p.sendMessage(UtilFormat.format(this.pn, "landShow22", new Object[]{flag.getPer()}));
                  return false;
               } else {
                  String name = UtilFormat.format(this.pn, 1305);
                  IconMenu.OptionClickEventHandler handler = new ShowHandleFlag(p, landId, page, flagName);
                  IconMenu.Info info = UtilIconMenu.register(name, 45, true, handler);
                  info.setItem(4, is);

                  for(ShowInfo showInfo : this.handleFlagHash.values()) {
                     int pos = showInfo.getPos();
                     String type = showInfo.getType();
                     ItemStack is2 = UtilItems.getItem(this.pn, type);
                     info.setItem(pos, is2);
                  }

                  Inventory inv = Bukkit.getServer().createInventory((InventoryHolder)null, 9, "");
                  inv.setItem(4, UtilItems.getItem(this.pn, "selFlag_back"));
                  UtilIconMenu.open(p, info, (String)null, inv);
                  return true;
               }
            }
         }
      }
   }

   public void showSel(Player p) {
      if (UtilPer.checkPer(p, this.landManager.getSelectHandler().getPer())) {
         String name = UtilFormat.format(this.pn, 1207);
         IconMenu.OptionClickEventHandler handler = new ShowSelInfo(p);
         IconMenu.Info info = UtilIconMenu.register(name, 45, true, handler);
         Iterator var6 = this.selHash.values().iterator();

         while(true) {
            int pos;
            ItemStack is;
            while(true) {
               if (!var6.hasNext()) {
                  Inventory inv = Bukkit.getServer().createInventory((InventoryHolder)null, 9, "");
                  inv.setItem(4, UtilItems.getItem(this.pn, "see_main"));
                  UtilIconMenu.open(p, info, (String)null, inv);
                  return;
               }

               ShowInfo showInfo = (ShowInfo)var6.next();
               pos = showInfo.getPos();
               String type = showInfo.getType();
               String name2 = showInfo.getName();
               if (p.isOp() || !name2.equals("create1") && !name2.equals("create2") && !name2.equals("setRange")) {
                  if (!name2.equals("info")) {
                     if (name2.equals("create")) {
                        if (!UtilPer.hasPer(p, this.per_land_admin)) {
                           continue;
                        }

                        is = UtilItems.getItem(this.pn, "sel_create").clone();
                        int baseCost = this.landManager.getCreateHandler().getBaseCost();
                        ItemMeta im = is.getItemMeta();
                        List<String> lore = im.getLore();
                        lore.set(0, ((String)lore.get(0)).replace("{0}", String.valueOf(baseCost)));
                        im.setLore(lore);
                        is.setItemMeta(im);
                        break;
                     }

                     if (name2.equals("create1")) {
                        is = UtilItems.getItem(this.pn, "sel_create1").clone();
                        int baseCost = this.landManager.getCreateHandler().getBaseCost();
                        ItemMeta im = is.getItemMeta();
                        List<String> lore = im.getLore();
                        lore.set(0, ((String)lore.get(0)).replace("{0}", String.valueOf(baseCost)));
                        im.setLore(lore);
                        is.setItemMeta(im);
                     } else if (name2.equals("create2")) {
                        is = UtilItems.getItem(this.pn, "sel_create2").clone();
                        int baseCost = this.landManager.getZoneHandler().getBaseCost();
                        ItemMeta im = is.getItemMeta();
                        List<String> lore = im.getLore();
                        lore.set(0, ((String)lore.get(0)).replace("{0}", String.valueOf(baseCost)));
                        im.setLore(lore);
                        is.setItemMeta(im);
                     } else if (name2.equals("create3")) {
                        is = UtilItems.getItem(this.pn, "sel_create3").clone();
                        int baseCost = this.landManager.getSubZoneHandler().getBaseCost();
                        ItemMeta im = is.getItemMeta();
                        List<String> lore = im.getLore();
                        lore.set(0, ((String)lore.get(0)).replace("{0}", String.valueOf(baseCost)));
                        im.setLore(lore);
                        is.setItemMeta(im);
                     } else {
                        is = UtilItems.getItem(this.pn, type);
                     }
                     break;
                  }

                  is = UtilItems.getItem(this.pn, "sel_info").clone();
                  ItemMeta im = is.getItemMeta();
                  List<String> lore = new ArrayList();
                  Range range = this.landManager.getSelectHandler().getRange(p);
                  if (range == null) {
                     lore.add(this.get(1015));
                  } else {
                     String world = range.getP1().getWorld();
                     double perCost = this.landManager.getCreateHandler().getPerCost(p);
                     long size = range.getSize();
                     long cost = (long)(perCost * (double)size);
                     String vip = "§m";
                     if (UtilPer.hasPer(p, this.landManager.getCreateHandler().getVipPer())) {
                        vip = "";
                     }

                     String show = UtilFormat.format(this.pn, "selectTip2", new Object[]{UtilNames.getWorldName(world), world, range.getP1().getX(), range.getP1().getY(), range.getP1().getZ(), range.getP2().getX(), range.getP2().getY(), range.getP2().getZ(), perCost, size, cost, vip});

                     String[] var26;
                     for(String s : var26 = show.split("\n")) {
                        lore.add(s);
                     }
                  }

                  im.setLore(lore);
                  is.setItemMeta(im);
                  break;
               }
            }

            info.setItem(pos, is);
         }
      }
   }

   private boolean check(Player p, Land land) {
      if (!UtilPer.checkPer(p, this.checkPer)) {
         return false;
      } else {
         p.sendMessage(UtilFormat.format(this.pn, "startToCheck", new Object[]{land.getName(), land.getId()}));
         if (land.hasFlag(AdminHandler.getFlagAdmin()) && land.hasPer(AdminHandler.getFlagAdmin(), PersHandler.getAll())) {
            p.sendMessage(this.get(1845));
            return true;
         } else {
            String all = PersHandler.getAll();
            boolean fail = false;

            for(Check c : this.checkList) {
               boolean success = false;

               for(String flag : c.flags) {
                  if (land.hasFlag(flag) && !land.hasPer(flag, all)) {
                     success = true;
                     break;
                  }
               }

               if (success) {
                  p.sendMessage(c.tip + this.get(1830));
               } else {
                  fail = true;
                  String flags = "";

                  for(String s : c.flags) {
                     if (!flags.isEmpty()) {
                        flags = flags + ",";
                     }

                     flags = flags + s;
                  }

                  p.sendMessage(c.tip + this.get(1835).replace("{0}", flags));
               }
            }

            if (fail) {
               p.sendMessage(this.get(1840));
            }

            return true;
         }
      }
   }

   private String getFlagName(ItemStack is) {
      try {
         return is.getItemMeta().getDisplayName().substring(this.flagNameCheckFrom);
      } catch (Exception var3) {
         return "";
      }
   }

   private void fix(Player p, long landId) {
      if (UtilPer.checkPer(p, this.fixPer)) {
         Land land = this.landManager.getLand(landId);
         if (land == null) {
            p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1225)}));
         } else if (!land.getOwner().equals(p.getName()) && !UtilPer.hasPer(p, this.per_land_admin)) {
            p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(740)}));
         } else if (land.isFix()) {
            p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1230)}));
         } else {
            land.setFix(true);
            this.landManager.addLand(land);
            p.sendMessage(UtilFormat.format((String)null, "success", new Object[]{this.get(1235)}));
         }
      }
   }

   private void showLandInfo(Player p, Land land) {
      String name = UtilFormat.format(this.pn, 1205);
      IconMenu.OptionClickEventHandler handler = new ShowLandInfo(p, land);
      IconMenu.Info info = UtilIconMenu.register(name, 45, true, handler);

      for(ShowInfo showInfo : this.landHash.values()) {
         int pos = showInfo.getPos();
         String type = showInfo.getType();
         String name2 = showInfo.getName();
         if (p.isOp() || !this.list1.has(name2) && (!name2.equals("delete") || land.getType() == 3)) {
            ItemStack is;
            if (name2.equals("info")) {
               is = this.getShowItemDetail(land);
            } else {
               is = UtilItems.getItem(this.pn, type);
            }

            info.setItem(pos, is);
         }
      }

      Inventory inv = Bukkit.getServer().createInventory((InventoryHolder)null, 9, "");
      inv.setItem(4, UtilItems.getItem(this.pn, "see_main"));
      if (p.isOp()) {
         inv.setItem(0, UtilItems.getItem(this.pn, "land_ex4"));
         inv.setItem(1, UtilItems.getItem(this.pn, "land_ex3"));
         inv.setItem(2, UtilItems.getItem(this.pn, "land_ex2"));
         inv.setItem(3, UtilItems.getItem(this.pn, "land_ex1"));
         inv.setItem(5, UtilItems.getItem(this.pn, "land_con1"));
         inv.setItem(6, UtilItems.getItem(this.pn, "land_con2"));
         inv.setItem(7, UtilItems.getItem(this.pn, "land_con3"));
         inv.setItem(8, UtilItems.getItem(this.pn, "land_con4"));
      }

      UtilIconMenu.open(p, info, (String)null, inv);
   }

   private long getId(String s) {
      try {
         return Long.parseLong(s.split(this.checkFrom)[1].split(this.checkTo)[0]);
      } catch (NumberFormatException var3) {
         return -1L;
      }
   }

   private void add(HashMap hash, YamlConfiguration config, String s, String name) {
      int pos = config.getInt("item." + s + "." + name);
      String type = s + "_" + name;
      ShowInfo showInfo = new ShowInfo(name, pos, type);
      hash.put(pos, showInfo);
   }

   private void loadConfig(YamlConfiguration config) {
      this.per_land_admin = config.getString("per_land_admin");
      this.fixPer = config.getString("fix.per");
      this.fixName = Util.convert(config.getString("fix.name"));
      this.fixLore = new ArrayList();

      for(String s : config.getStringList("fix.lore")) {
         this.fixLore.add(Util.convert(s));
      }

      this.deleteName = Util.convert(config.getString("remove.name"));
      this.deleteLore = new ArrayList();

      for(String s : config.getStringList("remove.lore")) {
         this.deleteLore.add(Util.convert(s));
      }

      this.sellName = Util.convert(config.getString("sell.name"));
      this.sellLore = new ArrayList();

      for(String s : config.getStringList("sell.lore")) {
         this.sellLore.add(Util.convert(s));
      }

      this.buyName = Util.convert(config.getString("buy.name"));
      this.buyLore = new ArrayList();

      for(String s : config.getStringList("buy.lore")) {
         this.buyLore.add(Util.convert(s));
      }

      this.addAllName = Util.convert(config.getString("pers.addAllName"));
      this.addAllLore = new ArrayList();

      for(String s : config.getStringList("pers.AddAllLore")) {
         this.addAllLore.add(Util.convert(s));
      }

      this.delAllName = Util.convert(config.getString("pers.delAllName"));
      this.delAllLore = new ArrayList();

      for(String s : config.getStringList("pers.delAllLore")) {
         this.delAllLore.add(Util.convert(s));
      }

      this.removeFlagName = Util.convert(config.getString("flag.removeName"));
      this.removeFlagLore = new ArrayList();

      for(String s : config.getStringList("flag.removeLore")) {
         this.removeFlagLore.add(Util.convert(s));
      }

      this.showMainMenuPer = config.getString("item.showMainMenuPer");
      this.showSeeAllPer = config.getString("item.showSeeAllPer");
      this.confirmTimeLimit = config.getInt("item.confirmTimeLimit");
      this.showInterval = config.getInt("item.showInterval");
      this.lineMaxLength = config.getInt("item.lineMaxLength");
      this.item = config.getInt("item.item");
      this.displayName = Util.convert(config.getString("item.displayName"));
      this.checkFrom = Util.convert(config.getString("item.check.from"));
      this.checkTo = Util.convert(config.getString("item.check.to"));
      this.flagDisplayName = Util.convert(config.getString("item.flagDisplayName"));
      this.flagNameCheckFrom = config.getInt("item.flagNameCheckFrom");
      this.flagItem = config.getInt("item.flagItem");
      String ss = "main";
      this.mainMenuHash = new HashMap();

      for(String s : ((MemorySection)config.get("item." + ss)).getValues(false).keySet()) {
         this.add(this.mainMenuHash, config, ss, s);
      }

      ss = "seeNow";
      this.seeNowHash = new HashMap();

      for(String s : ((MemorySection)config.get("item." + ss)).getValues(false).keySet()) {
         this.add(this.seeNowHash, config, ss, s);
      }

      ss = "land";
      this.landHash = new HashMap();

      for(String s : ((MemorySection)config.get("item." + ss)).getValues(false).keySet()) {
         this.add(this.landHash, config, ss, s);
      }

      ss = "sel";
      this.selHash = new HashMap();

      for(String s : ((MemorySection)config.get("item." + ss)).getValues(false).keySet()) {
         this.add(this.selHash, config, ss, s);
      }

      ss = "setFlag";
      this.setFlagHash = new HashMap();

      for(String s : ((MemorySection)config.get("item." + ss)).getValues(false).keySet()) {
         this.add(this.setFlagHash, config, ss, s);
      }

      ss = "selFlag";
      this.selFlagHash = new HashMap();

      for(String s : ((MemorySection)config.get("item." + ss)).getValues(false).keySet()) {
         this.add(this.selFlagHash, config, ss, s);
      }

      ss = "handleFlag";
      this.handleFlagHash = new HashMap();

      for(String s : ((MemorySection)config.get("item." + ss)).getValues(false).keySet()) {
         this.add(this.handleFlagHash, config, ss, s);
      }

      this.checkPer = config.getString("check.per");
      this.checkList = new ArrayList();

      for(int index = 1; config.contains("check.check" + index); ++index) {
         String tip = Util.convert(config.getString("check.check" + index + ".tip"));
         HashList<String> flags = new HashListImpl();

         for(String flag : config.getStringList("check.check" + index + ".flags")) {
            flags.add(flag);
         }

         this.checkList.add(new Check(tip, flags));
      }

   }

   private void loadData() {
      HashList<Land> landList = this.landManager.getLandCheck().getAllLands();
      this.landShowHash1 = new HashMap();
      this.landShowHash2 = new HashMap();
      this.landShowHash3 = new HashMap();
      this.landShowList1 = new HashListImpl();
      this.landShowList2 = new HashListImpl();
      this.landShowList3 = new HashListImpl();
      this.sellLandList = new HashListImpl();
      this.player1Hash = new HashMap();
      this.player2Hash = new HashMap();
      this.player3Hash = new HashMap();

      for(Land land : landList) {
         ItemStack is = this.getShowItem(land);
         switch (land.getType()) {
            case 1:
               this.landShowHash1.put(land.getId(), is);
               this.landShowList1.add(is);
               if (!this.player1Hash.containsKey(land.getOwner())) {
                  this.player1Hash.put(land.getOwner(), new HashListImpl());
               }

               ((HashList)this.player1Hash.get(land.getOwner())).add(land.getId());
               break;
            case 2:
               this.landShowHash2.put(land.getId(), is);
               this.landShowList2.add(is);
               if (!this.player2Hash.containsKey(land.getOwner())) {
                  this.player2Hash.put(land.getOwner(), new HashListImpl());
               }

               ((HashList)this.player2Hash.get(land.getOwner())).add(land.getId());
               break;
            case 3:
               this.landShowHash3.put(land.getId(), is);
               this.landShowList3.add(is);
               if (land.getPrice() >= 0) {
                  this.sellLandList.add(land.getId());
               }

               if (!this.player3Hash.containsKey(land.getOwner())) {
                  this.player3Hash.put(land.getOwner(), new HashListImpl());
               }

               ((HashList)this.player3Hash.get(land.getOwner())).add(land.getId());
         }
      }

      this.flagShowHash = new HashMap();
      this.flagShowListPlayer = new HashListImpl();
      this.flagShowListOp = new HashListImpl();
      HashMap<String, FlagHandler.Flag> flagHash = this.landManager.getFlagHandler().getFlagHash();

      for(FlagHandler.Flag flag : flagHash.values()) {
         ItemStack is = this.getFlagShowItem(flag);
         this.flagShowHash.put(flag.getName(), is);
         this.addFlagItem(is);
      }

   }

   private void addFlagItem(ItemStack is) {
      String s = this.getFlagName(is);
      FlagHandler.Flag flag = (FlagHandler.Flag)this.landManager.getFlagHandler().getFlagHash().get(s);
      HashList<ItemStack> flagList;
      if (flag.isUse()) {
         flagList = this.flagShowListPlayer;
      } else {
         flagList = this.flagShowListOp;
      }

      for(int index = 0; index < flagList.size(); ++index) {
         String f = this.getFlagName((ItemStack)flagList.get(index));
         if (s.compareToIgnoreCase(f) < 0) {
            flagList.add(is, index);
            return;
         }
      }

      flagList.add(is);
   }

   private ItemStack getLandFlagShowItem(Land land, FlagHandler.Flag flag) {
      String pers = "";
      if (!flag.isPlayer()) {
         pers = this.get(1040);
      } else {
         HashList<String> list2 = (HashList)land.getPers().get(flag.getName());
         if (list2 != null) {
            for(String name : list2) {
               if (!pers.isEmpty()) {
                  pers = pers + " ";
               }

               pers = pers + name;
            }
         }
      }

      String add = UtilFormat.format(this.pn, "landFlagShowItem2", new Object[]{pers});
      ItemStack result = ((ItemStack)this.flagShowHash.get(flag.getName())).clone();
      ItemMeta im = result.getItemMeta();
      List<String> lore = im.getLore();
      lore.set(1, UtilFormat.format(this.pn, "landFlagShowItem0", new Object[]{land.getFlag(flag.getName())}));
      lore.add(add);
      im.setLore(lore);
      result.setItemMeta(im);
      return result;
   }

   private ItemStack getFlagShowItem(FlagHandler.Flag flag) {
      ItemStack result = new ItemStack(this.flagItem);
      ItemMeta im = IM.clone();
      im.setDisplayName(this.flagDisplayName.replace("{0}", flag.getName()));
      String lore0 = UtilFormat.format(this.pn, "landFlagShowItem", new Object[]{flag.getTip(), this.getFlagValue(flag), this.getFlagPlayer(flag), this.getFlagPer(flag)});
      List<String> lore = new ArrayList();

      String[] var9;
      for(String s : var9 = lore0.split("\n")) {
         lore.add(s);
      }

      im.setLore(lore);
      result.setItemMeta(im);
      return result;
   }

   private ItemStack getExsitShowItem(Land land) {
      ItemStack result = null;
      switch (land.getType()) {
         case 1:
            result = (ItemStack)this.landShowHash1.get(land.getId());
            break;
         case 2:
            result = (ItemStack)this.landShowHash2.get(land.getId());
            break;
         case 3:
            result = (ItemStack)this.landShowHash3.get(land.getId());
      }

      return result;
   }

   private ItemStack getShowItem(Land land) {
      ItemStack result = new ItemStack(this.item);
      ItemMeta im = IM.clone();
      im.setDisplayName(this.displayName.replace("{0}", "" + land.getId()).replace("{1}", land.getName()));
      result.setItemMeta(im);
      return result;
   }

   private ItemStack getShowItemDetail(Land land) {
      ItemStack result = new ItemStack(this.item);
      ItemMeta im = IM.clone();
      im.setDisplayName(this.displayName.replace("{0}", "" + land.getId()).replace("{1}", land.getName()));
      String flags = "";

      for(String s : land.getFlags().keySet()) {
         if (!flags.isEmpty()) {
            flags = flags + "\n";
         }

         if (land.hasFlag(s)) {
            flags = flags + UtilFormat.format(this.pn, "landInfo4", new Object[]{s, this.landManager.getFlagHandler().getValue(s), this.landManager.getFlagHandler().getPlayer(s)});
         }
      }

      String price;
      if (land.getPrice() < 0) {
         price = this.get(1030);
      } else {
         price = String.valueOf(land.getPrice());
      }

      String lore0 = UtilFormat.format(this.pn, "landInfo", new Object[]{this.getType(land.getType()), land.getLevel(), this.getFix(land.isFix()), this.getOverlap(land.isOverlap()), price, land.getOwner(), UtilNames.getWorldName(land.getRange().getP1().getWorld()), land.getRange().getP1().getX(), land.getRange().getP1().getY(), land.getRange().getP1().getZ(), land.getRange().getP2().getX(), land.getRange().getP2().getY(), land.getRange().getP2().getZ(), this.landManager.getTipHandler().getEnterTip(land), this.landManager.getTipHandler().getLeaveTip(land), flags});
      List<String> lore = new ArrayList();

      String[] var11;
      for(String s : var11 = lore0.split("\n")) {
         lore.add(s);
      }

      Util.seperateLines(lore, this.lineMaxLength);
      im.setLore(lore);
      result.setItemMeta(im);
      return result;
   }

   private String getFix(boolean fix) {
      return fix ? this.get(1080) : this.get(1085);
   }

   private String getFlagValue(FlagHandler.Flag flag) {
      return flag.isValue() ? this.get(1035) : this.get(1040);
   }

   private String getFlagPlayer(FlagHandler.Flag flag) {
      return flag.isPlayer() ? this.get(1005) : this.get(1010);
   }

   private String getFlagPer(FlagHandler.Flag flag) {
      return flag.getPer().isEmpty() ? this.get(1015) : flag.getPer();
   }

   private String getOverlap(boolean overlap) {
      return overlap ? this.get(750) : this.get(755);
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   private class InputBuyMaxLands implements InputManager.InputHandler {
      private Player p;

      public InputBuyMaxLands(Player p) {
         this.p = p;
      }

      public boolean onInput(String s) {
         return ShowManager.this.landManager.getBuyHandler().buyMaxLands(this.p, s);
      }
   }

   private class InputShowLandInfo implements InputManager.InputHandler {
      private Player p;

      public InputShowLandInfo(Player p) {
         this.p = p;
      }

      public boolean onInput(String s) {
         return ShowManager.this.showLandInfo(this.p, s);
      }
   }

   private class InputSetOwner implements InputManager.InputHandler {
      private Player p;
      private long landId;

      public InputSetOwner(Player p, long landId) {
         this.p = p;
         this.landId = landId;
      }

      public boolean onInput(String s) {
         return ShowManager.this.landManager.getSetHandler().setOwner(this.p, String.valueOf(this.landId), s);
      }
   }

   private class InputSetName implements InputManager.InputHandler {
      private Player p;
      private long landId;

      public InputSetName(Player p, long landId) {
         this.p = p;
         this.landId = landId;
      }

      public boolean onInput(String s) {
         return ShowManager.this.landManager.getSetHandler().setName(this.p, String.valueOf(this.landId), s);
      }
   }

   private class InputEnterTip implements InputManager.InputHandler {
      private Player p;
      private long landId;

      public InputEnterTip(Player p, long landId) {
         this.p = p;
         this.landId = landId;
      }

      public boolean onInput(String s) {
         ShowManager.this.landManager.getTipHandler().setEnterTip(this.p, String.valueOf(this.landId), s);
         return true;
      }
   }

   private class InputLeaveTip implements InputManager.InputHandler {
      private Player p;
      private long landId;

      public InputLeaveTip(Player p, long landId) {
         this.p = p;
         this.landId = landId;
      }

      public boolean onInput(String s) {
         ShowManager.this.landManager.getTipHandler().setLeaveTip(this.p, String.valueOf(this.landId), s);
         return true;
      }
   }

   private class InputSetLevel implements InputManager.InputHandler {
      private Player p;
      private long landId;

      public InputSetLevel(Player p, long landId) {
         this.p = p;
         this.landId = landId;
      }

      public boolean onInput(String s) {
         return ShowManager.this.landManager.getSetHandler().setLevel(this.p, String.valueOf(this.landId), s);
      }
   }

   private class InputPrice implements InputManager.InputHandler {
      private Player p;
      private long landId;

      public InputPrice(Player p, long landId) {
         this.p = p;
         this.landId = landId;
      }

      public boolean onInput(String s) {
         return ShowManager.this.landManager.getSellHandler().sellLand(this.p, String.valueOf(this.landId), s);
      }
   }

   private class InputChangePrice implements InputManager.InputHandler {
      private Player p;
      private long landId;

      public InputChangePrice(Player p, long landId) {
         this.p = p;
         this.landId = landId;
      }

      public boolean onInput(String s) {
         return ShowManager.this.landManager.getSellHandler().changePrice(this.p, String.valueOf(this.landId), s);
      }
   }

   private class InputCreate implements InputManager.InputHandler {
      private Player p;

      public InputCreate(Player p) {
         this.p = p;
      }

      public boolean onInput(String s) {
         return ShowManager.this.landManager.getCreateHandler().createLand(this.p, s, true);
      }
   }

   private class InputCreate1 implements InputManager.InputHandler {
      private Player p;

      public InputCreate1(Player p) {
         this.p = p;
      }

      public boolean onInput(String s) {
         return ShowManager.this.landManager.getCreateHandler().createLand(this.p, s, false);
      }
   }

   private class InputCreate2 implements InputManager.InputHandler {
      private Player p;

      public InputCreate2(Player p) {
         this.p = p;
      }

      public boolean onInput(String s) {
         return ShowManager.this.landManager.getZoneHandler().createLand(this.p, s);
      }
   }

   private class InputCreate3 implements InputManager.InputHandler {
      private Player p;

      public InputCreate3(Player p) {
         this.p = p;
      }

      public boolean onInput(String s) {
         return ShowManager.this.landManager.getSubZoneHandler().createLand(this.p, s);
      }
   }

   private class InputRange implements InputManager.InputHandler {
      private Player p;

      public InputRange(Player p) {
         this.p = p;
      }

      public boolean onInput(String s) {
         return ShowManager.this.landManager.getSetHandler().setRange(this.p, s);
      }
   }

   private class InputAddPer implements InputManager.InputHandler {
      private Player p;
      private long landId;
      private String flagName;

      public InputAddPer(Player p, long landId, String flagName) {
         this.p = p;
         this.landId = landId;
         this.flagName = flagName;
      }

      public boolean onInput(String s) {
         return ShowManager.this.landManager.getPersHandler().addPer(this.p, String.valueOf(this.landId), this.flagName, s);
      }
   }

   private class InputDelPer implements InputManager.InputHandler {
      private Player p;
      private long landId;
      private String flagName;

      public InputDelPer(Player p, long landId, String flagName) {
         this.p = p;
         this.landId = landId;
         this.flagName = flagName;
      }

      public boolean onInput(String s) {
         return ShowManager.this.landManager.getPersHandler().removePer(this.p, String.valueOf(this.landId), this.flagName, s);
      }
   }

   private class InputChangeFlag implements InputManager.InputHandler {
      private Player p;
      private long landId;
      private String flagName;

      public InputChangeFlag(Player p, long landId, String flagName) {
         this.p = p;
         this.landId = landId;
         this.flagName = flagName;
      }

      public boolean onInput(String s) {
         return ShowManager.this.landManager.getFlagHandler().setFlag(this.p, String.valueOf(this.landId), this.flagName, s);
      }
   }

   private class InputEx implements InputManager.InputHandler {
      private Player p;
      private long landId;

      public InputEx(Player p, long landId) {
         this.p = p;
         this.landId = landId;
      }

      public boolean onInput(String s) {
         try {
            ShowManager.this.landManager.getSetHandler().expand(this.p, String.valueOf(this.landId), Integer.parseInt(s));
            return true;
         } catch (NumberFormatException var3) {
            this.p.sendMessage(UtilFormat.format(ShowManager.this.pn, "fail", new Object[]{ShowManager.this.get(110)}));
            return false;
         }
      }
   }

   private class InputCon implements InputManager.InputHandler {
      private Player p;
      private long landId;

      public InputCon(Player p, long landId) {
         this.p = p;
         this.landId = landId;
      }

      public boolean onInput(String s) {
         try {
            ShowManager.this.landManager.getSetHandler().contract(this.p, String.valueOf(this.landId), Integer.parseInt(s));
            return true;
         } catch (NumberFormatException var3) {
            this.p.sendMessage(UtilFormat.format(ShowManager.this.pn, "fail", new Object[]{ShowManager.this.get(110)}));
            return false;
         }
      }
   }

   private class SessionFix implements IconMenu.Session {
      private Player p;
      private long landId;

      public SessionFix(Player p, long landId) {
         this.p = p;
         this.landId = landId;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            ShowManager.this.fix(this.p, this.landId);
         }

         ShowManager.this.showLandInfo(this.p, String.valueOf(this.landId));
      }
   }

   private class SessionDelete implements IconMenu.Session {
      private Player p;
      private long landId;

      public SessionDelete(Player p, long landId) {
         this.p = p;
         this.landId = landId;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            ShowManager.this.landManager.getRemoveHandler().remove(this.p, String.valueOf(this.landId));
         }

      }
   }

   private class SessionCancelSell implements IconMenu.Session {
      private Player p;
      private long landId;

      public SessionCancelSell(Player p, long landId) {
         this.p = p;
         this.landId = landId;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            ShowManager.this.landManager.getSellHandler().cancelSell(this.p, String.valueOf(this.landId));
         }

         ShowManager.this.showLandInfo(this.p, String.valueOf(this.landId));
      }
   }

   private class SessionBuyLand implements IconMenu.Session {
      private Player p;
      private long landId;
      private int confirmPrice;

      public SessionBuyLand(Player p, long landId, int confirmPrice) {
         this.p = p;
         this.landId = landId;
         this.confirmPrice = confirmPrice;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            ShowManager.this.landManager.getSellHandler().buyLand(this.p, String.valueOf(this.landId), this.confirmPrice);
         }

         ShowManager.this.showLandInfo(this.p, String.valueOf(this.landId));
      }
   }

   private class SessionAddAll implements IconMenu.Session {
      private Player p;
      private long landId;
      private String flagName;

      public SessionAddAll(Player p, long landId, String flagName) {
         this.p = p;
         this.landId = landId;
         this.flagName = flagName;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            ShowManager.this.landManager.getPersHandler().addPer(this.p, String.valueOf(this.landId), this.flagName, (String)null);
         }

         ShowManager.this.showSetFlag(this.p, this.landId, 1);
      }
   }

   private class SessionDelAll implements IconMenu.Session {
      private Player p;
      private long landId;
      private String flagName;

      public SessionDelAll(Player p, long landId, String flagName) {
         this.p = p;
         this.landId = landId;
         this.flagName = flagName;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            ShowManager.this.landManager.getPersHandler().removePer(this.p, String.valueOf(this.landId), this.flagName, (String)null);
         }

         ShowManager.this.showSetFlag(this.p, this.landId, 1);
      }
   }

   private class SessionRemoveFlag implements IconMenu.Session {
      private Player p;
      private long landId;
      private String flagName;

      public SessionRemoveFlag(Player p, long landId, String flagName) {
         this.p = p;
         this.landId = landId;
         this.flagName = flagName;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            ShowManager.this.landManager.getFlagHandler().removeFlag(this.p, String.valueOf(this.landId), this.flagName);
         }

         ShowManager.this.showSetFlag(this.p, this.landId, 1);
      }
   }

   private class SessionEx1 implements IconMenu.Session {
      private Player p;
      private long landId;

      public SessionEx1(Player p, long landId) {
         this.p = p;
         this.landId = landId;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            ShowManager.this.landManager.getSetHandler().expand(this.p, String.valueOf(this.landId), 1);
         }

         ShowManager.this.showLandInfo(this.p, String.valueOf(this.landId));
      }
   }

   private class SessionEx2 implements IconMenu.Session {
      private Player p;
      private long landId;

      public SessionEx2(Player p, long landId) {
         this.p = p;
         this.landId = landId;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            ShowManager.this.landManager.getSetHandler().expand(this.p, String.valueOf(this.landId), 5);
         }

         ShowManager.this.showLandInfo(this.p, String.valueOf(this.landId));
      }
   }

   private class SessionEx3 implements IconMenu.Session {
      private Player p;
      private long landId;

      public SessionEx3(Player p, long landId) {
         this.p = p;
         this.landId = landId;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            ShowManager.this.landManager.getSetHandler().expand(this.p, String.valueOf(this.landId), 10);
         }

         ShowManager.this.showLandInfo(this.p, String.valueOf(this.landId));
      }
   }

   private class SessionCon1 implements IconMenu.Session {
      private Player p;
      private long landId;

      public SessionCon1(Player p, long landId) {
         this.p = p;
         this.landId = landId;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            ShowManager.this.landManager.getSetHandler().contract(this.p, String.valueOf(this.landId), 1);
         }

         ShowManager.this.showLandInfo(this.p, String.valueOf(this.landId));
      }
   }

   private class SessionCon2 implements IconMenu.Session {
      private Player p;
      private long landId;

      public SessionCon2(Player p, long landId) {
         this.p = p;
         this.landId = landId;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            ShowManager.this.landManager.getSetHandler().contract(this.p, String.valueOf(this.landId), 5);
         }

         ShowManager.this.showLandInfo(this.p, String.valueOf(this.landId));
      }
   }

   private class SessionCon3 implements IconMenu.Session {
      private Player p;
      private long landId;

      public SessionCon3(Player p, long landId) {
         this.p = p;
         this.landId = landId;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            ShowManager.this.landManager.getSetHandler().contract(this.p, String.valueOf(this.landId), 10);
         }

         ShowManager.this.showLandInfo(this.p, String.valueOf(this.landId));
      }
   }

   private class ShowMainMenu implements IconMenu.OptionClickEventHandler {
      private Player p;

      public ShowMainMenu(Player p) {
         this.p = p;
      }

      public void onOptionClick(IconMenu.OptionClickEvent event) {
         if (event.getPos() - 45 == 4) {
            event.setWillClose(true);
            ShowManager.this.showSel(this.p);
         } else if (event.getPos() - 45 == 0) {
            event.setWillClose(true);
            this.p.sendMessage(UtilFormat.format(ShowManager.this.pn, "verTip", new Object[]{"http://www.minecraft001.com"}));
         } else {
            ShowInfo showInfo = (ShowInfo)ShowManager.this.mainMenuHash.get(event.getPos());
            if (showInfo != null) {
               if (showInfo.getName().equals("seeNow")) {
                  if (ShowManager.this.seeNow(this.p)) {
                     event.setWillClose(true);
                  }
               } else if (showInfo.getName().equals("seeAll1")) {
                  if (ShowManager.this.seeAll(1, this.p, 1)) {
                     event.setWillClose(true);
                  }
               } else if (showInfo.getName().equals("seeYou1")) {
                  if (ShowManager.this.seeYou(1, this.p, this.p.getName(), 1, 0)) {
                     event.setWillClose(true);
                  }
               } else if (showInfo.getName().equals("seeAll2")) {
                  if (ShowManager.this.seeAll(2, this.p, 1)) {
                     event.setWillClose(true);
                  }
               } else if (showInfo.getName().equals("seeYou2")) {
                  if (ShowManager.this.seeYou(2, this.p, this.p.getName(), 1, 0)) {
                     event.setWillClose(true);
                  }
               } else if (showInfo.getName().equals("seeAll3")) {
                  if (ShowManager.this.seeAll(3, this.p, 1)) {
                     event.setWillClose(true);
                  }
               } else if (showInfo.getName().equals("seeYou3")) {
                  if (ShowManager.this.seeYou(3, this.p, this.p.getName(), 1, 0)) {
                     event.setWillClose(true);
                  }
               } else if (showInfo.getName().equals("seeAllSell3")) {
                  if (ShowManager.this.seeYou(3, this.p, this.p.getName(), 1, 1)) {
                     event.setWillClose(true);
                  }
               } else if (showInfo.getName().equals("buyMaxLand")) {
                  if (UtilPer.checkPer(this.p, ShowManager.this.landManager.getBuyHandler().getPerMaxLands())) {
                     InputManager.InputHandler inputHandler = ShowManager.this.new InputBuyMaxLands(this.p);
                     String tip = UtilFormat.format(ShowManager.this.pn, "landShow5", new Object[]{ShowManager.this.landManager.getBuyHandler().getAddMaxCost()});
                     if (Lib.getInputManager().input(this.p, inputHandler, tip)) {
                        event.setWillClose(true);
                     }
                  }
               } else if (showInfo.getName().equals("infoLand") && UtilPer.checkPer(this.p, ShowManager.this.landManager.getInfoHandler().getPerInfoLand())) {
                  InputManager.InputHandler inputHandler = ShowManager.this.new InputShowLandInfo(this.p);
                  String tip = ShowManager.this.get(1200);
                  if (Lib.getInputManager().input(this.p, inputHandler, tip)) {
                     event.setWillClose(true);
                  }
               }
            } else {
               event.setWillClose(true);
            }

         }
      }
   }

   private class ShowSetFlag implements IconMenu.OptionClickEventHandler {
      private Player p;
      private long landId;
      private int page;

      public ShowSetFlag(Player p, long landId, int page) {
         this.p = p;
         this.landId = landId;
         this.page = page;
      }

      public void onOptionClick(IconMenu.OptionClickEvent event) {
         if (event.getPos() >= 45) {
            ShowInfo showInfo = (ShowInfo)ShowManager.this.setFlagHash.get(event.getPos() - 45);
            if (showInfo != null) {
               String name = showInfo.getName();
               if (name.equals("add")) {
                  if (ShowManager.this.showSelFlag(this.p, this.landId, this.page, 1, true)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("pre")) {
                  if (ShowManager.this.showSetFlag(this.p, this.landId, this.page - 1)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("next")) {
                  if (ShowManager.this.showSetFlag(this.p, this.landId, this.page + 1)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("back")) {
                  event.setWillClose(true);
                  ShowManager.this.showLandInfo(this.p, String.valueOf(this.landId));
               }
            }
         } else if (event.getPos() >= 9) {
            ItemStack is = event.getInfo().getInv(this.p).getItem(event.getPos());
            String flagName = ShowManager.this.getFlagName(is);
            if (ShowManager.this.showHandleFlag(this.p, this.landId, this.page, flagName)) {
               event.setWillClose(true);
            }
         }

      }
   }

   private class ShowHandleFlag implements IconMenu.OptionClickEventHandler {
      private Player p;
      private long landId;
      private int page;
      private String flagName;

      public ShowHandleFlag(Player p, long landId, int page, String flagName) {
         this.p = p;
         this.landId = landId;
         this.page = page;
         this.flagName = flagName;
      }

      public void onOptionClick(IconMenu.OptionClickEvent event) {
         if (event.getPos() - 45 == 4) {
            if (ShowManager.this.showSetFlag(this.p, this.landId, this.page)) {
               event.setWillClose(true);
            }
         } else {
            ShowInfo showInfo = (ShowInfo)ShowManager.this.handleFlagHash.get(event.getPos());
            if (showInfo != null) {
               String name = showInfo.getName();
               if (name.equals("add")) {
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getPersHandler().getPer())) {
                     return;
                  }

                  InputAddPer inputAddPer = ShowManager.this.new InputAddPer(this.p, this.landId, this.flagName);
                  String tip = UtilFormat.format(ShowManager.this.pn, "landShow23", new Object[]{this.flagName});
                  if (Lib.getInputManager().input(this.p, inputAddPer, tip)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("addAll")) {
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getPersHandler().getPer())) {
                     return;
                  }

                  event.setWillClose(true);
                  IconMenu.Session session = ShowManager.this.new SessionAddAll(this.p, this.landId, this.flagName);
                  UtilIconMenu.openSession(this.p, ShowManager.this.addAllName, ShowManager.this.addAllLore, session, ShowManager.this.confirmTimeLimit);
               } else if (name.equals("del")) {
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getPersHandler().getPer())) {
                     return;
                  }

                  InputDelPer inputDelPer = ShowManager.this.new InputDelPer(this.p, this.landId, this.flagName);
                  String tip = UtilFormat.format(ShowManager.this.pn, "landShow24", new Object[]{this.flagName});
                  if (Lib.getInputManager().input(this.p, inputDelPer, tip)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("delAll")) {
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getPersHandler().getPer())) {
                     return;
                  }

                  event.setWillClose(true);
                  IconMenu.Session session = ShowManager.this.new SessionDelAll(this.p, this.landId, this.flagName);
                  UtilIconMenu.openSession(this.p, ShowManager.this.delAllName, ShowManager.this.delAllLore, session, ShowManager.this.confirmTimeLimit);
               } else if (name.equals("change")) {
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getPersHandler().getPer())) {
                     return;
                  }

                  InputChangeFlag inputChangeFlag = ShowManager.this.new InputChangeFlag(this.p, this.landId, this.flagName);
                  String tip = UtilFormat.format(ShowManager.this.pn, "landShow27", new Object[]{this.flagName});
                  if (Lib.getInputManager().input(this.p, inputChangeFlag, tip)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("remove")) {
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getPersHandler().getPer())) {
                     return;
                  }

                  event.setWillClose(true);
                  IconMenu.Session session = ShowManager.this.new SessionRemoveFlag(this.p, this.landId, this.flagName);
                  UtilIconMenu.openSession(this.p, ShowManager.this.removeFlagName, ShowManager.this.removeFlagLore, session, ShowManager.this.confirmTimeLimit);
               }
            }
         }

      }
   }

   private class ShowSelFlag implements IconMenu.OptionClickEventHandler {
      private Player p;
      private long landId;
      private int flagPage;
      private int page;
      private boolean player;

      public ShowSelFlag(Player p, long landId, int flagPage, int page, boolean player) {
         this.p = p;
         this.landId = landId;
         this.flagPage = flagPage;
         this.page = page;
         this.player = player;
      }

      public void onOptionClick(IconMenu.OptionClickEvent event) {
         if (event.getPos() >= 45) {
            ShowInfo showInfo = (ShowInfo)ShowManager.this.selFlagHash.get(event.getPos() - 45);
            if (showInfo != null) {
               String name = showInfo.getName();
               if (name.equals("pre")) {
                  if (ShowManager.this.showSelFlag(this.p, this.landId, this.flagPage, this.page - 1, this.player)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("next")) {
                  if (ShowManager.this.showSelFlag(this.p, this.landId, this.flagPage, this.page + 1, this.player)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("player")) {
                  if (ShowManager.this.showSelFlag(this.p, this.landId, this.flagPage, 1, true)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("op")) {
                  if (ShowManager.this.showSelFlag(this.p, this.landId, this.flagPage, 1, false)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("back")) {
                  event.setWillClose(true);
                  ShowManager.this.showSetFlag(this.p, this.landId, this.flagPage);
               }
            }
         } else {
            ItemStack is = event.getInfo().getInv(this.p).getItem(event.getPos());
            String flagName = ShowManager.this.getFlagName(is);
            if (ShowManager.this.landManager.getFlagHandler().addFlag(this.p, String.valueOf(this.landId), flagName)) {
               event.setWillClose(true);
               ShowManager.this.showSetFlag(this.p, this.landId, this.flagPage);
            }
         }

      }
   }

   private class ShowLandInfo implements IconMenu.OptionClickEventHandler {
      private Player p;
      private Land land;

      public ShowLandInfo(Player p, Land land) {
         this.p = p;
         this.land = land;
      }

      public void onOptionClick(IconMenu.OptionClickEvent event) {
         if (event.getPos() < 45) {
            ShowInfo showInfo = (ShowInfo)ShowManager.this.landHash.get(event.getPos());
            if (showInfo != null) {
               String name = showInfo.getName();
               if (name.equals("check")) {
                  if (ShowManager.this.check(this.p, this.land)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("tp")) {
                  if (ShowManager.this.landManager.getTpHandler().tp(this.p, String.valueOf(this.land.getId()))) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("setTp")) {
                  if (ShowManager.this.landManager.getTpHandler().setTp(this.p, String.valueOf(this.land.getId()))) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("showRange")) {
                  World w = Bukkit.getServer().getWorld(this.land.getRange().getP1().getWorld());
                  if (w == null) {
                     event.setWillClose(true);
                     return;
                  }

                  Range range = this.land.getRange();
                  if (ShowManager.this.landManager.getShowHandler().show(this.p, w, range, false)) {
                     this.p.sendMessage(UtilFormat.format((String)null, "success", new Object[]{ShowManager.this.get(1210)}));
                     event.setWillClose(true);
                  }
               } else if (name.equals("fix")) {
                  if (!UtilPer.checkPer(this.p, ShowManager.this.fixPer)) {
                     return;
                  }

                  event.setWillClose(true);
                  IconMenu.Session session = ShowManager.this.new SessionFix(this.p, this.land.getId());
                  UtilIconMenu.openSession(this.p, ShowManager.this.fixName, ShowManager.this.fixLore, session, ShowManager.this.confirmTimeLimit);
               } else if (name.equals("delete")) {
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getRemoveHandler().getPer())) {
                     return;
                  }

                  if (this.land.isFix() && !UtilPer.hasPer(this.p, ShowManager.this.per_land_admin)) {
                     this.p.sendMessage(ShowManager.this.get(1250));
                     return;
                  }

                  event.setWillClose(true);
                  IconMenu.Session session = ShowManager.this.new SessionDelete(this.p, this.land.getId());
                  UtilIconMenu.openSession(this.p, ShowManager.this.deleteName, ShowManager.this.deleteLore, session, ShowManager.this.confirmTimeLimit);
               } else if (name.equals("setOwner")) {
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getSetHandler().getSetOwnerPer())) {
                     return;
                  }

                  if (this.land.isFix()) {
                     this.p.sendMessage(ShowManager.this.get(1220));
                     return;
                  }

                  InputSetOwner inputSetOwner = ShowManager.this.new InputSetOwner(this.p, this.land.getId());
                  String tip = UtilFormat.format(ShowManager.this.pn, "landShow10", new Object[]{this.land.getId(), this.land.getName()});
                  if (Lib.getInputManager().input(this.p, inputSetOwner, tip)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("setName")) {
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getSetHandler().getSetNamePer())) {
                     return;
                  }

                  if (this.land.isFix()) {
                     this.p.sendMessage(ShowManager.this.get(1220));
                     return;
                  }

                  InputSetName inputSetName = ShowManager.this.new InputSetName(this.p, this.land.getId());
                  String tip = UtilFormat.format(ShowManager.this.pn, "landShow12", new Object[]{this.land.getId(), this.land.getName()});
                  if (Lib.getInputManager().input(this.p, inputSetName, tip)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("setLevel")) {
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getSetHandler().getSetLevelPer())) {
                     return;
                  }

                  if (this.land.isFix()) {
                     this.p.sendMessage(ShowManager.this.get(1220));
                     return;
                  }

                  InputSetLevel inputSetLevel = ShowManager.this.new InputSetLevel(this.p, this.land.getId());
                  String tip = UtilFormat.format(ShowManager.this.pn, "landShow11", new Object[]{this.land.getId(), this.land.getName()});
                  if (Lib.getInputManager().input(this.p, inputSetLevel, tip)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("toggleOverlap")) {
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getSetHandler().getSetOverlapPer())) {
                     return;
                  }

                  if (this.land.isFix()) {
                     this.p.sendMessage(ShowManager.this.get(1220));
                     return;
                  }

                  ShowManager.this.landManager.getSetHandler().toggleOverlap(this.p, String.valueOf(this.land.getId()));
               } else if (name.equals("enterTip")) {
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getTipHandler().getPer())) {
                     return;
                  }

                  if (this.land.isFix()) {
                     this.p.sendMessage(ShowManager.this.get(1220));
                     return;
                  }

                  InputEnterTip inputEnterTip = ShowManager.this.new InputEnterTip(this.p, this.land.getId());
                  String tip = UtilFormat.format(ShowManager.this.pn, "landShow13", new Object[]{this.land.getId(), this.land.getName()});
                  if (Lib.getInputManager().input(this.p, inputEnterTip, tip)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("leaveTip")) {
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getTipHandler().getPer())) {
                     return;
                  }

                  if (this.land.isFix()) {
                     this.p.sendMessage(ShowManager.this.get(1220));
                     return;
                  }

                  InputLeaveTip inputLeaveTip = ShowManager.this.new InputLeaveTip(this.p, this.land.getId());
                  String tip = UtilFormat.format(ShowManager.this.pn, "landShow14", new Object[]{this.land.getId(), this.land.getName()});
                  if (Lib.getInputManager().input(this.p, inputLeaveTip, tip)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("sell")) {
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getSellHandler().getPer())) {
                     return;
                  }

                  if (this.land.isFix()) {
                     this.p.sendMessage(ShowManager.this.get(1220));
                     return;
                  }

                  if (this.land.getType() != 3) {
                     this.p.sendMessage(ShowManager.this.get(422));
                     return;
                  }

                  if (this.land.getPrice() >= 0) {
                     this.p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{ShowManager.this.get(426)}));
                     return;
                  }

                  InputPrice inputPrice = ShowManager.this.new InputPrice(this.p, this.land.getId());
                  String tip = UtilFormat.format(ShowManager.this.pn, "landShow15", new Object[]{this.land.getId(), this.land.getName()});
                  if (Lib.getInputManager().input(this.p, inputPrice, tip)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("changePrice")) {
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getSellHandler().getPer())) {
                     return;
                  }

                  if (this.land.isFix()) {
                     this.p.sendMessage(ShowManager.this.get(1220));
                     return;
                  }

                  if (this.land.getType() != 3) {
                     this.p.sendMessage(ShowManager.this.get(422));
                     return;
                  }

                  if (this.land.getPrice() < 0) {
                     this.p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{ShowManager.this.get(425)}));
                     return;
                  }

                  InputChangePrice inputChangePrice = ShowManager.this.new InputChangePrice(this.p, this.land.getId());
                  String tip = UtilFormat.format(ShowManager.this.pn, "landShow16", new Object[]{this.land.getId(), this.land.getName()});
                  if (Lib.getInputManager().input(this.p, inputChangePrice, tip)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("cancelSell")) {
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getSellHandler().getPer())) {
                     return;
                  }

                  if (this.land.isFix()) {
                     this.p.sendMessage(ShowManager.this.get(1220));
                     return;
                  }

                  if (this.land.getType() != 3) {
                     this.p.sendMessage(ShowManager.this.get(422));
                     return;
                  }

                  if (this.land.getPrice() < 0) {
                     this.p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{ShowManager.this.get(425)}));
                     return;
                  }

                  SessionCancelSell session = ShowManager.this.new SessionCancelSell(this.p, this.land.getId());
                  UtilIconMenu.openSession(this.p, ShowManager.this.sellName, ShowManager.this.sellLore, session, ShowManager.this.confirmTimeLimit);
               } else if (name.equals("buy")) {
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getSellHandler().getBuyPer())) {
                     return;
                  }

                  if (this.land.isFix()) {
                     this.p.sendMessage(ShowManager.this.get(1220));
                     return;
                  }

                  if (this.land.getType() != 3) {
                     this.p.sendMessage(ShowManager.this.get(422));
                     return;
                  }

                  if (this.land.getPrice() < 0) {
                     this.p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{ShowManager.this.get(425)}));
                     return;
                  }

                  SessionBuyLand session = ShowManager.this.new SessionBuyLand(this.p, this.land.getId(), this.land.getPrice());
                  UtilIconMenu.openSession(this.p, ShowManager.this.buyName.replace("{0}", String.valueOf(this.land.getPrice())), ShowManager.this.buyLore, session, ShowManager.this.confirmTimeLimit);
               } else if (name.equals("setFlag")) {
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getFlagHandler().getPer())) {
                     return;
                  }

                  if (ShowManager.this.showSetFlag(this.p, this.land.getId(), 1)) {
                     event.setWillClose(true);
                  }
               }
            } else {
               event.setWillClose(true);
            }
         } else {
            switch (event.getPos() - 45) {
               case 0:
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getSetHandler().getSetRangePer())) {
                     return;
                  }

                  if (this.land.isFix()) {
                     this.p.sendMessage(ShowManager.this.get(1220));
                     return;
                  }

                  InputEx inputEx = ShowManager.this.new InputEx(this.p, this.land.getId());
                  if (Lib.getInputManager().input(this.p, inputEx, ShowManager.this.get(1630))) {
                     event.setWillClose(true);
                  }
                  break;
               case 1:
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getSetHandler().getSetRangePer())) {
                     return;
                  }

                  if (this.land.isFix()) {
                     this.p.sendMessage(ShowManager.this.get(1220));
                     return;
                  }

                  IconMenu.Session session = ShowManager.this.new SessionEx3(this.p, this.land.getId());
                  UtilIconMenu.openSession(this.p, ShowManager.this.get(1610), (List)null, session, ShowManager.this.confirmTimeLimit);
                  break;
               case 2:
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getSetHandler().getSetRangePer())) {
                     return;
                  }

                  if (this.land.isFix()) {
                     this.p.sendMessage(ShowManager.this.get(1220));
                     return;
                  }

                  IconMenu.Session session = ShowManager.this.new SessionEx2(this.p, this.land.getId());
                  UtilIconMenu.openSession(this.p, ShowManager.this.get(1605), (List)null, session, ShowManager.this.confirmTimeLimit);
                  break;
               case 3:
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getSetHandler().getSetRangePer())) {
                     return;
                  }

                  if (this.land.isFix()) {
                     this.p.sendMessage(ShowManager.this.get(1220));
                     return;
                  }

                  IconMenu.Session session = ShowManager.this.new SessionEx1(this.p, this.land.getId());
                  UtilIconMenu.openSession(this.p, ShowManager.this.get(1600), (List)null, session, ShowManager.this.confirmTimeLimit);
                  break;
               case 4:
                  if (ShowManager.this.showMainMenu(this.p)) {
                     event.setWillClose(true);
                  }
                  break;
               case 5:
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getSetHandler().getSetRangePer())) {
                     return;
                  }

                  if (this.land.isFix()) {
                     this.p.sendMessage(ShowManager.this.get(1220));
                     return;
                  }

                  IconMenu.Session session = ShowManager.this.new SessionCon1(this.p, this.land.getId());
                  UtilIconMenu.openSession(this.p, ShowManager.this.get(1615), (List)null, session, ShowManager.this.confirmTimeLimit);
                  break;
               case 6:
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getSetHandler().getSetRangePer())) {
                     return;
                  }

                  if (this.land.isFix()) {
                     this.p.sendMessage(ShowManager.this.get(1220));
                     return;
                  }

                  IconMenu.Session session = ShowManager.this.new SessionCon2(this.p, this.land.getId());
                  UtilIconMenu.openSession(this.p, ShowManager.this.get(1620), (List)null, session, ShowManager.this.confirmTimeLimit);
                  break;
               case 7:
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getSetHandler().getSetRangePer())) {
                     return;
                  }

                  if (this.land.isFix()) {
                     this.p.sendMessage(ShowManager.this.get(1220));
                     return;
                  }

                  IconMenu.Session session = ShowManager.this.new SessionCon3(this.p, this.land.getId());
                  UtilIconMenu.openSession(this.p, ShowManager.this.get(1625), (List)null, session, ShowManager.this.confirmTimeLimit);
                  break;
               case 8:
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getSetHandler().getSetRangePer())) {
                     return;
                  }

                  if (this.land.isFix()) {
                     this.p.sendMessage(ShowManager.this.get(1220));
                     return;
                  }

                  InputCon inputCon = ShowManager.this.new InputCon(this.p, this.land.getId());
                  if (Lib.getInputManager().input(this.p, inputCon, ShowManager.this.get(1635))) {
                     event.setWillClose(true);
                  }
            }
         }

      }
   }

   private class ShowSelInfo implements IconMenu.OptionClickEventHandler {
      private Player p;

      public ShowSelInfo(Player p) {
         this.p = p;
      }

      public void onOptionClick(IconMenu.OptionClickEvent event) {
         if (event.getPos() - 45 == 4) {
            if (ShowManager.this.showMainMenu(this.p)) {
               event.setWillClose(true);
            }

         } else {
            ShowInfo showInfo = (ShowInfo)ShowManager.this.selHash.get(event.getPos());
            if (showInfo != null) {
               String name = showInfo.getName();
               if (name.equals("free")) {
                  if (ShowManager.this.landManager.getSelectHandler().getFreeBar(this.p)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("select")) {
                  if (ShowManager.this.landManager.getSelectHandler().getBar(this.p)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("create")) {
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getCreateHandler().getPer())) {
                     return;
                  }

                  if (ShowManager.this.landManager.getSelectHandler().getRange(this.p) == null) {
                     this.p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{ShowManager.this.get(105)}));
                     return;
                  }

                  InputCreate inputCreate = ShowManager.this.new InputCreate(this.p);
                  String tip = ShowManager.this.get(1260);
                  if (Lib.getInputManager().input(this.p, inputCreate, tip)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("create1")) {
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getCreateHandler().getPer())) {
                     return;
                  }

                  if (ShowManager.this.landManager.getSelectHandler().getRange(this.p) == null) {
                     this.p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{ShowManager.this.get(105)}));
                     return;
                  }

                  InputCreate1 inputCreate1 = ShowManager.this.new InputCreate1(this.p);
                  String tip = ShowManager.this.get(1260);
                  if (Lib.getInputManager().input(this.p, inputCreate1, tip)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("create2")) {
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getZoneHandler().getPer())) {
                     return;
                  }

                  if (ShowManager.this.landManager.getSelectHandler().getRange(this.p) == null) {
                     this.p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{ShowManager.this.get(105)}));
                     return;
                  }

                  InputCreate2 inputCreate2 = ShowManager.this.new InputCreate2(this.p);
                  String tip = ShowManager.this.get(1270);
                  if (Lib.getInputManager().input(this.p, inputCreate2, tip)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("create3")) {
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getSubZoneHandler().getPer())) {
                     return;
                  }

                  if (ShowManager.this.landManager.getSelectHandler().getRange(this.p) == null) {
                     this.p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{ShowManager.this.get(105)}));
                     return;
                  }

                  InputCreate3 inputCreate3 = ShowManager.this.new InputCreate3(this.p);
                  String tip = ShowManager.this.get(1275);
                  if (Lib.getInputManager().input(this.p, inputCreate3, tip)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("setRange")) {
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getSetHandler().getSetRangePer())) {
                     return;
                  }

                  if (ShowManager.this.landManager.getSelectHandler().getRange(this.p) == null) {
                     this.p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{ShowManager.this.get(105)}));
                     return;
                  }

                  InputRange inputRange = ShowManager.this.new InputRange(this.p);
                  String tip = ShowManager.this.get(1280);
                  if (Lib.getInputManager().input(this.p, inputRange, tip)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("show")) {
                  if (!UtilPer.checkPer(this.p, ShowManager.this.landManager.getShowHandler().getPer())) {
                     return;
                  }

                  if (ShowManager.this.landManager.getShowHandler().showOn(this.p)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("up")) {
                  if (ShowManager.this.landManager.getSelectHandler().up(this.p)) {
                     event.setWillClose(true);
                     ShowManager.this.showSel(this.p);
                  }
               } else if (name.equals("down") && ShowManager.this.landManager.getSelectHandler().down(this.p)) {
                  event.setWillClose(true);
                  ShowManager.this.showSel(this.p);
               }
            }

         }
      }
   }

   private class ShowSeeNow implements IconMenu.OptionClickEventHandler {
      private Player p;

      public ShowSeeNow(Player p) {
         this.p = p;
      }

      public void onOptionClick(IconMenu.OptionClickEvent event) {
         if (event.getPos() < 45) {
            try {
               Inventory inv = event.getInfo().getInv(this.p);
               ItemStack is = inv.getItem(event.getPos());
               long id = ShowManager.this.getId(is.getItemMeta().getDisplayName());
               if (ShowManager.this.showLandInfo(this.p, String.valueOf(id))) {
                  event.setWillClose(true);
               }
            } catch (Exception var6) {
               event.setWillClose(true);
            }

         } else {
            ShowInfo showInfo = (ShowInfo)ShowManager.this.seeNowHash.get(event.getPos() - 45);
            if (showInfo != null) {
               if (showInfo.getName().equals("main") && ShowManager.this.showMainMenu(this.p)) {
                  event.setWillClose(true);
               }
            } else {
               event.setWillClose(true);
            }

         }
      }
   }

   private class ShowSee implements IconMenu.OptionClickEventHandler {
      private int type;
      private Player p;
      private String tar;
      private int page;
      private int all;

      public ShowSee(int type, Player p, String tar, int page, int all) {
         this.type = type;
         this.p = p;
         this.tar = tar;
         this.page = page;
         this.all = all;
      }

      public void onOptionClick(IconMenu.OptionClickEvent event) {
         if (event.getPos() < 45) {
            try {
               Inventory inv = event.getInfo().getInv(this.p);
               ItemStack is = inv.getItem(event.getPos());
               long id = ShowManager.this.getId(is.getItemMeta().getDisplayName());
               if (ShowManager.this.showLandInfo(this.p, String.valueOf(id))) {
                  event.setWillClose(true);
               }
            } catch (Exception var6) {
               event.setWillClose(true);
            }
         } else {
            int toPage;
            switch (event.getPos() - 45) {
               case 0:
                  toPage = 1;
                  break;
               case 1:
                  toPage = this.page - 10;
                  break;
               case 2:
                  toPage = this.page - 5;
                  break;
               case 3:
                  toPage = this.page - 1;
                  break;
               case 4:
                  if (ShowManager.this.showMainMenu(this.p)) {
                     event.setWillClose(true);
                  }

                  return;
               case 5:
                  toPage = this.page + 1;
                  break;
               case 6:
                  toPage = this.page + 5;
                  break;
               case 7:
                  toPage = this.page + 10;
                  break;
               case 8:
                  toPage = -1000;
                  break;
               default:
                  return;
            }

            if (this.all == 1) {
               ShowManager.this.seeAll(this.type, this.p, toPage);
            } else if (this.all == 2) {
               ShowManager.this.seeYou(this.type, this.p, this.tar, toPage, 0);
            } else {
               ShowManager.this.seeYou(this.type, this.p, this.tar, toPage, 1);
            }
         }

      }
   }

   class ShowInfo {
      private String name;
      private int pos;
      private String type;

      public ShowInfo(String name, int pos, String type) {
         this.name = name;
         this.pos = pos;
         this.type = type;
      }

      public String getName() {
         return this.name;
      }

      public int getPos() {
         return this.pos;
      }

      public String getType() {
         return this.type;
      }
   }

   private static class Check {
      String tip;
      HashList flags;

      public Check(String tip, HashList flags) {
         this.tip = tip;
         this.flags = flags;
      }
   }
}
