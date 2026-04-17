package fr.neatmonster.nocheatplus.checks.blockbreak;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class Frequency extends Check {
   public Frequency() {
      super(CheckType.BLOCKBREAK_FREQUENCY);
   }

   public boolean check(Player player, BlockBreakConfig cc, BlockBreakData data) {
      float interval = (float)(player.getGameMode() == GameMode.CREATIVE ? cc.frequencyIntervalCreative : cc.frequencyIntervalSurvival);
      data.frequencyBuckets.add(System.currentTimeMillis(), interval);
      float fullScore = data.frequencyBuckets.score(cc.frequencyBucketFactor);
      long fullTime = cc.frequencyBucketDur * (long)cc.frequencyBuckets;
      int tick = TickTask.getTick();
      if (tick < data.frequencyShortTermTick) {
         data.frequencyShortTermTick = tick;
         data.frequencyShortTermCount = 1;
      } else if (tick - data.frequencyShortTermTick < cc.frequencyShortTermTicks) {
         float stLag = cc.lag ? TickTask.getLag(50L * (long)(tick - data.frequencyShortTermTick), true) : 1.0F;
         if ((double)stLag < (double)1.5F) {
            ++data.frequencyShortTermCount;
         } else {
            data.frequencyShortTermTick = tick;
            data.frequencyShortTermCount = 1;
         }
      } else {
         data.frequencyShortTermTick = tick;
         data.frequencyShortTermCount = 1;
      }

      float fullLag = cc.lag ? TickTask.getLag(fullTime, true) : 1.0F;
      float fullViolation = fullScore > (float)fullTime * fullLag ? fullScore - (float)fullTime * fullLag : 0.0F;
      float shortTermWeight = 50.0F * (float)cc.frequencyShortTermTicks / (float)cc.frequencyShortTermLimit;
      float shortTermViolation = data.frequencyShortTermCount > cc.frequencyShortTermLimit ? (float)(data.frequencyShortTermCount - cc.frequencyShortTermLimit) * shortTermWeight : 0.0F;
      float violation = Math.max(fullViolation, shortTermViolation);
      boolean cancel = false;
      if (violation > 0.0F) {
         double change = (double)(violation / 1000.0F);
         data.frequencyVL += change;
         cancel = this.executeActions(player, data.frequencyVL, change, cc.frequencyActions);
      } else if (data.frequencyVL > (double)0.0F && (double)fullScore < (double)fullTime * (double)0.75F) {
         data.frequencyVL *= 0.95;
      }

      return cancel;
   }
}
