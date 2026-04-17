package com.sk89q.worldedit.tools;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.util.TreeGenerator;

public class TreePlanter implements BlockTool {
   private TreeGenerator gen;

   public TreePlanter(TreeGenerator gen) {
      this.gen = gen;
   }

   public boolean canUse(LocalPlayer player) {
      return player.hasPermission("worldedit.tool.tree");
   }

   public boolean actPrimary(ServerInterface server, LocalConfiguration config, LocalPlayer player, LocalSession session, WorldVector clicked) {
      EditSession editSession = session.createEditSession(player);

      try {
         boolean successful = false;

         for(int i = 0; i < 10; ++i) {
            if (this.gen.generate(editSession, clicked.add(0, 1, 0))) {
               successful = true;
               break;
            }
         }

         if (!successful) {
            player.printError("A tree can't go there.");
         }
      } catch (MaxChangedBlocksException var12) {
         player.printError("Max. blocks changed reached.");
      } finally {
         session.remember(editSession);
      }

      return true;
   }
}
