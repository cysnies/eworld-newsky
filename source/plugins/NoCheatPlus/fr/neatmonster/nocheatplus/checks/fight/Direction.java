package fr.neatmonster.nocheatplus.checks.fight;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Direction extends Check {
   public Direction() {
      super(CheckType.FIGHT_DIRECTION);
   }

   public boolean check(Player player, Entity damaged) {
      FightConfig cc = FightConfig.getConfig(player);
      FightData data = FightData.getData(player);
      boolean cancel = false;
      if (this.mcAccess.isComplexPart(damaged)) {
         return false;
      } else {
         double width = this.mcAccess.getWidth(damaged);
         double height = this.mcAccess.getHeight(damaged);
         Location dLoc = damaged.getLocation();
         Location loc = player.getLocation();
         Vector direction = player.getEyeLocation().getDirection();
         double off;
         if (cc.directionStrict) {
            off = TrigUtil.combinedDirectionCheck(loc, player.getEyeHeight(), direction, dLoc.getX(), dLoc.getY() + height / (double)2.0F, dLoc.getZ(), width, height, 2.6, (double)80.0F);
         } else {
            off = TrigUtil.directionCheck(loc, player.getEyeHeight(), direction, dLoc.getX(), dLoc.getY() + height / (double)2.0F, dLoc.getZ(), width, height, 2.6);
         }

         if (off > 0.1) {
            Vector blockEyes = new Vector(dLoc.getX() - loc.getX(), dLoc.getY() + height / (double)2.0F - loc.getY() - player.getEyeHeight(), dLoc.getZ() - loc.getZ());
            double distance = blockEyes.crossProduct(direction).length() / direction.length();
            data.directionVL += distance;
            cancel = this.executeActions(player, data.directionVL, distance, cc.directionActions);
            if (cancel) {
               data.directionLastViolationTime = System.currentTimeMillis();
            }
         } else {
            data.directionVL *= 0.8;
         }

         if (data.directionLastViolationTime + cc.directionPenalty > System.currentTimeMillis()) {
            if (data.directionLastViolationTime > System.currentTimeMillis()) {
               data.directionLastViolationTime = 0L;
            }

            return true;
         } else {
            return cancel;
         }
      }
   }
}
