package landHandler;

import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class AdminHandler implements Listener {
   private static final String FLAG_ADMIN = "admin";
   private String pn;
   private boolean use;
   private String per;
   private String tip;

   public AdminHandler(LandManager landManager) {
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      landManager.register("admin", this.tip, this.use, true, false, this.per);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   public static String getFlagAdmin() {
      return "admin";
   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("admin.use");
      this.per = config.getString("admin.per");
      this.tip = config.getString("admin.tip");
   }
}
