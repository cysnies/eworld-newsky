package com.goncalomb.bukkit.customitems.api;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

final class CustomItemConfig {
   private Logger _logger;
   private FileConfiguration _defaultConfig;
   private ConfigurationSection _defaultItemSection;
   private File _configFile;
   private FileConfiguration _config;
   private ConfigurationSection _itemsSection;

   public CustomItemConfig(CustomItemManager manager, Plugin plugin) {
      this._logger = manager.getLogger();
      this._defaultConfig = new YamlConfiguration();
      this._defaultItemSection = this._defaultConfig.createSection("custom-items");
      this._configFile = new File(manager.getDataFolder(), "ItemsConfig/" + plugin.getName() + ".yml");
      if (!this._configFile.exists()) {
         this._config = new YamlConfiguration();
         this._config.options().header("----- CustomItems ----- Item configuration file -----\nThis file configures the custom items registed by " + plugin.getName() + ".\n" + "For the changes to take effect you must reload the corresponding plugin.\n" + "\n" + "Note regarding allowed-worlds/blocked-worlds:\n" + "  allowed-worlds, when not empty, acts like a whitelist and only\n" + "  on worlds from this list the item will be enabled!\n");
      } else {
         this._config = YamlConfiguration.loadConfiguration(this._configFile);
      }

      this._itemsSection = this._config.getConfigurationSection("custom-items");
      if (this._itemsSection == null) {
         this._itemsSection = this._config.createSection("custom-items");
      }

      this._config.setDefaults(this._defaultConfig);
   }

   public void configureItem(CustomItem item) {
      if (!this._itemsSection.isSet(item.getSlug())) {
         this._itemsSection.createSection(item.getSlug(), item._defaultConfig);
      } else {
         this._defaultItemSection.createSection(item.getSlug(), item._defaultConfig);
      }

      item.applyConfig(this._itemsSection.getConfigurationSection(item.getSlug()));
   }

   public void removeItem(CustomItem item) {
      this._itemsSection.set(item.getSlug(), (Object)null);
      this._defaultItemSection.set(item.getSlug(), (Object)null);
   }

   public void saveToFile() {
      try {
         this._config.save(this._configFile);
      } catch (IOException e) {
         this._logger.log(Level.SEVERE, "Could not save config to " + this._configFile, e);
      }

   }
}
