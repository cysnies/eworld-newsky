package fr.neatmonster.nocheatplus.config;

import fr.neatmonster.nocheatplus.logging.LogUtil;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.DumperOptions;

public class RawConfigFile extends YamlConfiguration {
   public static Integer parseTypeId(String content) {
      content = content.trim().toUpperCase();

      try {
         return Integer.parseInt(content);
      } catch (NumberFormatException var3) {
         try {
            Material mat = Material.matchMaterial(content.replace(' ', '_').replace('-', '_').replace('.', '_'));
            if (mat != null) {
               return mat.getId();
            }
         } catch (Exception var2) {
         }

         return null;
      }
   }

   public double getDouble(String path, double min, double max, double preset) {
      double value = this.getDouble(path, preset);
      if (value < min) {
         return min;
      } else {
         return value > max ? max : value;
      }
   }

   public long getLong(String path, long min, long max, long preset) {
      long value = this.getLong(path, preset);
      if (value < min) {
         return min;
      } else {
         return value > max ? max : value;
      }
   }

   public long getInt(String path, int min, int max, int preset) {
      int value = this.getInt(path, preset);
      if (value < min) {
         return (long)min;
      } else {
         return value > max ? (long)max : (long)value;
      }
   }

   public Integer getTypeId(String path) {
      return this.getTypeId(path, (Integer)null);
   }

   public Integer getTypeId(String path, Integer preset) {
      String content = this.getString(path, (String)null);
      if (content != null) {
         Integer id = parseTypeId(content);
         if (id != null) {
            return id;
         }
      }

      int id = this.getInt(path, Integer.MAX_VALUE);
      return id == Integer.MAX_VALUE ? preset : id;
   }

   public void readMaterialFromList(String path, Collection target) {
      List<String> content = this.getStringList(path);
      if (content != null && !content.isEmpty()) {
         for(String entry : content) {
            Integer id = parseTypeId(entry);
            if (id == null) {
               LogUtil.logWarning("[NoCheatPlus] Bad material entry (" + path + "): " + entry);
            } else {
               target.add(id);
            }
         }

      }
   }

   public String saveToString() {
      try {
         Field op = YamlConfiguration.class.getDeclaredField("yamlOptions");
         op.setAccessible(true);
         DumperOptions options = (DumperOptions)op.get(this);
         options.setWidth(200);
      } catch (Exception var3) {
      }

      return super.saveToString();
   }
}
