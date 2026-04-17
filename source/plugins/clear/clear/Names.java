package clear;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;

public class Names implements Listener {
   private Main main;
   private static HashMap entityHash;

   public Names(Main main) {
      this.main = main;
      this.loadConfig();
   }

   public static String getEntityName(int id) {
      try {
         String result = (String)entityHash.get(id);
         if (result != null) {
            return result;
         } else {
            result = EntityType.fromId(id).name();
            if (result == null) {
               result = "";
            }

            return result;
         }
      } catch (Exception var2) {
         return "";
      }
   }

   public void loadConfig() {
      try {
         YamlConfiguration namesConfig = new YamlConfiguration();
         namesConfig.load(this.main.getPluginPath() + File.separator + this.main.getPn() + File.separator + "names.yml");
         entityHash = new HashMap();

         for(String s : namesConfig.getStringList("names.entity")) {
            int id = Integer.parseInt(s.split(" ")[0]);
            String name = s.split(" ")[1];
            entityHash.put(id, name);
         }
      } catch (NumberFormatException e) {
         e.printStackTrace();
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (InvalidConfigurationException e) {
         e.printStackTrace();
      }

   }
}
