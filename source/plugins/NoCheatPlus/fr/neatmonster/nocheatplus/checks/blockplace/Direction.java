package fr.neatmonster.nocheatplus.checks.blockplace;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Direction extends Check {
   public Direction() {
      super(CheckType.BLOCKPLACE_DIRECTION);
   }

   public boolean check(Player player, Block placed, Block against, BlockPlaceData data) {
      boolean cancel = false;
      Location loc = player.getLocation();
      Vector direction = loc.getDirection();
      double off = TrigUtil.directionCheck(loc, player.getEyeHeight(), direction, against, 2.6);
      double off2 = (double)0.0F;
      if (placed.getX() > against.getX()) {
         off2 = (double)against.getX() + (double)0.5F - loc.getX();
      } else if (placed.getX() < against.getX()) {
         off2 = -((double)against.getX() + (double)0.5F - loc.getX());
      } else if (placed.getY() > against.getY()) {
         off2 = (double)against.getY() + (double)0.5F - loc.getY() - player.getEyeHeight();
      } else if (placed.getY() < against.getY()) {
         off2 = -((double)against.getY() + (double)0.5F - loc.getY() - player.getEyeHeight());
      } else if (placed.getZ() > against.getZ()) {
         off2 = (double)against.getZ() + (double)0.5F - loc.getZ();
      } else if (placed.getZ() < against.getZ()) {
         off2 = -((double)against.getZ() + (double)0.5F - loc.getZ());
      }

      if (off2 > (double)0.0F) {
         off += off2;
      }

      if (off > 0.1) {
         Vector blockEyes = new Vector((double)0.5F + (double)placed.getX() - loc.getX(), (double)0.5F + (double)placed.getY() - loc.getY() - player.getEyeHeight(), (double)0.5F + (double)placed.getZ() - loc.getZ());
         double distance = blockEyes.crossProduct(direction).length() / direction.length();
         data.directionVL += distance;
         cancel = this.executeActions(player, data.directionVL, distance, BlockPlaceConfig.getConfig(player).directionActions);
      } else {
         data.directionVL *= 0.9;
      }

      return cancel;
   }
}
