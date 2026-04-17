package basic;

import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

public class AutoSave implements Listener {
   private Basic basic;
   private Server server;
   private String pn;
   private int saveInterval;
   private AutoSaveTimer autoSaveTimer;

   public AutoSave(Basic basic) {
      this.basic = basic;
      this.server = basic.getServer();
      this.pn = Basic.getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      basic.getPm().registerEvents(this, basic);
      this.autoSaveTimer = new AutoSaveTimer();
      this.autoSaveTimer.setScheduling(this.server.getScheduler().runTaskLater(basic, this.autoSaveTimer, (long)this.saveInterval));
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void reloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   private void loadConfig(FileConfiguration config) {
      this.saveInterval = config.getInt("saveInterval") * 20;
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   class AutoSaveTimer implements Runnable {
      private BukkitTask scheduling;

      public boolean isScheduling() {
         return this.scheduling != null;
      }

      public void setScheduling(BukkitTask scheduling) {
         if (this.scheduling != null) {
            this.scheduling.cancel();
         }

         this.scheduling = scheduling;
      }

      public void run() {
         AutoSave.this.server.broadcastMessage(AutoSave.this.get(95));

         for(World w : AutoSave.this.server.getWorlds()) {
            w.save();
         }

         AutoSave.this.server.broadcastMessage(AutoSave.this.get(100));
         AutoSave.this.autoSaveTimer.setScheduling(AutoSave.this.server.getScheduler().runTaskLater(AutoSave.this.basic, AutoSave.this.autoSaveTimer, (long)AutoSave.this.saveInterval));
      }
   }
}
