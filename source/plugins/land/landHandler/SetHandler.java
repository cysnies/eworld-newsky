package landHandler;

import event.LevelChangeEvent;
import event.NameChangeEvent;
import event.OverlapChangeEvent;
import event.OwnerChangeEvent;
import event.RangeChangeEvent;
import java.util.HashMap;
import land.Land;
import land.LandUser;
import land.Range;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilEco;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import lib.util.UtilSpeed;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

public class SetHandler implements Listener {
   private static final String SPEED_FRIEND = "toggleFriend";
   private LandManager landManager;
   private PluginManager pm;
   private String pn;
   private String per_land_admin;
   private String setOwnerPer;
   private String setNamePer;
   private String setLevelPer;
   private String setOverlapPer;
   private String setRangePer;
   private int cost;

   public SetHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pm = landManager.getLandMain().getPm();
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      UtilSpeed.register(this.pn, "toggleFriend");
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void reloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   public void toggleOverlap(Player p, String name) {
      if (UtilPer.checkPer(p, this.setOverlapPer)) {
         Land land = this.landManager.getLand(p, name);
         if (land != null) {
            if (land.isFix()) {
               p.sendMessage(this.get(1220));
            } else if (land.getType() != 1) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1255)}));
            } else if (!land.getOwner().equals(p.getName()) && !UtilPer.hasPer(p, this.per_land_admin)) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1045)}));
            } else {
               land.setOverlap(!land.isOverlap());
               this.landManager.addLand(land);
               String allow;
               if (land.isOverlap()) {
                  allow = this.get(750);
               } else {
                  allow = this.get(755);
               }

               p.sendMessage(UtilFormat.format(this.pn, "landSetOverlap", new Object[]{land.getName(), land.getId(), allow}));
               OverlapChangeEvent overlapChangeEvent = new OverlapChangeEvent(land);
               this.pm.callEvent(overlapChangeEvent);
            }
         }
      }
   }

   public boolean setOwner(Player p, String name, String newOwner) {
      if (!UtilPer.checkPer(p, this.setOwnerPer)) {
         return true;
      } else {
         Land land = this.landManager.getLand(p, name);
         if (land == null) {
            return true;
         } else {
            String oldOwner = land.getOwner();
            if (!land.getOwner().equals(p.getName()) && !UtilPer.hasPer(p, this.per_land_admin)) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(740)}));
               return true;
            } else if (land.isFix()) {
               p.sendMessage(this.get(1220));
               return true;
            } else {
               if (!newOwner.equalsIgnoreCase(Land.getSystem())) {
                  newOwner = Util.getRealName(p, newOwner);
                  if (newOwner == null) {
                     return false;
                  }
               }

               if (newOwner.equals(oldOwner)) {
                  p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(287)}));
                  return false;
               } else {
                  if (!newOwner.equalsIgnoreCase(Land.getSystem())) {
                     HashMap<String, LandUser> userHash = this.landManager.getLandUserHandler().getUserHash();
                     LandUser landUser = (LandUser)userHash.get(newOwner);
                     if (landUser == null) {
                        p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1245)}));
                        return false;
                     }

                     HashList<Land> landList = this.landManager.getUserLands(newOwner);
                     if (landList != null) {
                        int amount = 0;

                        for(Land l : landList) {
                           if (l.getType() != 3) {
                              ++amount;
                           }
                        }

                        if (amount >= landUser.getMaxLands()) {
                           p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(1240)}));
                           return false;
                        }
                     }
                  }

                  land.setOwner(newOwner);
                  this.landManager.addLand(land);
                  p.sendMessage(UtilFormat.format(this.pn, "landSetOwner", new Object[]{land.getName(), land.getId(), land.getOwner()}));
                  OwnerChangeEvent ownerChangeEvent = new OwnerChangeEvent(land, oldOwner);
                  this.pm.callEvent(ownerChangeEvent);
                  return true;
               }
            }
         }
      }
   }

   public boolean setName(Player p, String name, String newName) {
      if (!UtilPer.checkPer(p, this.setNamePer)) {
         return true;
      } else {
         Land land = this.landManager.getLand(p, name);
         if (land == null) {
            return true;
         } else {
            String oldName = land.getName();
            if (!land.getOwner().equals(p.getName()) && !UtilPer.hasPer(p, this.per_land_admin)) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(740)}));
               return true;
            } else if (land.isFix()) {
               p.sendMessage(this.get(1220));
               return true;
            } else if (newName.length() > Land.getNameMaxLength()) {
               p.sendMessage(UtilFormat.format(this.pn, "landNameErr", new Object[]{Land.getNameMaxLength()}));
               return false;
            } else if (newName.equals(oldName)) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(286)}));
               return false;
            } else if (this.landManager.getLand(newName) != null) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(230)}));
               return false;
            } else if (!this.landManager.getCreateHandler().checkName(p, newName)) {
               return false;
            } else {
               land.setName(newName);
               this.landManager.addLand(land);
               p.sendMessage(UtilFormat.format(this.pn, "landSetName", new Object[]{oldName, land.getId(), land.getName()}));
               NameChangeEvent nameChangeEvent = new NameChangeEvent(land, oldName);
               this.pm.callEvent(nameChangeEvent);
               return true;
            }
         }
      }
   }

   public boolean setLevel(Player p, String name, String s) {
      if (!UtilPer.checkPer(p, this.setLevelPer)) {
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
         } else {
            try {
               int newLevel = Integer.parseInt(s);
               int oldLevel = land.getLevel();
               if (oldLevel == newLevel) {
                  p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(205)}));
                  return false;
               } else {
                  newLevel = this.landManager.getFixedLevel(newLevel, land.getRange(), (Land)null);
                  land.setLevel(newLevel);
                  this.landManager.addLand(land);
                  p.sendMessage(UtilFormat.format(this.pn, "landSetLevel", new Object[]{land.getName(), land.getId(), newLevel}));
                  LevelChangeEvent levelChangeEvent = new LevelChangeEvent(land, oldLevel);
                  this.pm.callEvent(levelChangeEvent);
                  return true;
               }
            } catch (NumberFormatException var8) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(110)}));
               return false;
            }
         }
      }
   }

   public boolean setRange(Player p, String name) {
      if (!UtilPer.checkPer(p, this.setRangePer)) {
         return true;
      } else {
         Range range = this.landManager.getSelectHandler().getRange(p);
         if (range == null) {
            p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(105)}));
            return true;
         } else {
            Land land = this.landManager.getLand(p, name);
            return land == null ? false : this.setRange(p, land, range);
         }
      }
   }

   public boolean setRange(Player p, Land land, Range newRange) {
      if (!land.getOwner().equals(p.getName()) && !UtilPer.hasPer(p, this.per_land_admin)) {
         p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(740)}));
         return true;
      } else if (land.isFix()) {
         p.sendMessage(this.get(1220));
         return true;
      } else {
         long addSize = newRange.getSize() - land.getSize();
         long cost = 0L;
         if (addSize > 0L && !UtilPer.hasPer(p, this.per_land_admin)) {
            if (land.getType() == 1) {
               cost = this.landManager.getCreateHandler().getCost(p, addSize);
            } else if (land.getType() == 2) {
               cost = this.landManager.getZoneHandler().getCost(p, addSize);
            }
         }

         cost += (long)this.cost;
         if (UtilEco.get(p.getName()) < (double)cost) {
            p.sendMessage(UtilFormat.format(this.pn, "landAddRangeCost", new Object[]{cost, this.cost}));
            return true;
         } else {
            if (land.getType() == 1) {
               if (!land.isOverlap()) {
                  for(Land l : this.landManager.getLandCheck().getCollisionLands(newRange)) {
                     if (!l.isOverlap() && !l.equals(land)) {
                        p.sendMessage(UtilFormat.format(this.pn, "landOverlap2", new Object[]{l.getName(), l.getId()}));
                        return true;
                     }
                  }
               }
            } else if (land.getType() == 2) {
               HashList<Land> list0 = new HashListImpl();

               for(Land l : this.landManager.getLandCheck().getCollisionLands(land.getRange())) {
                  if (l.getType() == 3) {
                     list0.add(l);
                  }
               }

               for(Land l : list0) {
                  if (!l.getRange().isIn(newRange)) {
                     p.sendMessage(UtilFormat.format(this.pn, "landShow17", new Object[]{l.getName(), l.getId()}));
                     return true;
                  }
               }

               for(Land l : this.landManager.getLandCheck().getCollisionLands(newRange)) {
                  if (l.getType() != 3 && !l.isOverlap() && !l.equals(land)) {
                     p.sendMessage(UtilFormat.format(this.pn, "landOverlap2", new Object[]{l.getName(), l.getId()}));
                     return true;
                  }
               }
            } else {
               Land l0 = null;

               for(Land l : this.landManager.getLandCheck().getCollisionLands(land.getRange())) {
                  if (l.getType() == 2) {
                     l0 = l;
                     break;
                  }
               }

               if (l0 == null) {
                  p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1110)}));
                  return true;
               }

               if (!l0.getOwner().equals(p.getName())) {
                  p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1460)}));
                  return true;
               }

               if (!newRange.isIn(l0.getRange())) {
                  p.sendMessage(UtilFormat.format(this.pn, "landShow18", new Object[]{l0.getName(), l0.getId()}));
                  return true;
               }

               for(Land l : this.landManager.getLandCheck().getCollisionLands(newRange)) {
                  if (l.getType() != 2 && !l.isOverlap() && !l.equals(land)) {
                     p.sendMessage(UtilFormat.format(this.pn, "landOverlap2", new Object[]{l.getName(), l.getId()}));
                     return true;
                  }
               }
            }

            if (cost > 0L) {
               UtilEco.del(p.getName(), (double)cost);
               p.sendMessage(UtilFormat.format(this.pn, "landAddRangeCost2", new Object[]{cost}));
            }

            p.sendMessage(UtilFormat.format((String)null, "success", new Object[]{this.get(200)}));
            this.setRange(land, newRange);
            return true;
         }
      }
   }

   public void setRange(Land land, Range newRange) {
      Range oldRange = land.getRange();
      int level = this.landManager.getFixedLevel(land.getLevel(), newRange, land);
      land.setLevel(level);
      land.setRange(newRange.clone());
      this.landManager.addLand(land);
      RangeChangeEvent rangeChangeEvent = new RangeChangeEvent(land, oldRange);
      this.pm.callEvent(rangeChangeEvent);
   }

   public void expand(Player p, String name, int amount) {
      if (UtilPer.checkPer(p, this.setRangePer)) {
         Land land = this.landManager.getLand(p, name);
         if (land != null) {
            int xDir = 0;
            int yDir = 0;
            int zDir = 0;
            if (p.getLocation().getPitch() > 50.0F) {
               yDir = -1;
            } else if (p.getLocation().getPitch() < -50.0F) {
               yDir = 1;
            } else {
               double rot = (double)(p.getLocation().getYaw() % 360.0F);
               if (rot < (double)0.0F) {
                  rot += (double)360.0F;
               }

               if (!(rot < (double)45.0F) && !(rot > (double)315.0F)) {
                  if (rot > (double)45.0F && rot < (double)135.0F) {
                     xDir = -1;
                  } else if (rot > (double)135.0F && rot < (double)225.0F) {
                     zDir = -1;
                  } else {
                     xDir = 1;
                  }
               } else {
                  zDir = 1;
               }
            }

            Range result = land.getRange().clone();
            result.expand(xDir * amount, yDir * amount, zDir * amount);
            this.setRange(p, land, result);
         }
      }
   }

   public void contract(Player p, String name, int amount) {
      if (UtilPer.checkPer(p, this.setRangePer)) {
         Land land = this.landManager.getLand(p, name);
         if (land != null) {
            int xDir = 0;
            int yDir = 0;
            int zDir = 0;
            if (p.getLocation().getPitch() > 50.0F) {
               yDir = -1;
            } else if (p.getLocation().getPitch() < -50.0F) {
               yDir = 1;
            } else {
               double rot = (double)(p.getLocation().getYaw() % 360.0F);
               if (rot < (double)0.0F) {
                  rot += (double)360.0F;
               }

               if (!(rot < (double)45.0F) && !(rot > (double)315.0F)) {
                  if (rot > (double)45.0F && rot < (double)135.0F) {
                     xDir = -1;
                  } else if (rot > (double)135.0F && rot < (double)225.0F) {
                     zDir = -1;
                  } else {
                     xDir = 1;
                  }
               } else {
                  zDir = 1;
               }
            }

            Range result = land.getRange().clone();
            result.contract(xDir * amount, yDir * amount, zDir * amount);
            this.setRange(p, land, result);
         }
      }
   }

   public String getSetOverlapPer() {
      return this.setOverlapPer;
   }

   public String getSetNamePer() {
      return this.setNamePer;
   }

   public String getSetLevelPer() {
      return this.setLevelPer;
   }

   public String getSetOwnerPer() {
      return this.setOwnerPer;
   }

   public String getSetRangePer() {
      return this.setRangePer;
   }

   private void loadConfig(YamlConfiguration config) {
      this.per_land_admin = config.getString("per_land_admin");
      this.setOwnerPer = config.getString("set.setOwnerPer");
      this.setNamePer = config.getString("set.setNamePer");
      this.setLevelPer = config.getString("set.setLevelPer");
      this.setOverlapPer = config.getString("set.setOverlapPer");
      this.setRangePer = config.getString("set.setRangePer");
      this.cost = config.getInt("set.cost");
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
