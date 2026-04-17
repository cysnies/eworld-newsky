package lib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lib.config.Config;
import lib.config.ReloadConfigEvent;
import lib.hashList.ChanceHashList;
import lib.hashList.ChanceHashListImpl;
import lib.util.UtilFormat;
import lib.util.UtilNames;
import lib.util.UtilPer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Potions implements Listener {
   private Config con;
   private Random r;
   private Server server;
   private String pn;
   private HashMap potionsHash;
   private String adminPer;

   public Potions(Lib lib) {
      this.con = lib.getCon();
      this.r = new Random();
      this.server = lib.getServer();
      this.pn = lib.getPn();
      this.potionsHash = new HashMap();
      this.loadConfig(lib.getCon().getConfig(this.pn));
      this.server.getPluginManager().registerEvents(this, lib);
   }

   public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      try {
         Player p = null;
         if (sender instanceof Player) {
            p = (Player)sender;
         }

         if (p != null && !UtilPer.checkPer(p, this.adminPer)) {
            return;
         }

         int length = args.length;
         if (length != 1 || !args[0].equalsIgnoreCase("?")) {
            if (length == 1) {
               if (args[0].equalsIgnoreCase("reload")) {
                  if (this.loadConfig(this.con.getConfig(this.pn))) {
                     sender.sendMessage(UtilFormat.format(this.pn, "success", this.get(425)));
                  } else {
                     sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(430)));
                  }

                  return;
               }
            } else if (length >= 2) {
               if (p == null) {
                  sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(127)));
                  return;
               }

               boolean random = false;
               boolean force = false;
               boolean all = false;

               for(String s : args) {
                  if (s.equalsIgnoreCase("-r")) {
                     random = true;
                  } else if (s.equalsIgnoreCase("-f")) {
                     force = true;
                  } else if (s.equalsIgnoreCase("-a")) {
                     all = true;
                  }
               }

               String plugin;
               String type;
               if (length >= 3) {
                  if (args[2].charAt(0) == '-') {
                     plugin = this.pn;
                     type = args[1];
                  } else {
                     plugin = args[1];
                     type = args[2];
                  }
               } else {
                  plugin = this.pn;
                  type = args[1];
               }

               this.addPotions(p, plugin, type, p, random, force, all);
               return;
            }
         }

         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpHeader", this.get(400)));
         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", this.get(405), this.get(410)));
         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", this.get(415), this.get(420)));
      } catch (NumberFormatException var14) {
         sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(126)));
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   public boolean reloadPotions(String plugin, YamlConfiguration config) {
      if (plugin == null) {
         plugin = this.pn;
      }

      this.potionsHash.remove(plugin);
      Map<String, Object> map = ((MemorySection)config.get("potions")).getValues(false);
      boolean result = true;

      for(String s : map.keySet()) {
         if (!this.loadPotion(plugin, config, s)) {
            result = false;
         }
      }

      return result;
   }

   public List addPotions(CommandSender sender, String plugin, String type, LivingEntity le, boolean random, boolean force, boolean all) {
      if (plugin == null) {
         plugin = this.pn;
      }

      if (!this.potionsHash.containsKey(plugin)) {
         if (sender != null) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(435)));
         }

         return null;
      } else if (((HashMap)this.potionsHash.get(plugin)).size() == 0) {
         if (sender != null) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(440)));
         }

         return null;
      } else if (!((HashMap)this.potionsHash.get(plugin)).containsKey(type)) {
         if (sender != null) {
            sender.sendMessage(UtilFormat.format(this.pn, "potionsErr1", type));
         }

         return null;
      } else {
         if (sender != null) {
            sender.sendMessage(UtilFormat.format(this.pn, "potionsGetTip1", type));
         }

         List<PotionEffect> result = new ArrayList();
         if (random) {
            Potion potion = (Potion)((ChanceHashList)((HashMap)this.potionsHash.get(plugin)).get(type)).getRandom();
            PotionEffectType potionEffectType = potion.getPotionEffectType();
            if (this.checkAdd(le, potionEffectType, potion)) {
               if (sender != null) {
                  sender.sendMessage(this.get(445));
               }

               PotionEffect pe = new PotionEffect(potionEffectType, potion.getDuration(), potion.getLevel(), false);
               result.add(pe);
               le.addPotionEffect(pe, true);
            } else if (sender != null) {
               sender.sendMessage(this.get(450));
            }
         } else {
            for(Potion potion : (ChanceHashList)((HashMap)this.potionsHash.get(plugin)).get(type)) {
               int chance = potion.getChance();
               PotionEffectType potionEffectType = potion.getPotionEffectType();
               if (sender != null) {
                  sender.sendMessage(UtilFormat.format(this.pn, "potionsGetTip2", UtilNames.getPotionName(potionEffectType.getId()), potion.getChance()));
               }

               if (this.r.nextInt(100) >= chance && !force) {
                  if (sender != null) {
                     sender.sendMessage(this.get(455));
                  }
               } else if (this.checkAdd(le, potionEffectType, potion)) {
                  if (sender != null) {
                     sender.sendMessage(this.get(445));
                  }

                  PotionEffect pe = new PotionEffect(potionEffectType, potion.getDuration(), potion.getLevel(), false);
                  result.add(pe);
                  le.addPotionEffect(pe, true);
                  if (!all) {
                     break;
                  }
               } else if (sender != null) {
                  sender.sendMessage(this.get(450));
               }
            }
         }

         return result;
      }
   }

   public boolean isExsit(String plugin, String type) {
      if (plugin == null) {
         plugin = this.pn;
      }

      return this.potionsHash.containsKey(plugin) && ((HashMap)this.potionsHash.get(plugin)).containsKey(type);
   }

   private boolean checkAdd(LivingEntity le, PotionEffectType potionEffectType, Potion potion) {
      int level = 0;
      int leftTime = 0;

      try {
         for(PotionEffect potionEffect : le.getActivePotionEffects()) {
            if (potionEffect.getType().equals(potionEffectType)) {
               level = potionEffect.getAmplifier();
               leftTime = potionEffect.getDuration();
               break;
            }
         }
      } catch (Exception var8) {
      }

      boolean apply = true;
      if (level > 0 && !potion.isReplace()) {
         if (potion.isStrong()) {
            if (potion.getLevel() < level) {
               apply = false;
            } else if (potion.getLevel() == level) {
               apply = potion.getDuration() > leftTime;
            }
         } else {
            apply = false;
         }
      }

      return apply;
   }

   private boolean loadConfig(YamlConfiguration config) {
      this.adminPer = config.getString("potions.adminPer");
      String path = config.getString("potions.path");
      YamlConfiguration con = new YamlConfiguration();

      try {
         con.load((new File(path)).getCanonicalPath());
         return this.reloadPotions(this.pn, con);
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (InvalidConfigurationException e) {
         e.printStackTrace();
      }

      return false;
   }

   private boolean loadPotion(String plugin, FileConfiguration config, String type) {
      if (plugin == null) {
         plugin = this.pn;
      }

      if (!this.potionsHash.containsKey(plugin)) {
         this.potionsHash.put(plugin, new HashMap());
      }

      if (!((HashMap)this.potionsHash.get(plugin)).containsKey(type)) {
         ((HashMap)this.potionsHash.get(plugin)).put(type, new ChanceHashListImpl());
      }

      try {
         boolean result = true;

         for(String s : config.getStringList("potions." + type)) {
            int chance = Integer.parseInt(s.split(" ")[0]);

            PotionEffectType potionEffectType;
            try {
               potionEffectType = PotionEffectType.getById(Integer.parseInt(s.split(" ")[1]));
            } catch (NumberFormatException var14) {
               potionEffectType = PotionEffectType.getByName(s.split(" ")[1]);
            }

            if (potionEffectType == null) {
               result = false;
            } else {
               int duration = Integer.parseInt(s.split(" ")[2]);
               int level = Integer.parseInt(s.split(" ")[3]);
               boolean replace = Boolean.parseBoolean(s.split(" ")[4]);
               boolean strong = Boolean.parseBoolean(s.split(" ")[5]);
               Potion potion = new Potion(chance, potionEffectType, duration, level, replace, strong);
               ((ChanceHashList)((HashMap)this.potionsHash.get(plugin)).get(type)).addChance(potion, chance);
            }
         }

         return result;
      } catch (Exception e) {
         e.printStackTrace();
         return false;
      }
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   private class Potion {
      private int chance;
      private PotionEffectType potionEffectType;
      private int duration;
      private int level;
      private boolean replace;
      private boolean strong;

      public Potion(int chance, PotionEffectType potionEffectType, int duration, int level, boolean replace, boolean strong) {
         this.chance = chance;
         this.potionEffectType = potionEffectType;
         this.duration = duration;
         this.level = level;
         this.replace = replace;
         this.strong = strong;
      }

      public int getChance() {
         return this.chance;
      }

      public PotionEffectType getPotionEffectType() {
         return this.potionEffectType;
      }

      public int getDuration() {
         return this.duration;
      }

      public int getLevel() {
         return this.level;
      }

      public boolean isReplace() {
         return this.replace;
      }

      public boolean isStrong() {
         return this.strong;
      }
   }
}
