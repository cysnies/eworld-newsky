package landHandler;

import event.PlayerLandChangeEvent;
import java.util.HashMap;
import land.Land;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.util.UtilConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;

public class EnterLeaveHandler implements Listener {
   private LandManager landManager;
   private PluginManager pm;
   private String pn;
   private int checkInterval;
   private Check check;
   private HashMap inLandHash;

   public EnterLeaveHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pm = landManager.getLandMain().getPm();
      this.pn = landManager.getLandMain().getPn();
      this.inLandHash = new HashMap();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      this.check = new Check((Check)null);
      landManager.getServer().getScheduler().scheduleSyncRepeatingTask(landManager.getLandMain(), this.check, (long)(this.checkInterval / 50), (long)(this.checkInterval / 50));
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
      priority = EventPriority.HIGHEST
   )
   public void onPlayerJoin(PlayerJoinEvent e) {
      this.inLandHash.put(e.getPlayer(), new HashListImpl());
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerQuit(PlayerQuitEvent e) {
      Player p = e.getPlayer();
      HashList<Land> preLands = (HashList)this.inLandHash.get(p);
      this.inLandHash.remove(p);
      HashList<Land> nowLands = new HashListImpl();
      if (!preLands.isEmpty()) {
         PlayerLandChangeEvent playerLandChangeEvent = new PlayerLandChangeEvent(p, preLands, nowLands);
         this.pm.callEvent(playerLandChangeEvent);
      }

   }

   private boolean isSame(HashList pre, HashList now) {
      if (pre.size() != now.size()) {
         return false;
      } else {
         for(Land land : pre) {
            if (!now.has(land)) {
               return false;
            }
         }

         return true;
      }
   }

   private void loadConfig(YamlConfiguration config) {
      this.checkInterval = config.getInt("enterLeave.interval");
   }

   private class Check implements Runnable {
      private Check() {
      }

      public void run() {
         Player[] var4;
         for(Player p : var4 = EnterLeaveHandler.this.landManager.getServer().getOnlinePlayers()) {
            HashList<Land> preLands = (HashList)EnterLeaveHandler.this.inLandHash.get(p);
            HashList<Land> nowLands = EnterLeaveHandler.this.landManager.getLands(p.getLocation());
            EnterLeaveHandler.this.inLandHash.put(p, nowLands);
            if (!EnterLeaveHandler.this.isSame(preLands, nowLands)) {
               PlayerLandChangeEvent playerLandChangeEvent = new PlayerLandChangeEvent(p, preLands, nowLands);
               EnterLeaveHandler.this.pm.callEvent(playerLandChangeEvent);
            }
         }

      }

      // $FF: synthetic method
      Check(Check var2) {
         this();
      }
   }
}
