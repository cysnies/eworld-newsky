package com.sk89q.worldedit.tools;

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.WorldVector;

public class SinglePickaxe implements BlockTool {
   public boolean canUse(LocalPlayer player) {
      return player.hasPermission("worldedit.superpickaxe");
   }

   public boolean actPrimary(ServerInterface server, LocalConfiguration config, LocalPlayer player, LocalSession session, WorldVector clicked) {
      LocalWorld world = clicked.getWorld();
      int blockType = world.getBlockType(clicked);
      if (blockType == 7 && !player.canDestroyBedrock()) {
         return true;
      } else {
         if (config.superPickaxeDrop) {
            world.simulateBlockMine(clicked);
         }

         world.setBlockType(clicked, 0);
         world.playEffect(clicked, 2001, blockType);
         return true;
      }
   }
}
