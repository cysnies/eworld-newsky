package flag;

import land.Land;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class BanEntityChangeHandler implements Listener {
   private static final String FLAG_BAN_ENTITY_CHANGE = "banEntityChange";
   private LandManager landManager;
   private String pn;
   private boolean use;
   private String per;
   private String tip;

   public BanEntityChangeHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      landManager.register("banEntityChange", this.tip, this.use, false, false, this.per);
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
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onEntityChangeBlock(EntityChangeBlockEvent e) {
      if (e.getEntityType().equals(EntityType.SILVERFISH) || e.getEntityType().equals(EntityType.ENDERMAN)) {
         Land land = this.landManager.getHighestPriorityLand(e.getBlock().getLocation());
         if (land != null && land.hasFlag("banEntityChange")) {
            e.setCancelled(true);
         }
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("banEntityChange.use");
      this.per = config.getString("banEntityChange.per");
      this.tip = Util.convert(config.getString("banEntityChange.tip"));
   }
}
