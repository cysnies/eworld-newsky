package landHandler;

import java.util.HashMap;
import land.Land;
import land.Pos;
import land.Range;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.util.UtilConfig;
import lib.util.UtilEco;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class SubZoneHandler implements Listener {
   private static final int DEFAUTL_LEVEL = 200;
   private LandManager landManager;
   private String pn;
   private String per_land_admin;
   private String per;
   private HashMap addFlagsHash;
   private int baseCost;

   public SubZoneHandler(LandManager landManager) {
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

   public int getBaseCost() {
      return this.baseCost;
   }

   public String getPer() {
      return this.per;
   }

   public boolean createLand(Player p, String name) {
      if (!UtilPer.checkPer(p, this.per)) {
         return true;
      } else {
         Range range = this.landManager.getSelectHandler().getRange(p);
         if (range == null) {
            p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(105)}));
            return true;
         } else {
            Range rangeCopy = range.clone();
            if (!name.trim().isEmpty() && name.trim().length() <= Land.getNameMaxLength()) {
               if (this.landManager.getLand(name) != null) {
                  p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(230)}));
                  return false;
               } else if (!this.landManager.getCreateHandler().checkName(p, name)) {
                  return false;
               } else {
                  Range showRange = this.landManager.getShowHandler().getShowRange(p);
                  if (showRange != null && !showRange.equals(rangeCopy)) {
                     p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(325)}));
                     return true;
                  } else {
                     HashList<Land> list = this.landManager.getLandCheck().getCollisionLands(rangeCopy);

                     for(Land land : list) {
                        if (!land.isOverlap() && land.getType() != 2) {
                           p.sendMessage(UtilFormat.format(this.pn, "landOverlap3", new Object[]{land.getName(), land.getId()}));
                           return true;
                        }
                     }

                     boolean result = false;

                     for(Land land : list) {
                        if (land.getType() == 2 && rangeCopy.isIn(land.getRange()) && (land.getOwner().equals(p.getName()) || UtilPer.hasPer(p, this.per_land_admin))) {
                           result = true;
                           break;
                        }
                     }

                     if (!result) {
                        p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1265)}));
                        return true;
                     } else {
                        long cost = (long)this.baseCost;
                        if (!UtilPer.hasPer(p, this.per_land_admin)) {
                           if (UtilEco.get(p.getName()) < (double)cost) {
                              p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(130)}));
                              return true;
                           }

                           UtilEco.del(p.getName(), (double)cost);
                           p.sendMessage(UtilFormat.format(this.pn, "landAddRangeCost2", new Object[]{cost}));
                        }

                        Land land = LandManager.createLand(3, false, name, p.getName(), rangeCopy, 200);

                        for(String flag : this.addFlagsHash.keySet()) {
                           p.sendMessage(UtilFormat.format(this.pn, "landShow7", new Object[]{flag, this.addFlagsHash.get(flag)}));
                           this.landManager.getFlagHandler().addFlag(land, flag, (Integer)this.addFlagsHash.get(flag));
                        }

                        this.landManager.getTpHandler().setTp(land, Pos.toLoc(rangeCopy.getCenter()));
                        p.sendMessage(UtilFormat.format((String)null, "tip", new Object[]{this.get(1217)}));
                        p.sendMessage(UtilFormat.format((String)null, "success", new Object[]{this.get(127)}));
                        return true;
                     }
                  }
               }
            } else {
               p.sendMessage(UtilFormat.format(this.pn, "landNameErr", new Object[]{Land.getNameMaxLength()}));
               return false;
            }
         }
      }
   }

   private void loadConfig(YamlConfiguration config) {
      this.per_land_admin = config.getString("per_land_admin");
      this.per = config.getString("subzone.per");
      this.baseCost = config.getInt("subzone.baseCost");
      this.addFlagsHash = new HashMap();

      for(String s : config.getStringList("subzone.addFlags")) {
         String flag = s.split(":")[0];
         int value = Integer.parseInt(s.split(":")[1]);
         this.addFlagsHash.put(flag, value);
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
