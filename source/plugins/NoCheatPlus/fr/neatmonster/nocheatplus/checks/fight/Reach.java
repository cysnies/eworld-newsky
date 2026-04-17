package fr.neatmonster.nocheatplus.checks.fight;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.combined.Improbable;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Reach extends Check {
   public static final double CREATIVE_DISTANCE = (double)6.0F;

   private static double getDistMod(Entity damaged) {
      if (damaged instanceof EnderDragon) {
         return (double)6.5F;
      } else {
         return damaged instanceof Giant ? (double)1.5F : (double)0.0F;
      }
   }

   public Reach() {
      super(CheckType.FIGHT_REACH);
   }

   public boolean check(Player player, Entity damaged) {
      FightConfig cc = FightConfig.getConfig(player);
      FightData data = FightData.getData(player);
      boolean cancel = false;
      double SURVIVAL_DISTANCE = cc.reachSurvivalDistance;
      double DYNAMIC_RANGE = cc.reachReduceDistance;
      double DYNAMIC_STEP = cc.reachReduceStep / SURVIVAL_DISTANCE;
      double distanceLimit = player.getGameMode() == GameMode.CREATIVE ? (double)6.0F : SURVIVAL_DISTANCE + getDistMod(damaged);
      double distanceMin = (distanceLimit - DYNAMIC_RANGE) / distanceLimit;
      Location dRef = damaged.getLocation();
      double height = this.mcAccess.getHeight(damaged);
      Location pRef = player.getEyeLocation();
      double pY = pRef.getY();
      double dY = dRef.getY();
      if (!(pY <= dY)) {
         if (pY >= dY + height) {
            dRef.setY(dY + height);
         } else {
            dRef.setY(pY);
         }
      }

      Vector pRel = dRef.toVector().subtract(pRef.toVector());
      double lenpRel = pRel.length();
      double violation = lenpRel - distanceLimit;
      double reachMod = data.reachMod;
      if (violation > (double)0.0F) {
         if (TickTask.getLag(1000L) < 1.5F) {
            data.reachVL += violation;
         }

         cancel = this.executeActions(player, data.reachVL, violation, cc.reachActions);
         if (Improbable.check(player, (float)violation / 2.0F, System.currentTimeMillis(), "fight.reach")) {
            cancel = true;
         }

         if (cancel) {
            data.reachLastViolationTime = System.currentTimeMillis();
         }
      } else if (lenpRel - distanceLimit * reachMod > (double)0.0F) {
         data.reachLastViolationTime = Math.max(data.reachLastViolationTime, System.currentTimeMillis() - cc.reachPenalty / 2L);
         cancel = true;
         Improbable.feed(player, (float)(lenpRel - distanceLimit * reachMod) / 4.0F, System.currentTimeMillis());
      } else {
         data.reachVL *= 0.8;
      }

      if (!cc.reachReduce) {
         data.reachMod = (double)1.0F;
      } else if (lenpRel > distanceLimit - DYNAMIC_RANGE) {
         data.reachMod = Math.max(distanceMin, data.reachMod - DYNAMIC_STEP);
      } else {
         data.reachMod = Math.min((double)1.0F, data.reachMod + DYNAMIC_STEP);
      }

      boolean cancelByPenalty;
      if (data.reachLastViolationTime + cc.reachPenalty > System.currentTimeMillis()) {
         if (data.reachLastViolationTime > System.currentTimeMillis()) {
            data.reachLastViolationTime = 0L;
         }

         cancelByPenalty = !cancel;
         cancel = true;
      } else {
         cancelByPenalty = false;
      }

      if (cc.debug && player.hasPermission("nocheatplus.admin.debug")) {
         player.sendMessage("NC+: Attack " + (cancel ? (cancelByPenalty ? "(cancel/penalty) " : "(cancel/reach) ") : "") + damaged.getType() + " height=" + StringUtil.fdec3.format(height) + " dist=" + StringUtil.fdec3.format(lenpRel) + " @" + StringUtil.fdec3.format(reachMod));
      }

      return cancel;
   }
}
