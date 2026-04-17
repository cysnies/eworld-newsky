package com.sk89q.worldedit.tools;

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

public class AreaPickaxe implements BlockTool {
   private static final BaseBlock air = new BaseBlock(0);
   private int range;

   public AreaPickaxe(int range) {
      this.range = range;
   }

   public boolean canUse(LocalPlayer player) {
      return player.hasPermission("worldedit.superpickaxe.area");
   }

   public boolean actPrimary(ServerInterface server, LocalConfiguration config, LocalPlayer player, LocalSession session, WorldVector clicked) {
      LocalWorld world = clicked.getWorld();
      int ox = clicked.getBlockX();
      int oy = clicked.getBlockY();
      int oz = clicked.getBlockZ();
      int initialType = world.getBlockType(clicked);
      if (initialType == 0) {
         return true;
      } else if (initialType == 7 && !player.canDestroyBedrock()) {
         return true;
      } else {
         EditSession editSession = session.createEditSession(player);

         try {
            for(int x = ox - this.range; x <= ox + this.range; ++x) {
               for(int y = oy - this.range; y <= oy + this.range; ++y) {
                  for(int z = oz - this.range; z <= oz + this.range; ++z) {
                     Vector pos = new Vector(x, y, z);
                     if (world.getBlockType(pos) == initialType) {
                        if (config.superPickaxeManyDrop) {
                           world.simulateBlockMine(pos);
                        }

                        world.queueBlockBreakEffect(server, pos, initialType, clicked.distanceSq(pos));
                        editSession.setBlock(pos, air);
                     }
                  }
               }
            }
         } catch (MaxChangedBlocksException var19) {
            player.printError("Max blocks change limit reached.");
         } finally {
            session.remember(editSession);
         }

         return true;
      }
   }
}
