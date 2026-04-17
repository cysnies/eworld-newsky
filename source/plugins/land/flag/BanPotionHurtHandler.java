package flag;

import land.Land;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BanPotionHurtHandler implements Listener {
   private static final String FLAG_BAN_POTIONHURT = "banPotionHurt";
   private LandManager landManager;
   private String pn;
   private boolean use;
   private String per;
   private String tip;

   public BanPotionHurtHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(landManager.getLandMain().getPn()));
      landManager.registerEvents(this);
      landManager.register("banPotionHurt", this.tip, this.use, false, false, this.per);
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
   public void onPotionSplash(PotionSplashEvent e) {
      try {
         for(PotionEffect pe : e.getPotion().getEffects()) {
            if (pe.getType().equals(PotionEffectType.POISON) || pe.getType().equals(PotionEffectType.HARM)) {
               Location loc = e.getPotion().getLocation();
               Land land = this.landManager.getHighestPriorityLand(loc);
               if (land != null && land.hasFlag("banPotionHurt")) {
                  LivingEntity shooter = e.getEntity().getShooter();
                  if (shooter == null) {
                     e.setCancelled(true);
                     return;
                  }

                  if (shooter instanceof Player) {
                     e.setCancelled(true);
                     String msg = UtilFormat.format(this.pn, "tip21", new Object[]{"banPotionHurt"});
                     ((Player)shooter).sendMessage(msg);
                     return;
                  }
               }
            }
         }
      } catch (Exception var8) {
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("banPotionHurt.use");
      this.per = config.getString("banPotionHurt.per");
      this.tip = config.getString("banPotionHurt.tip");
   }
}
