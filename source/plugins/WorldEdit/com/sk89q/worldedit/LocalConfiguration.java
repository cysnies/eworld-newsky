package com.sk89q.worldedit;

import com.sk89q.worldedit.snapshots.SnapshotRepository;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public abstract class LocalConfiguration {
   protected static final int[] defaultDisallowedBlocks = new int[]{6, 26, 27, 28, 31, 32, 34, 36, 37, 38, 39, 40, 46, 50, 51, 55, 59, 66, 69, 75, 76, 93, 94, 77, 81, 83, 7, 14, 15, 16, 56};
   public boolean profile = false;
   public Set disallowedBlocks = new HashSet();
   public int defaultChangeLimit = -1;
   public int maxChangeLimit = -1;
   public int defaultMaxPolygonalPoints = -1;
   public int maxPolygonalPoints = 20;
   public int defaultMaxPolyhedronPoints = -1;
   public int maxPolyhedronPoints = 20;
   public String shellSaveType = "";
   public SnapshotRepository snapshotRepo = null;
   public int maxRadius = -1;
   public int maxSuperPickaxeSize = 5;
   public int maxBrushRadius = 6;
   public boolean logCommands = false;
   public String logFile = "";
   public boolean registerHelp = true;
   public int wandItem = 271;
   public boolean superPickaxeDrop = true;
   public boolean superPickaxeManyDrop = true;
   public boolean noDoubleSlash = false;
   public boolean useInventory = false;
   public boolean useInventoryOverride = false;
   public boolean useInventoryCreativeOverride = false;
   public int navigationWand = 345;
   public int navigationWandMaxDistance = 50;
   public int scriptTimeout = 3000;
   public Set allowedDataCycleBlocks = new HashSet();
   public String saveDir = "schematics";
   public String scriptsDir = "craftscripts";
   public boolean showFirstUseVersion = true;
   public int butcherDefaultRadius = -1;
   public int butcherMaxRadius = -1;
   public boolean allowExtraDataValues = false;
   public boolean allowSymlinks = false;

   public abstract void load();

   public File getWorkingDirectory() {
      return new File(".");
   }
}
