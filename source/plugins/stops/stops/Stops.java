package stops;

import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

public class Stops implements Listener {
   private Main main;
   private Server server;
   private String pn;
   private boolean banJoin;
   private boolean kickAll;
   private boolean isStoping;
   private int left;
   private Counter counter;

   public Stops(Main main) {
      this.main = main;
      this.server = main.getServer();
      this.pn = main.getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      this.server.getPluginManager().registerEvents(this, main);
      this.counter = new Counter();
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
   public void onPlayerLogin(PlayerLoginEvent e) {
      if (this.isStoping && this.banJoin) {
         e.setKickMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(310)}));
         e.setResult(Result.KICK_OTHER);
      }

   }

   public void stops(CommandSender sender, int time) {
      if (this.isStoping) {
         sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(285)}));
      } else if (time >= 1 && time <= 60) {
         this.left = time;
         this.isStoping = true;
         this.server.getScheduler().runTaskLater(this.main, this.counter, 20L);
         this.server.broadcastMessage(UtilFormat.format(this.pn, "stops", new Object[]{this.left}));
      } else {
         sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(290)}));
      }
   }

   public void stop(CommandSender sender) {
      if (!this.isStoping) {
         sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(305)}));
      } else {
         this.isStoping = false;
         sender.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(300)}));
      }
   }

   private void loadConfig(YamlConfiguration config) {
      this.banJoin = config.getBoolean("banJoin");
      this.kickAll = config.getBoolean("kickAll");
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   class Counter implements Runnable {
      public void run() {
         if (!Stops.this.isStoping) {
            Stops.this.server.broadcastMessage(UtilFormat.format(Stops.this.pn, "tip", new Object[]{Stops.this.get(295)}));
         } else {
            Stops var10000 = Stops.this;
            var10000.left = var10000.left - 1;
            if (Stops.this.left > 0) {
               Stops.this.server.broadcastMessage(UtilFormat.format(Stops.this.pn, "stops", new Object[]{Stops.this.left}));
               Stops.this.server.getScheduler().runTaskLater(Stops.this.main, Stops.this.counter, 20L);
            } else {
               if (Stops.this.kickAll) {
                  String tip = UtilFormat.format(Stops.this.pn, "fail", new Object[]{Stops.this.get(310)});

                  Player[] var5;
                  for(Player p : var5 = Stops.this.server.getOnlinePlayers()) {
                     p.kickPlayer(tip);
                  }
               }

               Stops.this.server.dispatchCommand(Stops.this.server.getConsoleSender(), "stop");
            }
         }
      }
   }
}
