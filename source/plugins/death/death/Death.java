package death;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Pattern;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.nbt.Attributes;
import lib.nbt.Attributes.Attribute;
import lib.nbt.Attributes.AttributeType;
import lib.nbt.Attributes.Operation;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilPer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Death extends JavaPlugin implements Listener {
   private static final String LIB = "lib";
   private Server server;
   private String pn;
   private PluginManager pm;
   private BukkitScheduler scheduler;
   private String mainPath;
   private String pluginPath;
   private String dataFolder;
   private String pluginVersion;
   private String per_death_admin;
   private String per_death_exp;
   private String per_death_durability;
   private int durabilityDel;
   private String savePath;
   private DeathMessage deathMessage;

   public void onEnable() {
      this.initBasic();
      this.initConfig();
      this.loadConfig0(UtilConfig.getConfig(this.pn));
      this.savePath = this.getDataFolder() + File.separator + "drop";
      (new File(this.savePath)).mkdirs();
      this.server.getPluginManager().registerEvents(this, this);
      this.deathMessage = new DeathMessage(this);
      Util.sendConsoleMessage(UtilFormat.format("lib", "pluginEnabled", new Object[]{this.pn, this.pluginVersion}));
   }

   public void onDisable() {
      if (this.scheduler != null) {
         this.scheduler.cancelAllTasks();
      }

      Util.sendConsoleMessage(UtilFormat.format("lib", "pluginDisabled", new Object[]{this.pn, this.pluginVersion}));
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      String cmdName = cmd.getName();
      int length = args.length;
      if (cmdName.equalsIgnoreCase("death")) {
         if (p != null && !UtilPer.checkPer(p, this.per_death_admin)) {
            return true;
         }

         if ((length != 1 || !args[0].equalsIgnoreCase("?")) && length == 1 && args[0].equalsIgnoreCase("reload")) {
            this.reloadConfig(sender);
            return true;
         }

         sender.sendMessage(UtilFormat.format("lib", "cmdHelpHeader", new Object[]{this.get(10)}));
         sender.sendMessage(UtilFormat.format("lib", "cmdHelpItem", new Object[]{this.get(15), this.get(20)}));
      }

      return true;
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void reloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig0(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void playerDeath(PlayerDeathEvent e) {
      Player p = e.getEntity();
      File file = new File(this.savePath + File.separator + p.getName() + ".ini");
      file.delete();
      e.setDroppedExp(0);
      if (e.getEntity() != null) {
         p.sendMessage(UtilFormat.format("lib", "tip", new Object[]{this.get(125)}));
         if (UtilPer.hasPer(p, this.per_death_exp)) {
            e.setKeepLevel(true);
            p.sendMessage(UtilFormat.format("lib", "tip", new Object[]{this.get(115)}));
         } else {
            e.setKeepLevel(false);
            p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(120)}));
         }

         boolean vip = true;
         if (!UtilPer.hasPer(p, this.per_death_durability)) {
            vip = false;

            for(int i = 0; i <= 39; ++i) {
               ItemStack is = p.getInventory().getItem(i);
               if (is != null && is.getTypeId() != 0 && is.getAmount() > 0 && UtilItems.hasDurability(is)) {
                  short result = (short)(is.getType().getMaxDurability() - (is.getType().getMaxDurability() - is.getDurability()) * (100 - this.durabilityDel) / 100);
                  result = (short)Math.min(result, is.getType().getMaxDurability());
                  is.setDurability(result);
               }
            }
         }

         boolean result = false;
         Properties pro = new Properties();
         YamlConfiguration saveConfig = new YamlConfiguration();

         for(int i = 0; i <= 39; ++i) {
            try {
               ItemStack is = p.getInventory().getItem(i);
               if (is != null && is.getAmount() > 0) {
                  result = true;
                  saveConfig.createSection(String.valueOf(i), is.serialize());
                  Attributes at = new Attributes(is);
                  if (at.size() > 0) {
                     int index = 1;

                     for(Attributes.Attribute a : at.values()) {
                        String s = a.getAmount() + " " + a.getAttributeType().getMinecraftId() + " " + a.getOperation().getId() + " " + a.getUUID().toString() + " " + a.getName();
                        pro.setProperty(i + "-" + index, s);
                        ++index;
                     }
                  }
               }
            } catch (Exception var24) {
            }
         }

         if (!result) {
            p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(100)}));
         } else {
            FileOutputStream fos = null;
            OutputStreamWriter osw = null;

            try {
               String s = saveConfig.saveToString();
               pro.setProperty("a", s);
               fos = new FileOutputStream(file);
               osw = new OutputStreamWriter(fos, Charset.forName("UTF-8"));
               pro.store(osw, "drop");
               if (vip) {
                  p.sendMessage(UtilFormat.format(this.pn, "tip", new Object[]{this.get(106)}));
               } else {
                  p.sendMessage(UtilFormat.format(this.pn, "tip", new Object[]{this.get(105)}));
               }
            } catch (IOException var22) {
               this.server.getConsoleSender().sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(80)}));
            } finally {
               try {
                  if (fos != null) {
                     fos.close();
                  }

                  if (osw != null) {
                     osw.close();
                  }
               } catch (IOException e1) {
                  e1.printStackTrace();
               }

            }

            e.getDrops().clear();
            p.sendMessage(UtilFormat.format("lib", "tip", new Object[]{this.get(125)}));
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void playerRespawn(PlayerRespawnEvent e) {
      this.getback(e.getPlayer());
   }

   public void getback(Player p) {
      boolean result = false;
      File file = new File(this.savePath + File.separator + p.getName() + ".ini");
      Properties pro = new Properties();
      FileInputStream fis = null;
      InputStreamReader isr = null;

      try {
         fis = new FileInputStream(file);
         isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
         pro.load(isr);
         YamlConfiguration loadConfig = new YamlConfiguration();
         String s = pro.getProperty("a");
         loadConfig.loadFromString(s);
         PlayerInventory pi = p.getInventory();
         MemorySection ms = null;

         for(int i = 0; i <= 39; ++i) {
            try {
               ms = (MemorySection)loadConfig.get(String.valueOf(i));
               if (ms != null) {
                  ItemStack is = ItemStack.deserialize(ms.getValues(true));
                  if (is.getAmount() > 0) {
                     result = true;

                     try {
                        int index = 1;
                        Attributes at = new Attributes(is);
                        boolean has = false;

                        while(true) {
                           String nbt = pro.getProperty(i + "-" + index);
                           ++index;
                           if (nbt == null) {
                              if (has) {
                                 is = at.getStack();
                              }
                              break;
                           }

                           has = true;
                           String[] ss = nbt.split(" ");
                           double amount = Double.parseDouble(ss[0]);
                           String minecraftId = ss[1];
                           int operationId = Integer.parseInt(ss[2]);
                           String UUIDStr = ss[3];
                           String name = Util.combine(ss, " ", 4, ss.length);
                           at.add(Attribute.newBuilder().name(name).amount(amount).type(AttributeType.fromId(minecraftId)).operation(Operation.fromId(operationId)).uuid(UUID.fromString(UUIDStr)).build());
                        }
                     } catch (Exception var40) {
                     }

                     if (pi.getItem(i) == null) {
                        pi.setItem(i, is);
                     } else {
                        pi.addItem(new ItemStack[]{is});
                     }
                  }
               }
            } catch (Exception var41) {
            }
         }

         if (result) {
            p.sendMessage(UtilFormat.format("lib", "success", new Object[]{this.get(85)}));
         } else {
            p.sendMessage(this.get(90));
         }
      } catch (FileNotFoundException var42) {
         p.sendMessage(this.get(90));
      } catch (IOException var43) {
         Util.sendConsoleMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(80)}));
      } catch (InvalidConfigurationException e) {
         e.printStackTrace();
      } catch (Exception var45) {
      } finally {
         try {
            if (isr != null) {
               isr.close();
            }

            if (fis != null) {
               fis.close();
            }

            file.delete();
         } catch (Exception e) {
            e.printStackTrace();
         }

      }

   }

   public PluginManager getPm() {
      return this.pm;
   }

   public String getMainPath() {
      return this.mainPath;
   }

   public String getPluginPath() {
      return this.pluginPath;
   }

   public String getPn() {
      return this.pn;
   }

   public DeathMessage getDeathMessage() {
      return this.deathMessage;
   }

   private void loadConfig0(FileConfiguration config) {
      this.per_death_admin = config.getString("per_death_admin");
      this.per_death_exp = config.getString("per_death_exp");
      this.per_death_durability = config.getString("per_death_durability");
      this.durabilityDel = config.getInt("durabilityDel");
   }

   private void initBasic() {
      this.server = this.getServer();
      this.pn = this.getName();
      this.pm = this.server.getPluginManager();
      this.scheduler = this.server.getScheduler();
      this.mainPath = System.getProperty("user.dir");
      this.pluginPath = this.getFile().getParentFile().getAbsolutePath();
      this.dataFolder = this.pluginPath + File.separator + this.pn;
      this.pluginVersion = Util.getPluginVersion(this.getFile());
   }

   private void initConfig() {
      HashList<Pattern> filter = UtilConfig.getDefaultFilter();
      filter.add(Pattern.compile("deathMsg.yml"));
      UtilConfig.register(new File(this.pluginPath + File.separator + this.pn + ".jar"), this.dataFolder, filter, this.pn);
      this.loadConfig((CommandSender)null);
   }

   private void reloadConfig(CommandSender sender) {
      if (this.loadConfig(sender)) {
         sender.sendMessage(UtilFormat.format("lib", "success", new Object[]{this.get(25)}));
      } else {
         sender.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(30)}));
      }

   }

   private boolean loadConfig(CommandSender sender) {
      try {
         return UtilConfig.loadConfig(this.pn);
      } catch (InvalidConfigurationException e) {
         if (sender == null) {
            sender = this.server.getConsoleSender();
         }

         if (sender != null) {
            sender.sendMessage(e.getMessage());
         } else {
            this.server.getLogger().info(e.getMessage());
         }

         return false;
      }
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
