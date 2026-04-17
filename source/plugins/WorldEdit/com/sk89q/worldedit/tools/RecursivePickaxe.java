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
import java.util.HashSet;
import java.util.Set;

public class RecursivePickaxe implements BlockTool {
   private static final BaseBlock air = new BaseBlock(0);
   private double range;

   public RecursivePickaxe(double range) {
      this.range = range;
   }

   public boolean canUse(LocalPlayer player) {
      return player.hasPermission("worldedit.superpickaxe.recursive");
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
            recurse(server, editSession, world, clicked.toBlockVector(), clicked, this.range, initialType, new HashSet(), config.superPickaxeManyDrop);
         } catch (MaxChangedBlocksException var13) {
            player.printError("Max blocks change limit reached.");
         } finally {
            session.remember(editSession);
         }

         return true;
      }
   }

   private static void recurse(ServerInterface server, EditSession editSession, LocalWorld world, BlockVector pos, Vector origin, double size, int initialType, Set visited, boolean drop) throws MaxChangedBlocksException {
      double distanceSq = origin.distanceSq(pos);
      if (!(distanceSq > size * size) && !visited.contains(pos)) {
         visited.add(pos);
         if (editSession.getBlock(pos).getType() == initialType) {
            if (drop) {
               world.simulateBlockMine(pos);
            }

            world.queueBlockBreakEffect(server, pos, initialType, distanceSq);
            editSession.setBlock(pos, (BaseBlock)air);
            recurse(server, editSession, world, pos.add(1, 0, 0).toBlockVector(), origin, size, initialType, visited, drop);
            recurse(server, editSession, world, pos.add(-1, 0, 0).toBlockVector(), origin, size, initialType, visited, drop);
            recurse(server, editSession, world, pos.add(0, 0, 1).toBlockVector(), origin, size, initialType, visited, drop);
            recurse(server, editSession, world, pos.add(0, 0, -1).toBlockVector(), origin, size, initialType, visited, drop);
            recurse(server, editSession, world, pos.add(0, 1, 0).toBlockVector(), origin, size, initialType, visited, drop);
            recurse(server, editSession, world, pos.add(0, -1, 0).toBlockVector(), origin, size, initialType, visited, drop);
         }
      }
   }
}
