package flag;

import land.Land;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PoisonWaterHandler implements Listener {
   private static final String FLAG_POISON_WATER = "poisonWater";
   private LandManager landManager;
   private Server server;
   private String pn;
   private boolean use;
   private String per;
   private String tip;

   public PoisonWaterHandler(LandManager landManager) {
      this.landManager = landManager;
      this.server = landManager.getLandMain().getServer();
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(landManager.getLandMain().getPn()));
      landManager.registerEvents(this);
      landManager.register("poisonWater", this.tip, this.use, true, false, this.per);
      this.server.getScheduler().scheduleSyncRepeatingTask(landManager.getLandMain(), new Runnable() {
         public void run() {
            PoisonWaterHandler.this.check();
         }
      }, 20L, 20L);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   private void check() {
      Player[] var4;
      for(Player p : var4 = this.server.getOnlinePlayers()) {
         Land land = this.landManager.getHighestPriorityLand(p.getLocation());
         if (land != null && land.hasFlag("poisonWater") && !land.hasPer("poisonWater", p.getName())) {
            int id = p.getLocation().getBlock().getTypeId();
            if (id == 8 || id == 9) {
               AddPotion addPotion = new AddPotion(p);
               this.server.getScheduler().scheduleSyncDelayedTask(this.landManager.getLandMain(), addPotion);
            }
         }
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("poisonWater.use");
      this.per = config.getString("poisonWater.per");
      this.tip = config.getString("poisonWater.tip");
   }

   class AddPotion implements Runnable {
      private Player p;

      public AddPotion(Player p) {
         this.p = p;
      }

      public void run() {
         if (this.p != null && this.p.isOnline()) {
            PotionEffect potionEffect = new PotionEffect(PotionEffectType.POISON, 50, 1, false);
            this.p.addPotionEffect(potionEffect, true);
         }

      }
   }
}
