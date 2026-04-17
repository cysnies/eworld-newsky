package fr.neatmonster.nocheatplus.checks.combined;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import org.bukkit.entity.Player;

public class Improbable extends Check {
   private static Improbable instance = null;

   public static final boolean check(Player player, float weight, long now, String tags) {
      return instance.checkImprobable(player, weight, now, tags);
   }

   public static final void feed(Player player, float weight, long now) {
      CombinedData.getData(player).improbableCount.add(now, weight);
   }

   public Improbable() {
      super(CheckType.COMBINED_IMPROBABLE);
      instance = this;
   }

   private boolean checkImprobable(Player player, float weight, long now, String tags) {
      if (!this.isEnabled(player)) {
         return false;
      } else {
         CombinedData data = CombinedData.getData(player);
         CombinedConfig cc = CombinedConfig.getConfig(player);
         data.improbableCount.add(now, weight);
         float shortTerm = data.improbableCount.bucketScore(0);
         double violation = (double)0.0F;
         boolean violated = false;
         if ((double)(shortTerm * 0.8F) > (double)cc.improbableLevel / (double)20.0F) {
            float lag = cc.lag ? TickTask.getLag(data.improbableCount.bucketDuration(), true) : 1.0F;
            if ((double)(shortTerm / lag) > (double)cc.improbableLevel / (double)20.0F) {
               violation += (double)shortTerm * (double)2.0F / (double)lag;
               violated = true;
            }
         }

         double full = (double)data.improbableCount.score(1.0F);
         if (full > (double)cc.improbableLevel) {
            float lag = cc.lag ? TickTask.getLag(data.improbableCount.bucketDuration() * (long)data.improbableCount.numberOfBuckets(), true) : 1.0F;
            if (full / (double)lag > (double)cc.improbableLevel) {
               violation += full / (double)lag;
               violated = true;
            }
         }

         boolean cancel = false;
         if (violated) {
            data.improbableVL += violation / (double)10.0F;
            ViolationData vd = new ViolationData(this, player, data.improbableVL, violation, cc.improbableActions);
            if (tags != null && !tags.isEmpty()) {
               vd.setParameter(ParameterName.TAGS, tags);
            }

            cancel = this.executeActions(vd);
         } else {
            data.improbableVL *= 0.95;
         }

         return cancel;
      }
   }
}
