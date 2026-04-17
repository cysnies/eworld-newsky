package flag;

import land.Land;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;

public class BanBreakHandler implements Listener {
   private static final String FLAG_BAN_BREAK = "banBreak";
   private LandManager landManager;
   private String pn;
   private boolean use;
   private String per;
   private String tip;

   public BanBreakHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      landManager.register("banBreak", this.tip, this.use, true, true, this.per);
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
   public void onBlockBreak(BlockBreakEvent e) {
      Land land = this.landManager.getHighestPriorityLand(e.getBlock().getLocation());
      if (land != null && land.hasFlag("banBreak")) {
         if (land.hasPer("banBreak", e.getPlayer().getName())) {
            return;
         }

         if (land.getFlag("banBreak") == e.getBlock().getTypeId()) {
            return;
         }

         e.getPlayer().sendMessage(UtilFormat.format(this.pn, "tip7", new Object[]{"banBreak"}));
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
         if (land != null && land.hasFlag("banBreak")) {
            if (land.hasPer("banBreak", p.getName())) {
               return;
            }

            p.sendMessage(UtilFormat.format(this.pn, "tip7", new Object[]{"banBreak"}));
            e.setCancelled(true);
         }
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("banBreak.use");
      this.per = config.getString("banBreak.per");
      this.tip = config.getString("banBreak.tip");
   }
}
