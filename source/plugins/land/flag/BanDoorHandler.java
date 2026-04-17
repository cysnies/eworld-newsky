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

public class BanDoorHandler implements Listener {
   private static final String FLAG_BAN_DOOR = "banDoor";
   private LandManager landManager;
   private String pn;
   private boolean use;
   private String per;
   private String tip;
   private String doorType;

   public BanDoorHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      landManager.register("banDoor", this.tip, this.use, true, false, this.per);
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
         if (e.hasBlock() && e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && UtilTypes.checkItem(this.pn, this.doorType, "" + e.getClickedBlock().getTypeId())) {
            Location loc = e.getClickedBlock().getLocation();
            Land land = this.landManager.getHighestPriorityLand(loc);
            if (land != null && land.hasFlag("banDoor")) {
               if (land.hasPer("banDoor", e.getPlayer().getName())) {
                  return;
               }

               e.getPlayer().sendMessage(UtilFormat.format(this.pn, "tip13", new Object[]{"banDoor"}));
               e.setCancelled(true);
            }
         }
      } catch (InvalidTypeException e1) {
         e1.printStackTrace();
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("banDoor.use");
      this.per = config.getString("banDoor.per");
      this.tip = Util.convert(config.getString("banDoor.tip"));
      this.doorType = config.getString("banDoor.blockType");
   }
}
