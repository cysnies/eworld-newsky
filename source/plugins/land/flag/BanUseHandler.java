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

public class BanUseHandler implements Listener {
   private static final String FLAG_BAN_USE = "banUse";
   private LandManager landManager;
   private String pn;
   private boolean use;
   private String per;
   private String tip;
   private String useType;

   public BanUseHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      landManager.register("banUse", this.tip, this.use, true, false, this.per);
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
      priority = EventPriority.NORMAL,
      ignoreCancelled = true
   )
   public void onPlayerInteract(PlayerInteractEvent e) {
      try {
         if (e.hasBlock() && e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && UtilTypes.checkItem(this.pn, this.useType, "" + e.getClickedBlock().getTypeId())) {
            Location loc = e.getClickedBlock().getLocation();
            Land land = this.landManager.getHighestPriorityLand(loc);
            if (land != null && land.hasFlag("banUse")) {
               if (land.hasPer("banUse", e.getPlayer().getName())) {
                  return;
               }

               e.getPlayer().sendMessage(UtilFormat.format(this.pn, "tip15", new Object[]{"banUse"}));
               e.setCancelled(true);
            }
         }
      } catch (InvalidTypeException e1) {
         e1.printStackTrace();
      }

   }

   public static String getFlagBanUse() {
      return "banUse";
   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("banUse.use");
      this.per = config.getString("banUse.per");
      this.tip = Util.convert(config.getString("banUse.tip"));
      this.useType = config.getString("banUse.blockType");
   }
}
