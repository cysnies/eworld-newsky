package com.sk89q.worldedit.tools;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.masks.CombinedMask;
import com.sk89q.worldedit.masks.Mask;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.patterns.SingleBlockPattern;
import com.sk89q.worldedit.tools.brushes.Brush;
import com.sk89q.worldedit.tools.brushes.SphereBrush;

public class BrushTool implements TraceTool {
   protected static int MAX_RANGE = 500;
   protected int range = -1;
   private Mask mask = null;
   private Brush brush = new SphereBrush();
   private Pattern material = new SingleBlockPattern(new BaseBlock(4));
   private double size = (double)1.0F;
   private String permission;

   public BrushTool(String permission) {
      this.permission = permission;
   }

   public boolean canUse(LocalPlayer player) {
      return player.hasPermission(this.permission);
   }

   public Mask getMask() {
      return this.mask;
   }

   public void setMask(Mask filter) {
      this.mask = filter;
   }

   public void setBrush(Brush brush, String perm) {
      this.brush = brush;
      this.permission = perm;
   }

   public Brush getBrush() {
      return this.brush;
   }

   public void setFill(Pattern material) {
      this.material = material;
   }

   public Pattern getMaterial() {
      return this.material;
   }

   public double getSize() {
      return this.size;
   }

   public void setSize(double radius) {
      this.size = radius;
   }

   public int getRange() {
      return this.range < 0 ? MAX_RANGE : Math.min(this.range, MAX_RANGE);
   }

   public void setRange(int range) {
      this.range = range;
   }

   public boolean actPrimary(ServerInterface server, LocalConfiguration config, LocalPlayer player, LocalSession session) {
      WorldVector target = null;
      target = player.getBlockTrace(this.getRange(), true);
      if (target == null) {
         player.printError("No block in sight!");
         return true;
      } else {
         BlockBag bag = session.getBlockBag(player);
         EditSession editSession = session.createEditSession(player);
         if (this.mask != null) {
            this.mask.prepare(session, player, target);
            Mask existingMask = editSession.getMask();
            if (existingMask == null) {
               editSession.setMask(this.mask);
            } else if (existingMask instanceof CombinedMask) {
               ((CombinedMask)existingMask).add(this.mask);
            } else {
               CombinedMask newMask = new CombinedMask(existingMask);
               newMask.add(this.mask);
               editSession.setMask(newMask);
            }
         }

         try {
            this.brush.build(editSession, target, this.material, this.size);
         } catch (MaxChangedBlocksException var13) {
            player.printError("Max blocks change limit reached.");
         } finally {
            if (bag != null) {
               bag.flushChanges();
            }

            session.remember(editSession);
         }

         return true;
      }
   }
}
