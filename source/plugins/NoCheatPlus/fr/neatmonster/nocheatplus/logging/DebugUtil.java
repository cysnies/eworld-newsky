package fr.neatmonster.nocheatplus.logging;

import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.build.BuildParameters;
import java.util.Arrays;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class DebugUtil {
   public static void addFormattedMove(PlayerLocation from, PlayerLocation to, Location loc, StringBuilder builder) {
      builder.append(StringUtil.fdec3.format(from.getX()) + (from.getX() == loc.getX() ? "" : "(" + StringUtil.fdec3.format(loc.getX()) + ")"));
      builder.append(", " + StringUtil.fdec3.format(from.getY()) + (from.getY() == loc.getY() ? "" : "(" + StringUtil.fdec3.format(loc.getY()) + ")"));
      builder.append(", " + StringUtil.fdec3.format(from.getZ()) + (from.getZ() == loc.getZ() ? "" : "(" + StringUtil.fdec3.format(loc.getZ()) + ")"));
      builder.append(" -> " + StringUtil.fdec3.format(to.getX()) + ", " + StringUtil.fdec3.format(to.getY()) + ", " + StringUtil.fdec3.format(to.getZ()));
   }

   public static void addMove(PlayerLocation from, PlayerLocation to, Location loc, StringBuilder builder) {
      builder.append("from: " + from.getX() + (from.getX() == loc.getX() ? "" : "(" + loc.getX() + ")"));
      builder.append(", " + from.getY() + (from.getY() == loc.getY() ? "" : "(" + loc.getY() + ")"));
      builder.append(", " + from.getZ() + (from.getZ() == loc.getZ() ? "" : "(" + loc.getZ() + ")"));
      builder.append("\nto: " + to.getX() + ", " + to.getY() + ", " + to.getZ());
   }

   public static void outputMoveDebug(Player player, PlayerLocation from, PlayerLocation to, double maxYOnGround, MCAccess mcAccess) {
      StringBuilder builder = new StringBuilder(250);
      Location loc = player.getLocation();
      if (BuildParameters.debugLevel > 0) {
         builder.append("\n-------------- MOVE --------------\n");
         builder.append(player.getName() + " " + from.getWorld().getName() + ":\n");
         addMove(from, to, loc, builder);
      } else {
         builder.append(player.getName() + " " + from.getWorld().getName() + " ");
         addFormattedMove(from, to, loc, builder);
      }

      double jump = mcAccess.getJumpAmplifier(player);
      double speed = mcAccess.getFasterMovementAmplifier(player);
      if (speed != Double.NEGATIVE_INFINITY || jump != Double.NEGATIVE_INFINITY) {
         builder.append(" (" + (speed != Double.NEGATIVE_INFINITY ? "speed=" + (speed + (double)1.0F) : "") + (jump != Double.NEGATIVE_INFINITY ? "jump=" + (jump + (double)1.0F) : "") + ")");
      }

      System.out.print(builder.toString());
      if (BuildParameters.debugLevel > 0) {
         builder.setLength(0);
         from.collectBlockFlags(maxYOnGround);
         if (from.getBlockFlags() != 0L) {
            builder.append("\nfrom flags: " + StringUtil.join(BlockProperties.getFlagNames(from.getBlockFlags()), "+"));
         }

         if (from.getTypeId() != 0) {
            addBlockInfo(builder, from, "\nfrom");
         }

         if (from.getTypeIdBelow() != 0) {
            addBlockBelowInfo(builder, from, "\nfrom");
         }

         if (!from.isOnGround() && from.isOnGround((double)0.5F)) {
            builder.append(" (ground within 0.5)");
         }

         to.collectBlockFlags(maxYOnGround);
         if (to.getBlockFlags() != 0L) {
            builder.append("\nto flags: " + StringUtil.join(BlockProperties.getFlagNames(to.getBlockFlags()), "+"));
         }

         if (to.getTypeId() != 0) {
            addBlockInfo(builder, to, "\nto");
         }

         if (to.getTypeIdBelow() != 0) {
            addBlockBelowInfo(builder, to, "\nto");
         }

         if (!to.isOnGround() && to.isOnGround((double)0.5F)) {
            builder.append(" (ground within 0.5)");
         }

         System.out.print(builder.toString());
      }

   }

   public static void addBlockBelowInfo(StringBuilder builder, PlayerLocation loc, String tag) {
      builder.append(tag + " below id=" + loc.getTypeIdBelow() + " data=" + loc.getData(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ()) + " shape=" + Arrays.toString(loc.getBlockCache().getBounds(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ())));
   }

   public static void addBlockInfo(StringBuilder builder, PlayerLocation loc, String tag) {
      builder.append(tag + " id=" + loc.getTypeId() + " data=" + loc.getData() + " shape=" + Arrays.toString(loc.getBlockCache().getBounds(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())));
   }
}
