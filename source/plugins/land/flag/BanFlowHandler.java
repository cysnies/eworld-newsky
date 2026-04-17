package flag;

import land.Land;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

public class BanFlowHandler implements Listener {
   private static final String FLAG_BAN_FLOW = "banFlow";
   private LandManager landManager;
   private String pn;
   private boolean use;
   private String per;
   private String tip;

   public BanFlowHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      landManager.register("banFlow", this.tip, this.use, false, false, this.per);
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
   public void onBlockFromTo(BlockFromToEvent e) {
      Land land = this.landManager.getHighestPriorityLand(e.getBlock().getLocation());
      if (land != null && land.hasFlag("banFlow")) {
         e.setCancelled(true);
      } else {
         land = this.landManager.getHighestPriorityLand(e.getToBlock().getLocation());
         if (land != null && land.hasFlag("banFlow")) {
            e.setCancelled(true);
         }
      }
   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("banFlow.use");
      this.per = config.getString("banFlow.per");
      this.tip = config.getString("banFlow.tip");
   }
}
