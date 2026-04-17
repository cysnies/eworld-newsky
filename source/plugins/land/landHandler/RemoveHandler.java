package landHandler;

import land.Land;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class RemoveHandler implements Listener {
   private LandManager landManager;
   private String pn;
   private String per_land_admin;
   private String per;

   public RemoveHandler(LandManager landManager) {
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

   public void remove(Player p, String s) {
      if (UtilPer.checkPer(p, this.per)) {
         Land land = this.landManager.getLand(p, s);
         if (land != null) {
            if (!UtilPer.hasPer(p, this.per_land_admin) && land.isFix()) {
               p.sendMessage(this.get(1250));
            } else {
               String owner = land.getOwner();
               if (!owner.equals(p.getName()) && !UtilPer.hasPer(p, this.per_land_admin)) {
                  p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(740)}));
               } else {
                  if (land.getType() == 2) {
                     for(Land l : this.landManager.getLandCheck().getCollisionLands(land.getRange())) {
                        if (l.getType() == 3) {
                           p.sendMessage(UtilFormat.format(this.pn, "landShow19", new Object[]{l.getName(), l.getId()}));
                           return;
                        }
                     }
                  }

                  this.landManager.remove(land);
                  p.sendMessage(UtilFormat.format(this.pn, "landRemoved", new Object[]{land.getName(), land.getId()}));
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
      this.per = config.getString("remove.per");
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
