package flag;

import land.Land;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class BanPlaceHandler implements Listener {
   private static final String FLAG_BAN_PLACE = "banPlace";
   private LandManager landManager;
   private String pn;
   private boolean use;
   private String per;
   private String tip;

   public BanPlaceHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      landManager.register("banPlace", this.tip, this.use, true, true, this.per);
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
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onBlockPlace(BlockPlaceEvent e) {
      Land land = this.landManager.getHighestPriorityLand(e.getBlockPlaced().getLocation());
      if (land != null && land.hasFlag("banPlace")) {
         if (land.hasPer("banPlace", e.getPlayer().getName())) {
            return;
         }

         if (land.getFlag("banPlace") == e.getBlock().getTypeId()) {
            return;
         }

         e.getPlayer().sendMessage(UtilFormat.format(this.pn, "tip8", new Object[]{"banPlace"}));
         e.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onHangingPlace(HangingPlaceEvent e) {
      Player p = e.getPlayer();
      Land land = this.landManager.getHighestPriorityLand(e.getEntity().getLocation());
      if (land != null && land.hasFlag("banPlace")) {
         if (land.hasPer("banPlace", p.getName())) {
            return;
         }

         p.sendMessage(UtilFormat.format(this.pn, "tip8", new Object[]{"banPlace"}));
         e.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
      if (e.getRightClicked().getType().equals(EntityType.ITEM_FRAME)) {
         Land land = this.landManager.getHighestPriorityLand(e.getRightClicked().getLocation());
         if (land != null && land.hasFlag("banPlace")) {
            if (land.hasPer("banPlace", e.getPlayer().getName())) {
               return;
            }

            e.getPlayer().sendMessage(UtilFormat.format(this.pn, "tip8", new Object[]{"banPlace"}));
            e.setCancelled(true);
         }
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("banPlace.use");
      this.per = config.getString("banPlace.per");
      this.tip = config.getString("banPlace.tip");
   }
}
