package com.goncalomb.bukkit.nbteditor.nbt;

import org.bukkit.block.Block;

public class MinecartHopperNBT extends MinecartContainerNBT {
   public void copyFromChest(Block block) {
      this.internalCopyFromChest(block, 5);
   }
}
