package fr.neatmonster.nocheatplus.checks.moving;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.utilities.ActionAccumulator;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SurvivalFly extends Check {
   public static final double sneakingSpeed = 0.13;
   public static final double walkingSpeed = 0.22;
   public static final double sprintingSpeed = 0.35;
   public static final double blockingSpeed = 0.16;
   public static final double swimmingSpeed = 0.115;
   public static final double webSpeed = 0.105;
   public static final double climbSpeed = 0.35;
   public static final double modIce = (double)2.5F;
   public static final double modDownStream = 1.6521739130434783;
   private static final int bunnyHopMax = 9;
   private final ArrayList tags = new ArrayList(15);
   private final Set reallySneaking = new HashSet(30);

   public SurvivalFly() {
      super(CheckType.MOVING_SURVIVALFLY);
   }

   public Location check(Player player, PlayerLocation from, PlayerLocation to, MovingData data, MovingConfig cc, long now) {
      this.tags.clear();
      boolean sprinting = now <= data.timeSprinting + cc.sprintingGrace;
      if (sprinting && now != data.timeSprinting) {
         this.tags.add("sprintgrace");
      }

      boolean fromOnGround = from.isOnGround();
      boolean toOnGround = to.isOnGround();
      double xDistance = to.getX() - from.getX();
      double yDistance = to.getY() - from.getY();
      double zDistance = to.getZ() - from.getZ();
      double hDistance = Math.sqrt(xDistance * xDistance + zDistance * zDistance);
      if (!data.hasSetBack()) {
         data.setSetBack(from);
      }

      boolean resetFrom;
      if (!fromOnGround && !from.isResetCond()) {
         boolean lostGround = false;
         if (yDistance >= (double)-0.5F && yDistance <= 0.52 + data.jumpAmplifier * 0.2) {
            if (from.isAboveStairs()) {
               this.applyLostGround(player, from, true, data, "stairs");
               lostGround = true;
            }

            if (!lostGround && yDistance <= (double)0.0F && this.lostGroundDescend(player, from, to, hDistance, yDistance, sprinting, data, cc)) {
               lostGround = true;
            }

            if (!lostGround && yDistance >= (double)0.0F && this.lostGroundAscend(player, from, to, hDistance, yDistance, sprinting, data, cc)) {
               lostGround = true;
            }
         } else if (yDistance < (double)-0.5F && hDistance <= (double)0.5F && this.lostGroundFastDescend(player, from, to, hDistance, yDistance, sprinting, data, cc)) {
            lostGround = true;
         }

         resetFrom = lostGround;
      } else {
         resetFrom = true;
      }

      boolean downStream = hDistance > 0.115 && from.isInLiquid() && from.isDownStream(xDistance, zDistance);
      double hAllowedDistance = this.getAllowedhDist(player, from, to, sprinting, downStream, hDistance, data, cc, false);
      double hDistanceAboveLimit = hDistance - hAllowedDistance;
      double hFreedom = (double)0.0F;
      if (hDistanceAboveLimit > (double)0.0F) {
         double extraUsed;
         if (data.sfHBufExtra <= 0) {
            extraUsed = (double)0.0F;
         } else {
            extraUsed = 0.11;
            hDistanceAboveLimit = Math.max((double)0.0F, hDistanceAboveLimit - extraUsed);
            --data.sfHBufExtra;
            this.tags.add("hbufextra");
            if (data.sfHBufExtra < 3 && to.isOnGround() || to.isResetCond()) {
               data.sfHBufExtra = 0;
            }
         }

         if (hDistanceAboveLimit > (double)0.0F) {
            hFreedom = data.getHorizontalFreedom();
            if (hFreedom < hDistanceAboveLimit) {
               hFreedom += data.useHorizontalVelocity(hDistanceAboveLimit - hFreedom);
            }

            if (hFreedom > (double)0.0F) {
               hDistanceAboveLimit = Math.max((double)0.0F, hDistanceAboveLimit - hFreedom);
            }
         } else {
            data.hVelActive.clear();
            hFreedom = (double)0.0F;
         }

         if (hDistanceAboveLimit > (double)0.0F) {
            hAllowedDistance = this.getAllowedhDist(player, from, to, sprinting, downStream, hDistance, data, cc, true);
            if (hFreedom > (double)0.0F) {
               hDistanceAboveLimit = hDistance - hAllowedDistance - extraUsed - hFreedom;
            } else {
               hDistanceAboveLimit = hDistance - hAllowedDistance - extraUsed;
            }

            if (hAllowedDistance > (double)0.0F) {
               this.tags.add("hspeed");
            }
         }
      } else {
         data.hVelActive.clear();
         data.sfHBufExtra = 0;
      }

      if (hDistanceAboveLimit <= (double)0.0F && hDistance > 0.1 && yDistance == (double)0.0F && BlockProperties.isLiquid(to.getTypeId()) && !toOnGround && to.getY() % (double)1.0F < 0.8) {
         hDistanceAboveLimit = Math.max(hDistanceAboveLimit, hDistance);
         this.tags.add("waterwalk");
      }

      if (hDistanceAboveLimit <= (double)0.0F && sprinting && data.hVelActive.isEmpty()) {
         float yaw = from.getYaw();
         if ((xDistance < (double)0.0F && zDistance > (double)0.0F && yaw > 180.0F && yaw < 270.0F || xDistance < (double)0.0F && zDistance < (double)0.0F && yaw > 270.0F && yaw < 360.0F || xDistance > (double)0.0F && zDistance < (double)0.0F && yaw > 0.0F && yaw < 90.0F || xDistance > (double)0.0F && zDistance > (double)0.0F && yaw > 90.0F && yaw < 180.0F) && !player.hasPermission("nocheatplus.checks.moving.survivalfly.sprinting")) {
            hDistanceAboveLimit = Math.max(hDistanceAboveLimit, hDistance);
            this.tags.add("sprintback");
         }
      }

      --data.bunnyhopDelay;
      if (hDistanceAboveLimit > (double)0.0F && sprinting && data.bunnyhopDelay <= 0 && hDistanceAboveLimit > 0.05 && hDistanceAboveLimit < 0.28) {
         data.bunnyhopDelay = 9;
         hDistanceAboveLimit = (double)0.0F;
         this.tags.add("bunny");
      }

      boolean resetTo = toOnGround || to.isResetCond();
      if (hDistanceAboveLimit > (double)0.0F && data.sfHorizontalBuffer != (double)0.0F) {
         if (data.sfHorizontalBuffer > (double)0.0F) {
            this.tags.add("hbufuse");
         } else {
            this.tags.add("hbufpen");
         }

         hDistanceAboveLimit -= data.sfHorizontalBuffer;
         data.sfHorizontalBuffer = (double)0.0F;
         if (hDistanceAboveLimit < (double)0.0F) {
            data.sfHorizontalBuffer = -hDistanceAboveLimit;
         }
      } else if (hDistance != (double)0.0F) {
         data.sfHorizontalBuffer = Math.min((double)1.0F, data.sfHorizontalBuffer - hDistanceAboveLimit);
      }

      if (data.sfDirty) {
         if (!resetFrom && !resetTo) {
            this.tags.add("dirty");
         } else {
            data.sfDirty = false;
         }
      }

      double vAllowedDistance = (double)0.0F;
      double vDistanceAboveLimit = (double)0.0F;
      if (from.isInWeb()) {
         data.sfNoLowJump = true;
         vAllowedDistance = from.isOnGround() ? 0.1 : (double)0.0F;
         data.jumpAmplifier = (double)0.0F;
         vDistanceAboveLimit = yDistance - vAllowedDistance;
         if (cc.survivalFlyCobwebHack && vDistanceAboveLimit > (double)0.0F && hDistanceAboveLimit <= (double)0.0F) {
            Location silentSetBack = this.hackCobweb(player, data, to, now, vDistanceAboveLimit);
            if (silentSetBack != null) {
               if (cc.debug) {
                  this.tags.add("silentsbcobweb");
                  this.outputDebug(player, to, data, cc, hDistance, hAllowedDistance, hFreedom, yDistance, vAllowedDistance, fromOnGround, resetFrom, toOnGround, resetTo);
               }

               return silentSetBack;
            }
         }

         if (vDistanceAboveLimit > (double)0.0F) {
            this.tags.add("vweb");
         }
      } else if (data.verticalFreedom <= 0.001 && from.isOnClimbable()) {
         data.sfNoLowJump = true;
         double jumpHeight = 1.35 + (data.jumpAmplifier > (double)0.0F ? 0.6 + data.jumpAmplifier - (double)1.0F : (double)0.0F);
         if (yDistance > 0.35 && !from.isOnGround(jumpHeight, (double)0.0F, (double)0.0F, 512L)) {
            this.tags.add("climbspeed");
            vDistanceAboveLimit = Math.max(vDistanceAboveLimit, yDistance - 0.35);
         }

         if (yDistance > (double)0.0F && !fromOnGround && !toOnGround && !data.noFallAssumeGround && !from.canClimbUp(jumpHeight)) {
            this.tags.add("climbup");
            vDistanceAboveLimit = Math.max(vDistanceAboveLimit, yDistance);
         }
      } else if (!(data.verticalFreedom <= 0.001) || !from.isInLiquid() || !(Math.abs(yDistance) > 0.2) && !to.isInLiquid()) {
         vAllowedDistance = 1.35 + data.verticalFreedom;
         int maxJumpPhase;
         if (data.mediumLiftOff == MediumLiftOff.LIMIT_JUMP) {
            maxJumpPhase = 3;
            data.sfNoLowJump = true;
            if (data.sfJumpPhase > 0) {
               this.tags.add("limitjump");
            }
         } else if (data.jumpAmplifier > (double)0.0F) {
            vAllowedDistance += 0.6 + data.jumpAmplifier - (double)1.0F;
            maxJumpPhase = (int)((double)9.0F + (data.jumpAmplifier - (double)1.0F) * (double)6.0F);
         } else {
            maxJumpPhase = 6;
         }

         if (data.sfJumpPhase > maxJumpPhase && data.verticalVelocityCounter <= 0) {
            if (!data.sfDirty && !(yDistance < (double)0.0F) && !resetFrom) {
               if (!data.sfDirty) {
                  this.tags.add("maxphase");
                  vDistanceAboveLimit = Math.max(vDistanceAboveLimit, Math.max(yDistance, 0.15));
               }
            } else if (data.getSetBackY() > to.getY()) {
               if (data.sfJumpPhase <= 2 * maxJumpPhase) {
                  vAllowedDistance -= Math.max((double)0.0F, (double)(data.sfJumpPhase - maxJumpPhase) * 0.15);
               }
            } else {
               vAllowedDistance -= Math.max((double)0.0F, (double)(data.sfJumpPhase - maxJumpPhase) * 0.15);
            }
         }

         vDistanceAboveLimit = Math.max(vDistanceAboveLimit, to.getY() - data.getSetBackY() - vAllowedDistance);
         if (vDistanceAboveLimit > (double)0.0F) {
            this.tags.add("vdist");
         }

         if ((fromOnGround || data.noFallAssumeGround) && toOnGround && Math.abs(yDistance - (double)1.0F) <= cc.yStep && vDistanceAboveLimit <= (double)0.0F && yDistance > 0.52 + data.jumpAmplifier * 0.2 && !player.hasPermission("nocheatplus.checks.moving.survivalfly.step")) {
            vDistanceAboveLimit = Math.max(vDistanceAboveLimit, Math.abs(yDistance));
            this.tags.add("step");
         }
      } else {
         data.sfNoLowJump = true;
         if (yDistance >= (double)0.0F) {
            vAllowedDistance = 0.135;
            vDistanceAboveLimit = yDistance - vAllowedDistance;
            if (vDistanceAboveLimit > (double)0.0F) {
               if (yDistance <= (double)0.5F && (data.mediumLiftOff == MediumLiftOff.GROUND && !BlockProperties.isLiquid(from.getTypeIdAbove()) || !to.isInLiquid() || toOnGround || data.sfLastYDist - yDistance >= 0.01 || to.isAboveStairs())) {
                  vAllowedDistance = 0.615;
                  vDistanceAboveLimit = yDistance - vAllowedDistance;
               }

               if (vDistanceAboveLimit > (double)0.0F) {
                  this.tags.add("swimup");
               }
            }
         }
      }

      if (data.noFallAssumeGround || fromOnGround || toOnGround) {
         data.jumpAmplifier = this.getJumpAmplifier(player);
      }

      if (!resetFrom && !resetTo) {
         vDistanceAboveLimit = Math.max(vDistanceAboveLimit, this.verticalAccounting(now, from, to, hDistance, yDistance, data, cc));
      }

      double result = (Math.max(hDistanceAboveLimit, (double)0.0F) + Math.max(vDistanceAboveLimit, (double)0.0F)) * (double)100.0F;
      if (cc.debug) {
         this.outputDebug(player, to, data, cc, hDistance, hAllowedDistance, hFreedom, yDistance, vAllowedDistance, fromOnGround, resetFrom, toOnGround, resetTo);
      }

      ++data.sfJumpPhase;
      if (result > (double)0.0F) {
         Location vLoc = this.handleViolation(now, result, player, from, to, data, cc);
         if (vLoc != null) {
            return vLoc;
         }
      } else if (now - data.sfVLTime > cc.survivalFlyVLFreeze) {
         data.survivalFlyVL *= 0.95;
      }

      if (to.isInLiquid()) {
         if (fromOnGround && !toOnGround && data.mediumLiftOff == MediumLiftOff.GROUND && data.sfJumpPhase <= 1 && !from.isInLiquid()) {
            data.mediumLiftOff = MediumLiftOff.GROUND;
         } else if (to.isNextToGround(0.15, 0.4)) {
            data.mediumLiftOff = MediumLiftOff.GROUND;
         } else {
            data.mediumLiftOff = MediumLiftOff.LIMIT_JUMP;
         }
      } else if (to.isInWeb()) {
         data.mediumLiftOff = MediumLiftOff.LIMIT_JUMP;
      } else if (resetTo) {
         data.mediumLiftOff = MediumLiftOff.GROUND;
      } else if (from.isInLiquid()) {
         if (!resetTo && data.mediumLiftOff == MediumLiftOff.GROUND && data.sfJumpPhase <= 1) {
            data.mediumLiftOff = MediumLiftOff.GROUND;
         } else if (to.isNextToGround(0.15, 0.4)) {
            data.mediumLiftOff = MediumLiftOff.GROUND;
         } else {
            data.mediumLiftOff = MediumLiftOff.LIMIT_JUMP;
         }
      } else if (from.isInWeb()) {
         data.mediumLiftOff = MediumLiftOff.LIMIT_JUMP;
      } else if (resetFrom || data.noFallAssumeGround) {
         data.mediumLiftOff = MediumLiftOff.GROUND;
      }

      data.toWasReset = resetTo || data.noFallAssumeGround;
      data.fromWasReset = resetFrom || data.noFallAssumeGround;
      if (data.verticalVelocityUsed > cc.velocityGraceTicks && yDistance <= (double)0.0F && data.sfLastYDist > (double)0.0F) {
         data.verticalVelocityCounter = 0;
         data.verticalVelocity = (double)0.0F;
      }

      if (resetTo) {
         data.setSetBack(to);
         data.sfJumpPhase = 0;
         data.clearAccounting();
         data.sfLowJump = false;
         data.sfNoLowJump = false;
         if (data.verticalVelocityUsed > cc.velocityGraceTicks && toOnGround && yDistance < (double)0.0F) {
            data.verticalVelocityCounter = 0;
            data.verticalFreedom = (double)0.0F;
            data.verticalVelocity = (double)0.0F;
            data.verticalVelocityUsed = 0;
         }
      } else if (resetFrom) {
         data.setSetBack(from);
         data.sfJumpPhase = 1;
         data.clearAccounting();
         data.sfLowJump = false;
      }

      if (hDistance <= (cc.velocityStrictInvalidation ? hAllowedDistance : hAllowedDistance / (double)2.0F)) {
         data.hVelActive.clear();
      }

      data.sfLastYDist = yDistance;
      return null;
   }

   private void outputDebug(Player player, PlayerLocation to, MovingData data, MovingConfig cc, double hDistance, double hAllowedDistance, double hFreedom, double yDistance, double vAllowedDistance, boolean fromOnGround, boolean resetFrom, boolean toOnGround, boolean resetTo) {
      StringBuilder builder = new StringBuilder(500);
      String hBuf = data.sfHorizontalBuffer < (double)1.0F ? " hbuf=" + StringUtil.fdec3.format(data.sfHorizontalBuffer) : "";
      String hBufExtra = data.sfHBufExtra > 0 ? " hbufextra=" + data.sfHBufExtra : "";
      String hVelUsed = hFreedom > (double)0.0F ? " hVelUsed=" + StringUtil.fdec3.format(hFreedom) : "";
      builder.append(player.getName() + " SurvivalFly\nground: " + (data.noFallAssumeGround ? "(assumeonground) " : "") + (fromOnGround ? "onground -> " : (resetFrom ? "resetcond -> " : "--- -> ")) + (toOnGround ? "onground" : (resetTo ? "resetcond" : "---")) + ", jumpphase: " + data.sfJumpPhase);
      builder.append("\n hDist: " + StringUtil.fdec3.format(hDistance) + " / " + StringUtil.fdec3.format(hAllowedDistance) + hBuf + hBufExtra + hVelUsed + " , vDist: " + StringUtil.fdec3.format(yDistance) + " (" + StringUtil.fdec3.format(to.getY() - data.getSetBackY()) + " / " + StringUtil.fdec3.format(vAllowedDistance) + "), sby=" + (data.hasSetBack() ? data.getSetBackY() : "?"));
      if (data.verticalVelocityCounter > 0 || data.verticalFreedom >= 0.001) {
         builder.append("\n vertical freedom: " + StringUtil.fdec3.format(data.verticalFreedom) + " (vel=" + StringUtil.fdec3.format(data.verticalVelocity) + "/counter=" + data.verticalVelocityCounter + "/used=" + data.verticalVelocityUsed);
      }

      if (!data.hVelActive.isEmpty()) {
         builder.append("\n horizontal velocity (active):");
         this.addVeloctiy(builder, data.hVelActive);
      }

      if (!data.hVelQueued.isEmpty()) {
         builder.append("\n horizontal velocity (queued):");
         this.addVeloctiy(builder, data.hVelQueued);
      }

      if (!resetFrom && !resetTo && cc.survivalFlyAccountingV && data.vDistAcc.count() > data.vDistAcc.bucketCapacity()) {
         builder.append("\n vacc=" + data.vDistAcc.toInformalString());
      }

      if (player.isSleeping()) {
         this.tags.add("sleeping");
      }

      if (player.getFoodLevel() <= 5 && player.isSprinting()) {
         this.tags.add("lowfoodsprint");
      }

      if (!this.tags.isEmpty()) {
         builder.append("\n tags: " + StringUtil.join(this.tags, "+"));
      }

      builder.append("\n");
      System.out.print(builder.toString());
   }

   private void addVeloctiy(StringBuilder builder, List entries) {
      for(Velocity vel : entries) {
         builder.append(" ");
         builder.append(vel);
      }

   }

   private boolean lostGroundAscend(Player player, PlayerLocation from, PlayerLocation to, double hDistance, double yDistance, boolean sprinting, MovingData data, MovingConfig cc) {
      double setBackYDistance = to.getY() - data.getSetBackY();
      if (!(yDistance <= (double)0.5F) || !(hDistance < (double)0.5F) || !(setBackYDistance <= 1.3 + 0.2 * data.jumpAmplifier) || !to.isOnGround() || !(data.sfLastYDist < (double)0.0F) && !from.isOnGround((double)0.5F - Math.abs(yDistance))) {
         if (data.fromX != Double.MAX_VALUE && yDistance > (double)0.0F && data.sfLastYDist < (double)0.0F && !to.isOnGround() && (setBackYDistance > (double)0.0F && setBackYDistance <= (double)1.5F + 0.2 * data.jumpAmplifier || setBackYDistance < (double)0.0F && Math.abs(setBackYDistance) < (double)3.0F)) {
            double dX = from.getX() - data.fromX;
            double dY = from.getY() - data.fromY;
            double dZ = from.getZ() - data.fromZ;
            if (dX * dX + dY * dY + dZ * dZ < (double)0.5F) {
               double minY = Math.min(data.toY, Math.min(data.fromY, from.getY()));
               double r = from.getWidth() / (double)2.0F;
               double yMargin = cc.yOnGround;
               if (BlockProperties.isOnGround(from.getBlockCache(), Math.min(data.fromX, from.getX()) - r, minY - yMargin, Math.min(data.fromZ, from.getZ()) - r, Math.max(data.fromX, from.getX()) + r, minY + (double)0.25F, Math.max(data.fromZ, from.getZ()) + r, 0L)) {
                  return this.applyLostGround(player, from, true, data, "interpolate");
               }
            }
         }

         return false;
      } else {
         return this.applyLostGround(player, from, true, data, "step");
      }
   }

   private boolean lostGroundDescend(Player player, PlayerLocation from, PlayerLocation to, double hDistance, double yDistance, boolean sprinting, MovingData data, MovingConfig cc) {
      double setBackYDistance = to.getY() - data.getSetBackY();
      if (data.sfJumpPhase <= 7) {
         if (data.sfLastYDist <= yDistance && setBackYDistance < (double)0.0F && !to.isOnGround() && from.isOnGround(0.6, 0.4, (double)0.0F, 0L)) {
            return this.applyLostGround(player, from, true, data, "pyramid");
         }

         if (yDistance == (double)0.0F && data.sfLastYDist > (double)0.0F && data.sfLastYDist < (double)0.25F && (double)data.sfJumpPhase <= (double)6.0F + data.jumpAmplifier * (double)3.0F && setBackYDistance > (double)1.0F && setBackYDistance < (double)1.5F + 0.2 * data.jumpAmplifier && !to.isOnGround() && from.isOnGround((double)0.25F, 0.4, (double)0.0F, 0L)) {
            return this.applyLostGround(player, from, true, data, "ministep");
         }
      }

      return !(yDistance < (double)0.0F) || !(hDistance <= (double)0.5F) || !(data.sfLastYDist < (double)0.0F) || !(yDistance > data.sfLastYDist) || to.isOnGround() || !from.isOnGround((double)0.5F, 0.2, (double)0.0F) && !to.isOnGround((double)0.5F, Math.min(0.2, 0.01 + hDistance), Math.min(0.1, 0.01 + -yDistance)) ? false : this.applyLostGround(player, from, true, data, "edge");
   }

   private boolean lostGroundFastDescend(Player player, PlayerLocation from, PlayerLocation to, double hDistance, double yDistance, boolean sprinting, MovingData data, MovingConfig cc) {
      return !(yDistance > data.sfLastYDist) || to.isOnGround() || !from.isOnGround((double)0.5F, 0.2, (double)0.0F) && !to.isOnGround((double)0.5F, Math.min(0.3, 0.01 + hDistance), Math.min(0.1, 0.01 + -yDistance)) ? false : this.applyLostGround(player, from, true, data, "fastedge");
   }

   private boolean applyLostGround(Player player, PlayerLocation from, boolean setBackSafe, MovingData data, String tag) {
      if (setBackSafe) {
         data.setSetBack(from);
      }

      data.sfJumpPhase = 0;
      data.jumpAmplifier = this.getJumpAmplifier(player);
      data.clearAccounting();
      data.noFallAssumeGround = true;
      this.tags.add("lostground_" + tag);
      return true;
   }

   private double getAllowedhDist(Player player, PlayerLocation from, PlayerLocation to, boolean sprinting, boolean downStream, double hDistance, MovingData data, MovingConfig cc, boolean checkPermissions) {
      if (checkPermissions) {
         this.tags.add("permchecks");
      }

      double hAllowedDistance = (double)0.0F;
      if (!from.isOnIce() && !to.isOnIce()) {
         if (data.sfFlyOnIce > 0) {
            --data.sfFlyOnIce;
         }
      } else {
         data.sfFlyOnIce = 20;
      }

      boolean sfDirty = data.sfDirty;
      if (from.isInWeb()) {
         data.sfFlyOnIce = 0;
         hAllowedDistance = 0.105 * (double)cc.survivalFlyWalkingSpeed / (double)100.0F;
      } else if (from.isInLiquid() && to.isInLiquid()) {
         hAllowedDistance = 0.115 * (double)cc.survivalFlySwimmingSpeed / (double)100.0F;
      } else if (sfDirty || !player.isSneaking() || !this.reallySneaking.contains(player.getName()) || checkPermissions && player.hasPermission("nocheatplus.checks.moving.survivalfly.sneaking")) {
         if (sfDirty || !player.isBlocking() || checkPermissions && player.hasPermission("nocheatplus.checks.moving.survivalfly.blocking")) {
            if (!sprinting) {
               hAllowedDistance = 0.22 * (double)cc.survivalFlyWalkingSpeed / (double)100.0F;
            } else {
               hAllowedDistance = 0.35 * (double)cc.survivalFlySprintingSpeed / (double)100.0F;
            }

            if (checkPermissions && player.hasPermission("nocheatplus.checks.moving.survivalfly.speeding")) {
               hAllowedDistance *= (double)cc.survivalFlySpeedingSpeed / (double)100.0F;
            }
         } else {
            hAllowedDistance = 0.16 * (double)cc.survivalFlyBlockingSpeed / (double)100.0F;
         }
      } else {
         hAllowedDistance = 0.13 * (double)cc.survivalFlySneakingSpeed / (double)100.0F;
      }

      if (data.sfFlyOnIce > 0) {
         hAllowedDistance *= (double)2.5F;
      }

      if (downStream) {
         hAllowedDistance *= 1.6521739130434783;
      }

      if (hDistance <= hAllowedDistance) {
         return hAllowedDistance;
      } else {
         double speedAmplifier = this.mcAccess.getFasterMovementAmplifier(player);
         if (speedAmplifier != Double.NEGATIVE_INFINITY) {
            hAllowedDistance *= (double)1.0F + 0.2 * (speedAmplifier + (double)1.0F);
         }

         return hAllowedDistance;
      }
   }

   private final Location handleViolation(long now, double result, Player player, PlayerLocation from, PlayerLocation to, MovingData data, MovingConfig cc) {
      data.survivalFlyVL += result;
      data.sfVLTime = now;
      ViolationData vd = new ViolationData(this, player, data.survivalFlyVL, result, cc.survivalFlyActions);
      if (vd.needsParameters()) {
         vd.setParameter(ParameterName.LOCATION_FROM, String.format(Locale.US, "%.2f, %.2f, %.2f", from.getX(), from.getY(), from.getZ()));
         vd.setParameter(ParameterName.LOCATION_TO, String.format(Locale.US, "%.2f, %.2f, %.2f", to.getX(), to.getY(), to.getZ()));
         vd.setParameter(ParameterName.DISTANCE, String.format(Locale.US, "%.2f", to.getLocation().distance(from.getLocation())));
         vd.setParameter(ParameterName.TAGS, StringUtil.join(this.tags, "+"));
      }

      if (this.executeActions(vd)) {
         return data.getSetBack(to);
      } else {
         data.clearAccounting();
         data.sfJumpPhase = 0;
         return null;
      }
   }

   protected final void handleHoverViolation(Player player, Location loc, MovingConfig cc, MovingData data) {
      data.survivalFlyVL += cc.sfHoverViolation;
      data.sfVLTime = System.currentTimeMillis();
      ViolationData vd = new ViolationData(this, player, data.survivalFlyVL, cc.sfHoverViolation, cc.survivalFlyActions);
      if (vd.needsParameters()) {
         vd.setParameter(ParameterName.LOCATION_FROM, String.format(Locale.US, "%.2f, %.2f, %.2f", loc.getX(), loc.getY(), loc.getZ()));
         vd.setParameter(ParameterName.LOCATION_TO, "(HOVER)");
         vd.setParameter(ParameterName.DISTANCE, "0.0(HOVER)");
         vd.setParameter(ParameterName.TAGS, "hover");
      }

      if (this.executeActions(vd)) {
         if (data.hasSetBack()) {
            data.clearAccounting();
            data.sfJumpPhase = 0;
            data.sfLastYDist = Double.MAX_VALUE;
            data.toWasReset = false;
            data.fromWasReset = false;
            data.setTeleported(data.getSetBack(loc));
            player.teleport(data.getTeleported());
         } else {
            player.kickPlayer("Hovering?");
         }
      }

   }

   private double verticalAccounting(long now, PlayerLocation from, PlayerLocation to, double hDistance, double yDistance, MovingData data, MovingConfig cc) {
      double vDistanceAboveLimit = (double)0.0F;
      boolean yDirChange = data.sfLastYDist != Double.MAX_VALUE && data.sfLastYDist != yDistance && (yDistance <= (double)0.0F && data.sfLastYDist >= (double)0.0F || yDistance >= (double)0.0F && data.sfLastYDist <= (double)0.0F);
      if (yDirChange) {
         if (yDistance > (double)0.0F) {
            if (data.toWasReset) {
               this.tags.add("ychinc");
            } else if (!(data.verticalFreedom <= 0.001) || data.bunnyhopDelay >= 9 || data.fromWasReset && data.sfLastYDist == (double)0.0F) {
               this.tags.add("ychincair");
            } else {
               vDistanceAboveLimit = Math.max(vDistanceAboveLimit, Math.abs(yDistance));
               this.tags.add("ychincfly");
            }
         } else {
            this.tags.add("ychdec");
            if (!data.sfNoLowJump && !data.sfDirty && data.mediumLiftOff == MediumLiftOff.GROUND) {
               double setBackYDistance = to.getY() - data.getSetBackY();
               if (setBackYDistance > (double)0.0F) {
                  Player player = from.getPlayer();
                  double estimate = 1.15;
                  if (data.jumpAmplifier > (double)0.0F) {
                     estimate += (double)0.5F * this.getJumpAmplifier(player);
                  }

                  if (setBackYDistance < estimate) {
                     long flags = 132L;
                     if ((BlockProperties.getBlockFlags(from.getTypeIdAbove()) & 132L) == 0L) {
                        int refY = Location.locToBlock(from.getY() + (double)0.5F);
                        if (refY == from.getBlockY() || (BlockProperties.getBlockFlags(from.getTypeId(from.getBlockX(), refY, from.getBlockZ())) & 132L) == 0L) {
                           this.tags.add("lowjump");
                           data.sfLowJump = true;
                        }
                     }
                  }
               }
            }
         }
      }

      if (cc.survivalFlyAccountingV) {
         if (yDirChange && data.sfLastYDist > (double)0.0F) {
            data.vDistAcc.clear();
            data.vDistAcc.add((float)yDistance);
         } else if (data.verticalFreedom <= 0.001) {
            if (yDistance != (double)0.0F) {
               double accAboveLimit = verticalAccounting(now, from, to, yDistance, data.vDistAcc, this.tags, "vacc");
               if (accAboveLimit > vDistanceAboveLimit) {
                  vDistanceAboveLimit = accAboveLimit;
               }
            }
         } else {
            data.vDistAcc.clear();
         }
      }

      return vDistanceAboveLimit;
   }

   private static final double verticalAccounting(long now, PlayerLocation from, PlayerLocation to, double yDistance, ActionAccumulator acc, ArrayList tags, String tag) {
      acc.add((float)yDistance);
      int i1 = 1;
      int i2 = 2;
      if (acc.bucketCount(i1) > 0 && acc.bucketCount(i2) > 0) {
         float sc1 = acc.bucketScore(i1);
         float sc2 = acc.bucketScore(i2);
         double diff = (double)(sc1 - sc2);
         double aDiff = Math.abs(diff);
         if (diff > (double)0.0F || yDistance > -1.1 && aDiff <= 0.07) {
            if (!(yDistance < -1.1) || !(aDiff < Math.abs(yDistance)) && !(sc2 < -10.0F)) {
               tags.add(tag);
               if (diff < (double)0.0F) {
                  return 1.3 - aDiff;
               }

               return diff;
            }

            tags.add(tag + "grace");
            return (double)0.0F;
         }
      }

      return (double)0.0F;
   }

   private final Location hackCobweb(Player player, MovingData data, PlayerLocation to, long now, double vDistanceAboveLimit) {
      if (now - data.sfCobwebTime > 3000L) {
         data.sfCobwebTime = now;
         data.sfCobwebVL = vDistanceAboveLimit * (double)100.0F;
      } else {
         data.sfCobwebVL += vDistanceAboveLimit * (double)100.0F;
      }

      if (data.sfCobwebVL < (double)550.0F) {
         if (!data.hasSetBack()) {
            data.setSetBack(player.getLocation());
         }

         data.sfJumpPhase = 0;
         data.sfLastYDist = Double.MAX_VALUE;
         return data.getSetBack(to);
      } else {
         return null;
      }
   }

   public void setReallySneaking(Player player, boolean sneaking) {
      if (sneaking) {
         this.reallySneaking.add(player.getName());
      } else {
         this.reallySneaking.remove(player.getName());
      }

   }

   protected final double getJumpAmplifier(Player player) {
      double amplifier = this.mcAccess.getJumpAmplifier(player);
      return amplifier == Double.NEGATIVE_INFINITY ? (double)0.0F : (double)1.0F + amplifier;
   }
}
