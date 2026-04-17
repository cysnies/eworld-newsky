package fr.neatmonster.nocheatplus.checks.fight;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import java.util.Map;
import org.bukkit.entity.Player;

public class Speed extends Check {
   public Speed() {
      super(CheckType.FIGHT_SPEED);
   }

   public boolean check(Player player, long now) {
      FightConfig cc = FightConfig.getConfig(player);
      FightData data = FightData.getData(player);
      boolean cancel = false;
      data.speedBuckets.add(now, 1.0F);
      long fullTime = cc.speedBucketDur * (long)cc.speedBuckets;
      float fullLag = cc.lag ? TickTask.getLag(fullTime, true) : 1.0F;
      float total = data.speedBuckets.score(cc.speedBucketFactor) * 1000.0F / (fullLag * (float)fullTime);
      int tick = TickTask.getTick();
      if (tick < data.speedShortTermTick) {
         data.speedShortTermTick = tick;
         data.speedShortTermCount = 1;
      } else if (tick - data.speedShortTermTick < cc.speedShortTermTicks) {
         if (cc.lag && !(TickTask.getLag(50L * (long)(tick - data.speedShortTermTick), true) < 1.5F)) {
            data.speedShortTermTick = tick;
            data.speedShortTermCount = 1;
         } else {
            ++data.speedShortTermCount;
         }
      } else {
         data.speedShortTermTick = tick;
         data.speedShortTermCount = 1;
      }

      float shortTerm = (float)data.speedShortTermCount * 1000.0F / (50.0F * (float)cc.speedShortTermTicks);
      float max = Math.max(shortTerm, total);
      if (max > (float)cc.speedLimit) {
         data.speedVL += (double)(max - (float)cc.speedLimit);
         cancel = this.executeActions(player, data.speedVL, (double)(max - (float)cc.speedLimit), cc.speedActions);
      } else {
         data.speedVL *= 0.96;
      }

      return cancel;
   }

   protected Map getParameterMap(ViolationData violationData) {
      Map<ParameterName, String> parameters = super.getParameterMap(violationData);
      parameters.put(ParameterName.LIMIT, String.valueOf(Math.round((float)FightConfig.getConfig(violationData.player).speedLimit)));
      return parameters;
   }
}
