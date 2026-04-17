package com.sk89q.worldedit.regions;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import java.util.List;

public interface RegionSelector {
   boolean selectPrimary(Vector var1);

   boolean selectSecondary(Vector var1);

   void explainPrimarySelection(LocalPlayer var1, LocalSession var2, Vector var3);

   void explainSecondarySelection(LocalPlayer var1, LocalSession var2, Vector var3);

   void explainRegionAdjust(LocalPlayer var1, LocalSession var2);

   BlockVector getPrimaryPosition() throws IncompleteRegionException;

   Region getRegion() throws IncompleteRegionException;

   Region getIncompleteRegion();

   boolean isDefined();

   int getArea();

   void learnChanges();

   void clear();

   String getTypeName();

   List getInformationLines();
}
