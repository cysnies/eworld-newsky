package fr.neatmonster.nocheatplus.checks.inventory;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class FastConsume extends Check implements Listener {
   public static void testAvailability() {
      if (!PlayerItemConsumeEvent.class.getSimpleName().equals("PlayerItemConsumeEvent")) {
         throw new RuntimeException("This exception should not even get thrown.");
      }
   }

   public FastConsume() {
      super(CheckType.INVENTORY_FASTCONSUME);
      ConfigManager.setForAllConfigs("checks.inventory.instanteat.active", false);
      LogUtil.logInfo("[NoCheatPlus] Inventory checks: FastConsume is available, disabled InstantEat.");
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onItemConsume(PlayerItemConsumeEvent event) {
      Player player = event.getPlayer();
      if (this.isEnabled(player)) {
         InventoryData data = InventoryData.getData(player);
         if (this.check(player, event.getItem(), data)) {
            event.setCancelled(true);
            DataManager.getPlayerData(player.getName(), true).task.updateInventory();
         }

         data.instantEatInteract = 0L;
         data.instantEatFood = null;
      }
   }

   private boolean check(Player player, ItemStack stack, InventoryData data) {
      if (stack == null) {
         return false;
      } else {
         long time = System.currentTimeMillis();
         long ref = Math.max(data.instantEatInteract, data.lastClickTime);
         if (time < ref) {
            return false;
         } else {
            InventoryConfig cc = InventoryConfig.getConfig(player);
            Material mat = stack == null ? null : stack.getType();
            if (mat != null) {
               if (cc.fastConsumeWhitelist) {
                  if (!cc.fastConsumeItems.contains(mat.getId())) {
                     return false;
                  }
               } else if (cc.fastConsumeItems.contains(mat.getId())) {
                  return false;
               }
            }

            long timeSpent = ref == 0L ? 0L : time - ref;
            long expectedDuration = cc.fastConsumeDuration;
            if (timeSpent < expectedDuration) {
               float lag = TickTask.getLag(expectedDuration);
               if ((float)timeSpent * lag < (float)expectedDuration) {
                  double difference = (double)((float)expectedDuration - (float)timeSpent * lag) / (double)100.0F;
                  data.instantEatVL += difference;
                  ViolationData vd = new ViolationData(this, player, data.instantEatVL, difference, cc.fastConsumeActions);
                  vd.setParameter(ParameterName.FOOD, "" + mat);
                  if (data.instantEatFood != mat) {
                     vd.setParameter(ParameterName.TAGS, "inconsistent(" + data.instantEatFood + ")");
                  } else {
                     vd.setParameter(ParameterName.TAGS, "");
                  }

                  if (this.executeActions(vd)) {
                     return true;
                  }
               }
            } else {
               data.instantEatVL *= 0.6;
            }

            return false;
         }
      }
   }
}
