package lib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import lib.config.ReloadConfigEvent;
import lib.util.UtilFormat;
import org.bukkit.Server;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

public class Names implements Listener {
   private Server server;
   private String pn;
   private HashMap worldHash;
   private HashMap enchantHash;
   private HashMap potionHash;
   private HashMap itemHash;
   private HashMap entityHash;

   public Names(Lib lib) {
      this.server = lib.getServer();
      this.pn = lib.getPn();
      this.loadConfig(lib.getCon().getConfig(this.pn));
      this.server.getPluginManager().registerEvents(this, lib);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void reloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   public String getWorldName(String world) {
      return this.worldHash.containsKey(world) ? (String)this.worldHash.get(world) : world;
   }

   public String getEnchantName(int id) {
      if (this.enchantHash.containsKey(id)) {
         return (String)this.enchantHash.get(id);
      } else {
         Enchantment enchantment = Enchantment.getById(id);
         return enchantment == null ? "" : enchantment.getName();
      }
   }

   public String getItemName(ItemStack is) {
      if (is.hasItemMeta()) {
         ItemMeta im = is.getItemMeta();
         if (im.getDisplayName() != null && !im.getDisplayName().isEmpty()) {
            return im.getDisplayName();
         }
      }

      return this.getItemName(is.getTypeId(), is.getDurability());
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

   public String getEntityName(Entity entity, boolean customName, boolean playerName) {
      if (entity == null) {
         return UtilFormat.format(this.pn, 800);
      } else {
         try {
            if (playerName && entity instanceof Player) {
               return UtilFormat.format(this.pn, "namesPlayer", ((Player)entity).getName());
            }

            if (customName && entity instanceof LivingEntity) {
               String name = ((LivingEntity)entity).getCustomName();
               if (name != null && !name.trim().isEmpty()) {
                  return name;
               }
            }
         } catch (Exception var5) {
         }

         return this.getEntityName(entity.getType().getTypeId());
      }
   }

   public String getEntityName(int id) {
      try {
         String result = (String)this.entityHash.get(id);
         if (result != null) {
            return result;
         } else {
            result = EntityType.fromId(id).name();
            if (result == null) {
               result = "";
            }

            return result;
         }
      } catch (Exception var3) {
         return UtilFormat.format(this.pn, 800);
      }
   }

   public String getPotionName(int id) {
      try {
         String result = (String)this.potionHash.get(id);
         if (result != null) {
            return result;
         } else {
            result = PotionEffectType.getById(id).getName();
            if (result == null) {
               result = "";
            }

            return result;
         }
      } catch (Exception var3) {
         return "";
      }
   }

   public HashMap getItemHash() {
      return this.itemHash;
   }

   private void loadConfig(FileConfiguration config) {
      String namesPath = config.getString("names.path");
      YamlConfiguration namesConfig = new YamlConfiguration();

      try {
         namesConfig.load((new File(namesPath)).getCanonicalPath());
         this.worldHash = new HashMap();
         this.enchantHash = new HashMap();
         this.potionHash = new HashMap();
         this.itemHash = new HashMap();
         this.entityHash = new HashMap();

         for(String s : namesConfig.getStringList("names.world")) {
            String world = s.split(" ")[0];
            String display = s.split(" ")[1];
            this.worldHash.put(world, display);
         }

         for(String s : namesConfig.getStringList("names.enchant")) {
            int id = Integer.parseInt(s.split(" ")[0]);
            String display = s.split(" ")[1];
            this.enchantHash.put(id, display);
         }

         for(String s : namesConfig.getStringList("names.potion")) {
            int id = Integer.parseInt(s.split(" ")[0]);
            String display = s.split(" ")[1];
            this.potionHash.put(id, display);
         }

         for(String s : namesConfig.getStringList("names.item")) {
            String id = s.split(" ")[0];
            String name = s.split(" ")[1];
            if (id.indexOf(":") == -1) {
               id = id + ":0";
            }

            this.itemHash.put(id, name);
         }

         for(String s : namesConfig.getStringList("names.entity")) {
            int id = Integer.parseInt(s.split(" ")[0]);
            String name = s.split(" ")[1];
            this.entityHash.put(id, name);
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
