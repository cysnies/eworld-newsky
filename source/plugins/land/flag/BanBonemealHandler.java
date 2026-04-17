package flag;

import land.Land;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class BanBonemealHandler implements Listener {
   private static final String FLAG_BAN_BONEMEAL = "banBonemeal";
   private LandManager landManager;
   private String pn;
   private boolean use;
   private String per;
   private String tip;

   public BanBonemealHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      landManager.register("banBonemeal", this.tip, this.use, true, false, this.per);
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
   public void onPlayerInteract(PlayerInteractEvent e) {
      if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.hasItem() && e.getItem().getTypeId() == 351 && e.getItem().getDurability() == 15) {
         Land land = this.landManager.getHighestPriorityLand(e.getClickedBlock().getLocation());
         if (land != null && land.hasFlag("banBonemeal")) {
            if (land.hasPer("banBonemeal", e.getPlayer().getName())) {
               return;
            }

            e.getPlayer().sendMessage(UtilFormat.format(this.pn, "tip24", new Object[]{"banBonemeal"}));
            e.setCancelled(true);
         }
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("banBonemeal.use");
      this.per = config.getString("banBonemeal.per");
      this.tip = Util.convert(config.getString("banBonemeal.tip"));
   }
}
