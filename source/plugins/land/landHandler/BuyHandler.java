package landHandler;

import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import lib.util.UtilEco;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class BuyHandler implements Listener {
   private LandManager landManager;
   private String pn;
   private String perMaxLands;
   private int addMaxCost;

   public BuyHandler(LandManager landManager) {
      this.landManager = landManager;
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

   public int getAddMaxCost() {
      return this.addMaxCost;
   }

   public String getPerMaxLands() {
      return this.perMaxLands;
   }

   public boolean buyMaxLands(Player p, String s) {
      if (!UtilPer.checkPer(p, this.perMaxLands)) {
         return true;
      } else {
         try {
            int amount = Integer.parseInt(s);
            if (amount < 1) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1860)}));
               return false;
            } else {
               int has = (int)UtilEco.get(p.getName());
               if (has < amount * this.addMaxCost) {
                  amount = has / this.addMaxCost;
               }

               if (amount <= 0) {
                  p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(130)}));
                  return true;
               } else {
                  int cost = amount * this.addMaxCost;
                  if (!UtilEco.del(p.getName(), (double)cost)) {
                     p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1110)}));
                     return true;
                  } else {
                     this.landManager.getLandUserHandler().addMaxLands(p.getName(), amount);
                     p.sendMessage(UtilFormat.format(this.pn, "landAddMax", new Object[]{cost, amount}));
                     return true;
                  }
               }
            }
         } catch (NumberFormatException var6) {
            p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(110)}));
            return false;
         }
      }
   }

   private void loadConfig(YamlConfiguration config) {
      this.perMaxLands = config.getString("buy.perMaxLands");
      this.addMaxCost = config.getInt("buy.land.maxCost");
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
