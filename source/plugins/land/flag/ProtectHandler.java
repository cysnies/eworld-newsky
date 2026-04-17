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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class ProtectHandler implements Listener {
   private static final String FLAG_PROTECT = "protect";
   private LandManager landManager;
   private String pn;
   private boolean use;
   private String per;
   private String tip;
   private String protectType;

   public ProtectHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(landManager.getLandMain().getPn()));
      landManager.registerEvents(this);
      landManager.register("protect", this.tip, this.use, true, false, this.per);
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
   public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
      try {
         Entity damager;
         if (e.getDamager() instanceof Projectile) {
            damager = ((Projectile)e.getDamager()).getShooter();
         } else {
            damager = e.getDamager();
         }

         if (damager != null && damager instanceof Player) {
            Entity entity = e.getEntity();
            if (UtilTypes.checkEntity(this.pn, this.protectType, entity.getType().name())) {
               Player p = (Player)damager;
               Location loc = entity.getLocation();
               Land land = this.landManager.getHighestPriorityLand(loc);
               if (land != null && land.hasFlag("protect") && !land.hasPer("protect", p.getName())) {
                  p.sendMessage(UtilFormat.format(this.pn, "tip1", new Object[]{"protect"}));
                  e.setCancelled(true);
               }
            }
         }
      } catch (InvalidTypeException e1) {
         e1.printStackTrace();
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("protect.use");
      this.per = config.getString("protect.per");
      this.tip = Util.convert(config.getString("protect.tip"));
      this.protectType = config.getString("protect.protectType");
   }
}
