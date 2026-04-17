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
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

public class BanContactHandler implements Listener {
   private static final String FLAG_BAN_CONTACT = "banContact";
   private LandManager landManager;
   private String pn;
   private boolean use;
   private String per;
   private String tip;
   private String contact;

   public BanContactHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      landManager.register("banContact", this.tip, this.use, true, false, this.per);
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
   public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
      try {
         if (UtilTypes.checkEntity(this.pn, this.contact, e.getRightClicked().getType().name())) {
            Location loc = e.getRightClicked().getLocation();
            Land land = this.landManager.getHighestPriorityLand(loc);
            if (land != null && land.hasFlag("banContact")) {
               if (land.hasPer("banContact", e.getPlayer().getName())) {
                  return;
               }

               e.getPlayer().sendMessage(UtilFormat.format(this.pn, "tip18", new Object[]{"banContact"}));
               e.setCancelled(true);
            }
         }
      } catch (InvalidTypeException e1) {
         e1.printStackTrace();
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onInventoryOpen(InventoryOpenEvent e) {
      if (e.getInventory().getHolder() != null && e.getInventory().getHolder() instanceof Horse && e.getPlayer() instanceof Player) {
         Horse horse = (Horse)e.getInventory().getHolder();
         Player p = (Player)e.getPlayer();
         Location loc = horse.getLocation();
         Land land = this.landManager.getHighestPriorityLand(loc);
         if (land != null && land.hasFlag("banContact")) {
            if (land.hasPer("banContact", p.getName())) {
               return;
            }

            p.sendMessage(UtilFormat.format(this.pn, "tip18", new Object[]{"banContact"}));
            e.setCancelled(true);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onVehicleEnter(VehicleEnterEvent e) {
      if (e.getVehicle() instanceof Horse && e.getEntered() instanceof Player) {
         Location loc = e.getVehicle().getLocation();
         Land land = this.landManager.getHighestPriorityLand(loc);
         if (land != null && land.hasFlag("banContact")) {
            Player p = (Player)e.getEntered();
            if (land.hasPer("banContact", p.getName())) {
               return;
            }

            p.sendMessage(UtilFormat.format(this.pn, "tip18", new Object[]{"banContact"}));
            e.setCancelled(true);
         }
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("banContact.use");
      this.per = config.getString("banContact.per");
      this.tip = Util.convert(config.getString("banContact.tip"));
      this.contact = config.getString("banContact.contact");
   }
}
