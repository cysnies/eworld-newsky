package fr.neatmonster.nocheatplus.checks.moving;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.compat.BridgeHealth;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class NoFall extends Check {
   public NoFall() {
      super(CheckType.MOVING_NOFALL);
   }

   protected static final double getDamage(float fallDistance) {
      return (double)fallDistance - (double)3.0F;
   }

   private final void handleOnGround(Player player, double y, boolean reallyOnGround, MovingData data, MovingConfig cc) {
      double maxD = getDamage(Math.max((float)(data.noFallMaxY - y), Math.max(data.noFallFallDistance, player.getFallDistance())));
      if (maxD > (double)0.0F) {
         if (cc.debug) {
            System.out.println(player.getName() + " NoFall deal damage" + (reallyOnGround ? "" : "violation") + ": " + maxD);
         }

         data.noFallSkipAirCheck = true;
         this.dealFallDamage(player, maxD);
      } else {
         data.clearNoFallData();
      }

   }

   private final void adjustFallDistance(Player player, double minY, boolean reallyOnGround, MovingData data, MovingConfig cc) {
      float noFallFallDistance = Math.max(data.noFallFallDistance, (float)(data.noFallMaxY - minY));
      if ((double)noFallFallDistance >= (double)3.0F) {
         float fallDistance = player.getFallDistance();
         if (noFallFallDistance - fallDistance >= 0.5F || noFallFallDistance >= 3.5F && noFallFallDistance < 3.5F) {
            player.setFallDistance(noFallFallDistance);
         }
      }

      data.clearNoFallData();
   }

   private void dealFallDamage(Player player, double damage) {
      EntityDamageEvent event = BridgeHealth.getEntityDamageEvent(player, DamageCause.FALL, damage);
      Bukkit.getPluginManager().callEvent(event);
      if (!event.isCancelled()) {
         player.setLastDamageCause(event);
         this.mcAccess.dealFallDamage(player, BridgeHealth.getDamage(event));
      }

      player.setFallDistance(0.0F);
   }

   public void check(Player player, Location loc, PlayerLocation from, PlayerLocation to, MovingData data, MovingConfig cc) {
      double fromY = from.getY();
      double toY = to.getY();
      double yDiff = toY - fromY;
      double oldNFDist = (double)data.noFallFallDistance;
      boolean fromReset = from.isResetCond();
      boolean toReset = to.isResetCond();
      if (yDiff < (double)0.0F && cc.yOnGround < cc.noFallyOnGround) {
         this.adjustYonGround(from, to, cc.noFallyOnGround);
      }

      boolean fromOnGround = from.isOnGround();
      boolean toOnGround = to.isOnGround();
      double pY = loc.getY();
      double minY = Math.min(fromY, Math.min(toY, pY));
      if (fromReset) {
         data.clearNoFallData();
      } else if (!fromOnGround && !data.noFallAssumeGround) {
         if (toReset) {
            data.clearNoFallData();
         } else if (toOnGround) {
            if (yDiff < (double)0.0F) {
               data.noFallFallDistance = (float)((double)data.noFallFallDistance - yDiff);
            }

            if (cc.noFallDealDamage) {
               this.handleOnGround(player, minY, true, data, cc);
            } else {
               this.adjustFallDistance(player, minY, true, data, cc);
            }
         }
      } else if (cc.noFallDealDamage) {
         this.handleOnGround(player, minY, true, data, cc);
      } else {
         this.adjustFallDistance(player, minY, true, data, cc);
      }

      data.noFallMaxY = Math.max(Math.max(fromY, Math.max(toY, pY)), data.noFallMaxY);
      float mcFallDistance = player.getFallDistance();
      data.noFallFallDistance = Math.max(mcFallDistance, data.noFallFallDistance);
      if (!toReset && !toOnGround && yDiff < (double)0.0F) {
         data.noFallFallDistance = (float)((double)data.noFallFallDistance - yDiff);
      } else if (cc.noFallAntiCriticals && (toReset || toOnGround || (fromReset || fromOnGround || data.noFallAssumeGround) && yDiff >= (double)0.0F)) {
         double max = (double)Math.max(data.noFallFallDistance, mcFallDistance);
         if (max > (double)0.0F && max < (double)0.75F) {
            if (cc.debug) {
               System.out.println(player.getName() + " NoFall: Reset fall distance (anticriticals): mc=" + StringUtil.fdec3.format((double)mcFallDistance) + " / nf=" + StringUtil.fdec3.format((double)data.noFallFallDistance));
            }

            if (data.noFallFallDistance > 0.0F) {
               data.noFallFallDistance = 0.0F;
            }

            if (mcFallDistance > 0.0F) {
               player.setFallDistance(0.0F);
            }
         }
      }

      if (cc.debug) {
         System.out.println(player.getName() + " NoFall: mc=" + StringUtil.fdec3.format((double)mcFallDistance) + " / nf=" + StringUtil.fdec3.format((double)data.noFallFallDistance) + (oldNFDist < (double)data.noFallFallDistance ? " (+" + StringUtil.fdec3.format((double)data.noFallFallDistance - oldNFDist) + ")" : "") + " | ymax=" + StringUtil.fdec3.format(data.noFallMaxY));
      }

   }

   private void adjustYonGround(PlayerLocation from, PlayerLocation to, double yOnGround) {
      if (!from.isOnGround()) {
         from.setyOnGround(yOnGround);
      }

      if (!to.isOnGround()) {
         to.setyOnGround(yOnGround);
      }

   }

   public void onLeave(Player player) {
      MovingData data = MovingData.getData(player);
      float fallDistance = player.getFallDistance();
      if (data.noFallFallDistance - fallDistance > 0.0F) {
         float yDiff = (float)(data.noFallMaxY - player.getLocation().getY());
         float maxDist = Math.max(yDiff, Math.max(data.noFallFallDistance, fallDistance));
         player.setFallDistance(maxDist);
      }

   }

   public void checkDamage(Player player, MovingData data, double y) {
      MovingConfig cc = MovingConfig.getConfig(player);
      this.handleOnGround(player, y, false, data, cc);
   }
}
