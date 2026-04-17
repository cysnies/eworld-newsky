package flag;

import event.PlayerLandChangeEvent;
import land.Land;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class FlyHandler implements Listener {
   private static final String FLAG_FLY = "fly";
   private LandManager landManager;
   private String pn;
   private boolean use;
   private String per;
   private String tip;
   private HashList allowFlyList;

   public FlyHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      landManager.register("fly", this.tip, this.use, true, false, this.per);
      this.allowFlyList = new HashListImpl();
      Bukkit.getScheduler().scheduleSyncRepeatingTask(landManager.getLandMain(), new Runnable() {
         public void run() {
            FlyHandler.this.checkAll();
         }
      }, 40L, 40L);
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
      priority = EventPriority.LOWEST
   )
   public void onPlayerQuit(PlayerQuitEvent e) {
      this.allowFlyList.remove(e.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerLandChange(PlayerLandChangeEvent e) {
      Land land = this.landManager.getHighestPriorityLand(e.getPlayer().getLocation());
      if (land != null && land.hasFlag("fly") && land.hasPer("fly", e.getPlayer().getName())) {
         this.fly(e.getPlayer());
      } else {
         this.cancelFly(e.getPlayer());
      }

   }

   public boolean isAllowFly(Player p) {
      return this.allowFlyList.has(p);
   }

   private void checkAll() {
      this.allowFlyList.clear();

      Player[] var4;
      for(Player p : var4 = Bukkit.getServer().getOnlinePlayers()) {
         Land land = this.landManager.getHighestPriorityLand(p.getLocation());
         if (land != null && land.hasFlag("fly") && land.hasPer("fly", p.getName())) {
            this.allowFlyList.add(p);
         }
      }

   }

   private void fly(Player p) {
      boolean tip = true;
      if (this.allowFlyList.has(p)) {
         tip = false;
      }

      this.allowFlyList.add(p);
      p.setAllowFlight(true);
      p.setFlying(true);
      if (tip && p.isOnline()) {
         p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(1800)}));
      }

   }

   private void cancelFly(Player p) {
      if (!UtilPer.hasPer(p, "essentials.fly")) {
         boolean tip = true;
         if (!this.allowFlyList.has(p)) {
            tip = false;
         }

         this.allowFlyList.remove(p);
         p.setAllowFlight(false);
         p.setFlying(false);
         if (tip && p.isOnline()) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(1805)}));
         }

      }
   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("fly.use");
      this.per = config.getString("fly.per");
      this.tip = config.getString("fly.tip");
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
