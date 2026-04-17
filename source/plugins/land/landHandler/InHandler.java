package landHandler;

import event.PlayerLandChangeEvent;
import java.util.HashMap;
import land.Land;
import landMain.LandManager;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.util.UtilConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class InHandler implements Listener {
   private HashMap inHash = new HashMap();

   public InHandler(LandManager landManager) {
      this.loadConfig(UtilConfig.getConfig(landManager.getLandMain().getPn()));
      landManager.registerEvents(this);
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerLandChange(PlayerLandChangeEvent e) {
      Player p = e.getPlayer();

      for(Land land : e.getEnterList()) {
         HashList<Player> list = (HashList)this.inHash.get(land.getId());
         if (list == null) {
            list = new HashListImpl();
            this.inHash.put(land.getId(), list);
         }

         list.add(p);
      }

      for(Land land : e.getLeaveList()) {
         HashList<Player> list = (HashList)this.inHash.get(land.getId());
         if (list != null) {
            list.remove(p);
            if (list.isEmpty()) {
               this.inHash.remove(land.getId());
            }
         }
      }

   }

   public HashList getPlayers(long landId) {
      return (HashList)this.inHash.get(landId);
   }

   public int getPlayerAmount(int landId) {
      try {
         return ((HashList)this.inHash.get(landId)).size();
      } catch (Exception var3) {
         return 0;
      }
   }

   private void loadConfig(YamlConfiguration config) {
   }
}
