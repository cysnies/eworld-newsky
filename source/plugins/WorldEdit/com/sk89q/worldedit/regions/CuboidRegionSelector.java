package com.sk89q.worldedit.regions;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.cui.CUIRegion;
import com.sk89q.worldedit.cui.SelectionPointEvent;
import java.util.ArrayList;
import java.util.List;

public class CuboidRegionSelector implements RegionSelector, CUIRegion {
   protected BlockVector pos1;
   protected BlockVector pos2;
   protected CuboidRegion region;

   public CuboidRegionSelector(LocalWorld world) {
      this.region = new CuboidRegion(world, new Vector(), new Vector());
   }

   public CuboidRegionSelector() {
      this((LocalWorld)null);
   }

   public CuboidRegionSelector(RegionSelector oldSelector) {
      this(oldSelector.getIncompleteRegion().getWorld());
      if (oldSelector instanceof CuboidRegionSelector) {
         CuboidRegionSelector cuboidRegionSelector = (CuboidRegionSelector)oldSelector;
         this.pos1 = cuboidRegionSelector.pos1;
         this.pos2 = cuboidRegionSelector.pos2;
      } else {
         Region oldRegion;
         try {
            oldRegion = oldSelector.getRegion();
         } catch (IncompleteRegionException var4) {
            return;
         }

         this.pos1 = oldRegion.getMinimumPoint().toBlockVector();
         this.pos2 = oldRegion.getMaximumPoint().toBlockVector();
      }

      this.region.setPos1(this.pos1);
      this.region.setPos2(this.pos2);
   }

   public CuboidRegionSelector(LocalWorld world, Vector pos1, Vector pos2) {
      this(world);
      this.pos1 = pos1.toBlockVector();
      this.pos2 = pos2.toBlockVector();
      this.region.setPos1(pos1);
      this.region.setPos2(pos2);
   }

   public boolean selectPrimary(Vector pos) {
      if (this.pos1 != null && pos.compareTo((Vector)this.pos1) == 0) {
         return false;
      } else {
         this.pos1 = pos.toBlockVector();
         this.region.setPos1(this.pos1);
         return true;
      }
   }

   public boolean selectSecondary(Vector pos) {
      if (this.pos2 != null && pos.compareTo((Vector)this.pos2) == 0) {
         return false;
      } else {
         this.pos2 = pos.toBlockVector();
         this.region.setPos2(this.pos2);
         return true;
      }
   }

   public void explainPrimarySelection(LocalPlayer player, LocalSession session, Vector pos) {
      if (this.pos1 != null && this.pos2 != null) {
         player.print("First position set to " + this.pos1 + " (" + this.region.getArea() + ").");
      } else {
         player.print("First position set to " + this.pos1 + ".");
      }

      session.dispatchCUIEvent(player, new SelectionPointEvent(0, pos, this.getArea()));
   }

   public void explainSecondarySelection(LocalPlayer player, LocalSession session, Vector pos) {
      if (this.pos1 != null && this.pos2 != null) {
         player.print("Second position set to " + this.pos2 + " (" + this.region.getArea() + ").");
      } else {
         player.print("Second position set to " + this.pos2 + ".");
      }

      session.dispatchCUIEvent(player, new SelectionPointEvent(1, pos, this.getArea()));
   }

   public void explainRegionAdjust(LocalPlayer player, LocalSession session) {
      if (this.pos1 != null) {
         session.dispatchCUIEvent(player, new SelectionPointEvent(0, this.pos1, this.getArea()));
      }

      if (this.pos2 != null) {
         session.dispatchCUIEvent(player, new SelectionPointEvent(1, this.pos2, this.getArea()));
      }

   }

   public BlockVector getPrimaryPosition() throws IncompleteRegionException {
      if (this.pos1 == null) {
         throw new IncompleteRegionException();
      } else {
         return this.pos1;
      }
   }

   public boolean isDefined() {
      return this.pos1 != null && this.pos2 != null;
   }

   public CuboidRegion getRegion() throws IncompleteRegionException {
      if (this.pos1 != null && this.pos2 != null) {
         return this.region;
      } else {
         throw new IncompleteRegionException();
      }
   }

   public CuboidRegion getIncompleteRegion() {
      return this.region;
   }

   public void learnChanges() {
      this.pos1 = this.region.getPos1().toBlockVector();
      this.pos2 = this.region.getPos2().toBlockVector();
   }

   public void clear() {
      this.pos1 = null;
      this.pos2 = null;
   }

   public String getTypeName() {
      return "cuboid";
   }

   public List getInformationLines() {
      List<String> lines = new ArrayList();
      if (this.pos1 != null) {
         lines.add("Position 1: " + this.pos1);
      }

      if (this.pos2 != null) {
         lines.add("Position 2: " + this.pos2);
      }

      return lines;
   }

   public int getArea() {
      if (this.pos1 == null) {
         return -1;
      } else {
         return this.pos2 == null ? -1 : this.region.getArea();
      }
   }

   public void describeCUI(LocalSession session, LocalPlayer player) {
      if (this.pos1 != null) {
         session.dispatchCUIEvent(player, new SelectionPointEvent(0, this.pos1, this.getArea()));
      }

      if (this.pos2 != null) {
         session.dispatchCUIEvent(player, new SelectionPointEvent(1, this.pos2, this.getArea()));
      }

   }

   public void describeLegacyCUI(LocalSession session, LocalPlayer player) {
      this.describeCUI(session, player);
   }

   public int getProtocolVersion() {
      return 0;
   }

   public String getTypeID() {
      return "cuboid";
   }

   public String getLegacyTypeID() {
      return "cuboid";
   }
}
