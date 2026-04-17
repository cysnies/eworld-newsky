package fr.neatmonster.nocheatplus.checks.fight;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.checks.moving.MovingListener;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class Critical extends Check {
   public Critical() {
      super(CheckType.FIGHT_CRITICAL);
   }

   public boolean check(Player player) {
      FightConfig cc = FightConfig.getConfig(player);
      FightData data = FightData.getData(player);
      boolean cancel = false;
      Location loc = player.getLocation();
      float mcFallDistance = player.getFallDistance();
      MovingConfig mCc = MovingConfig.getConfig(player);
      if ((double)mcFallDistance > (double)0.0F && cc.debug && player.hasPermission("nocheatplus.admin.debug")) {
         MovingData mData = MovingData.getData(player);
         if (MovingListener.shouldCheckSurvivalFly(player, mData, mCc) && CheckType.MOVING_NOFALL.isEnabled(player)) {
            player.sendMessage("Critical: fd=" + mcFallDistance + "(" + mData.noFallFallDistance + ") y=" + loc.getY() + (mData.hasSetBack() && mData.getSetBackY() < mData.noFallMaxY ? " jumped=" + StringUtil.fdec3.format(mData.noFallMaxY - mData.getSetBackY()) : ""));
         }
      }

      if (mcFallDistance > 0.0F && !player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
         MovingData dataM = MovingData.getData(player);
         if (dataM.sfLowJump || (double)player.getFallDistance() < cc.criticalFallDistance && !BlockProperties.isOnGroundOrResetCond(player, loc, mCc.yOnGround)) {
            MovingConfig ccM = MovingConfig.getConfig(player);
            if (MovingListener.shouldCheckSurvivalFly(player, dataM, ccM)) {
               double deltaFallDistance = (cc.criticalFallDistance - (double)player.getFallDistance()) / cc.criticalFallDistance;
               double deltaVelocity = (cc.criticalVelocity - Math.abs(player.getVelocity().getY())) / cc.criticalVelocity;
               double delta = deltaFallDistance > (double)0.0F ? deltaFallDistance : ((double)0.0F + deltaVelocity > (double)0.0F ? deltaVelocity : (double)0.0F);
               List<String> tags = new ArrayList();
               if ((double)TickTask.getLag(1000L) < (double)1.5F) {
                  data.criticalVL += delta;
               } else {
                  tags.add("lag");
                  delta = (double)0.0F;
               }

               ViolationData vd = new ViolationData(this, player, data.criticalVL, delta, cc.criticalActions);
               if (vd.needsParameters()) {
                  if (dataM.sfLowJump) {
                     tags.add("sf_lowjump");
                  }

                  vd.setParameter(ParameterName.TAGS, StringUtil.join(tags, "+"));
               }

               cancel = this.executeActions(vd);
            }
         }
      }

      return cancel;
   }
}
