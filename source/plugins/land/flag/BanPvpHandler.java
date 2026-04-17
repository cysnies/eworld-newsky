package flag;

import land.Land;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class BanPvpHandler implements Listener {
   private static final String FLAG_BAN_PVP = "banPvp";
   private LandManager landManager;
   private String pn;
   private boolean use;
   private String per;
   private String tip;

   public BanPvpHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(landManager.getLandMain().getPn()));
      landManager.registerEvents(this);
      landManager.register("banPvp", this.tip, this.use, false, false, this.per);
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
      priority = EventPriority.MONITOR,
      ignoreCancelled = false
   )
   public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
      if (e.getEntity() instanceof Player) {
         Player damager = null;
         if (e.getDamager() instanceof Player) {
            damager = (Player)e.getDamager();
         } else if (e.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile)e.getDamager();
            LivingEntity shooter;
            if ((shooter = projectile.getShooter()) != null && shooter instanceof Player) {
               damager = (Player)shooter;
            }
         }

         if (damager != null) {
            Land land = this.landManager.getHighestPriorityLand(e.getEntity().getLocation());
            if (land != null) {
               if (this.landManager.getForcePvpHandler().checkPvp(land)) {
                  e.setCancelled(false);
                  return;
               }

               if (!e.isCancelled() && land.hasFlag("banPvp")) {
                  e.setCancelled(true);
                  damager.sendMessage(UtilFormat.format(this.pn, "tip9", new Object[]{"banPvp"}));
               }
            }
         }
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("banPvp.use");
      this.per = config.getString("banPvp.per");
      this.tip = config.getString("banPvp.tip");
   }
}
