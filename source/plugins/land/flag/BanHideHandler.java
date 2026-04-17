package flag;

import java.util.Collection;
import land.Land;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BanHideHandler implements Listener {
   private static final String FLAG_BAN_HIDE = "banHide";
   private LandManager landManager;
   private String pn;
   private boolean use;
   private String per;
   private String tip;
   private int interval;

   public BanHideHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(landManager.getLandMain().getPn()));
      landManager.registerEvents(this);
      landManager.register("banHide", this.tip, this.use, false, false, this.per);
      Bukkit.getScheduler().scheduleSyncRepeatingTask(landManager.getLandMain(), new Runnable() {
         public void run() {
            BanHideHandler.this.check();
         }
      }, (long)this.interval, (long)this.interval);
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
      for(Player p : var4 = Bukkit.getOnlinePlayers()) {
         if (p.isValid()) {
            Collection<PotionEffect> c = p.getActivePotionEffects();
            if (c != null && !c.isEmpty()) {
               Land land = this.landManager.getHighestPriorityLand(p.getLocation());
               if (land != null && land.hasFlag("banHide")) {
                  for(PotionEffect pe : c) {
                     if (pe.getType().equals(PotionEffectType.INVISIBILITY)) {
                        p.removePotionEffect(PotionEffectType.INVISIBILITY);
                        p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(1850)}));
                        break;
                     }
                  }
               }
            }
         }
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("banHide.use");
      this.per = config.getString("banHide.per");
      this.tip = config.getString("banHide.tip");
      this.interval = config.getInt("banHide.interval");
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
