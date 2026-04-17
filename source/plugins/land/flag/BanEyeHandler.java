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
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class BanEyeHandler implements Listener {
   private static final String FLAG_BAN_EYE = "banEye";
   private LandManager landManager;
   private String pn;
   private boolean use;
   private String per;
   private String tip;

   public BanEyeHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      landManager.register("banEye", this.tip, this.use, true, false, this.per);
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
      if (e.getCause().equals(TeleportCause.ENDER_PEARL)) {
         Location toLoc = e.getTo();
         if (toLoc == null) {
            return;
         }

         Land land = this.landManager.getHighestPriorityLand(toLoc);
         Land from = this.landManager.getHighestPriorityLand(e.getFrom());
         if (!this.canEye(land, e.getPlayer().getName()) || !this.canEye(from, e.getPlayer().getName())) {
            e.getPlayer().sendMessage(UtilFormat.format(this.pn, "tip3", new Object[]{"banEye"}));
            e.setCancelled(true);
         }
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("banEye.use");
      this.per = config.getString("banEye.per");
      this.tip = Util.convert(config.getString("banEye.tip"));
   }

   private boolean canEye(Land land, String name) {
      return land != null && land.hasFlag("banEye") ? land.hasPer("banEye", name) : true;
   }
}
