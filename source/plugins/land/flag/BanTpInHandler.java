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

public class BanTpInHandler implements Listener {
   private static final String FLAG_BAN_TP_IN = "banTpIn";
   private LandManager landManager;
   private String pn;
   private boolean use;
   private String per;
   private String tip;

   public BanTpInHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      landManager.register("banTpIn", this.tip, this.use, true, false, this.per);
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
      Location toLoc = e.getTo();
      if (toLoc != null) {
         Land land = this.landManager.getHighestPriorityLand(toLoc);
         if (land != null && land.hasFlag("banTpIn")) {
            Land from = this.landManager.getHighestPriorityLand(e.getFrom());
            if (from != null && from.getId() == land.getId()) {
               return;
            }

            if (land.hasPer("banTpIn", e.getPlayer().getName())) {
               return;
            }

            e.getPlayer().sendMessage(UtilFormat.format(this.pn, "tip5", new Object[]{"banTpIn"}));
            e.setCancelled(true);
         }

      }
   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("banTpIn.use");
      this.per = config.getString("banTpIn.per");
      this.tip = Util.convert(config.getString("banTpIn.tip"));
   }
}
