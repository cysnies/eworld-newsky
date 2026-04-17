package flag;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import event.PlayerLandChangeEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import land.Land;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class StormHandler implements Listener {
   private static final String FLAG_STORM = "storm";
   private LandManager landManager;
   private static ProtocolManager protocolManager;
   private String pn;
   private boolean use;
   private String per;
   private String tip;
   private HashMap stormHash = new HashMap();

   public StormHandler(LandManager landManager) {
      this.landManager = landManager;
      protocolManager = ProtocolLibrary.getProtocolManager();
      landManager.registerEvents(this);
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(landManager.getLandMain().getPn()));
      landManager.registerEvents(this);
      landManager.register("storm", this.tip, this.use, false, false, this.per);
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
      priority = EventPriority.LOWEST
   )
   public void onPlayerJoin(PlayerJoinEvent e) {
      this.stormHash.put(e.getPlayer(), e.getPlayer().getWorld().hasStorm());
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerQuit(PlayerQuitEvent e) {
      this.stormHash.remove(e.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onWeatherChange(WeatherChangeEvent e) {
      for(Player p : e.getWorld().getPlayers()) {
         Land land = this.landManager.getHighestPriorityLand(p.getLocation());
         if (land != null && land.hasFlag("storm")) {
            this.setStorm(p, true);
         } else {
            this.setRealStorm(p);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onPlayerLandChange(PlayerLandChangeEvent e) {
      Land land = this.landManager.getHighestPriorityLand(e.getPlayer().getLocation());
      if (land != null && land.hasFlag("storm")) {
         this.setStorm(e.getPlayer(), true);
      } else {
         this.setRealStorm(e.getPlayer());
      }

   }

   private void setRealStorm(Player p) {
      this.setStorm(p, p.getWorld().hasStorm());
   }

   private void setStorm(Player p, boolean storm) {
      try {
         if (!((Boolean)this.stormHash.get(p) ^ storm)) {
            return;
         }

         this.stormHash.put(p, storm);
         int status;
         if (storm) {
            status = 1;
         } else {
            status = 2;
         }

         PacketContainer packet = protocolManager.createPacket(70);
         packet.getIntegers().write(0, status);

         try {
            protocolManager.sendServerPacket(p, packet);
         } catch (InvocationTargetException e) {
            e.printStackTrace();
         }
      } catch (Exception var7) {
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("storm.use");
      this.per = config.getString("storm.per");
      this.tip = config.getString("storm.tip");
   }
}
