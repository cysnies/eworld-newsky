package fr.neatmonster.nocheatplus.checks.blockplace;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;
import java.util.Map;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Reach extends Check {
   public static final double CREATIVE_DISTANCE = 5.6;
   public static final double SURVIVAL_DISTANCE = 5.2;

   public Reach() {
      super(CheckType.BLOCKPLACE_REACH);
   }

   public boolean check(Player player, Block block, BlockPlaceData data) {
      boolean cancel = false;
      double distanceLimit = player.getGameMode() == GameMode.CREATIVE ? 5.6 : 5.2;
      double distance = TrigUtil.distance(player.getEyeLocation(), block) - distanceLimit;
      if (distance > (double)0.0F) {
         data.reachVL += distance;
         data.reachDistance = distance;
         cancel = this.executeActions(player, data.reachVL, distance, BlockPlaceConfig.getConfig(player).reachActions);
      } else {
         data.reachVL *= 0.9;
      }

      return cancel;
   }

   protected Map getParameterMap(ViolationData violationData) {
      Map<ParameterName, String> parameters = super.getParameterMap(violationData);
      parameters.put(ParameterName.REACH_DISTANCE, "" + Math.round(BlockPlaceData.getData(violationData.player).reachDistance));
      return parameters;
   }
}
