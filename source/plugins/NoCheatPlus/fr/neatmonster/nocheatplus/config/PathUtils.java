package fr.neatmonster.nocheatplus.config;

import fr.neatmonster.nocheatplus.logging.LogUtil;
import fr.neatmonster.nocheatplus.utilities.ds.prefixtree.CharPrefixTree;
import fr.neatmonster.nocheatplus.utilities.ds.prefixtree.SimpleCharPrefixTree;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemoryConfiguration;

public class PathUtils {
   private static final Set deprecatedFields = new LinkedHashSet();
   private static final SimpleCharPrefixTree deprecatedPrefixes = new SimpleCharPrefixTree();
   private static final Set globalOnlyFields = new HashSet();
   private static final SimpleCharPrefixTree globalOnlyPrefixes = new SimpleCharPrefixTree();
   private static final Map movedPaths = new LinkedHashMap();

   private static void initPaths() {
      deprecatedFields.clear();
      deprecatedPrefixes.clear();
      globalOnlyFields.clear();
      globalOnlyPrefixes.clear();
      movedPaths.clear();

      for(Field field : ConfPaths.class.getDeclaredFields()) {
         if (field.getType() == String.class) {
            String fieldName = field.getName();
            checkAddPrefixes(field, fieldName, GlobalConfig.class, globalOnlyFields, globalOnlyPrefixes);
            checkAddPrefixes(field, fieldName, Deprecated.class, deprecatedFields, deprecatedPrefixes);
            if (field.isAnnotationPresent(Moved.class)) {
               addMoved(field, (Moved)field.getAnnotation(Moved.class));
            }
         }
      }

   }

   private static void checkAddPrefixes(Field field, String fieldName, Class annotation, Set fieldNames, SimpleCharPrefixTree pathPrefixes) {
      if (field.isAnnotationPresent(annotation)) {
         fieldNames.add(fieldName);
         addPrefixesField(field, pathPrefixes);
      } else {
         for(String refName : fieldNames) {
            if (fieldName.startsWith(refName)) {
               addPrefixesField(field, pathPrefixes);
            }
         }
      }

   }

   private static void addPrefixesField(Field field, SimpleCharPrefixTree pathPrefixes) {
      try {
         String path = field.get((Object)null).toString();
         if (path != null) {
            pathPrefixes.feed(path);
         }
      } catch (IllegalArgumentException var3) {
      } catch (IllegalAccessException var4) {
      }

   }

   private static void addMoved(Field field, Moved rel) {
      try {
         String path = field.get((Object)null).toString();
         movedPaths.put(path, rel.newPath());
      } catch (IllegalArgumentException var3) {
      } catch (IllegalAccessException var4) {
      }

   }

   protected static void warnPaths(ConfigFile config, CharPrefixTree paths, String msgPrefix, Set warnedPaths) {
      Logger logger = Bukkit.getLogger();

      for(String path : config.getKeys(true)) {
         if (paths.hasPrefix(path)) {
            logger.warning("[NoCheatPlus] Config path '" + path + "'" + msgPrefix);
            if (warnedPaths != null) {
               warnedPaths.add(path);
            }
         }
      }

   }

   public static void processPaths(File file, String configName, boolean isWorldConfig) {
      ConfigFile config = new ConfigFile();

      try {
         config.load(file);
         Set<String> removePaths = new LinkedHashSet();
         Map<String, Object> addPaths = new LinkedHashMap();
         if (isWorldConfig) {
            processGlobalOnlyPaths(config, configName, (Set)null);
         }

         processDeprecatedPaths(config, configName, removePaths);
         processMovedPaths(config, configName, removePaths, addPaths);
         boolean changed = false;
         if (!removePaths.isEmpty()) {
            config = removePaths(config, removePaths);
            changed = true;
         }

         if (!addPaths.isEmpty()) {
            setPaths(config, addPaths);
            changed = true;
         }

         if (changed) {
            try {
               config.save(file);
            } catch (Throwable t) {
               LogUtil.logSevere("[NoCheatPlus] Failed to save configuration (" + configName + ") with changes: " + t.getClass().getSimpleName());
               LogUtil.logSevere(t);
            }
         }
      } catch (FileNotFoundException var9) {
      } catch (IOException var10) {
      } catch (InvalidConfigurationException var11) {
      }

   }

   public static void setPaths(ConfigFile config, Map setPaths) {
      for(Map.Entry entry : setPaths.entrySet()) {
         config.set((String)entry.getKey(), entry.getValue());
      }

   }

   public static ConfigFile removePaths(ConfigFile config, Collection removePaths) {
      SimpleCharPrefixTree prefixes = new SimpleCharPrefixTree();

      for(String path : removePaths) {
         prefixes.feed(path);
      }

      ConfigFile newConfig = new ConfigFile();

      for(Map.Entry entry : config.getValues(true).entrySet()) {
         String path = (String)entry.getKey();
         Object value = entry.getValue();
         if (!(value instanceof ConfigurationSection) && !prefixes.hasPrefix(path)) {
            newConfig.set(path, value);
         }
      }

      return newConfig;
   }

   protected static void processMovedPaths(ConfigFile config, String configName, Set removePaths, Map addPaths) {
      Logger logger = Bukkit.getLogger();

      for(Map.Entry entry : movedPaths.entrySet()) {
         String path = (String)entry.getKey();
         if (config.contains(path)) {
            String newPath = (String)entry.getValue();
            String to;
            if (newPath == null | newPath.isEmpty()) {
               to = ".";
            } else {
               to = " to '" + newPath + "'.";
               Object value = config.get(path);
               config.set(newPath, value);
               addPaths.put(newPath, value);
               removePaths.add(path);
            }

            logger.warning("[NoCheatPlus] Config path '" + path + "' (" + configName + ") has been moved" + to);
         }
      }

   }

   protected static void processDeprecatedPaths(ConfigFile config, String configName, Set removePaths) {
      warnPaths(config, deprecatedPrefixes, " (" + configName + ") is not in use anymore.", removePaths);
   }

   protected static void processGlobalOnlyPaths(ConfigFile config, String configName, Set removePaths) {
      warnPaths(config, globalOnlyPrefixes, " (" + configName + ") should only be set in the global configuration.", removePaths);
   }

   public static MemoryConfiguration getWorldsDefaultConfig(ConfigFile defaultConfig) {
      char sep = defaultConfig.options().pathSeparator();
      MemoryConfiguration config = new ConfigFile();
      config.options().pathSeparator(sep);
      Map<String, Object> defaults = defaultConfig.getValues(false);

      for(Map.Entry entry : defaults.entrySet()) {
         String part = (String)entry.getKey();
         if (part.isEmpty() || mayBeInWorldConfig(part)) {
            Object value = entry.getValue();
            if (value instanceof ConfigurationSection) {
               addWorldConfigSection(config, (ConfigurationSection)value, part, sep);
            } else {
               config.set(part, value);
            }
         }
      }

      return config;
   }

   protected static void addWorldConfigSection(MemoryConfiguration config, ConfigurationSection section, String path, char sep) {
      Map<String, Object> values = section.getValues(false);

      for(Map.Entry entry : values.entrySet()) {
         String fullPath = path + sep + (String)entry.getKey();
         if (mayBeInWorldConfig(fullPath)) {
            Object value = entry.getValue();
            if (value instanceof ConfigurationSection) {
               addWorldConfigSection(config, (ConfigurationSection)value, fullPath, sep);
            } else {
               config.set(fullPath, value);
            }
         }
      }

   }

   public static boolean mayBeInWorldConfig(String path) {
      return globalOnlyPrefixes.hasPrefix(path) ? false : mayBeInConfig(path);
   }

   public static boolean mayBeInConfig(String path) {
      if (deprecatedPrefixes.hasPrefix(path)) {
         return false;
      } else {
         return !movedPaths.containsKey(path);
      }
   }

   static {
      initPaths();
   }
}
