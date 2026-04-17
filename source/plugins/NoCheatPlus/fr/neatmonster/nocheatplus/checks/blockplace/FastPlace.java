package fr.neatmonster.nocheatplus.checks.blockplace;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class FastPlace extends Check {
   public FastPlace() {
      super(CheckType.BLOCKPLACE_FASTPLACE);
   }

   public boolean check(Player player, Block block) {
      BlockPlaceConfig cc = BlockPlaceConfig.getConfig(player);
      BlockPlaceData data = BlockPlaceData.getData(player);
      data.fastPlaceBuckets.add(System.currentTimeMillis(), 1.0F);
      float fullScore = data.fastPlaceBuckets.score(1.0F);
      int tick = TickTask.getTick();
      if (tick < data.fastPlaceShortTermTick) {
         data.fastPlaceShortTermTick = tick;
         data.fastPlaceShortTermCount = 1;
      } else if (tick - data.fastPlaceShortTermTick < cc.fastPlaceShortTermTicks) {
         if (cc.lag && !((double)TickTask.getLag(50L * (long)(tick - data.fastPlaceShortTermTick), true) < 1.2)) {
            data.fastPlaceShortTermTick = tick;
            data.fastPlaceShortTermCount = 1;
         } else {
            ++data.fastPlaceShortTermCount;
         }
      } else {
         data.fastPlaceShortTermTick = tick;
         data.fastPlaceShortTermCount = 1;
      }

      float fullViolation;
      if (fullScore > (float)cc.fastPlaceLimit) {
         if (cc.lag) {
            fullViolation = fullScore / TickTask.getLag(data.fastPlaceBuckets.bucketDuration() * (long)data.fastPlaceBuckets.numberOfBuckets(), true) - (float)cc.fastPlaceLimit;
         } else {
            fullViolation = fullScore - (float)cc.fastPlaceLimit;
         }
      } else {
         fullViolation = 0.0F;
      }

      float shortTermViolation = (float)(data.fastPlaceShortTermCount - cc.fastPlaceShortTermLimit);
      float violation = Math.max(fullViolation, shortTermViolation);
      boolean cancel = false;
      if (violation > 0.0F) {
         double change = (double)(violation / 1000.0F);
         data.fastPlaceVL += change;
         cancel = this.executeActions(player, data.fastPlaceVL, change, cc.fastPlaceActions);
      } else if (data.fastPlaceVL > (double)0.0F && (double)fullScore < (double)cc.fastPlaceLimit * (double)0.75F) {
         data.fastPlaceVL *= 0.95;
      }

      return cancel;
   }
}
