package landHandler;

import java.util.HashMap;
import java.util.List;
import land.Pos;
import land.Range;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilNames;
import lib.util.UtilPer;
import lib.util.UtilSpeed;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class SelectHandler implements Listener {
   private static final String GET_BAR = "getBar";
   private static final String SEL = "sel";
   private static final int MAX = 256;
   private static final int MIN = 0;
   private LandManager landManager;
   private String pn;
   private String per;
   private int interval;
   private int getInterval;
   private String check;
   private HashMap rangeHash;

   public SelectHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.rangeHash = new HashMap();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      UtilSpeed.register(this.pn, "sel");
      UtilSpeed.register(this.pn, "getBar");
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = false
   )
   public void onPlayerInteract(PlayerInteractEvent e) {
      if (e.hasItem() && e.hasBlock() && this.isSelectBar(e.getPlayer().getItemInHand())) {
         e.setCancelled(true);
         if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            this.selectPos1(e.getPlayer(), e.getClickedBlock().getLocation());
         } else if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            this.selectPos2(e.getPlayer(), e.getClickedBlock().getLocation());
         }
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
      priority = EventPriority.LOWEST
   )
   public void onPlayerQuit(PlayerQuitEvent e) {
      this.rangeHash.remove(e.getPlayer());
   }

   public Range getRange(Player p) {
      return (Range)this.rangeHash.get(p);
   }

   public void setRange(Player p, Range range) {
      this.rangeHash.put(p, range);
   }

   public boolean getBar(Player p) {
      if (!UtilPer.checkPer(p, this.per)) {
         return false;
      } else if (!UtilSpeed.check(p, this.pn, "getBar", this.getInterval)) {
         return false;
      } else {
         int emptySlots = UtilItems.getEmptySlots(p.getInventory());
         if (emptySlots <= 0) {
            p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1060)}));
            return false;
         } else {
            ItemStack is = UtilItems.getItem(this.pn, "selBar").clone();
            p.getInventory().addItem(new ItemStack[]{is});
            p.sendMessage(UtilFormat.format((String)null, "success", new Object[]{this.get(1170)}));
            return true;
         }
      }
   }

   public boolean getFreeBar(Player p) {
      if (!UtilPer.checkPer(p, this.per)) {
         return false;
      } else if (!UtilSpeed.check(p, this.pn, "getBar", this.getInterval)) {
         return false;
      } else {
         int emptySlots = UtilItems.getEmptySlots(p.getInventory());
         if (emptySlots <= 0) {
            p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1060)}));
            return false;
         } else {
            ItemStack is = UtilItems.getItem(this.pn, "sel_free").clone();
            p.getInventory().addItem(new ItemStack[]{is});
            p.sendMessage(UtilFormat.format((String)null, "success", new Object[]{this.get(1172)}));
            return true;
         }
      }
   }

   public String getPer() {
      return this.per;
   }

   public boolean up(Player p) {
      Range range = (Range)this.rangeHash.get(p);
      if (range == null) {
         p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(105)}));
         return false;
      } else {
         this.landManager.getShowHandler().checkCancelShow(p);
         int y = Math.max(range.getP1().getY(), range.getP2().getY());
         if (y >= 256) {
            p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1445)}));
            return false;
         } else {
            range.expand(0, 256 - y, 0);
            this.showSelectInfo(p);
            p.sendMessage(UtilFormat.format((String)null, "success", new Object[]{this.get(1285)}));
            return true;
         }
      }
   }

   public boolean down(Player p) {
      Range range = (Range)this.rangeHash.get(p);
      if (range == null) {
         p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(105)}));
         return false;
      } else {
         this.landManager.getShowHandler().checkCancelShow(p);
         int y = Math.min(range.getP1().getY(), range.getP2().getY());
         if (y <= 0) {
            p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1450)}));
            return false;
         } else {
            range.expand(0, 0 - y, 0);
            this.showSelectInfo(p);
            p.sendMessage(UtilFormat.format((String)null, "success", new Object[]{this.get(1290)}));
            return true;
         }
      }
   }

   private void loadConfig(YamlConfiguration config) {
      this.per = config.getString("select.per");
      this.interval = config.getInt("select.interval");
      this.getInterval = config.getInt("select.getInterval");
      this.check = Util.convert(config.getString("select.check"));
   }

   private boolean isSelectBar(ItemStack is) {
      if (is == null) {
         return false;
      } else if (!is.hasItemMeta()) {
         return false;
      } else {
         List<String> lore = is.getItemMeta().getLore();
         return lore != null && lore.size() > 0 && ((String)lore.get(0)).equalsIgnoreCase(this.check);
      }
   }

   private void showSelectInfo(Player p) {
      if (!this.rangeHash.containsKey(p)) {
         p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(105)}));
      } else {
         Range range = (Range)this.rangeHash.get(p);
         double perCost = this.landManager.getCreateHandler().getPerCost(p);
         long size = range.getSize();
         long cost = (long)((int)(perCost * (double)size));
         String vip = "§m";
         if (UtilPer.hasPer(p, this.landManager.getCreateHandler().getVipPer())) {
            vip = "";
         }

         p.sendMessage(UtilFormat.format(this.pn, "selectTip", new Object[]{UtilNames.getWorldName(range.getP1().getWorld()), range.getP1().getWorld(), range.getP1().getX(), range.getP1().getY(), range.getP1().getZ(), range.getP2().getX(), range.getP2().getY(), range.getP2().getZ(), perCost, size, cost, vip}));
      }
   }

   private void selectPos1(Player p, Location l) {
      if (UtilPer.checkPer(p, this.per)) {
         if (UtilSpeed.check(p, this.pn, "sel", this.interval)) {
            this.landManager.getShowHandler().checkCancelShow(p);
            Pos pos = Pos.getPos(l);
            if (!this.rangeHash.containsKey(p)) {
               this.rangeHash.put(p, new Range(pos, pos.clone()));
            } else {
               Range range = (Range)this.rangeHash.get(p);
               if (!range.getP1().getWorld().equals(pos.getWorld())) {
                  range.setP1(pos);
                  range.setP2(pos);
                  p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1175)}));
               } else {
                  range.setP1(pos);
               }
            }

            this.showSelectInfo(p);
         }
      }
   }

   private void selectPos2(Player p, Location l) {
      if (UtilPer.checkPer(p, this.per)) {
         if (UtilSpeed.check(p, this.pn, "sel", this.interval)) {
            this.landManager.getShowHandler().checkCancelShow(p);
            Pos pos = Pos.getPos(l);
            if (!this.rangeHash.containsKey(p)) {
               this.rangeHash.put(p, new Range(pos, pos.clone()));
            } else {
               Range range = (Range)this.rangeHash.get(p);
               if (!range.getP1().getWorld().equals(pos.getWorld())) {
                  range.setP1(pos);
                  range.setP2(pos);
                  p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1175)}));
               } else {
                  range.setP2(pos);
               }
            }

            this.showSelectInfo(p);
         }
      }
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
