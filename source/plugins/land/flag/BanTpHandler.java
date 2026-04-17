package flag;

import land.Land;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class BanTpHandler implements Listener {
   private static final String FLAG_BAN_TP = "banTp";
   private String pn;
   private boolean use;
   private String per;
   private String tip;

   public BanTpHandler(LandManager landManager) {
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      landManager.register("banTp", this.tip, this.use, true, false, this.per);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   public boolean checkTp(Player p, Land land) {
      if (land.hasFlag("banTp") && !land.hasPer("banTp", p.getName())) {
         p.sendMessage(UtilFormat.format(this.pn, "tip10", new Object[]{"banTp"}));
         return false;
      } else {
         return true;
      }
   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("banTp.use");
      this.per = config.getString("banTp.per");
      this.tip = config.getString("banTp.tip");
   }
}
