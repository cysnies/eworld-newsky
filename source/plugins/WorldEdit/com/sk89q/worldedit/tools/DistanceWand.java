package com.sk89q.worldedit.tools;

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.regions.RegionSelector;

public class DistanceWand extends BrushTool implements DoubleActionTraceTool {
   public DistanceWand() {
      super("worldedit.wand");
   }

   public boolean canUse(LocalPlayer player) {
      return player.hasPermission("worldedit.wand");
   }

   public boolean actSecondary(ServerInterface server, LocalConfiguration config, LocalPlayer player, LocalSession session) {
      if (session.isToolControlEnabled() && player.hasPermission("worldedit.selection.pos")) {
         WorldVector target = this.getTarget(player);
         if (target == null) {
            return true;
         } else {
            RegionSelector selector = session.getRegionSelector(player.getWorld());
            if (selector.selectPrimary(target)) {
               selector.explainPrimarySelection(player, session, target);
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public boolean actPrimary(ServerInterface server, LocalConfiguration config, LocalPlayer player, LocalSession session) {
      if (session.isToolControlEnabled() && player.hasPermission("worldedit.selection.pos")) {
         WorldVector target = this.getTarget(player);
         if (target == null) {
            return true;
         } else {
            RegionSelector selector = session.getRegionSelector(player.getWorld());
            if (selector.selectSecondary(target)) {
               selector.explainSecondarySelection(player, session, target);
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public WorldVector getTarget(LocalPlayer player) {
      WorldVector target = null;
      if (this.range > -1) {
         target = player.getBlockTrace(this.getRange(), true);
      } else {
         target = player.getBlockTrace(MAX_RANGE);
      }

      if (target == null) {
         player.printError("No block in sight!");
         return null;
      } else {
         return target;
      }
   }
}
