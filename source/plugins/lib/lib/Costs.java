package lib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lib.config.Config;
import lib.types.InvalidTypeException;
import lib.util.Util;
import lib.util.UtilEco;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilNames;
import lib.util.UtilPer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Costs implements Listener {
   private String pn;
   private Config con;
   private HashMap costsHash;
   private String adminPer;

   public Costs(Lib lib) {
      this.pn = lib.getPn();
      this.con = lib.getCon();
      this.costsHash = new HashMap();
      this.loadConfig(lib.getCon().getConfig(this.pn));
      lib.getPm().registerEvents(this, lib);
   }

   public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null || UtilPer.checkPer(p, this.adminPer)) {
         int length = args.length;
         if ((length != 1 || !args[0].equalsIgnoreCase("?")) && length == 1 && args[0].equalsIgnoreCase("reload")) {
            this.loadConfig(this.con.getConfig(this.pn));
            sender.sendMessage(UtilFormat.format(this.pn, "success", this.get(1315)));
         } else {
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpHeader", this.get(1303)));
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", this.get(1305), this.get(1310)));
         }
      }
   }

   public void reloadCosts(String plugin, YamlConfiguration config) {
      if (plugin == null) {
         plugin = this.pn;
      }

      this.costsHash.remove(plugin);

      try {
         Map<String, Object> map = ((MemorySection)config.get("costs")).getValues(false);

         for(String s : map.keySet()) {
            this.loadCost(plugin, config, s);
         }
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   public boolean cost(Player p, String plugin, String type, boolean force) throws InvalidTypeException {
      return this.cost(p, plugin, type, force, true);
   }

   public boolean cost(Player p, String plugin, String type, boolean force, boolean tip) throws InvalidTypeException {
      if (this.costsHash.containsKey(plugin) && ((HashMap)this.costsHash.get(plugin)).get(type) != null) {
         if (tip) {
            p.sendMessage(UtilFormat.format(this.pn, "tip", UtilFormat.format(this.pn, 1300)));
         }

         CostInfo costInfo = (CostInfo)((HashMap)this.costsHash.get(plugin)).get(type);
         boolean result = true;
         int hasMoney = (int)UtilEco.get(p.getName());
         if (hasMoney < costInfo.getMoney()) {
            if (tip) {
               p.sendMessage(UtilFormat.format(this.pn, "costMoney", costInfo.getMoney(), hasMoney));
            }

            if (!force) {
               return false;
            }

            result = false;
         }

         int hasExp = Util.getTotalExperience(p);
         if (hasExp < costInfo.getExp()) {
            if (tip) {
               p.sendMessage(UtilFormat.format(this.pn, "costExp", costInfo.getExp(), hasExp));
            }

            if (!force) {
               return false;
            }

            result = false;
         }

         int hasLevel = p.getLevel();
         if (hasLevel < costInfo.getLevel()) {
            if (tip) {
               p.sendMessage(UtilFormat.format(this.pn, "costLevel", costInfo.getLevel(), hasLevel));
            }

            if (!force) {
               return false;
            }

            result = false;
         }

         Inventory inv = p.getInventory();
         Items.CheckResult checkResult = UtilItems.check(inv, costInfo.getItemsPlugin(), costInfo.getItemsType());
         HashMap<Integer, Integer> idNeedHash = checkResult.getIdNeedHash();
         HashMap<Integer, Integer> idLackHash = checkResult.getIdLackHash();
         HashMap<ItemStack, Integer> itemNeedHash = checkResult.getItemNeedHash();
         HashMap<ItemStack, Integer> itemLackHash = checkResult.getItemLackHash();
         if (costInfo.getItemsType() != null && !costInfo.getItemsType().isEmpty() && !checkResult.isSuccess()) {
            for(int id : idLackHash.keySet()) {
               if (tip) {
                  p.sendMessage(UtilFormat.format(this.pn, "costItem", idNeedHash.get(id), UtilNames.getItemName(id, 0), id, idLackHash.get(id)));
               }
            }

            if (itemLackHash.size() > 0) {
               if (tip) {
                  p.sendMessage(UtilFormat.format(this.pn, "costItem3", costInfo.getItemsType(), costInfo.getItemsPlugin()));
               }

               for(ItemStack is : itemLackHash.keySet()) {
                  int amount = (Integer)itemNeedHash.get(is);
                  int lack = (Integer)itemLackHash.get(is);
                  if (tip) {
                     p.sendMessage(UtilFormat.format(this.pn, "costItem2", amount, UtilNames.getItemName(is.getTypeId(), is.getDurability()), is.getTypeId(), lack));
                  }
               }
            }

            if (!force) {
               return false;
            }

            result = false;
         }

         int costMoney = Math.min(hasMoney, costInfo.getMoney());
         if (costMoney > 0) {
            UtilEco.del(p.getName(), (double)costMoney);
            if (tip) {
               p.sendMessage(UtilFormat.format(this.pn, "costMoney2", costMoney));
            }
         }

         int costExp = Math.min(hasExp, costInfo.getExp());
         if (costExp > 0) {
            Util.setTotalExperience(p, hasExp - costInfo.getExp());
            if (tip) {
               p.sendMessage(UtilFormat.format(this.pn, "costExp2", costExp));
            }
         }

         hasLevel = p.getLevel();
         int costLevel = Math.min(hasLevel, costInfo.getLevel());
         if (costLevel > 0) {
            p.setLevel(hasLevel - costInfo.getLevel());
            if (tip) {
               p.sendMessage(UtilFormat.format(this.pn, "costLevel2", costLevel));
            }
         }

         if (costInfo.getItemsType() != null && !costInfo.getItemsType().isEmpty()) {
            for(int id : idNeedHash.keySet()) {
               int amount = (Integer)idNeedHash.get(id);
               UtilItems.removeItem(inv, id, amount, true);
               if (tip) {
                  p.sendMessage(UtilFormat.format(this.pn, "costItem5", UtilNames.getItemName(id, 0), id, amount));
               }
            }

            for(ItemStack is : itemNeedHash.keySet()) {
               int amount = (Integer)itemNeedHash.get(is);
               UtilItems.removeItem(inv, is, amount, true);
               if (tip) {
                  p.sendMessage(UtilFormat.format(this.pn, "costItem5", UtilNames.getItemName(is), is.getTypeId(), amount));
               }
            }
         }

         return result;
      } else {
         throw new InvalidTypeException();
      }
   }

   private void loadCost(String plugin, YamlConfiguration config, String type) {
      if (!this.costsHash.containsKey(plugin)) {
         this.costsHash.put(plugin, new HashMap());
      }

      try {
         int money = config.getInt("costs." + type + ".money");
         int exp = config.getInt("costs." + type + ".exp");
         int level = config.getInt("costs." + type + ".level");
         String itemsPlugin = config.getString("costs." + type + ".item.plugin", "");
         String itemsType = config.getString("costs." + type + ".item.type", "");
         CostInfo costInfo = new CostInfo(money, exp, level, itemsPlugin, itemsType);
         ((HashMap)this.costsHash.get(plugin)).put(type, costInfo);
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   private void loadConfig(YamlConfiguration config) {
      try {
         this.adminPer = config.getString("costs.adminPer");
         String path = config.getString("costs.path");
         YamlConfiguration saveConfig = new YamlConfiguration();
         saveConfig.load((new File(path)).getCanonicalPath());
         this.reloadCosts(this.pn, saveConfig);
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

   private class CostInfo {
      private int money;
      private int exp;
      private int level;
      private String itemsPlugin;
      private String itemsType;

      public CostInfo(int money, int exp, int level, String itemsPlugin, String itemsType) {
         this.money = money;
         this.exp = exp;
         this.level = level;
         this.itemsPlugin = itemsPlugin;
         this.itemsType = itemsType;
      }

      public int getMoney() {
         return this.money;
      }

      public int getExp() {
         return this.exp;
      }

      public int getLevel() {
         return this.level;
      }

      public String getItemsPlugin() {
         return this.itemsPlugin;
      }

      public String getItemsType() {
         return this.itemsType;
      }
   }
}
