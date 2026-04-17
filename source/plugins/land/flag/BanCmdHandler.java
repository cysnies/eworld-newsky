package flag;

import land.Land;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class BanCmdHandler implements Listener {
   private static final String FLAG_BAN_CMD = "banCmd";
   private LandManager landManager;
   private String pn;
   private boolean use;
   private String per;
   private String tip;
   private HashList canUseList;

   public BanCmdHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      landManager.register("banCmd", this.tip, this.use, true, false, this.per);
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
      Land land = this.landManager.getHighestPriorityLand(e.getPlayer().getLocation());
      if (land != null && land.hasFlag("banCmd") && !land.hasPer("banCmd", e.getPlayer().getName())) {
         String cmd = e.getMessage().split(" ")[0];
         if (cmd.trim().length() <= 1) {
            return;
         }

         cmd = cmd.substring(1, cmd.length()).toLowerCase();
         if (!this.canUseList.has(cmd)) {
            e.getPlayer().sendMessage(UtilFormat.format(this.pn, "tip2", new Object[]{"banCmd"}));
            e.setCancelled(true);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("banCmd.use");
      this.per = config.getString("banCmd.per");
      this.tip = config.getString("banCmd.tip");
      this.canUseList = new HashListImpl();

      for(String s : config.getStringList("banCmd.canUse")) {
         this.canUseList.add(s.toLowerCase());
      }

   }
}
