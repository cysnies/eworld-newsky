package flag;

import land.Land;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.types.InvalidTypeException;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilTypes;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class BanMonsterHandler implements Listener {
   private static final String FLAG_BAN_MONSTER = "banMonster";
   private LandManager landManager;
   private String pn;
   private boolean use;
   private String per;
   private String tip;
   private String monsterType;

   public BanMonsterHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      landManager.register("banMonster", this.tip, this.use, false, false, this.per);
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
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onCreatureSpawn(CreatureSpawnEvent e) {
      try {
         if (UtilTypes.checkEntity(this.pn, this.monsterType, e.getEntity().getType().name())) {
            Land land = this.landManager.getHighestPriorityLand(e.getLocation());
            if (land != null && land.hasFlag("banMonster")) {
               e.setCancelled(true);
            }
         }
      } catch (InvalidTypeException e1) {
         e1.printStackTrace();
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("banMonster.use");
      this.per = config.getString("banMonster.per");
      this.tip = Util.convert(config.getString("banMonster.tip"));
      this.monsterType = config.getString("banMonster.monsterType");
   }
}
