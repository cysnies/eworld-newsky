package fr.neatmonster.nocheatplus.checks.inventory;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import java.util.Map;
import org.bukkit.entity.Player;

public class InstantEat extends Check {
   public InstantEat() {
      super(CheckType.INVENTORY_INSTANTEAT);
   }

   public boolean check(Player player, int level) {
      long time = System.currentTimeMillis();
      InventoryData data = InventoryData.getData(player);
      boolean cancel = false;
      if (data.instantEatFood != null && level > player.getFoodLevel()) {
         long expectedTimeWhenEatingFinished = Math.max(data.instantEatInteract, data.lastClickTime) + 700L;
         if (data.instantEatInteract > 0L && expectedTimeWhenEatingFinished < time) {
            data.instantEatVL *= 0.6;
         } else if (data.instantEatInteract <= time) {
            double difference = (double)(expectedTimeWhenEatingFinished - time) / (double)100.0F;
            data.instantEatVL += difference;
            cancel = this.executeActions(player, data.instantEatVL, difference, InventoryConfig.getConfig(player).instantEatActions);
         }

         data.instantEatInteract = 0L;
         data.instantEatFood = null;
         return cancel;
      } else {
         return false;
      }
   }

   protected Map getParameterMap(ViolationData violationData) {
      Map<ParameterName, String> parameters = super.getParameterMap(violationData);
      parameters.put(ParameterName.FOOD, InventoryData.getData(violationData.player).instantEatFood.toString());
      return parameters;
   }
}
