package fr.neatmonster.nocheatplus.config;

import org.bukkit.configuration.MemorySection;

public class ConfigFile extends ConfigFileWithActions {
   public void regenerateActionLists() {
      this.factory = ConfigManager.getActionFactory(((MemorySection)this.get("strings")).getValues(false));
   }
}
