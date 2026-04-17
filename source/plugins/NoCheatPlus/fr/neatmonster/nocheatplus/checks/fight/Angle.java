package fr.neatmonster.nocheatplus.checks.fight;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;
import java.util.TreeMap;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Angle extends Check {
   public Angle() {
      super(CheckType.FIGHT_ANGLE);
   }

   public boolean check(Player player, boolean worldChanged) {
      FightConfig cc = FightConfig.getConfig(player);
      FightData data = FightData.getData(player);
      if (worldChanged) {
         data.angleHits.clear();
      }

      boolean cancel = false;

      for(long time : (new TreeMap(data.angleHits)).navigableKeySet()) {
         if (System.currentTimeMillis() - time > 1000L) {
            data.angleHits.remove(time);
         }
      }

      data.angleHits.put(System.currentTimeMillis(), player.getLocation());
      if (data.angleHits.size() < 2) {
         return false;
      } else {
         double deltaMove = (double)0.0F;
         long deltaTime = 0L;
         float deltaYaw = 0.0F;
         long previousTime = 0L;
         Location previousLocation = null;

         for(long time : data.angleHits.descendingKeySet()) {
            Location location = (Location)data.angleHits.get(time);
            if (previousLocation != null) {
               deltaMove += previousLocation.distanceSquared(location);
               deltaTime += previousTime - time;
               float dYaw = TrigUtil.yawDiff(previousLocation.getYaw(), location.getYaw());
               deltaYaw += Math.abs(dYaw);
            }

            previousTime = time;
            previousLocation = location;
         }

         double averageMove = deltaMove / (double)(data.angleHits.size() - 1);
         double averageTime = (double)(deltaTime / (long)(data.angleHits.size() - 1));
         double averageYaw = (double)(deltaYaw / (float)(data.angleHits.size() - 1));
         double violation = (double)0.0F;
         if (averageMove > (double)0.0F && averageMove < 0.2) {
            violation += (double)200.0F * (0.2 - averageMove) / 0.2;
         }

         if (averageTime > (double)0.0F && averageTime < (double)150.0F) {
            violation += (double)500.0F * ((double)150.0F - averageTime) / (double)150.0F;
         }

         if (averageYaw > (double)50.0F) {
            violation += (double)300.0F * ((double)360.0F - averageYaw) / (double)360.0F;
         }

         violation /= (double)10.0F;
         if (violation > (double)cc.angleThreshold) {
            if (TickTask.getLag(1000L) < 1.5F) {
               data.angleVL += violation;
            }

            cancel = this.executeActions(player, data.angleVL, violation, cc.angleActions);
         } else {
            data.angleVL *= 0.98;
         }

         return cancel;
      }
   }
}
