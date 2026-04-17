package com.sk89q.worldedit.tools;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockType;

public class BlockReplacer implements DoubleActionBlockTool {
   private BaseBlock targetBlock;

   public BlockReplacer(BaseBlock targetBlock) {
      this.targetBlock = targetBlock;
   }

   public boolean canUse(LocalPlayer player) {
      return player.hasPermission("worldedit.tool.replacer");
   }

   public boolean actPrimary(ServerInterface server, LocalConfiguration config, LocalPlayer player, LocalSession session, WorldVector clicked) {
      BlockBag bag = session.getBlockBag(player);
      LocalWorld world = clicked.getWorld();
      EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1, bag, player);

      try {
         editSession.setBlock(clicked, (BaseBlock)this.targetBlock);
      } catch (MaxChangedBlocksException var13) {
      } finally {
         if (bag != null) {
            bag.flushChanges();
         }

         session.remember(editSession);
      }

      return true;
   }

   public boolean actSecondary(ServerInterface server, LocalConfiguration config, LocalPlayer player, LocalSession session, WorldVector clicked) {
      LocalWorld world = clicked.getWorld();
      EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1, (LocalPlayer)player);
      this.targetBlock = editSession.getBlock(clicked);
      BlockType type = BlockType.fromID(this.targetBlock.getType());
      if (type != null) {
         player.print("Replacer tool switched to: " + type.getName());
      }

      return true;
   }
}
