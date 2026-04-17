package landHandler;

import event.LandRemoveEvent;
import event.PlayerLandChangeEvent;
import java.util.HashMap;
import land.Land;
import land.LandCmd;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class CmdHandler implements Listener {
   private LandManager landManager;
   private String pn;
   private String per;
   private HashMap cmdHash;

   public CmdHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
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
      priority = EventPriority.LOWEST
   )
   public void onLandRemove(LandRemoveEvent e) {
      LandCmd landCmd = (LandCmd)this.cmdHash.get(e.getRemovedLand());
      if (landCmd != null) {
         this.landManager.removeLandCmd(landCmd);
         this.cmdHash.remove(e.getRemovedLand());
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerLandChange(PlayerLandChangeEvent e) {
      Land land = null;

      for(Land l : e.getEnterList()) {
         if (land == null || l.getLevel() > land.getLevel()) {
            land = l;
         }
      }

      if (land != null) {
         LandCmd landCmd = (LandCmd)this.cmdHash.get(land);
         if (landCmd != null) {
            String[] var7;
            for(String s : var7 = landCmd.getCmd().split(";")) {
               e.getPlayer().chat(s);
            }

         }
      }
   }

   public void set(CommandSender sender, String name, String info) {
      if (sender instanceof Player) {
         Player p = (Player)sender;
         if (!UtilPer.checkPer(p, this.per)) {
            return;
         }
      }

      Land land = this.landManager.getLand(sender, name);
      if (land != null) {
         LandCmd landCmd = new LandCmd(land.getId(), info);
         this.landManager.addLandCmd(landCmd);
         this.cmdHash.put(land, landCmd);
         sender.sendMessage(UtilFormat.format(this.pn, "landCmdAdd", new Object[]{land.getName(), land.getId(), info}));
      }
   }

   public void remove(CommandSender sender, String name) {
      if (sender instanceof Player) {
         Player p = (Player)sender;
         if (!UtilPer.checkPer(p, this.per)) {
            return;
         }
      }

      Land land = this.landManager.getLand(sender, name);
      if (land != null) {
         LandCmd landCmd = (LandCmd)this.cmdHash.get(land);
         if (landCmd == null) {
            sender.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(490)}));
         } else {
            this.landManager.removeLandCmd(landCmd);
            this.cmdHash.remove(land);
            sender.sendMessage(UtilFormat.format(this.pn, "landCmdRemove", new Object[]{land.getName(), land.getId()}));
         }
      }
   }

   public void show(CommandSender sender, String name) {
      if (sender instanceof Player) {
         Player p = (Player)sender;
         if (!UtilPer.checkPer(p, this.per)) {
            return;
         }
      }

      Land land = this.landManager.getLand(sender, name);
      if (land != null) {
         LandCmd landCmd = (LandCmd)this.cmdHash.get(land);
         if (landCmd == null) {
            sender.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(480)}));
         } else {
            sender.sendMessage(UtilFormat.format((String)null, "success", new Object[]{this.get(485)}));
            sender.sendMessage(landCmd.getCmd());
         }
      }
   }

   private void loadConfig(YamlConfiguration config) {
      this.per = config.getString("cmd.per");
   }

   private void loadData() {
      this.cmdHash = new HashMap();

      for(LandCmd landCmd : this.landManager.getAllLandCmds()) {
         Land land = this.landManager.getLand(landCmd.getLandId());
         if (land != null) {
            this.cmdHash.put(land, landCmd);
         }
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
