package landHandler;

import event.OwnerChangeEvent;
import event.SellChangeEvent;
import land.Land;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import lib.util.UtilEco;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class SellHandler implements Listener {
   private LandManager landManager;
   private Server server;
   private String pn;
   private String per_land_admin;
   private String per;
   private String buyPer;
   private int tax;

   public SellHandler(LandManager landManager) {
      this.landManager = landManager;
      this.server = landManager.getServer();
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   public boolean sellLand(Player p, String name, String s) {
      if (!UtilPer.checkPer(p, this.per)) {
         return true;
      } else {
         Land land = this.landManager.getLand(p, name);
         if (land == null) {
            return true;
         } else if (!land.getOwner().equals(p.getName()) && !UtilPer.hasPer(p, this.per_land_admin)) {
            p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(740)}));
            return true;
         } else if (land.isFix()) {
            p.sendMessage(this.get(1220));
            return true;
         } else if (land.getType() != 3) {
            p.sendMessage(this.get(422));
            return true;
         } else if (land.getPrice() >= 0) {
            p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(426)}));
            return true;
         } else {
            int price;
            try {
               price = Integer.parseInt(s);
            } catch (NumberFormatException var7) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(110)}));
               return false;
            }

            if (price < 0) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(435)}));
               return false;
            } else {
               p.sendMessage(UtilFormat.format(this.pn, "landSellStatus", new Object[]{land.getName(), price}));
               land.setPrice(price);
               this.landManager.addLand(land);
               SellChangeEvent sellChangeEvent = new SellChangeEvent(land);
               this.server.getPluginManager().callEvent(sellChangeEvent);
               return true;
            }
         }
      }
   }

   public boolean changePrice(Player p, String name, String s) {
      if (!UtilPer.checkPer(p, this.per)) {
         return true;
      } else {
         Land land = this.landManager.getLand(p, name);
         if (land == null) {
            return true;
         } else if (!land.getOwner().equals(p.getName()) && !UtilPer.hasPer(p, this.per_land_admin)) {
            p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(740)}));
            return true;
         } else if (land.isFix()) {
            p.sendMessage(this.get(1220));
            return true;
         } else if (land.getType() != 3) {
            p.sendMessage(this.get(422));
            return true;
         } else if (land.getPrice() < 0) {
            p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(425)}));
            return true;
         } else {
            int price;
            try {
               price = Integer.parseInt(s);
            } catch (NumberFormatException var7) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(110)}));
               return false;
            }

            if (price < 0) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(435)}));
               return false;
            } else if (land.getPrice() == price) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(437)}));
               return false;
            } else {
               p.sendMessage(UtilFormat.format(this.pn, "landChangeStatus", new Object[]{land.getName(), land.getPrice(), price}));
               land.setPrice(price);
               this.landManager.addLand(land);
               return true;
            }
         }
      }
   }

   public void cancelSell(Player p, String name) {
      if (UtilPer.checkPer(p, this.per)) {
         Land land = this.landManager.getLand(p, name);
         if (land != null) {
            if (!land.getOwner().equals(p.getName()) && !UtilPer.hasPer(p, this.per_land_admin)) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(740)}));
            } else if (land.isFix()) {
               p.sendMessage(this.get(1220));
            } else if (land.getType() != 3) {
               p.sendMessage(this.get(422));
            } else if (land.getPrice() < 0) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(425)}));
            } else {
               p.sendMessage(UtilFormat.format((String)null, "success", new Object[]{this.get(433)}));
               land.setPrice(-1);
               this.landManager.addLand(land);
               SellChangeEvent sellChangeEvent = new SellChangeEvent(land);
               this.server.getPluginManager().callEvent(sellChangeEvent);
            }
         }
      }
   }

   public void buyLand(Player p, String name, int confirmPrice) {
      if (UtilPer.checkPer(p, this.buyPer)) {
         Land land = this.landManager.getLand(p, name);
         if (land != null) {
            if (land.isFix()) {
               p.sendMessage(this.get(1220));
            } else if (land.getType() != 3) {
               p.sendMessage(this.get(422));
            } else if (land.getPrice() < 0) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(425)}));
            } else if (land.getOwner().equals(p.getName())) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(430)}));
            } else {
               int price = land.getPrice();
               if (price != confirmPrice) {
                  p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(427)}));
               } else if (price < 0) {
                  p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(435)}));
               } else if (UtilEco.get(p.getName()) < (double)price) {
                  p.sendMessage(UtilFormat.format(this.pn, "landBuyCost", new Object[]{price}));
               } else if (!UtilEco.del(p.getName(), (double)price)) {
                  p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1110)}));
               } else {
                  int taxAmount = price * this.tax / 100;
                  int ownerGet = price - taxAmount;
                  if (!UtilEco.add(land.getOwner(), (double)ownerGet)) {
                     UtilEco.add(p.getName(), (double)price);
                     p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1110)}));
                  } else {
                     String oldOwner = land.getOwner();
                     p.sendMessage(UtilFormat.format(this.pn, "landBuyCost3", new Object[]{price, land.getName(), land.getId()}));
                     if (this.server.getPlayer(oldOwner) != null) {
                        this.server.getPlayer(oldOwner).sendMessage(UtilFormat.format(this.pn, "landBuyCost2", new Object[]{p.getName(), land.getName(), taxAmount, ownerGet}));
                     }

                     land.setOwner(p.getName());
                     land.setPrice(-1);
                     SellChangeEvent sellChangeEvent = new SellChangeEvent(land);
                     this.server.getPluginManager().callEvent(sellChangeEvent);
                     this.landManager.addLand(land);
                     OwnerChangeEvent ownerChangeEvent = new OwnerChangeEvent(land, oldOwner);
                     this.server.getPluginManager().callEvent(ownerChangeEvent);
                  }
               }
            }
         }
      }
   }

   public String getPer() {
      return this.per;
   }

   public String getBuyPer() {
      return this.buyPer;
   }

   private void loadConfig(YamlConfiguration config) {
      this.per_land_admin = config.getString("per_land_admin");
      this.per = config.getString("sell.per");
      this.buyPer = config.getString("sell.buyPer");
      this.tax = config.getInt("sell.tax");
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
