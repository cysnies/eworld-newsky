package fr.neatmonster.nocheatplus.checks.moving;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.PassableRayTracing;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Passable extends Check {
   private final PassableRayTracing rayTracing = new PassableRayTracing();

   public Passable() {
      super(CheckType.MOVING_PASSABLE);
      this.rayTracing.setMaxSteps(60);
   }

   public Location check(Player player, Location loc, PlayerLocation from, PlayerLocation to, MovingData data, MovingConfig cc) {
      boolean toPassable = to.isPassable();
      if (toPassable && cc.passableRayTracingCheck && (!cc.passableRayTracingVclipOnly || from.getY() > to.getY()) && (!cc.passableRayTracingBlockChangeOnly || from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ())) {
         this.rayTracing.set(from, to);
         this.rayTracing.loop();
         if (this.rayTracing.collides() || this.rayTracing.getStepsDone() >= this.rayTracing.getMaxSteps()) {
            toPassable = false;
         }

         this.rayTracing.cleanup();
      }

      if (toPassable) {
         data.passableVL *= 0.99;
         return null;
      } else {
         int lbX = loc.getBlockX();
         int lbY = loc.getBlockY();
         int lbZ = loc.getBlockZ();
         if (from.isPassable()) {
            loc = null;
         } else if (!BlockProperties.isPassable(from.getBlockCache(), loc.getX(), loc.getY(), loc.getZ(), from.getTypeId(lbX, lbY, lbZ)) && from.isSameBlock(lbX, lbY, lbZ)) {
            if (to.isBlockAbove(from) && BlockProperties.isPassable(from.getBlockCache(), from.getX(), from.getY() + player.getEyeHeight(), from.getZ(), from.getTypeId(from.getBlockX(), Location.locToBlock(from.getY() + player.getEyeHeight()), from.getBlockZ()))) {
               return null;
            }

            if (from.isSameBlock(to)) {
               return null;
            }

            loc = null;
         }

         if (data.hasSetBack()) {
            Location ref = data.getSetBack(to);
            if (BlockProperties.isPassable(from.getBlockCache(), ref)) {
               loc = ref;
            }
         }

         ++data.passableVL;
         ViolationData vd = new ViolationData(this, player, data.passableVL, (double)1.0F, cc.passableActions);
         if (cc.debug || vd.needsParameters()) {
            vd.setParameter(ParameterName.BLOCK_ID, "" + to.getTypeId());
         }

         if (this.executeActions(vd)) {
            Location newTo;
            if (loc != null) {
               newTo = loc;
            } else {
               newTo = from.getLocation();
            }

            newTo.setYaw(to.getYaw());
            newTo.setPitch(to.getPitch());
            return newTo;
         } else {
            return null;
         }
      }
   }

   protected Map getParameterMap(ViolationData violationData) {
      return super.getParameterMap(violationData);
   }
}
