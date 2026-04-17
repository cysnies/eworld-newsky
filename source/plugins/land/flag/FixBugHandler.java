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
import org.bukkit.event.block.BlockPistonExtendEvent;

public class FixBugHandler implements Listener {
   private static final String FLAG_FIXBUG = "fixBug";
   private LandManager landManager;
   private String pn;
   private boolean use;
   private String per;
   private String tip;

   public FixBugHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      landManager.register("fixBug", this.tip, this.use, false, false, this.per);
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
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onBlockFromTo(BlockFromToEvent e) {
      try {
         int id = e.getBlock().getTypeId();
         if (id == 8 || id == 9) {
            Land land = this.landManager.getHighestPriorityLand(e.getBlock().getLocation());
            if (land != null && land.hasFlag("fixBug")) {
               land = this.landManager.getHighestPriorityLand(e.getBlock().getRelative(e.getFace().getOppositeFace()).getLocation());
               if (land != null && land.hasFlag("fixBug")) {
                  return;
               }

               e.setCancelled(true);
               return;
            }

            e.setCancelled(true);
            return;
         }
      } catch (Exception var4) {
         e.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onBlockPistonExtend(BlockPistonExtendEvent e) {
      try {
         Land land = this.landManager.getHighestPriorityLand(e.getBlock().getRelative(e.getDirection()).getLocation());
         if (land != null && land.hasFlag("fixBug")) {
            e.setCancelled(true);
            return;
         }

         land = this.landManager.getHighestPriorityLand(e.getBlock().getRelative(e.getDirection(), 2).getLocation());
         if (land != null && land.hasFlag("fixBug")) {
            e.setCancelled(true);
            return;
         }
      } catch (Exception var3) {
         e.setCancelled(true);
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("fixBug.use");
      this.per = config.getString("fixBug.per");
      this.tip = config.getString("fixBug.tip");
   }
}
