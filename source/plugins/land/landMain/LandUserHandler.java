package landMain;

import java.util.HashMap;
import land.LandUser;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class LandUserHandler implements Listener {
   private LandManager landManager;
   private String pn;
   private HashMap userHash;
   private int initMax;

   public LandUserHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadData();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
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
      priority = EventPriority.LOWEST,
      ignoreCancelled = false
   )
   public void onPlayerJoin(PlayerJoinEvent e) {
      if (!this.userHash.containsKey(e.getPlayer().getName())) {
         String name = e.getPlayer().getName();
         LandUser landUser = new LandUser(name, this.initMax);
         this.userHash.put(name, landUser);
         this.landManager.addLandUser(landUser);
      }

   }

   public HashMap getUserHash() {
      return this.userHash;
   }

   public void addMaxLands(String name, int amount) {
      LandUser landUser = (LandUser)this.userHash.get(name);
      if (landUser != null) {
         landUser.setMaxLands(landUser.getMaxLands() + amount);
         this.landManager.addLandUser(landUser);
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.initMax = config.getInt("land.initMax");
   }

   private void loadData() {
      this.userHash = new HashMap();

      for(LandUser landUser : this.landManager.getAllLandUsers()) {
         this.userHash.put(landUser.getName(), landUser);
      }

   }
}
