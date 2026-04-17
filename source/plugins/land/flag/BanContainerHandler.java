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
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class BanContainerHandler implements Listener {
   private static final String FLAG_BAN_CONTAINER = "banContainer";
   private LandManager landManager;
   private String pn;
   private boolean use;
   private String per;
   private String tip;
   private String containerType;

   public BanContainerHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      landManager.register("banContainer", this.tip, this.use, true, false, this.per);
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
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onPlayerInteract(PlayerInteractEvent e) {
      try {
         if (e.hasBlock() && e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && UtilTypes.checkItem(this.pn, this.containerType, "" + e.getClickedBlock().getTypeId())) {
            Location loc = e.getClickedBlock().getLocation();
            Land land = this.landManager.getHighestPriorityLand(loc);
            if (land != null && land.hasFlag("banContainer")) {
               if (land.hasPer("banContainer", e.getPlayer().getName())) {
                  return;
               }

               e.getPlayer().sendMessage(UtilFormat.format(this.pn, "tip11", new Object[]{"banContainer"}));
               e.setCancelled(true);
            }
         }
      } catch (InvalidTypeException e1) {
         e1.printStackTrace();
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("banContainer.use");
      this.per = config.getString("banContainer.per");
      this.tip = Util.convert(config.getString("banContainer.tip"));
      this.containerType = config.getString("banContainer.blockType");
   }
}
