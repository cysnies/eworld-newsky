package flag;

import land.Land;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ForcePvpHandler implements Listener {
   private static final String FLAG_FORCE_PVP = "forcePvp";
   private String pn;
   private boolean use;
   private String per;
   private String tip;

   public ForcePvpHandler(LandManager landManager) {
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(landManager.getLandMain().getPn()));
      landManager.registerEvents(this);
      landManager.register("forcePvp", this.tip, this.use, false, false, this.per);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   public boolean checkPvp(Land land) {
      return land.hasFlag("forcePvp");
   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("forcePvp.use");
      this.per = config.getString("forcePvp.per");
      this.tip = config.getString("forcePvp.tip");
   }
}
