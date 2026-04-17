package lib.time;

import java.util.HashMap;
import lib.Lib;
import lib.config.ReloadConfigEvent;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

public class Day implements Listener {
   private String pn;
   private Server server;
   private PluginManager pm;
   private HashMap dayHash;
   private int timeInterval;
   private int nightStart;
   private int nightEnd;

   public Day(Lib lib) {
      this.pn = lib.getPn();
      this.server = lib.getServer();
      this.pm = lib.getPm();
      this.dayHash = new HashMap();
      this.loadConfig(lib.getCon().getConfig(this.pn));
      lib.getPm().registerEvents(this, lib);
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
      priority = EventPriority.LOW
   )
   public void onTime(TimeEvent e) {
      if (TimeEvent.getTime() % (long)this.timeInterval == 0L) {
         for(World w : this.server.getWorlds()) {
            boolean isDay = this.isDay(w);
            if (!this.dayHash.containsKey(w)) {
               this.dayHash.put(w, isDay);
            }

            boolean pre = (Boolean)this.dayHash.get(w);
            this.dayHash.put(w, isDay);
            if (pre) {
               if (!isDay) {
                  DayChangeEvent dayChangeEvent = new DayChangeEvent(w, DayChangeEvent.State.night);
                  this.pm.callEvent(dayChangeEvent);
               }
            } else if (isDay) {
               DayChangeEvent dayChangeEvent = new DayChangeEvent(w, DayChangeEvent.State.day);
               this.pm.callEvent(dayChangeEvent);
            }
         }

      }
   }

   public boolean isDay(World w) {
      int time = (int)w.getTime() % 24000;
      return time <= this.nightStart || time >= this.nightEnd;
   }

   private void loadConfig(FileConfiguration config) {
      this.timeInterval = config.getInt("day.interval");
      this.nightStart = config.getInt("day.night.start");
      this.nightEnd = config.getInt("day.night.end");
   }
}
