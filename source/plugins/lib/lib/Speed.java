package lib;

import java.util.HashMap;
import lib.config.ReloadConfigEvent;
import lib.util.UtilFormat;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class Speed implements Listener {
   private Server server;
   private String pn;
   private HashMap speedHash;

   public Speed(Lib lib) {
      this.server = lib.getServer();
      this.pn = lib.getPn();
      this.speedHash = new HashMap();
      this.loadConfig(lib.getCon().getConfig(this.pn));
      this.server.getPluginManager().registerEvents(this, lib);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   public void register(String plugin, String type) {
      if (!this.speedHash.containsKey(plugin)) {
         this.speedHash.put(plugin, new HashMap());
      }

      ((HashMap)this.speedHash.get(plugin)).put(type, new HashMap());
   }

   public boolean check(Player p, String plugin, String type, int limit) {
      return this.check(p, plugin, type, limit, true);
   }

   public boolean check(Player p, String plugin, String type, int limit, boolean tip) {
      try {
         String name = p.getName();
         long now = System.currentTimeMillis();
         Long pre = (Long)((HashMap)((HashMap)this.speedHash.get(plugin)).get(type)).get(name);
         if (pre != null && now - pre < (long)limit) {
            if (tip) {
               int wait = (int)((long)limit - (now - pre));
               int minute = wait / '\uea60';
               int second = wait % '\uea60' / 1000;
               int mills = wait % 1000;
               p.sendMessage(UtilFormat.format(this.pn, "speedLimit", minute, second, mills));
            }

            return false;
         } else {
            ((HashMap)((HashMap)this.speedHash.get(plugin)).get(type)).put(name, now);
            return true;
         }
      } catch (Exception e) {
         e.printStackTrace();
         return false;
      }
   }

   private void loadConfig(FileConfiguration config) {
   }
}
