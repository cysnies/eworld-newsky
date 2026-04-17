package io.nv.bukkit.CleanroomGenerator;

import java.util.logging.Logger;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class CleanroomGenerator extends JavaPlugin {
   private Logger log = Logger.getLogger("Minecraft");
   PluginDescriptionFile pluginDescriptionFile;

   public void onEnable() {
      this.pluginDescriptionFile = this.getDescription();
      this.log.info("[CleanroomGenerator] " + this.pluginDescriptionFile.getFullName() + " enabled");
   }

   public void onDisable() {
   }

   public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
      return new CleanroomChunkGenerator(id);
   }
}
