package trade;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class Names implements Listener {
   private Main main;
   private HashMap enchantHash;
   private HashMap itemHash;

   public Names(Main main) {
      this.main = main;
      this.loadConfig();
   }

   public String getEnchantName(int id) {
      if (this.enchantHash.containsKey(id)) {
         return (String)this.enchantHash.get(id);
      } else {
         Enchantment enchantment = Enchantment.getById(id);
         return enchantment == null ? "" : enchantment.getName();
      }
   }

   public String getItemName(int id, int smallId) {
      String result = (String)this.itemHash.get(id + ":" + smallId);
      if (result != null) {
         return result;
      } else {
         result = (String)this.itemHash.get(id + ":" + 0);
         if (result == null) {
            result = (new ItemStack(id, 1, (short)smallId)).getType().name();
         }

         if (result == null) {
            result = "";
         }

         return result;
      }
   }

   public HashMap getItemHash() {
      return this.itemHash;
   }

   private void loadConfig() {
      List<Pattern> filter = new ArrayList();
      filter.add(Pattern.compile("names.yml"));
      Util.generateFiles(new File(this.main.getDataFolder().getParentFile().getAbsolutePath() + File.separator + "trade.jar"), this.main.getDataFolder().getAbsolutePath(), filter);
      String namesPath = this.main.getDataFolder().getAbsolutePath() + File.separator + "names.yml";
      YamlConfiguration namesConfig = new YamlConfiguration();

      try {
         namesConfig.load(namesPath);
         this.enchantHash = new HashMap();
         this.itemHash = new HashMap();

         for(String s : namesConfig.getStringList("names.enchant")) {
            int id = Integer.parseInt(s.split(" ")[0]);
            String display = s.split(" ")[1];
            this.enchantHash.put(id, display);
         }

         for(String s : namesConfig.getStringList("names.item")) {
            String id = s.split(" ")[0];
            String name = s.split(" ")[1];
            if (id.indexOf(":") == -1) {
               id = id + ":0";
            }

            this.itemHash.put(id, name);
         }
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (InvalidConfigurationException e) {
         e.printStackTrace();
      }

   }
}
