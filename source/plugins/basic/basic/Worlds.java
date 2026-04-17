package basic;

import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.util.UtilConfig;
import org.bukkit.Server;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class Worlds implements Listener {
   private Server server;
   private String pn;
   private HashList worlds;

   public Worlds(Basic basic) {
      this.server = basic.getServer();
      this.pn = Basic.getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      this.server.getPluginManager().registerEvents(this, basic);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void reloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   public HashList getWorlds() {
      return this.worlds;
   }

   private void loadConfig(FileConfiguration config) {
      this.worlds = new HashListImpl();

      for(String s : config.getStringList("worlds")) {
         this.worlds.add(s);
         if (this.server.getWorld(s) == null) {
            this.server.createWorld(new WorldCreator(s));
         }
      }

   }
}
