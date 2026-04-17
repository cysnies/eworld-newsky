package com.sk89q.worldedit.tools;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.WorldVectorFace;
import com.sk89q.worldedit.blocks.BaseBlock;

public class LongRangeBuildTool extends BrushTool implements DoubleActionTraceTool {
   BaseBlock primary;
   BaseBlock secondary;

   public LongRangeBuildTool(BaseBlock primary, BaseBlock secondary) {
      super("worldedit.tool.lrbuild");
      this.primary = primary;
      this.secondary = secondary;
   }

   public boolean canUse(LocalPlayer player) {
      return player.hasPermission("worldedit.tool.lrbuild");
   }

   public boolean actSecondary(ServerInterface server, LocalConfiguration config, LocalPlayer player, LocalSession session) {
      WorldVectorFace pos = this.getTargetFace(player);
      if (pos == null) {
         return false;
      } else {
         EditSession eS = session.createEditSession(player);

         try {
            if (this.secondary.getType() == 0) {
               eS.setBlock(pos, (BaseBlock)this.secondary);
            } else {
               eS.setBlock(pos.getFaceVector(), (BaseBlock)this.secondary);
            }

            return true;
         } catch (MaxChangedBlocksException var8) {
            return false;
         }
      }
   }

   public boolean actPrimary(ServerInterface server, LocalConfiguration config, LocalPlayer player, LocalSession session) {
      WorldVectorFace pos = this.getTargetFace(player);
      if (pos == null) {
         return false;
      } else {
         EditSession eS = session.createEditSession(player);

         try {
            if (this.primary.getType() == 0) {
               eS.setBlock(pos, (BaseBlock)this.primary);
            } else {
               eS.setBlock(pos.getFaceVector(), (BaseBlock)this.primary);
            }

            return true;
         } catch (MaxChangedBlocksException var8) {
            return false;
         }
      }
   }

   public WorldVectorFace getTargetFace(LocalPlayer player) {
      WorldVectorFace target = null;
      target = player.getBlockTraceFace(this.getRange(), true);
      if (target == null) {
         player.printError("No block in sight!");
         return null;
      } else {
         return target;
      }
   }
}
