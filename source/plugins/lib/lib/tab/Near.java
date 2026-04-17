package lib.tab;

import java.util.HashMap;
import lib.Lib;
import lib.config.ReloadConfigEvent;
import lib.time.TimeEvent;
import lib.util.Util;
import lib.util.UtilTab;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class Near implements Listener {
   private static final String MODE = "near";
   private String pn;
   private int mode = 0;
   private long interval = 3L;
   private int near = 100;
   private String pre = "";
   private String dis = "";
   private HashMap playerHash;

   public Near(Lib lib) {
      this.pn = lib.getPn();
      this.loadConfig(lib.getCon().getConfig(this.pn));
      Bukkit.getPluginManager().registerEvents(this, lib);
      UtilTab.register("near");
      this.playerHash = UtilTab.getMode("near");
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
      if (TimeEvent.getTime() % this.interval == 0L) {
         this.playerHash.clear();
         Player[] list = Bukkit.getOnlinePlayers();

         for(Player p : list) {
            this.checkInit(p);
            ((Tab.Mode)this.playerHash.get(p)).add(p.getName(), this.getName(p, 0));
         }

         for(int index = 0; index < list.length; ++index) {
            Player p = list[index];

            for(int index2 = index + 1; index2 < list.length; ++index2) {
               Player tar = list[index2];
               int dis = this.getDistance(p, tar);
               if (dis != -1) {
                  ((Tab.Mode)this.playerHash.get(p)).add(tar.getName(), this.getName(tar, dis));
                  ((Tab.Mode)this.playerHash.get(tar)).add(p.getName(), this.getName(p, dis));
               }
            }
         }
      }

   }

   private void checkInit(Player p) {
      if (!this.playerHash.containsKey(p)) {
         this.playerHash.put(p, new Tab.Mode());
      }

   }

   private String getName(Player p, int dis) {
      String s = this.dis.replace("*", String.valueOf(dis));
      int length = s.length() + this.pre.length();
      switch (this.mode) {
         case 0:
            return this.pre + p.getName().substring(0, Math.min(p.getName().length(), 16 - length)) + s;
         case 1:
            return this.pre + p.getDisplayName().substring(0, Math.min(p.getDisplayName().length(), 16 - length)) + s;
         case 2:
            return this.pre + p.getPlayerListName().substring(0, Math.min(p.getPlayerListName().length(), 16 - length)) + s;
         default:
            return this.pre + p.getName().substring(0, Math.min(p.getName().length(), 16 - length)) + s;
      }
   }

   private int getDistance(Player p, Player tar) {
      if (!p.getWorld().equals(tar.getWorld())) {
         return -1;
      } else {
         int result = (int)p.getLocation().distance(tar.getLocation());
         return result > this.near ? -1 : result;
      }
   }

   private void loadConfig(YamlConfiguration config) {
      this.mode = config.getInt("near.mode");
      this.interval = (long)config.getInt("near.interval");
      this.near = config.getInt("near.range");
      this.pre = Util.convert(config.getString("near.pre"));
      this.dis = Util.convert(config.getString("near.dis"));
   }
}
