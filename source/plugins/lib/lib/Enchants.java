package lib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class Enchants implements Listener {
   private Random r = new Random();
   private Server server;
   private String pn;
   private Config con;
   private String adminPer;
   private HashMap enchantsHash;

   public Enchants(Lib lib) {
      this.server = lib.getServer();
      this.pn = lib.getPn();
      this.con = lib.getCon();
      this.enchantsHash = new HashMap();
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
                  this.loadConfig(this.con.getConfig(this.pn));
                  sender.sendMessage(UtilFormat.format(this.pn, "success", this.get(325)));
                  return;
               }
            } else if (length >= 2) {
               if (p == null) {
                  sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(127)));
                  return;
               }

               ItemStack is = p.getItemInHand();
               if (is != null && is.getTypeId() != 0) {
                  boolean replace = false;
                  boolean force = false;
                  boolean all = false;

                  for(String s : args) {
                     if (s.equalsIgnoreCase("-r")) {
                        replace = true;
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

                  this.addEnchant(p, plugin, type, is, replace, force, all);
                  return;
               }

               sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(335)));
               return;
            }
         }

         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpHeader", this.get(300)));
         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", this.get(305), this.get(310)));
         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", this.get(315), this.get(320)));
      } catch (NumberFormatException var15) {
         sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(126)));
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void reloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   public boolean addEnchant(CommandSender sender, String plugin, String type, ItemStack is, boolean replace, boolean force, boolean all) {
      if (plugin == null) {
         plugin = this.pn;
      }

      if (!this.enchantsHash.containsKey(plugin)) {
         if (sender != null) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(340)));
         }

         return false;
      } else if (((HashMap)this.enchantsHash.get(plugin)).size() == 0) {
         if (sender != null) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(345)));
         }

         return false;
      } else if (!((HashMap)this.enchantsHash.get(plugin)).containsKey(type)) {
         if (sender != null) {
            sender.sendMessage(UtilFormat.format(this.pn, "enchantsErr1", type));
         }

         return false;
      } else {
         if (sender != null) {
            sender.sendMessage(UtilFormat.format(this.pn, "enchantsGetTip1", type));
         }

         boolean result = false;

         for(Enchant enchant : (ChanceHashList)((HashMap)this.enchantsHash.get(plugin)).get(type)) {
            int chance = enchant.getChance();
            Enchantment enchantment = enchant.getEnchantment();
            if (sender != null) {
               sender.sendMessage(UtilFormat.format(this.pn, "enchantsGetTip2", UtilNames.getEnchantName(enchantment.getId()), chance));
            }

            if (enchant.isFit() && !enchantment.getItemTarget().includes(is)) {
               if (sender != null) {
                  sender.sendMessage(this.get(360));
               }
            } else {
               int level = enchant.getLevel();
               if (this.r.nextInt(100) >= chance && !force) {
                  if (sender != null) {
                     sender.sendMessage(this.get(355));
                  }
               } else {
                  try {
                     int preLevel = is.getEnchantmentLevel(enchantment);
                     if (!replace && preLevel >= level) {
                        if (sender != null) {
                           sender.sendMessage(this.get(355));
                        }
                     } else {
                        if (preLevel > 0) {
                           is.removeEnchantment(enchantment);
                        }

                        if (enchant.isSafe()) {
                           is.addEnchantment(enchantment, level);
                        } else {
                           is.addUnsafeEnchantment(enchantment, level);
                        }

                        result = true;
                        if (sender != null) {
                           sender.sendMessage(this.get(350));
                        }
                     }
                  } catch (Exception var15) {
                     if (sender != null) {
                        sender.sendMessage(this.get(355));
                     }
                  }

                  if (!all) {
                     break;
                  }
               }
            }
         }

         return result;
      }
   }

   public boolean isEnchantExsit(String plugin, String type) {
      if (type != null && !type.trim().isEmpty()) {
         if (plugin == null) {
            plugin = this.pn;
         }

         return this.enchantsHash.containsKey(plugin) && ((HashMap)this.enchantsHash.get(plugin)).containsKey(type);
      } else {
         return false;
      }
   }

   public void reloadEnchants(String plugin, YamlConfiguration config) {
      if (plugin == null) {
         plugin = this.pn;
      }

      this.enchantsHash.remove(plugin);

      try {
         Map<String, Object> map = ((MemorySection)config.get("enchants")).getValues(false);

         for(String s : map.keySet()) {
            this.loadEnchant(plugin, config, s);
         }
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   private void loadEnchant(String plugin, YamlConfiguration config, String type) {
      if (!this.enchantsHash.containsKey(plugin)) {
         this.enchantsHash.put(plugin, new HashMap());
      }

      if (!((HashMap)this.enchantsHash.get(plugin)).containsKey(type)) {
         ((HashMap)this.enchantsHash.get(plugin)).put(type, new ChanceHashListImpl());
      }

      try {
         for(String s : config.getStringList("enchants." + type)) {
            int chance = Integer.parseInt(s.split(" ")[0]);

            Enchantment enchantment;
            try {
               enchantment = Enchantment.getById(Integer.parseInt(s.split(" ")[1]));
            } catch (NumberFormatException var12) {
               enchantment = Enchantment.getByName(s.split(" ")[1]);
            }

            if (enchantment != null) {
               int level = Integer.parseInt(s.split(" ")[2]);
               boolean fit = Boolean.parseBoolean(s.split(" ")[3]);
               boolean safe = Boolean.parseBoolean(s.split(" ")[4]);
               Enchant enchant = new Enchant(chance, enchantment, level, fit, safe);
               ((ChanceHashList)((HashMap)this.enchantsHash.get(plugin)).get(type)).addChance(enchant, chance);
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   private void loadConfig(FileConfiguration config) {
      try {
         this.adminPer = config.getString("enchants.adminPer");
         String path = config.getString("enchants.path");
         YamlConfiguration saveConfig = new YamlConfiguration();
         saveConfig.load((new File(path)).getCanonicalPath());
         this.reloadEnchants(this.pn, saveConfig);
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (InvalidConfigurationException e) {
         e.printStackTrace();
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   private class Enchant {
      private int chance;
      private Enchantment enchantment;
      private int level;
      private boolean fit;
      private boolean safe;

      public Enchant(int chance, Enchantment enchantment, int level, boolean fit, boolean safe) {
         this.chance = chance;
         this.enchantment = enchantment;
         this.level = level;
         this.fit = fit;
         this.safe = safe;
      }

      public int getChance() {
         return this.chance;
      }

      public Enchantment getEnchantment() {
         return this.enchantment;
      }

      public int getLevel() {
         return this.level;
      }

      public boolean isFit() {
         return this.fit;
      }

      public boolean isSafe() {
         return this.safe;
      }
   }
}
