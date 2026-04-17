package landHandler;

import event.LandCreateEvent;
import event.LandRemoveEvent;
import event.PlayerLandChangeEvent;
import java.util.HashMap;
import land.EnterTip;
import land.Land;
import land.LeaveTip;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class TipHandler implements Listener {
   private LandManager landManager;
   private String pn;
   private String defaultEnterTip;
   private String defaultLeaveTip;
   private int maxLength;
   private HashMap enterTipHash;
   private HashMap leaveTipHash;
   private String per_land_admin;
   private String per;

   public TipHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      this.loadData();
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

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onLandCreate(LandCreateEvent e) {
      EnterTip enterTip = new EnterTip(e.getLand().getId(), this.defaultEnterTip);
      this.enterTipHash.put(e.getLand(), enterTip);
      this.landManager.addEnterLandTip(enterTip);
      LeaveTip leaveTip = new LeaveTip(e.getLand().getId(), this.defaultLeaveTip);
      this.leaveTipHash.put(e.getLand(), leaveTip);
      this.landManager.addLeaveLandTip(leaveTip);
      Player p = Bukkit.getServer().getPlayerExact(e.getLand().getOwner());
      if (p != null) {
         p.sendMessage(UtilFormat.format(this.pn, "landShow8", new Object[]{this.defaultEnterTip}));
         p.sendMessage(UtilFormat.format(this.pn, "landShow9", new Object[]{this.defaultLeaveTip}));
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onLandRemove(LandRemoveEvent e) {
      EnterTip enterTip = (EnterTip)this.enterTipHash.get(e.getRemovedLand());
      if (enterTip != null) {
         this.enterTipHash.remove(e.getRemovedLand());
         this.landManager.removeEnterLandTip(enterTip);
      }

      LeaveTip leaveTip = (LeaveTip)this.leaveTipHash.get(e.getRemovedLand());
      if (leaveTip != null) {
         this.leaveTipHash.remove(e.getRemovedLand());
         this.landManager.removeLeaveLandTip(leaveTip);
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerLandChange(PlayerLandChangeEvent e) {
      Player p = e.getPlayer();

      for(Land land : e.getLeaveList()) {
         if (this.leaveTipHash.containsKey(land)) {
            String tip = ((LeaveTip)this.leaveTipHash.get(land)).getTip();
            if (tip != null && !tip.isEmpty()) {
               p.sendMessage(this.getMsg(p, land, tip));
            }
         }
      }

      for(Land land : e.getEnterList()) {
         if (this.enterTipHash.containsKey(land)) {
            String tip = ((EnterTip)this.enterTipHash.get(land)).getTip();
            if (tip != null && !tip.isEmpty()) {
               p.sendMessage(this.getMsg(p, land, tip));
            }
         }
      }

   }

   public String getEnterTip(Land land) {
      try {
         String result = ((EnterTip)this.enterTipHash.get(land)).getTip();
         return result == null ? "" : result;
      } catch (Exception var3) {
         return "";
      }
   }

   public String getLeaveTip(Land land) {
      try {
         String result = ((LeaveTip)this.leaveTipHash.get(land)).getTip();
         return result == null ? "" : result;
      } catch (Exception var3) {
         return "";
      }
   }

   public void setEnterTip(Player p, String name, String msg) {
      if (UtilPer.checkPer(p, this.per)) {
         Land land = this.landManager.getLand(p, name);
         if (land != null) {
            if (!land.getOwner().equals(p.getName()) && !UtilPer.hasPer(p, this.per_land_admin)) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(740)}));
            } else if (land.isFix()) {
               p.sendMessage(this.get(1220));
            } else {
               EnterTip enterTip = (EnterTip)this.enterTipHash.get(land);
               if (msg.equalsIgnoreCase("none")) {
                  this.enterTipHash.remove(land);
                  if (enterTip != null) {
                     this.landManager.removeEnterLandTip(enterTip);
                     p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(90)}));
                  } else {
                     p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(92)}));
                  }
               } else {
                  msg = Util.convert(msg);
                  msg = msg.substring(0, Math.min(msg.length(), this.maxLength));
                  if (enterTip == null) {
                     enterTip = new EnterTip(land.getId(), msg);
                  } else {
                     enterTip.setTip(msg);
                  }

                  this.landManager.addEnterLandTip(enterTip);
                  this.enterTipHash.put(land, enterTip);
                  p.sendMessage(UtilFormat.format(this.pn, "landSetEnterTip", new Object[]{land.getName(), msg}));
               }

            }
         }
      }
   }

   public void setLeaveTip(Player p, String name, String msg) {
      if (UtilPer.checkPer(p, this.per)) {
         Land land = this.landManager.getLand(p, name);
         if (land != null) {
            if (!land.getOwner().equals(p.getName()) && !UtilPer.hasPer(p, this.per_land_admin)) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(740)}));
            } else if (land.isFix()) {
               p.sendMessage(this.get(1220));
            } else {
               LeaveTip leaveTip = (LeaveTip)this.leaveTipHash.get(land);
               if (msg.equalsIgnoreCase("none")) {
                  this.leaveTipHash.remove(land);
                  if (leaveTip != null) {
                     this.landManager.removeLeaveLandTip(leaveTip);
                     p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(95)}));
                  } else {
                     p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(97)}));
                  }
               } else {
                  msg = Util.convert(msg);
                  msg = msg.substring(0, Math.min(msg.length(), this.maxLength));
                  if (leaveTip == null) {
                     leaveTip = new LeaveTip(land.getId(), msg);
                  } else {
                     leaveTip.setTip(msg);
                  }

                  this.landManager.addLeaveLandTip(leaveTip);
                  this.leaveTipHash.put(land, leaveTip);
                  p.sendMessage(UtilFormat.format(this.pn, "landSetLeaveTip", new Object[]{land.getName(), msg}));
               }

            }
         }
      }
   }

   public String getPer() {
      return this.per;
   }

   private void loadConfig(YamlConfiguration config) {
      this.per_land_admin = config.getString("per_land_admin");
      this.per = config.getString("tip.per");
      this.defaultEnterTip = Util.convert(config.getString("tip.defaultEnterTip"));
      this.defaultLeaveTip = Util.convert(config.getString("tip.defaultLeaveTip"));
      this.maxLength = config.getInt("tip.maxLength");
   }

   private void loadData() {
      this.enterTipHash = new HashMap();
      this.leaveTipHash = new HashMap();

      for(EnterTip enterTip : this.landManager.getAllEnterTips()) {
         Land land = this.landManager.getLand(enterTip.getLandId());
         if (land != null) {
            this.enterTipHash.put(land, enterTip);
         }
      }

      for(LeaveTip leaveTip : this.landManager.getAllLeaveTips()) {
         Land land = this.landManager.getLand(leaveTip.getLandId());
         if (land != null) {
            this.leaveTipHash.put(land, leaveTip);
         }
      }

   }

   private String getMsg(Player p, Land land, String msg) {
      if (p != null) {
         msg = msg.replace("<player>", p.getName());
      }

      msg = msg.replace("<id>", "" + land.getId()).replace("<name>", land.getName()).replace("<owner>", land.getOwner()).replace("<level>", "" + land.getLevel()).replace("<size>", "" + land.getSize()).replace("<world>", land.getRange().getP1().getWorld());
      return msg;
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
