package landHandler;

import land.Land;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PersHandler implements Listener {
   private static final String ALL = "@所有玩家@";
   private LandManager landManager;
   private String pn;
   private String per_land_admin;
   private String per;
   private int max;

   public PersHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void reloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   public String getPer() {
      return this.per;
   }

   public boolean addPer(Player p, String s, String flagName, String name) {
      if (!UtilPer.checkPer(p, this.per)) {
         return true;
      } else {
         Land land = this.landManager.getLand(p, s);
         if (land == null) {
            return true;
         } else if (!land.getOwner().equals(p.getName()) && !UtilPer.hasPer(p, this.per_land_admin)) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(740)}));
            return false;
         } else if (!land.hasFlag(flagName)) {
            p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1095)}));
            return true;
         } else {
            FlagHandler.Flag flag = (FlagHandler.Flag)this.landManager.getFlagHandler().getFlagHash().get(flagName);
            if (flag == null) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1090)}));
               return true;
            } else if (!flag.isPlayer()) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1320)}));
               return true;
            } else {
               HashList<String> pers = (HashList)land.getPers().get(flagName);
               if (pers != null && pers.has("@所有玩家@")) {
                  p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1455)}));
                  return true;
               } else {
                  if (name != null) {
                     name = Util.getRealName(p, name);
                     if (name == null) {
                        return false;
                     }

                     if (pers != null && pers.size() >= this.max) {
                        p.sendMessage(UtilFormat.format(this.pn, "landPersMax", new Object[]{this.max}));
                        return true;
                     }

                     if (name.equals(p.getName())) {
                        p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1430)}));
                        return false;
                     }

                     if (pers != null && pers.has(name)) {
                        p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1435)}));
                        return false;
                     }
                  }

                  if (pers == null) {
                     pers = new HashListImpl();
                     land.getPers().put(flagName, pers);
                  }

                  if (name == null) {
                     pers.clear();
                     pers.add("@所有玩家@");
                  } else {
                     pers.add(name);
                  }

                  this.landManager.addLand(land);
                  if (name == null) {
                     p.sendMessage(UtilFormat.format((String)null, "success", new Object[]{this.get(1315)}));
                  } else {
                     p.sendMessage(UtilFormat.format(this.pn, "landShow26", new Object[]{name}));
                  }

                  return false;
               }
            }
         }
      }
   }

   public boolean removePer(Player p, String s, String flagName, String name) {
      if (p != null && !UtilPer.checkPer(p, this.per)) {
         return true;
      } else {
         Land land = this.landManager.getLand(p, s);
         if (land == null) {
            return true;
         } else if (!land.getOwner().equals(p.getName()) && !UtilPer.hasPer(p, this.per_land_admin)) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(740)}));
            return false;
         } else if (!land.hasFlag(flagName)) {
            p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1095)}));
            return true;
         } else {
            FlagHandler.Flag flag = (FlagHandler.Flag)this.landManager.getFlagHandler().getFlagHash().get(flagName);
            if (flag == null) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1090)}));
               return true;
            } else if (!flag.isPlayer()) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1320)}));
               return true;
            } else {
               if (name != null) {
                  name = Util.getRealName(p, name);
                  if (name == null) {
                     return false;
                  }
               }

               HashList<String> pers = (HashList)land.getPers().get(flagName);
               if (pers != null && !pers.isEmpty()) {
                  if (name != null && !pers.has(name)) {
                     p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1105)}));
                     return false;
                  } else {
                     if (name == null) {
                        land.getPers().remove(flagName);
                     } else {
                        pers.remove(name);
                        if (pers.isEmpty()) {
                           land.getPers().remove(flagName);
                        }
                     }

                     this.landManager.addLand(land);
                     if (name == null) {
                        p.sendMessage(UtilFormat.format((String)null, "success", new Object[]{this.get(1310)}));
                     } else {
                        p.sendMessage(UtilFormat.format(this.pn, "landShow25", new Object[]{name}));
                     }

                     return false;
                  }
               } else {
                  p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1100)}));
                  return true;
               }
            }
         }
      }
   }

   public static String getAll() {
      return "@所有玩家@";
   }

   private void loadConfig(YamlConfiguration config) {
      this.per_land_admin = config.getString("per_land_admin");
      this.per = config.getString("pers.per");
      this.max = config.getInt("pers.max");
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
