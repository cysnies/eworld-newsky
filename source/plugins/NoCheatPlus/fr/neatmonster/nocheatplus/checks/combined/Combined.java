package fr.neatmonster.nocheatplus.checks.combined;

import fr.neatmonster.nocheatplus.utilities.TickTask;
import org.bukkit.entity.Player;

public class Combined {
   private static float stationary = 32.0F;

   public static final boolean checkYawRate(Player player, float yaw, long now, String worldName) {
      return checkYawRate(player, yaw, now, worldName, CombinedData.getData(player));
   }

   public static final void feedYawRate(Player player, float yaw, long now, String worldName) {
      feedYawRate(player, yaw, now, worldName, CombinedData.getData(player));
   }

   public static final void feedYawRate(Player player, float yaw, long now, String worldName, CombinedData data) {
      if (yaw <= -360.0F) {
         yaw = -(-yaw % 360.0F);
      } else if (yaw >= 360.0F) {
         yaw %= 360.0F;
      }

      if (now - data.lastYawTime > 999L || !worldName.equals(data.lastWorld)) {
         data.lastYaw = yaw;
         data.sumYaw = 0.0F;
         data.lastYawTime = now;
         data.lastWorld = worldName;
      }

      float yawDiff = data.lastYaw - yaw;
      if (yawDiff < -180.0F) {
         yawDiff += 360.0F;
      } else if (yawDiff > 180.0F) {
         yawDiff -= 360.0F;
      }

      long elapsed = now - data.lastYawTime;
      data.lastYaw = yaw;
      data.lastYawTime = now;
      float dAbs = Math.abs(yawDiff);
      if (dAbs < stationary) {
         data.sumYaw += yawDiff;
         if (Math.abs(data.sumYaw) < stationary) {
            data.yawFreq.update(now);
            return;
         }

         data.sumYaw = 0.0F;
      } else {
         data.sumYaw = 0.0F;
      }

      float dNorm = dAbs / (float)(1L + elapsed);
      data.yawFreq.add(now, dNorm);
   }

   public static final boolean checkYawRate(Player player, float yaw, long now, String worldName, CombinedData data) {
      feedYawRate(player, yaw, now, worldName, data);
      CombinedConfig cc = CombinedConfig.getConfig(player);
      float threshold = cc.yawRate;
      float stScore = data.yawFreq.bucketScore(0) * 3.0F;
      float stViol;
      if (stScore > threshold) {
         if (cc.lag && !((double)TickTask.getLag(data.yawFreq.bucketDuration(), true) < 1.2)) {
            stViol = 0.0F;
         } else {
            stViol = stScore;
         }
      } else {
         stViol = 0.0F;
      }

      float fullScore = data.yawFreq.score(1.0F);
      float fullViol;
      if (fullScore > threshold) {
         if (cc.lag) {
            fullViol = fullScore / TickTask.getLag(data.yawFreq.bucketDuration() * (long)data.yawFreq.numberOfBuckets(), true);
         } else {
            fullViol = fullScore;
         }
      } else {
         fullViol = 0.0F;
      }

      float total = Math.max(stViol, fullViol);
      boolean cancel = false;
      if (total > threshold) {
         float amount = (total - threshold) / threshold * 1000.0F;
         data.timeFreeze = Math.max(data.timeFreeze, now + (long)Math.min(Math.max(cc.yawRatePenaltyFactor * amount, (float)cc.yawRatePenaltyMin), (float)cc.yawRatePenaltyMax));
         if (cc.yawRateImprobable && Improbable.check(player, amount / 100.0F, now, "combined.yawrate")) {
            cancel = true;
         }
      }

      if (now < data.timeFreeze) {
         cancel = true;
      }

      return cancel;
   }

   public static final void resetYawRate(Player player, float yaw, long time, boolean clear) {
      if (yaw <= -360.0F) {
         yaw = -(-yaw % 360.0F);
      } else if (yaw >= 360.0F) {
         yaw %= 360.0F;
      }

      CombinedData data = CombinedData.getData(player);
      data.lastYaw = yaw;
      data.lastYawTime = time;
      data.sumYaw = 0.0F;
      if (clear) {
         data.yawFreq.clear(time);
      }

   }

   public static final boolean checkYawRate(Player player, float yaw, long now, String worldName, boolean yawRateCheck) {
      if (yawRateCheck) {
         return checkYawRate(player, yaw, now, worldName);
      } else {
         feedYawRate(player, yaw, now, worldName);
         return false;
      }
   }
}
