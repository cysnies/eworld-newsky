package landHandler;

import java.util.HashMap;
import land.Land;
import land.LandUser;
import land.Pos;
import land.Range;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.util.UtilConfig;
import lib.util.UtilEco;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ZoneHandler implements Listener {
   private static final int DEFAUTL_LEVEL = 150;
   private LandManager landManager;
   private String pn;
   private String per_land_admin;
   private String per;
   private String perNoLimit;
   private HashList buyWorld;
   private HashMap addFlagsHash;
   private int baseCost;
   private double perCost;
   private int minSize;
   private int maxSize;
   private int minXZRange;
   private int maxXZRange;
   private int minYRange;
   private int maxYRange;

   public ZoneHandler(LandManager landManager) {
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

   public long getCost(Player p, long size) {
      double perCost = this.landManager.getCreateHandler().getPerCost(p);
      return (long)((double)size * perCost);
   }

   public int getBaseCost() {
      return this.baseCost;
   }

   public double getPerCost() {
      return this.perCost;
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
                  HashList<Land> landList = this.landManager.getUserLands(p.getName());
                  HashMap<String, LandUser> userHash = this.landManager.getLandUserHandler().getUserHash();
                  if (landList != null) {
                     int amount = 0;

                     for(Land land : landList) {
                        if (land.getType() != 3) {
                           ++amount;
                        }
                     }

                     if (amount >= ((LandUser)userHash.get(p.getName())).getMaxLands()) {
                        p.sendMessage(UtilFormat.format(this.pn, "landMax", new Object[]{((LandUser)userHash.get(p.getName())).getMaxLands()}));
                        return true;
                     }
                  }

                  if (!UtilPer.hasPer(p, this.per_land_admin) && !this.buyWorld.has(rangeCopy.getP1().getWorld())) {
                     p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(421)}));
                     return true;
                  } else {
                     if (!UtilPer.hasPer(p, this.perNoLimit)) {
                        long size = rangeCopy.getSize();
                        int xLength = rangeCopy.getXLength();
                        int zLength = rangeCopy.getZLength();
                        int yLength = rangeCopy.getYLength();
                        if (size < (long)this.minSize || size > (long)this.maxSize || xLength < this.minXZRange || xLength > this.maxXZRange || zLength < this.minXZRange || zLength > this.maxXZRange || yLength < this.minYRange || yLength > this.maxYRange) {
                           p.sendMessage(UtilFormat.format(this.pn, "landLimit", new Object[]{this.minSize, this.maxSize, this.minXZRange, this.maxXZRange, this.minYRange, this.maxYRange}));
                           return true;
                        }
                     }

                     Range showRange = this.landManager.getShowHandler().getShowRange(p);
                     if (showRange != null && !showRange.equals(rangeCopy)) {
                        p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(325)}));
                        return true;
                     } else {
                        for(Land land : this.landManager.getLandCheck().getCollisionLands(rangeCopy)) {
                           if (!land.isOverlap()) {
                              p.sendMessage(UtilFormat.format(this.pn, "landOverlap2", new Object[]{land.getName(), land.getId()}));
                              return true;
                           }
                        }

                        long cost = 0L;
                        if (!UtilPer.hasPer(p, this.per_land_admin)) {
                           double perCost = this.landManager.getCreateHandler().getPerCost(p);
                           cost = (long)((double)this.baseCost + perCost * (double)rangeCopy.getSize());
                           if (UtilEco.get(p.getName()) < (double)cost) {
                              p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(130)}));
                              return true;
                           }

                           UtilEco.del(p.getName(), (double)cost);
                           p.sendMessage(UtilFormat.format(this.pn, "landAddRangeCost2", new Object[]{cost}));
                        }

                        Land land = LandManager.createLand(2, false, name, p.getName(), rangeCopy, 150);

                        for(String flag : this.addFlagsHash.keySet()) {
                           p.sendMessage(UtilFormat.format(this.pn, "landShow7", new Object[]{flag, this.addFlagsHash.get(flag)}));
                           this.landManager.getFlagHandler().addFlag(land, flag, (Integer)this.addFlagsHash.get(flag));
                        }

                        this.landManager.getTpHandler().setTp(land, Pos.toLoc(rangeCopy.getCenter()));
                        p.sendMessage(UtilFormat.format((String)null, "tip", new Object[]{this.get(1216)}));
                        p.sendMessage(UtilFormat.format((String)null, "success", new Object[]{this.get(126)}));
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

   public String getPer() {
      return this.per;
   }

   private void loadConfig(YamlConfiguration config) {
      this.per_land_admin = config.getString("per_land_admin");
      this.per = config.getString("zone.per");
      this.perNoLimit = config.getString("zone.perNoLimit");
      this.buyWorld = new HashListImpl();

      for(String s : config.getStringList("zone.buyWorld")) {
         this.buyWorld.add(s);
      }

      this.addFlagsHash = new HashMap();

      for(String s : config.getStringList("zone.addFlags")) {
         String flag = s.split(":")[0];
         int value = Integer.parseInt(s.split(":")[1]);
         this.addFlagsHash.put(flag, value);
      }

      this.baseCost = config.getInt("zone.baseCost");
      this.perCost = config.getDouble("create.perCost");
      this.minSize = config.getInt("zone.limit.size.min");
      this.maxSize = config.getInt("zone.limit.size.max");
      this.minXZRange = config.getInt("zone.limit.xzRange.min");
      this.maxXZRange = config.getInt("zone.limit.xzRange.max");
      this.minYRange = config.getInt("zone.limit.yRange.min");
      this.maxYRange = config.getInt("zone.limit.yRange.max");
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
