package fr.neatmonster.nocheatplus.checks.blockinteract;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Reach extends Check {
   public static final double CREATIVE_DISTANCE = 5.6;
   public static final double SURVIVAL_DISTANCE = 5.2;

   public Reach() {
      super(CheckType.BLOCKINTERACT_REACH);
   }

   public boolean check(Player player, Location loc, Block block, BlockInteractData data, BlockInteractConfig cc) {
      boolean cancel = false;
      double distanceLimit = player.getGameMode() == GameMode.CREATIVE ? 5.6 : 5.2;
      double distance = TrigUtil.distance(loc.getX(), loc.getY() + player.getEyeHeight(), loc.getZ(), (double)0.5F + (double)block.getX(), (double)0.5F + (double)block.getY(), (double)0.5F + (double)block.getZ()) - distanceLimit;
      if (distance > (double)0.0F) {
         data.reachVL += distance;
         data.reachDistance = distance;
         ViolationData vd = new ViolationData(this, player, data.reachVL, distance, cc.reachActions);
         vd.setParameter(ParameterName.REACH_DISTANCE, String.valueOf(Math.round(data.reachDistance)));
         cancel = this.executeActions(vd);
      } else {
         data.reachVL *= 0.9;
      }

      return cancel;
   }
}
