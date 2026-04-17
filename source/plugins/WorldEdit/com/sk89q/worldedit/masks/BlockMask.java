package com.sk89q.worldedit.masks;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class BlockMask implements Mask {
   protected Set blocks;

   public BlockMask() {
      this.blocks = new HashSet();
   }

   public BlockMask(Set types) {
      this.blocks = types;
   }

   public BlockMask(BaseBlock block) {
      this();
      this.add(block);
   }

   public void add(BaseBlock block) {
      this.blocks.add(block);
   }

   public void addAll(Collection blocks) {
      blocks.addAll(blocks);
   }

   public void prepare(LocalSession session, LocalPlayer player, Vector target) {
   }

   public boolean matches(EditSession editSession, Vector pos) {
      BaseBlock block = editSession.getBlock(pos);
      return this.blocks.contains(block) || this.blocks.contains(new BaseBlock(block.getType(), -1));
   }
}
