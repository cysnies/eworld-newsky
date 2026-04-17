package com.sk89q.worldedit.util;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.snapshots.SnapshotRepository;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

public class YAMLConfiguration extends LocalConfiguration {
   protected final YAMLProcessor config;
   protected final Logger logger;

   public YAMLConfiguration(YAMLProcessor config, Logger logger) {
      this.config = config;
      this.logger = logger;
   }

   public void load() {
      try {
         this.config.load();
      } catch (IOException e) {
         this.logger.severe("Error loading WorldEdit configuration: " + e);
         e.printStackTrace();
      }

      this.showFirstUseVersion = false;
      this.profile = this.config.getBoolean("debug", this.profile);
      this.wandItem = this.config.getInt("wand-item", this.wandItem);
      this.defaultChangeLimit = Math.max(-1, this.config.getInt("limits.max-blocks-changed.default", this.defaultChangeLimit));
      this.maxChangeLimit = Math.max(-1, this.config.getInt("limits.max-blocks-changed.maximum", this.maxChangeLimit));
      this.defaultMaxPolygonalPoints = Math.max(-1, this.config.getInt("limits.max-polygonal-points.default", this.defaultMaxPolygonalPoints));
      this.maxPolygonalPoints = Math.max(-1, this.config.getInt("limits.max-polygonal-points.maximum", this.maxPolygonalPoints));
      this.defaultMaxPolyhedronPoints = Math.max(-1, this.config.getInt("limits.max-polyhedron-points.default", this.defaultMaxPolyhedronPoints));
      this.maxPolyhedronPoints = Math.max(-1, this.config.getInt("limits.max-polyhedron-points.maximum", this.maxPolyhedronPoints));
      this.maxRadius = Math.max(-1, this.config.getInt("limits.max-radius", this.maxRadius));
      this.maxBrushRadius = this.config.getInt("limits.max-brush-radius", this.maxBrushRadius);
      this.maxSuperPickaxeSize = Math.max(1, this.config.getInt("limits.max-super-pickaxe-size", this.maxSuperPickaxeSize));
      this.butcherDefaultRadius = Math.max(-1, this.config.getInt("limits.butcher-radius.default", this.butcherDefaultRadius));
      this.butcherMaxRadius = Math.max(-1, this.config.getInt("limits.butcher-radius.maximum", this.butcherMaxRadius));
      this.disallowedBlocks = new HashSet(this.config.getIntList("limits.disallowed-blocks", (List)null));
      this.allowedDataCycleBlocks = new HashSet(this.config.getIntList("limits.allowed-data-cycle-blocks", (List)null));
      this.allowExtraDataValues = this.config.getBoolean("limits.allow-extra-data-values", false);
      this.registerHelp = this.config.getBoolean("register-help", true);
      this.logCommands = this.config.getBoolean("logging.log-commands", this.logCommands);
      this.logFile = this.config.getString("logging.file", this.logFile);
      this.superPickaxeDrop = this.config.getBoolean("super-pickaxe.drop-items", this.superPickaxeDrop);
      this.superPickaxeManyDrop = this.config.getBoolean("super-pickaxe.many-drop-items", this.superPickaxeManyDrop);
      this.noDoubleSlash = this.config.getBoolean("no-double-slash", this.noDoubleSlash);
      this.useInventory = this.config.getBoolean("use-inventory.enable", this.useInventory);
      this.useInventoryOverride = this.config.getBoolean("use-inventory.allow-override", this.useInventoryOverride);
      this.useInventoryCreativeOverride = this.config.getBoolean("use-inventory.creative-mode-overrides", this.useInventoryCreativeOverride);
      this.navigationWand = this.config.getInt("navigation-wand.item", this.navigationWand);
      this.navigationWandMaxDistance = this.config.getInt("navigation-wand.max-distance", this.navigationWandMaxDistance);
      this.scriptTimeout = this.config.getInt("scripting.timeout", this.scriptTimeout);
      this.scriptsDir = this.config.getString("scripting.dir", this.scriptsDir);
      this.saveDir = this.config.getString("saving.dir", this.saveDir);
      this.allowSymlinks = this.config.getBoolean("files.allow-symbolic-links", false);
      LocalSession.MAX_HISTORY_SIZE = Math.max(0, this.config.getInt("history.size", 15));
      LocalSession.EXPIRATION_GRACE = this.config.getInt("history.expiration", 10) * 60 * 1000;
      String snapshotsDir = this.config.getString("snapshots.directory", "");
      if (snapshotsDir.length() > 0) {
         this.snapshotRepo = new SnapshotRepository(snapshotsDir);
      }

      String type = this.config.getString("shell-save-type", "").trim();
      this.shellSaveType = type.equals("") ? null : type;
   }

   public void unload() {
   }
}
