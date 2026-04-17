package basic;

import lib.config.ReloadConfigEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ServerMsg implements Listener {
   private static final String CHECKPER1 = "per.basic.check1";
   private static final String CHECKPER2 = "per.basic.check2";
   private static final String LIB = "lib";
   private Basic main;
   private Server server;
   private String pn;
   private String per_basic_serverMsg;
   private String per_basic_ignoreJoinLeaveMsg;
   private int dur;
   private boolean displayName;

   public ServerMsg(Basic basic) {
      this.main = basic;
      this.server = basic.getServer();
      this.pn = Basic.getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      basic.getPm().registerEvents(this, basic);
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
      priority = EventPriority.MONITOR
   )
   public void onPlayerJoin(PlayerJoinEvent e) {
      e.setJoinMessage("");
      this.server.getScheduler().scheduleSyncDelayedTask(this.main, new Join(e.getPlayer()));
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerQuit(PlayerQuitEvent e) {
      e.setQuitMessage("");
      if (!UtilPer.hasPer(e.getPlayer(), this.per_basic_ignoreJoinLeaveMsg)) {
         String name;
         if (this.displayName) {
            name = e.getPlayer().getDisplayName();
         } else {
            name = e.getPlayer().getName();
         }

         String msg = UtilFormat.format(this.pn, "quitMsg", new Object[]{name, this.server.getOnlinePlayers().length - 1});

         Player[] var7;
         for(Player p : var7 = this.server.getOnlinePlayers()) {
            switch (this.getMode(p)) {
               case 0:
                  p.sendMessage(msg);
                  break;
               case 1:
                  Util.sendItemMessage(p, msg, this.dur);
            }
         }

      }
   }

   public int getMode(Player p) {
      if (UtilPer.hasPer(p, "per.basic.check1")) {
         return 1;
      } else {
         return UtilPer.hasPer(p, "per.basic.check2") ? 2 : 0;
      }
   }

   public String getModeShow(Player p) {
      switch (this.getMode(p)) {
         case 0:
            return this.get(130);
         case 1:
            return this.get(125);
         case 2:
            return this.get(135);
         default:
            return "";
      }
   }

   public void toggleServerMsg(Player p) {
      if (UtilPer.checkPer(p, this.per_basic_serverMsg)) {
         int mode = this.getMode(p) + 1;
         if (mode >= 3) {
            mode = 0;
         }

         this.setMode(p, mode);
         switch (mode) {
            case 0:
               p.sendMessage(UtilFormat.format("lib", "success", new Object[]{this.get(107)}));
               break;
            case 1:
               p.sendMessage(UtilFormat.format("lib", "success", new Object[]{this.get(105)}));
               break;
            case 2:
               p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(110)}));
         }

      }
   }

   private void setMode(Player p, int mode) {
      switch (mode) {
         case 0:
            UtilPer.remove(p, "per.basic.check1");
            UtilPer.remove(p, "per.basic.check2");
            break;
         case 1:
            UtilPer.add(p, "per.basic.check1");
            UtilPer.remove(p, "per.basic.check2");
            break;
         case 2:
            UtilPer.remove(p, "per.basic.check1");
            UtilPer.add(p, "per.basic.check2");
      }

   }

   private void loadConfig(FileConfiguration config) {
      this.per_basic_serverMsg = config.getString("per_basic_serverMsg");
      this.per_basic_ignoreJoinLeaveMsg = config.getString("per_basic_ignoreJoinLeaveMsg");
      this.dur = config.getInt("dur");
      this.displayName = config.getBoolean("displayName");
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   class Join implements Runnable {
      private Player p;

      public Join(Player p) {
         this.p = p;
      }

      public void run() {
         if (this.p != null && this.p.isOnline()) {
            if (UtilPer.hasPer(this.p, ServerMsg.this.per_basic_ignoreJoinLeaveMsg)) {
               return;
            }

            String name;
            if (ServerMsg.this.displayName) {
               name = this.p.getDisplayName();
            } else {
               name = this.p.getName();
            }

            String msg = UtilFormat.format(ServerMsg.this.pn, "joinMsg", new Object[]{name, ServerMsg.this.server.getOnlinePlayers().length});

            Player[] var6;
            for(Player p : var6 = ServerMsg.this.server.getOnlinePlayers()) {
               switch (ServerMsg.this.getMode(p)) {
                  case 0:
                     p.sendMessage(msg);
                     break;
                  case 1:
                     Util.sendItemMessage(p, msg, ServerMsg.this.dur);
               }
            }
         }

      }
   }
}
