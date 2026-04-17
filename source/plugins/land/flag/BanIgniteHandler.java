package flag;

import land.Land;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.types.InvalidTypeException;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilTypes;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class BanIgniteHandler implements Listener {
   private static final String FLAG_BAN_IGNITE = "banIgnite";
   private LandManager landManager;
   private String pn;
   private boolean use;
   private String per;
   private String tip;
   private String igniteType;

   public BanIgniteHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      landManager.register("banIgnite", this.tip, this.use, true, false, this.per);
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
   public void onPlayerInteract(PlayerInteractEvent e) {
      try {
         if (e.hasItem() && UtilTypes.checkItem(this.pn, this.igniteType, "" + e.getItem().getTypeId())) {
            Location loc;
            if (e.hasBlock()) {
               loc = e.getClickedBlock().getLocation();
            } else {
               loc = e.getPlayer().getLocation();
            }

            Land land = this.landManager.getHighestPriorityLand(loc);
            if (land != null && land.hasFlag("banIgnite")) {
               if (land.hasPer("banIgnite", e.getPlayer().getName())) {
                  return;
               }

               e.getPlayer().sendMessage(UtilFormat.format(this.pn, "tip17", new Object[]{"banIgnite"}));
               e.setCancelled(true);
            }
         }
      } catch (InvalidTypeException e1) {
         e1.printStackTrace();
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("banIgnite.use");
      this.per = config.getString("banIgnite.per");
      this.tip = Util.convert(config.getString("banIgnite.tip"));
      this.igniteType = config.getString("banIgnite.igniteType");
   }
}
