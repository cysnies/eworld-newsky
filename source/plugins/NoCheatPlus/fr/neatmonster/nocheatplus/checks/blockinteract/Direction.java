package fr.neatmonster.nocheatplus.checks.blockinteract;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Direction extends Check {
   public Direction() {
      super(CheckType.BLOCKINTERACT_DIRECTION);
   }

   public boolean check(Player player, Location loc, Block block, BlockInteractData data, BlockInteractConfig cc) {
      boolean cancel = false;
      Vector direction = loc.getDirection();
      double off = TrigUtil.directionCheck(loc, player.getEyeHeight(), direction, block, 2.6);
      if (off > 0.1) {
         Vector blockEyes = new Vector((double)0.5F + (double)block.getX() - loc.getX(), (double)0.5F + (double)block.getY() - loc.getY() - player.getEyeHeight(), (double)0.5F + (double)block.getZ() - loc.getZ());
         double distance = blockEyes.crossProduct(direction).length() / direction.length();
         data.directionVL += distance;
         cancel = this.executeActions(player, data.directionVL, distance, cc.directionActions);
      } else {
         data.directionVL *= 0.9;
      }

      return cancel;
   }
}
