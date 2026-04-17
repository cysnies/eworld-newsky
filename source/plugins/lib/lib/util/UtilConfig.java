package lib.util;

import java.io.File;
import java.util.regex.Pattern;
import lib.Lib;
import lib.config.Config;
import lib.hashList.HashList;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class UtilConfig {
   private static Config config;

   public static void init(Lib lib) {
      config = lib.getCon();
   }

   public static HashList getDefaultFilter() {
      return config.getDefaultFilter();
   }

   public static void register(File sourceJarFile, String destPath, HashList filter, String pluginName) {
      config.register(sourceJarFile, destPath, filter, pluginName);
   }

   public static boolean loadConfig(String pluginName) throws InvalidConfigurationException {
      return config.loadConfig(pluginName);
   }

   public static YamlConfiguration getConfig(String pluginName) {
      return config.getConfig(pluginName);
   }
}
