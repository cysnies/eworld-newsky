package com.goncalomb.bukkit.nbteditor.nbt;

import org.bukkit.block.Block;

public class MinecartChestNBT extends MinecartContainerNBT {
   public void copyFromChest(Block block) {
      this.internalCopyFromChest(block, 27);
   }
}
