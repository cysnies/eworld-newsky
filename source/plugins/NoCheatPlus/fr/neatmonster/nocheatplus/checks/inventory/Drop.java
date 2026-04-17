package fr.neatmonster.nocheatplus.checks.inventory;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import org.bukkit.entity.Player;

public class Drop extends Check {
   public Drop() {
      super(CheckType.INVENTORY_DROP);
   }

   public boolean check(Player player) {
      long time = System.currentTimeMillis();
      InventoryConfig cc = InventoryConfig.getConfig(player);
      InventoryData data = InventoryData.getData(player);
      boolean cancel = false;
      if (data.dropLastTime + cc.dropTimeFrame <= time) {
         data.dropLastTime = time;
         data.dropCount = 0;
         data.dropVL = (double)0.0F;
      } else if (data.dropLastTime > time) {
         data.dropLastTime = -2147483648L;
      }

      ++data.dropCount;
      if (data.dropCount > cc.dropLimit) {
         data.dropVL = (double)(data.dropCount - cc.dropLimit);
         cancel = this.executeActions(player, data.dropVL, (double)(data.dropCount - cc.dropLimit), cc.dropActions);
      }

      return cancel;
   }
}
