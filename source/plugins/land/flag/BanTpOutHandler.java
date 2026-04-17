package flag;

import land.Land;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class BanTpOutHandler implements Listener {
   private static final String FLAG_BAN_TP_OUT = "banTpOut";
   private LandManager landManager;
   private String pn;
   private boolean use;
   private String per;
   private String tip;

   public BanTpOutHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      landManager.register("banTpOut", this.tip, this.use, true, false, this.per);
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
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerTeleport(PlayerTeleportEvent e) {
      Location fromLoc = e.getFrom();
      if (fromLoc != null) {
         Land land = this.landManager.getHighestPriorityLand(fromLoc);
         if (land != null && land.hasFlag("banTpOut")) {
            Land to = this.landManager.getHighestPriorityLand(e.getTo());
            if (to != null && to.getId() == land.getId()) {
               return;
            }

            if (land.hasPer("banTpOut", e.getPlayer().getName())) {
               return;
            }

            e.getPlayer().sendMessage(UtilFormat.format(this.pn, "tip19", new Object[]{"banTpOut"}));
            e.setCancelled(true);
         }

      }
   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("banTpOut.use");
      this.per = config.getString("banTpOut.per");
      this.tip = Util.convert(config.getString("banTpOut.tip"));
   }
}
