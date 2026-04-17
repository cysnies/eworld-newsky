package fr.neatmonster.nocheatplus.checks.moving;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;
import java.util.Locale;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CreativeFly extends Check {
   private static final double HORIZONTAL_SPEED = 0.6;
   private static final double VERTICAL_SPEED = (double)1.0F;

   public CreativeFly() {
      super(CheckType.MOVING_CREATIVEFLY);
   }

   public Location check(Player player, PlayerLocation from, PlayerLocation to, MovingData data, MovingConfig cc, long time) {
      if (!data.hasSetBack()) {
         data.setSetBack(from);
      }

      int maximumHeight = cc.creativeFlyMaxHeight + player.getWorld().getMaxHeight();
      if (to.getY() - data.verticalFreedom > (double)maximumHeight) {
         return new Location(player.getWorld(), data.getSetBackX(), (double)maximumHeight - (double)10.0F, data.getSetBackZ(), to.getYaw(), to.getPitch());
      } else {
         double xDistance = to.getX() - from.getX();
         double yDistance = to.getY() - from.getY();
         double zDistance = to.getZ() - from.getZ();
         double hDistance = Math.sqrt(xDistance * xDistance + zDistance * zDistance);
         double speedModifier = this.mcAccess.getFasterMovementAmplifier(player);
         double fSpeed;
         if (speedModifier == Double.NEGATIVE_INFINITY) {
            fSpeed = (double)1.0F;
         } else {
            fSpeed = (double)1.0F + 0.2 * (speedModifier + (double)1.0F);
         }

         double limitH = (double)cc.creativeFlyHorizontalSpeed / (double)100.0F * 0.6 * fSpeed;
         double resultH = Math.max((double)0.0F, hDistance - limitH);
         if (resultH > (double)0.0F) {
            double hFreedom = data.getHorizontalFreedom();
            if (hFreedom < resultH) {
               hFreedom += data.useHorizontalVelocity(resultH - hFreedom);
            }

            if (hFreedom > (double)0.0F) {
               resultH = Math.max((double)0.0F, resultH - hFreedom);
            }
         } else {
            data.hVelActive.clear();
         }

         boolean sprinting = time <= data.timeSprinting + cc.sprintingGrace;
         --data.bunnyhopDelay;
         if (resultH > (double)0.0F && sprinting && data.bunnyhopDelay <= 0 && resultH < 0.4) {
            data.bunnyhopDelay = 9;
            resultH = (double)0.0F;
         }

         resultH *= (double)100.0F;
         double limitV = (double)cc.creativeFlyVerticalSpeed / (double)100.0F * (double)1.0F;
         double resultV = Math.max((double)0.0F, yDistance - data.verticalFreedom - limitV) * (double)100.0F;
         double result = resultH + resultV;
         if (result > (double)0.0F) {
            if (data.creativeFlyPreviousRefused) {
               data.creativeFlyVL += result;
               ViolationData vd = new ViolationData(this, player, data.creativeFlyVL, result, cc.creativeFlyActions);
               if (vd.needsParameters()) {
                  vd.setParameter(ParameterName.LOCATION_FROM, String.format(Locale.US, "%.2f, %.2f, %.2f", from.getX(), from.getY(), from.getZ()));
                  vd.setParameter(ParameterName.LOCATION_TO, String.format(Locale.US, "%.2f, %.2f, %.2f", to.getX(), to.getY(), to.getZ()));
                  vd.setParameter(ParameterName.DISTANCE, String.format(Locale.US, "%.2f", to.getLocation().distance(from.getLocation())));
               }

               if (this.executeActions(vd)) {
                  return data.getSetBack(to);
               }
            } else {
               data.creativeFlyPreviousRefused = true;
            }
         } else {
            data.creativeFlyPreviousRefused = false;
         }

         data.creativeFlyVL *= 0.97;
         data.setSetBack(to);
         return null;
      }
   }
}
