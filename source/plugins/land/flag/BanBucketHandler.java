package flag;

import land.Land;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

public class BanBucketHandler implements Listener {
   private static final String FLAG_BAN_BUCKET = "banBucket";
   private LandManager landManager;
   private String pn;
   private boolean use;
   private String per;
   private String tip;

   public BanBucketHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      landManager.register("banBucket", this.tip, this.use, true, false, this.per);
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
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onPlayerBucketEmpty(PlayerBucketEmptyEvent e) {
      if (e.getBlockClicked() != null) {
         Location loc = e.getBlockClicked().getRelative(e.getBlockFace()).getLocation();
         Land land = this.landManager.getHighestPriorityLand(loc);
         if (land != null && land.hasFlag("banBucket")) {
            if (land.hasPer("banBucket", e.getPlayer().getName())) {
               return;
            }

            e.getPlayer().sendMessage(UtilFormat.format(this.pn, "tip6", new Object[]{"banBucket"}));
            e.setCancelled(true);
         }

      }
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onPlayerBucketFill(PlayerBucketFillEvent e) {
      if (e.getBlockClicked() != null) {
         Location loc = e.getBlockClicked().getLocation();
         Land land = this.landManager.getHighestPriorityLand(loc);
         if (land != null && land.hasFlag("banBucket")) {
            if (land.hasPer("banBucket", e.getPlayer().getName())) {
               return;
            }

            e.getPlayer().sendMessage(UtilFormat.format(this.pn, "tip6", new Object[]{"banBucket"}));
            e.setCancelled(true);
         }

      }
   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("banBucket.use");
      this.per = config.getString("banBucket.per");
      this.tip = config.getString("banBucket.tip");
   }
}
