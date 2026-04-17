package fr.neatmonster.nocheatplus.players;

import fr.neatmonster.nocheatplus.utilities.OnDemandTickListener;
import org.bukkit.entity.Player;

public class PlayerTask extends OnDemandTickListener {
   public final String lcName;
   protected boolean updateInventory = false;

   public PlayerTask(String name) {
      this.lcName = name.toLowerCase();
   }

   public boolean delegateTick(int tick, long timeLast) {
      Player player = DataManager.getPlayer(this.lcName);
      if (player != null && player.isOnline() && this.updateInventory) {
         player.updateInventory();
         this.updateInventory = false;
      }

      return false;
   }

   public void updateInventory() {
      this.updateInventory = true;
      this.register();
   }
}
