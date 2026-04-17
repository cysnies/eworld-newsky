package fr.neatmonster.nocheatplus.checks.blockbreak;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.PotionUtil;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public class FastBreak extends Check {
   public FastBreak() {
      super(CheckType.BLOCKBREAK_FASTBREAK);
   }

   public boolean check(Player player, Block block, boolean isInstaBreak, BlockBreakConfig cc, BlockBreakData data) {
      long now = System.currentTimeMillis();
      boolean cancel = false;
      int id = block.getTypeId();
      long breakingTime;
      if (player.getGameMode() == GameMode.CREATIVE) {
         breakingTime = Math.max(0L, Math.round((double)cc.fastBreakModCreative / (double)100.0F * (double)100.0F));
      } else {
         breakingTime = Math.max(0L, Math.round((double)cc.fastBreakModSurvival / (double)100.0F * (double)BlockProperties.getBreakingDuration(id, player)));
      }

      long elapsedTime;
      if (cc.fastBreakStrict) {
         elapsedTime = data.fastBreakBreakTime > data.fastBreakfirstDamage ? 0L : now - data.fastBreakfirstDamage;
      } else {
         elapsedTime = data.fastBreakBreakTime > now ? 0L : now - data.fastBreakBreakTime;
      }

      if (elapsedTime >= 0L) {
         if (elapsedTime + cc.fastBreakDelay < breakingTime) {
            float lag = cc.lag ? TickTask.getLag(breakingTime, true) : 1.0F;
            long missingTime = breakingTime - (long)(lag * (float)elapsedTime);
            if (missingTime > 0L) {
               data.fastBreakPenalties.add(now, (float)missingTime);
               if (data.fastBreakPenalties.score(cc.fastBreakBucketFactor) > (float)cc.fastBreakGrace) {
                  double vlAdded = (double)missingTime / (double)1000.0F;
                  data.fastBreakVL += vlAdded;
                  ViolationData vd = new ViolationData(this, player, data.fastBreakVL, vlAdded, cc.fastBreakActions);
                  if (vd.needsParameters()) {
                     vd.setParameter(ParameterName.BLOCK_ID, "" + id);
                  }

                  cancel = this.executeActions(vd);
               }
            }
         } else if (breakingTime > cc.fastBreakDelay) {
            data.fastBreakVL *= 0.9;
         }
      }

      if ((cc.fastBreakDebug || cc.debug) && player.hasPermission("nocheatplus.admin.debug")) {
         if (data.stats != null) {
            data.stats.addStats(data.stats.getId(Integer.toString(block.getTypeId()) + "u", true), elapsedTime);
            data.stats.addStats(data.stats.getId(Integer.toString(block.getTypeId()) + "r", true), breakingTime);
            player.sendMessage(data.stats.getStatsStr(true));
         }

         int blockId = block.getTypeId();
         ItemStack stack = player.getItemInHand();
         boolean isValidTool = BlockProperties.isValidTool(blockId, stack);
         double haste = PotionUtil.getPotionEffectAmplifier(player, PotionEffectType.FAST_DIGGING);
         String msg = (isInstaBreak ? "[Insta]" : "[Normal]") + "[" + blockId + "] " + elapsedTime + "u / " + breakingTime + "r (" + (isValidTool ? "tool" : "no-tool") + ")" + (haste == Double.NEGATIVE_INFINITY ? "" : " haste=" + ((int)haste + 1));
         player.sendMessage(msg);
      }

      return cancel;
   }
}
