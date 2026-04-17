package com.sk89q.worldedit.tools;

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.blocks.BaseBlock;

public class BlockDataCyler implements DoubleActionBlockTool {
   public boolean canUse(LocalPlayer player) {
      return player.hasPermission("worldedit.tool.data-cycler");
   }

   private boolean handleCycle(ServerInterface server, LocalConfiguration config, LocalPlayer player, LocalSession session, WorldVector clicked, boolean forward) {
      LocalWorld world = clicked.getWorld();
      int type = world.getBlockType(clicked);
      int data = world.getBlockData(clicked);
      if (config.allowedDataCycleBlocks.size() > 0 && !player.hasPermission("worldedit.override.data-cycler") && !config.allowedDataCycleBlocks.contains(type)) {
         player.printError("You are not permitted to cycle the data value of that block.");
         return true;
      } else {
         int increment = forward ? 1 : -1;
         data = (new BaseBlock(type, data)).cycleData(increment);
         if (data < 0) {
            player.printError("That block's data cannot be cycled!");
         } else {
            world.setBlockData(clicked, data);
         }

         return true;
      }
   }

   public boolean actPrimary(ServerInterface server, LocalConfiguration config, LocalPlayer player, LocalSession session, WorldVector clicked) {
      return this.handleCycle(server, config, player, session, clicked, true);
   }

   public boolean actSecondary(ServerInterface server, LocalConfiguration config, LocalPlayer player, LocalSession session, WorldVector clicked) {
      return this.handleCycle(server, config, player, session, clicked, false);
   }
}
