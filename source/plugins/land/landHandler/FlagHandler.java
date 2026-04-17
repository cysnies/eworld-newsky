package landHandler;

import event.FlagAddEvent;
import event.FlagRemoveEvent;
import event.FlagSetEvent;
import java.util.HashMap;
import java.util.Iterator;
import land.Land;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class FlagHandler implements Listener {
   private LandManager landManager;
   private Server server;
   private String pn;
   private String per_land_admin;
   private String per;
   private HashList invaidFlags;
   private HashMap flagHash;

   public FlagHandler(LandManager landManager) {
      this.landManager = landManager;
      this.server = landManager.getServer();
      this.pn = landManager.getLandMain().getPn();
      this.flagHash = new HashMap();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
   }

   public void init() {
      this.checkInvalidFlags();
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void reloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   public void register(String flag, String tip, boolean use, boolean player, boolean value, String per) {
      if (!this.invaidFlags.has(flag)) {
         this.flagHash.put(flag, new Flag(flag, tip, use, player, value, per));
      }

   }

   public void addFlag(Land land, String flagName, int flagValue) {
      if (!land.hasFlag(flagName)) {
         land.addFlag(flagName, flagValue);
         FlagAddEvent flagAddEvent = new FlagAddEvent(land, flagName, flagValue);
         this.server.getPluginManager().callEvent(flagAddEvent);
      }
   }

   public void setFlag(Land land, String flagName, int flagValue) {
      if (land.getFlag(flagName) != flagValue) {
         land.setFlag(flagName, flagValue);
         FlagSetEvent flagSetEvent = new FlagSetEvent(land, flagName, land.getFlag(flagName), flagValue);
         this.server.getPluginManager().callEvent(flagSetEvent);
      }
   }

   public void removeFlag(Land land, String flagName) {
      if (land.hasFlag(flagName)) {
         land.removeFlag(flagName);
         FlagRemoveEvent flagRemoveEvent = new FlagRemoveEvent(land, flagName);
         this.server.getPluginManager().callEvent(flagRemoveEvent);
      }
   }

   public String getPlayer(String s) {
      try {
         Flag flag = (Flag)this.flagHash.get(s);
         return flag.isPlayer() ? this.get(1005) : this.get(1010);
      } catch (Exception var3) {
         return this.get(1012);
      }
   }

   public String getValue(String s) {
      try {
         Flag flag = (Flag)this.flagHash.get(s);
         return !flag.isValue() ? this.get(1040) : this.get(1035);
      } catch (Exception var3) {
         return this.get(1012);
      }
   }

   public HashMap getFlagHash() {
      return this.flagHash;
   }

   public String getPer() {
      return this.per;
   }

   public boolean addFlag(Player p, String name, String flagName) {
      if (!UtilPer.checkPer(p, this.per)) {
         return false;
      } else {
         Land land = this.landManager.getLand(p, name);
         if (land == null) {
            return false;
         } else if (!p.getName().equals(land.getOwner()) && !UtilPer.hasPer(p, this.per_land_admin)) {
            p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(740)}));
            return false;
         } else if (land.isFix()) {
            p.sendMessage(this.get(1220));
            return false;
         } else if (land.hasFlag(flagName)) {
            p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(275)}));
            return false;
         } else if (!this.flagHash.containsKey(flagName)) {
            p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(270)}));
            return false;
         } else {
            Flag flag = (Flag)this.flagHash.get(flagName);
            if (!flag.getPer().isEmpty() && !UtilPer.hasPer(p, flag.getPer())) {
               p.sendMessage(UtilFormat.format(this.pn, "landShow22", new Object[]{flag.getPer()}));
               return false;
            } else {
               this.addFlag(land, flagName, 0);
               p.sendMessage(UtilFormat.format(this.pn, "landFlagAdd", new Object[]{land.getName(), land.getId(), flagName, 0}));
               return true;
            }
         }
      }
   }

   public boolean setFlag(Player p, String name, String flagName, String s) {
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
         } else if (!land.hasFlag(flagName)) {
            p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(280)}));
            return true;
         } else {
            Flag f = (Flag)this.flagHash.get(flagName);
            if (f == null) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(270)}));
               return true;
            } else if (!f.isValue()) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(272)}));
               return true;
            } else {
               Flag flag = (Flag)this.flagHash.get(flagName);
               if (!flag.getPer().isEmpty() && !UtilPer.hasPer(p, flag.getPer())) {
                  p.sendMessage(UtilFormat.format(this.pn, "landShow22", new Object[]{flagName}));
                  return true;
               } else {
                  int value;
                  try {
                     value = Integer.parseInt(s);
                  } catch (NumberFormatException var10) {
                     p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(110)}));
                     return true;
                  }

                  this.setFlag(land, flagName, value);
                  p.sendMessage(UtilFormat.format(this.pn, "landFlagSet", new Object[]{land.getName(), land.getId(), flagName, value}));
                  return true;
               }
            }
         }
      }
   }

   public void removeFlag(Player p, String name, String flagName) {
      if (UtilPer.checkPer(p, this.per)) {
         Land land = this.landManager.getLand(p, name);
         if (land != null) {
            if (!p.getName().equals(land.getOwner()) && !UtilPer.hasPer(p, this.per_land_admin)) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(740)}));
            } else if (land.isFix()) {
               p.sendMessage(this.get(1220));
            } else if (!land.hasFlag(flagName)) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(250)}));
            } else {
               Flag flag = (Flag)this.flagHash.get(flagName);
               if (!flag.getPer().isEmpty() && !UtilPer.hasPer(p, flag.getPer())) {
                  p.sendMessage(UtilFormat.format(this.pn, "landShow22", new Object[]{flag.getPer()}));
               } else {
                  this.removeFlag(land, flagName);
                  p.sendMessage(UtilFormat.format(this.pn, "landFlagRemove", new Object[]{land.getName(), land.getId(), flagName}));
               }
            }
         }
      }
   }

   private void loadConfig(YamlConfiguration config) {
      this.per_land_admin = config.getString("per_land_admin");
      this.per = config.getString("flag.per");
      this.invaidFlags = new HashListImpl();

      for(String flag : config.getStringList("flag.invaidFlags")) {
         this.invaidFlags.add(flag);
      }

   }

   private void checkInvalidFlags() {
      for(Land land : this.landManager.getLandCheck().getAllLands()) {
         Iterator<String> it = land.getFlags().keySet().iterator();

         while(it.hasNext()) {
            String flagName = (String)it.next();
            if (!this.flagHash.containsKey(flagName)) {
               it.remove();
            }
         }

         Iterator<String> it2 = land.getPers().keySet().iterator();

         while(it2.hasNext()) {
            String flagName = (String)it2.next();
            if (!this.flagHash.containsKey(flagName)) {
               it2.remove();
            }
         }
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   public class Flag {
      private String name;
      private String tip;
      private boolean use;
      private boolean player;
      private boolean value;
      private String per;

      public Flag(String name, String tip, boolean use, boolean player, boolean value, String per) {
         this.name = name;
         this.tip = tip;
         this.use = use;
         this.player = player;
         this.value = value;
         this.per = per;
         if (per == null) {
            per = "";
         }

      }

      public String getName() {
         return this.name;
      }

      public void setName(String name) {
         this.name = name;
      }

      public String getTip() {
         return this.tip;
      }

      public void setTip(String tip) {
         this.tip = tip;
      }

      public boolean isUse() {
         return this.use;
      }

      public void setUse(boolean use) {
         this.use = use;
      }

      public boolean isPlayer() {
         return this.player;
      }

      public void setPlayer(boolean player) {
         this.player = player;
      }

      public String getPer() {
         return this.per;
      }

      public boolean isValue() {
         return this.value;
      }

      public void setValue(boolean value) {
         this.value = value;
      }

      public void setPer(String per) {
         this.per = per;
      }
   }
}
