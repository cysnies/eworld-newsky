package lib.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;
import lib.Lib;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.util.Util;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {
   private HashList defaultFilter = new HashListImpl();
   private Lib lib;
   private HashMap configItemHash;
   private HashMap configHash;

   public Config(Lib lib) {
      this.defaultFilter.add(Pattern.compile("config.yml"));
      this.defaultFilter.add(Pattern.compile("config_[a-zA-Z]+.yml"));
      this.defaultFilter.add(Pattern.compile("language.yml"));
      this.defaultFilter.add(Pattern.compile("language_[a-zA-Z]+.yml"));
      this.defaultFilter.add(Pattern.compile("hibernate.cfg.xml"));
      this.lib = lib;
      this.configItemHash = new HashMap();
      this.configHash = new HashMap();
   }

   public HashList getDefaultFilter() {
      return this.defaultFilter.clone();
   }

   public void register(File sourceJarFile, String destPath, HashList filter, String pluginName) {
      if (filter == null) {
         filter = this.defaultFilter;
      }

      ConfigMeta configItem = new ConfigMeta(sourceJarFile, destPath, filter, pluginName);
      this.configItemHash.put(pluginName, configItem);
      Util.generateFiles(sourceJarFile, destPath, filter);
   }

   public boolean loadConfig(String pluginName) throws InvalidConfigurationException {
      ConfigMeta configItem = (ConfigMeta)this.configItemHash.get(pluginName);
      if (configItem == null) {
         return false;
      } else {
         Util.generateFiles(configItem.getSourceJarFile(), configItem.getDestPath(), configItem.getFilter());

         try {
            YamlConfiguration config = new YamlConfiguration();
            config.load(configItem.getDestPath() + File.separator + "config.yml");
            this.configHash.put(pluginName, config);
            ReloadConfigEvent e = new ReloadConfigEvent(pluginName, config);
            this.lib.getPm().callEvent(e);
            return true;
         } catch (FileNotFoundException e) {
            e.printStackTrace();
         } catch (IOException e) {
            e.printStackTrace();
         }

         return false;
      }
   }

   public YamlConfiguration getConfig(String pluginName) {
      return (YamlConfiguration)this.configHash.get(pluginName);
   }

   class ConfigMeta {
      private File sourceJarFile;
      private String destPath;
      private HashList filter;
      private String pluginName;

      public ConfigMeta(File sourceJarFile, String destPath, HashList filter, String pluginName) {
         this.sourceJarFile = sourceJarFile;
         this.destPath = destPath;
         this.filter = filter;
         this.pluginName = pluginName;
      }

      public File getSourceJarFile() {
         return this.sourceJarFile;
      }

      public String getDestPath() {
         return this.destPath;
      }

      public HashList getFilter() {
         return this.filter;
      }

      public String getPluginName() {
         return this.pluginName;
      }
   }
}
