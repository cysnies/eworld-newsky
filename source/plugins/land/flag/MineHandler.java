package flag;

import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class MineHandler implements Listener {
   private static final String FLAG_MINE = "mine";
   private String pn;
   private boolean use;
   private String per;
   private String tip;

   public MineHandler(LandManager landManager) {
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      landManager.register("mine", this.tip, this.use, false, false, this.per);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void reloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("mine.use");
      this.per = config.getString("mine.per");
      this.tip = config.getString("mine.tip");
   }
}
