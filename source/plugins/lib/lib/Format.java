package lib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;
import lib.config.ReloadConfigEvent;
import lib.util.Util;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class Format implements Listener {
   private static final Pattern PLAIN_PATTERN = Pattern.compile("lang_\\d{1,5}");
   private static final Pattern FORMAT_PATTERN = Pattern.compile("lang-\\d{1,5}");
   private String pn;
   private HashMap plainHash;
   private HashMap formatHash;

   public Format(Lib lib) {
      this.pn = lib.getPn();
      this.plainHash = new HashMap();
      this.formatHash = new HashMap();
      lib.getPm().registerEvents(this, lib);
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void reloadConfig(ReloadConfigEvent e) {
      if (e.getConfig().contains("language")) {
         String languagePath;
         try {
            languagePath = (new File(e.getConfig().getString("language"))).getCanonicalPath();
         } catch (IOException e2) {
            e2.printStackTrace();
            return;
         }

         YamlConfiguration languageConfig = new YamlConfiguration();

         try {
            languageConfig.load(languagePath);
            this.register(e.getCallPlugin(), languageConfig);
         } catch (FileNotFoundException e1) {
            e1.printStackTrace();
         } catch (IOException e1) {
            e1.printStackTrace();
         } catch (InvalidConfigurationException e1) {
            e1.printStackTrace();
         }

      }
   }

   public String format(String pluginName, int id) {
      try {
         if (pluginName == null) {
            pluginName = this.pn;
         }

         return (String)((HashMap)this.plainHash.get(pluginName)).get(id);
      } catch (Exception var4) {
         return "";
      }
   }

   public String format(String pn, String type, Object... args) {
      if (pn == null) {
         pn = this.pn;
      }

      if (this.formatHash.containsKey(pn)) {
         String result = (String)((HashMap)this.formatHash.get(pn)).get(type);
         if (result != null) {
            for(int i = 0; i < args.length; ++i) {
               if (args[i] == null) {
                  args[i] = "";
               }

               result = result.replace("{" + i + "}", args[i].toString());
            }

            return result;
         }

         result = (String)((HashMap)this.formatHash.get(this.pn)).get(type);
         if (result != null) {
            for(int i = 0; i < args.length; ++i) {
               if (args[i] == null) {
                  args[i] = "";
               }

               result = result.replace("{" + i + "}", args[i].toString());
            }

            return result;
         }
      }

      return "";
   }

   private boolean register(String pn, YamlConfiguration languageConfig) {
      HashMap<Integer, String> hash1 = new HashMap();
      HashMap<String, String> hash2 = new HashMap();

      for(String key : languageConfig.getKeys(true)) {
         if (PLAIN_PATTERN.matcher(key).matches()) {
            hash1.put(this.getId(key), Util.convertBr(Util.convert(languageConfig.getString(key))));
         } else if (FORMAT_PATTERN.matcher(key).matches()) {
            String s = languageConfig.getString(key);
            int index = s.indexOf(":");
            if (index < 1) {
               return false;
            }

            String name = s.substring(0, index);
            if (name.isEmpty()) {
               return false;
            }

            if (index == -1) {
               return false;
            }

            String value = Util.convertBr(Util.convert(s.substring(index + 1, s.length())));
            hash2.put(name, value);
         }
      }

      this.plainHash.put(pn, hash1);
      this.formatHash.put(pn, hash2);
      return true;
   }

   private int getId(String s) {
      try {
         for(int i = 0; i < s.length(); ++i) {
            if (s.charAt(i) >= '0' && s.charAt(i) <= '9') {
               return Integer.parseInt(s.substring(i, s.length()));
            }
         }
      } catch (NumberFormatException var3) {
      }

      return -1;
   }
}
