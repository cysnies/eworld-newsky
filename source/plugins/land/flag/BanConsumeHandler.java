package flag;

import java.util.HashMap;
import land.Land;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.types.InvalidTypeException;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilTypes;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class BanConsumeHandler implements Listener {
   private static final String FLAG_BAN_CONSUME = "banConsume";
   private LandManager landManager;
   private String pn;
   private boolean use;
   private String per;
   private String tip;
   private HashMap typeHash;

   public BanConsumeHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      landManager.register("banConsume", this.tip, this.use, true, true, this.per);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void reloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onPlayerItemConsume(PlayerItemConsumeEvent e) {
      try {
         Location loc = e.getPlayer().getLocation();
         Land land = this.landManager.getHighestPriorityLand(loc);
         if (land != null && land.hasFlag("banConsume")) {
            String type = (String)this.typeHash.get(land.getFlag("banConsume"));
            if (type != null && UtilTypes.checkItem(this.pn, type, e.getItem().getTypeId() + ":" + e.getItem().getDurability())) {
               if (land.hasPer("banConsume", e.getPlayer().getName())) {
                  return;
               }

               e.getPlayer().sendMessage(UtilFormat.format(this.pn, "tip22", new Object[]{"banConsume"}));
               e.setCancelled(true);
            }
         }
      } catch (InvalidTypeException e1) {
         e1.printStackTrace();
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("banConsume.use");
      this.per = config.getString("banConsume.per");
      this.tip = Util.convert(config.getString("banConsume.tip"));
      this.typeHash = new HashMap();

      for(String s : config.getStringList("banConsume.types")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         String type = s.split(" ")[1];
         this.typeHash.put(id, type);
      }

   }
}
