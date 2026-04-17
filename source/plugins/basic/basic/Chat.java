package basic;

import java.util.HashMap;
import lib.config.ReloadConfigEvent;
import lib.hashList.ChanceHashList;
import lib.hashList.ChanceHashListImpl;
import lib.time.TimeEvent;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ticket.GroupChangeEvent;

public class Chat implements Listener {
   private String pn = Basic.getPn();
   private int interval;
   private int limit;
   private int defaultSpeed;
   private ChanceHashList speeds;
   private HashMap speedHash = new HashMap();
   private HashMap amountHash = new HashMap();

   public Chat(Basic basic) {
      this.loadConfig(UtilConfig.getConfig(this.pn));
      Bukkit.getPluginManager().registerEvents(this, basic);
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
   public void onPlayerJoin(PlayerJoinEvent e) {
      Player p = e.getPlayer();
      this.check(p);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onPlayerQuit(PlayerQuitEvent e) {
      this.speedHash.remove(e.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onTime(TimeEvent e) {
      if (TimeEvent.getTime() % (long)this.interval == 0L) {
         Player[] var5;
         for(Player p : var5 = Bukkit.getOnlinePlayers()) {
            int speed = (Integer)this.speedHash.get(p);
            if (TimeEvent.getTime() % (long)speed == 0L) {
               if (!this.amountHash.containsKey(p.getName())) {
                  this.amountHash.put(p.getName(), this.limit);
               }

               int result = (Integer)this.amountHash.get(p.getName()) + 1;
               if (result <= this.limit) {
                  this.amountHash.put(p.getName(), result);
                  this.tip(p, true);
               }
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
      Player p = e.getPlayer();
      if (!this.amountHash.containsKey(p.getName())) {
         this.amountHash.put(p.getName(), this.limit);
      }

      int left = (Integer)this.amountHash.get(p.getName());
      if (left <= 0) {
         e.setCancelled(true);
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(150)}));
      } else {
         this.amountHash.put(p.getName(), left - 1);
         this.tip(p, false);
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onGroupChange(GroupChangeEvent e) {
      this.check(e.getP());
   }

   private void check(Player p) {
      int speed = this.defaultSpeed;

      for(String per : this.speeds) {
         if (UtilPer.hasPer(p, per)) {
            speed = this.speeds.getChance(per);
            break;
         }
      }

      this.speedHash.put(p, speed);
   }

   private void tip(Player p, boolean add) {
      if (!this.amountHash.containsKey(p.getName())) {
         this.amountHash.put(p.getName(), this.limit);
      }

      if (add) {
         p.sendMessage(UtilFormat.format(this.pn, "chatInfo1", new Object[]{this.amountHash.get(p.getName()), this.limit}));
      } else {
         p.sendMessage(UtilFormat.format(this.pn, "chatInfo2", new Object[]{this.amountHash.get(p.getName()), this.limit}));
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.interval = config.getInt("chat.interval");
      this.limit = config.getInt("chat.limit");
      this.defaultSpeed = config.getInt("chat.defaultSpeed");
      this.speeds = new ChanceHashListImpl();

      for(String s : config.getStringList("chat.speeds")) {
         String per = s.split(" ")[0];
         int speed = Integer.parseInt(s.split(" ")[1]);
         this.speeds.addChance(per, speed);
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
