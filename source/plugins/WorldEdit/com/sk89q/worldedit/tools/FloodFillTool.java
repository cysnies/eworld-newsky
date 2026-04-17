package com.sk89q.worldedit.tools;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.patterns.Pattern;
import java.util.HashSet;
import java.util.Set;

public class FloodFillTool implements BlockTool {
   private int range;
   private Pattern pattern;

   public FloodFillTool(int range, Pattern pattern) {
      this.range = range;
      this.pattern = pattern;
   }

   public boolean canUse(LocalPlayer player) {
      return player.hasPermission("worldedit.tool.flood-fill");
   }

   public boolean actPrimary(ServerInterface server, LocalConfiguration config, LocalPlayer player, LocalSession session, WorldVector clicked) {
      LocalWorld world = clicked.getWorld();
      int initialType = world.getBlockType(clicked);
      if (initialType == 0) {
         return true;
      } else if (initialType == 7 && !player.canDestroyBedrock()) {
         return true;
      } else {
         EditSession editSession = session.createEditSession(player);

         try {
            this.recurse(server, editSession, world, clicked.toBlockVector(), clicked, this.range, initialType, new HashSet());
         } catch (MaxChangedBlocksException var13) {
            player.printError("Max blocks change limit reached.");
         } finally {
            session.remember(editSession);
         }

         return true;
      }
   }

   private void recurse(ServerInterface server, EditSession editSession, LocalWorld world, BlockVector pos, Vector origin, int size, int initialType, Set visited) throws MaxChangedBlocksException {
      if (!(origin.distance(pos) > (double)size) && !visited.contains(pos)) {
         visited.add(pos);
         if (editSession.getBlock(pos).getType() == initialType) {
            editSession.setBlock(pos, (BaseBlock)this.pattern.next(pos));
            this.recurse(server, editSession, world, pos.add(1, 0, 0).toBlockVector(), origin, size, initialType, visited);
            this.recurse(server, editSession, world, pos.add(-1, 0, 0).toBlockVector(), origin, size, initialType, visited);
            this.recurse(server, editSession, world, pos.add(0, 0, 1).toBlockVector(), origin, size, initialType, visited);
            this.recurse(server, editSession, world, pos.add(0, 0, -1).toBlockVector(), origin, size, initialType, visited);
            this.recurse(server, editSession, world, pos.add(0, 1, 0).toBlockVector(), origin, size, initialType, visited);
            this.recurse(server, editSession, world, pos.add(0, -1, 0).toBlockVector(), origin, size, initialType, visited);
         }
      }
   }
}
