package com.sk89q.worldedit.tools;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.blocks.ClothColor;
import com.sk89q.worldedit.blocks.MobSpawnerBlock;
import com.sk89q.worldedit.blocks.NoteBlock;

public class QueryTool implements BlockTool {
   public boolean canUse(LocalPlayer player) {
      return player.hasPermission("worldedit.tool.info");
   }

   public boolean actPrimary(ServerInterface server, LocalConfiguration config, LocalPlayer player, LocalSession session, WorldVector clicked) {
      LocalWorld world = clicked.getWorld();
      EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, 0, (LocalPlayer)player);
      BaseBlock block = editSession.rawGetBlock(clicked);
      BlockType type = BlockType.fromID(block.getType());
      player.print("§9@" + clicked + ": " + "§e" + "#" + block.getType() + "§7" + " (" + (type == null ? "Unknown" : type.getName()) + ") " + "§f" + "[" + block.getData() + "]" + " (" + world.getBlockLightLevel(clicked) + "/" + world.getBlockLightLevel(clicked.add(0, 1, 0)) + ")");
      if (block instanceof MobSpawnerBlock) {
         player.printRaw("§eMob Type: " + ((MobSpawnerBlock)block).getMobType());
      } else if (block instanceof NoteBlock) {
         player.printRaw("§eNote block: " + ((NoteBlock)block).getNote());
      } else if (block.getType() == 35) {
         player.printRaw("§eColor: " + ClothColor.fromID(block.getData()).getName());
      }

      return true;
   }
}
