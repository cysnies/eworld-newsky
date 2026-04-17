package landHandler;

import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import lib.util.UtilSpeed;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class InfoHandler implements Listener {
   private static final String INFO = "info";
   private String pn;
   private String perInfoNow;
   private String perInfoLand;
   private int interval;

   public InfoHandler(LandManager landManager) {
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      UtilSpeed.register(this.pn, "info");
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   public String getPerInfoNow() {
      return this.perInfoNow;
   }

   public String getPerInfoLand() {
      return this.perInfoLand;
   }

   public int getInterval() {
      return this.interval;
   }

   public static String getInfo() {
      return "info";
   }

   private void loadConfig(YamlConfiguration config) {
      this.perInfoNow = config.getString("info.perInfoNow");
      this.perInfoLand = config.getString("info.perInfoLand");
      this.interval = config.getInt("info.interval");
   }
}
