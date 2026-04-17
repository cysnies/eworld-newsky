package flag;

import land.Land;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BanInteractHandler implements Listener {
   private static final String FLAG_BAN_INTERACT = "banInteract";
   private LandManager landManager;
   private String pn;
   private boolean use;
   private String per;
   private String tip;

   public BanInteractHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      landManager.register("banInteract", this.tip, this.use, true, false, this.per);
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
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPlayerInteract(PlayerInteractEvent e) {
      Location loc;
      if (e.hasBlock()) {
         loc = e.getClickedBlock().getLocation();
      } else {
         loc = e.getPlayer().getLocation();
      }

      Land land = this.landManager.getHighestPriorityLand(loc);
      if (land != null && land.hasFlag("banInteract")) {
         if (land.hasPer("banInteract", e.getPlayer().getName())) {
            return;
         }

         e.getPlayer().sendMessage(UtilFormat.format(this.pn, "tip12", new Object[]{"banInteract"}));
         e.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onHangingBreakByEntity(HangingBreakByEntityEvent e) {
      if (e.getRemover() instanceof Player) {
         Player p = (Player)e.getRemover();
         Land land = this.landManager.getHighestPriorityLand(e.getEntity().getLocation());
         if (land != null && land.hasFlag("banInteract")) {
            if (land.hasPer("banInteract", p.getName())) {
               return;
            }

            p.sendMessage(UtilFormat.format(this.pn, "tip12", new Object[]{"banInteract"}));
            e.setCancelled(true);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onHangingPlace(HangingPlaceEvent e) {
      Player p = e.getPlayer();
      Land land = this.landManager.getHighestPriorityLand(e.getEntity().getLocation());
      if (land != null && land.hasFlag("banInteract")) {
         if (land.hasPer("banInteract", p.getName())) {
            return;
         }

         p.sendMessage(UtilFormat.format(this.pn, "tip12", new Object[]{"banInteract"}));
         e.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onEntityChangeBlock(EntityChangeBlockEvent e) {
      if (e.getEntityType().equals(EntityType.SILVERFISH) || e.getEntityType().equals(EntityType.ENDERMAN)) {
         Land land = this.landManager.getHighestPriorityLand(e.getBlock().getLocation());
         if (land != null && land.hasFlag("banInteract")) {
            e.setCancelled(true);
         }
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("banInteract.use");
      this.per = config.getString("banInteract.per");
      this.tip = Util.convert(config.getString("banInteract.tip"));
   }
}
