package com.sk89q.worldedit.bukkit;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.util.YAMLConfiguration;
import java.io.File;

public class BukkitConfiguration extends YAMLConfiguration {
   public boolean noOpPermissions = false;
   private final WorldEditPlugin plugin;

   public BukkitConfiguration(YAMLProcessor config, WorldEditPlugin plugin) {
      super(config, plugin.getLogger());
      this.plugin = plugin;
   }

   public void load() {
      super.load();
      this.noOpPermissions = this.config.getBoolean("no-op-permissions", false);
      this.migrateLegacyFolders();
   }

   private void migrateLegacyFolders() {
      this.migrate(this.scriptsDir, "craftscripts");
      this.migrate(this.saveDir, "schematics");
      this.migrate("drawings", "draw.js images");
   }

   private void migrate(String file, String name) {
      File fromDir = new File(".", file);
      File toDir = new File(this.getWorkingDirectory(), file);
      if (fromDir.exists() & !toDir.exists()) {
         if (fromDir.renameTo(toDir)) {
            this.plugin.getLogger().info("Migrated " + name + " folder '" + file + "' from server root to plugin data folder.");
         } else {
            this.plugin.getLogger().warning("Error while migrating " + name + " folder!");
         }
      }

   }

   public File getWorkingDirectory() {
      return this.plugin.getDataFolder();
   }
}
