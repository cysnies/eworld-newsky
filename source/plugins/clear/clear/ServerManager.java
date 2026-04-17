package clear;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitScheduler;

public class ServerManager implements Listener {
   private Main main;
   private Server server;
   private BukkitScheduler scheduler;
   private int checkInterval;
   private boolean broadcast;
   private AutoTpsTip autoTpsTip;
   private List levelList;

   public ServerManager(Main main) {
      this.main = main;
      this.server = main.getServer();
      this.scheduler = main.getServer().getScheduler();
      YamlConfiguration config = new YamlConfiguration();

      try {
         config.load(main.getPluginPath() + File.separator + main.getPn() + File.separator + "config.yml");
         this.loadConfig(config);
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (InvalidConfigurationException e) {
         e.printStackTrace();
      }

      this.autoTpsTip = new AutoTpsTip();
      this.scheduler.scheduleSyncDelayedTask(main, this.autoTpsTip, (long)(this.checkInterval * 20));
   }

   public int getServerStatus() {
      double tps = (double)((int)Tps.getTps());
      if (tps != (double)-1.0F) {
         for(Level level : this.levelList) {
            if (tps >= level.getThreshold()) {
               return this.levelList.indexOf(level) + 1;
            }
         }
      }

      return 0;
   }

   public void loadConfig(YamlConfiguration config) {
      this.checkInterval = config.getInt("tps.checkInterval");
      this.broadcast = config.getBoolean("tps.broadcast");
      this.levelList = new ArrayList();

      String[] var9;
      for(String s : var9 = new String[]{"good", "fine", "bad", "unknown"}) {
         double threshold = config.getDouble("tps.levels." + s + ".threshold", (double)0.0F);
         String status = Main.convert(config.getString("tps.levels." + s + ".status"));
         String show = Main.convert(config.getString("tps.levels." + s + ".show"));
         Level level = new Level(threshold, status, show);
         this.levelList.add(level);
      }

   }

   class Level {
      private double threshold;
      private String status;
      private String show;

      public Level(double threshold, String status, String show) {
         this.threshold = threshold;
         this.status = status;
         this.show = show;
      }

      public double getThreshold() {
         return this.threshold;
      }

      public String getStatus() {
         return this.status;
      }

      public String getShow() {
         return this.show;
      }
   }

   class AutoTpsTip implements Runnable {
      private int preStatus = 3;

      public void run() {
         int nowStatus = ServerManager.this.getServerStatus();
         if (ServerManager.this.broadcast) {
            if (this.preStatus == 3) {
               this.preStatus = nowStatus;
            } else if (nowStatus != this.preStatus) {
               String pre = ((Level)ServerManager.this.levelList.get(this.preStatus)).getStatus();
               String now = ((Level)ServerManager.this.levelList.get(nowStatus)).getStatus();
               String tip = ((Level)ServerManager.this.levelList.get(nowStatus)).getShow();
               ServerManager.this.server.broadcastMessage(ServerManager.this.main.format("tpsChange", pre, now));
               if (!tip.isEmpty()) {
                  ServerManager.this.server.broadcastMessage(tip);
               }
            }

            this.preStatus = nowStatus;
         }

         ServerManager.this.scheduler.scheduleSyncDelayedTask(ServerManager.this.main, ServerManager.this.autoTpsTip, (long)(ServerManager.this.checkInterval * 20));
      }
   }
}
