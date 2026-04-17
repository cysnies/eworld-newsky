package clear;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Main extends JavaPlugin implements Listener {
   private static final Pattern PLAIN_PATTERN = Pattern.compile("lang_\\d{1,5}");
   private static final Pattern FORMAT_PATTERN = Pattern.compile("lang-\\d{1,5}");
   private String pn;
   private BukkitScheduler scheduler;
   private String mainPath;
   private String pluginPath;
   private String dataFolder;
   private String pluginVersion;
   private Tps tps;
   private Names names;
   private Time time;
   private ServerManager serverManager;
   private Clear clear;
   private RedStone redStone;
   private Crop crop;
   private Liquid liquid;
   private HashMap plainHash;
   private HashMap formatHash;
   private List filter = new ArrayList();

   public void onEnable() {
      this.initBasic();
      this.initConfig();
      this.tps = new Tps(this);
      this.names = new Names(this);
      this.time = new Time(this);
      this.serverManager = new ServerManager(this);
      this.redStone = new RedStone(this);
      this.crop = new Crop(this);
      this.liquid = new Liquid(this);
      this.clear = new Clear(this);
      sendConsoleMessage(this.format("pluginEnabled", this.pn, this.pluginVersion));
   }

   public void onDisable() {
      if (this.scheduler != null) {
         this.scheduler.cancelAllTasks();
      }

      sendConsoleMessage(this.format("pluginDisabled", this.pn, this.pluginVersion));
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      String cmdName = cmd.getName();
      int length = args.length;

      try {
         if (cmdName.equalsIgnoreCase("clear")) {
            sender.sendMessage("§4§l作者:fyxridd 论坛: www.minecraft001.com");
            if (p != null && !p.isOp()) {
               p.sendMessage(this.get(5));
               return true;
            }

            if (length != 1 || !args[0].equalsIgnoreCase("?")) {
               if (length == 1) {
                  if (args[0].equalsIgnoreCase("reload")) {
                     this.loadConfig(sender);
                     sender.sendMessage(this.get(7));
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("info")) {
                     this.clear.info(sender);
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("start")) {
                     this.clear.clear(true, -1);
                     return true;
                  }
               } else if (length == 2 && args[0].equalsIgnoreCase("start")) {
                  this.clear.clear(true, Integer.parseInt(args[1]));
                  return true;
               }
            }

            sender.sendMessage(this.format("cmdHelpHeader", this.get(10)));
            sender.sendMessage(this.format("cmdHelpItem", this.get(15), this.get(20)));
            sender.sendMessage(this.format("cmdHelpItem", this.get(1215), this.get(1220)));
            sender.sendMessage(this.format("cmdHelpItem", this.get(1225), this.get(1230)));
         }
      } catch (NumberFormatException var9) {
         sender.sendMessage(this.format("fail", this.get(190)));
      }

      return true;
   }

   private void initBasic() {
      this.pn = this.getName();
      this.scheduler = Bukkit.getScheduler();
      this.mainPath = System.getProperty("user.dir");
      this.pluginPath = this.getFile().getParentFile().getAbsolutePath();
      this.dataFolder = this.pluginPath + File.separator + this.pn;
      this.pluginVersion = getPluginVersion(this.getFile());
   }

   private void initConfig() {
      this.filter.add(Pattern.compile("config.yml"));
      this.filter.add(Pattern.compile("config_[a-zA-Z]+.yml"));
      this.filter.add(Pattern.compile("language.yml"));
      this.filter.add(Pattern.compile("language_[a-zA-Z]+.yml"));
      this.filter.add(Pattern.compile("hibernate.cfg.xml"));
      this.filter.add(Pattern.compile("names.yml"));
      this.loadConfig((CommandSender)null);
   }

   private void loadConfig(CommandSender sender) {
      generateFiles(new File(this.pluginPath + File.separator + this.pn + ".jar"), this.dataFolder, this.filter);

      try {
         YamlConfiguration config = new YamlConfiguration();
         config.load(this.getPluginPath() + File.separator + this.pn + File.separator + "config.yml");
         this.loadConfig0();
         if (this.clear != null) {
            this.clear.loadConfig(config);
         }

         if (this.crop != null) {
            this.crop.loadConfig(config);
         }

         if (this.liquid != null) {
            this.liquid.loadConfig(config);
         }

         if (this.names != null) {
            this.names.loadConfig();
         }

         if (this.redStone != null) {
            this.redStone.loadConfig(config);
         }

         if (this.serverManager != null) {
            this.serverManager.loadConfig(config);
         }
      } catch (InvalidConfigurationException e) {
         if (sender == null) {
            sender = Bukkit.getConsoleSender();
         }

         if (sender != null) {
            sender.sendMessage(e.getMessage());
         } else {
            Bukkit.getLogger().info(e.getMessage());
         }
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }

   }

   private void loadConfig0() {
      this.plainHash = new HashMap();
      this.formatHash = new HashMap();

      try {
         YamlConfiguration languageConfig = new YamlConfiguration();
         languageConfig.load(this.pluginPath + File.separator + this.pn + File.separator + "language.yml");

         for(String key : languageConfig.getKeys(true)) {
            if (PLAIN_PATTERN.matcher(key).matches()) {
               this.plainHash.put(this.getId(key), convertBr(convert(languageConfig.getString(key))));
            } else if (FORMAT_PATTERN.matcher(key).matches()) {
               String s = languageConfig.getString(key);
               int index = s.indexOf(":");
               if (index < 1) {
                  return;
               }

               String name = s.substring(0, index);
               if (name.isEmpty()) {
                  return;
               }

               if (index == -1) {
                  return;
               }

               String value = convertBr(convert(s.substring(index + 1, s.length())));
               this.formatHash.put(name, value);
            }
         }
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (InvalidConfigurationException e) {
         e.printStackTrace();
      }

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

   public static double getDouble(double num, int accuracy) {
      if (accuracy < 0) {
         accuracy = 0;
      }

      String s = String.valueOf(num);
      if (s.split("\\.").length == 2) {
         String[] ss = s.split("\\.");
         return Double.parseDouble(ss[0] + "." + ss[1].substring(0, Math.min(accuracy, ss[1].length())));
      } else {
         return num;
      }
   }

   public static String convert(String s) {
      if (s == null) {
         return null;
      } else {
         s = s.replace("//", "\u0001");
         s = s.replace("/&", "\u0002");
         s = s.replace("&", String.valueOf('§'));
         s = s.replace("\u0002", "&");
         s = s.replace("\u0001", "/");
         return s;
      }
   }

   public static String convertBr(String s) {
      if (s == null) {
         return null;
      } else {
         s = s.replace("\n ", "\n");
         return s;
      }
   }

   public static void sendConsoleMessage(String msg) {
      try {
         if (Bukkit.getConsoleSender() != null) {
            Bukkit.getConsoleSender().sendMessage(msg);
         } else {
            Bukkit.getLogger().info(msg);
         }
      } catch (Exception var2) {
         System.out.println(msg);
      }

   }

   public static String getPluginVersion(File plugin) {
      JarInputStream jis = null;

      String var6;
      try {
         jis = new JarInputStream(new FileInputStream(plugin));

         String fileName;
         do {
            JarEntry entry;
            if ((entry = jis.getNextJarEntry()) == null) {
               return null;
            }

            fileName = entry.getName();
         } while(!fileName.equalsIgnoreCase("plugin.yml"));

         YamlConfiguration config = new YamlConfiguration();
         config.load(jis);
         var6 = config.getString("version", (String)null);
      } catch (FileNotFoundException var19) {
         return null;
      } catch (IOException var20) {
         return null;
      } catch (InvalidConfigurationException var21) {
         return null;
      } finally {
         try {
            if (jis != null) {
               jis.close();
            }
         } catch (IOException var18) {
            return null;
         }

      }

      return var6;
   }

   public static boolean generateFiles(File sourceJarFile, String destPath, List filter) {
      JarInputStream jis = null;
      FileOutputStream fos = null;

      try {
         (new File(destPath)).mkdirs();
         jis = new JarInputStream(new FileInputStream(sourceJarFile));
         byte[] buff = new byte[1024];

         JarEntry entry;
         while((entry = jis.getNextJarEntry()) != null) {
            String fileName = entry.getName();

            for(Pattern pattern : filter) {
               Matcher matcher = pattern.matcher(fileName);
               if (matcher.find() && !(new File(destPath + File.separator + fileName)).exists()) {
                  fos = new FileOutputStream(destPath + File.separator + fileName);

                  int read;
                  while((read = jis.read(buff)) > 0) {
                     fos.write(buff, 0, read);
                  }

                  fos.close();
               }
            }
         }

         return true;
      } catch (FileNotFoundException var27) {
      } catch (IOException var28) {
         return false;
      } finally {
         try {
            if (jis != null) {
               jis.close();
            }
         } catch (IOException var25) {
            return false;
         }

         try {
            if (fos != null) {
               fos.close();
            }
         } catch (IOException var26) {
            return false;
         }

      }

      return false;
   }

   public String get(int id) {
      try {
         return (String)this.plainHash.get(id);
      } catch (Exception var3) {
         return "";
      }
   }

   public String format(String type, Object... args) {
      String result = (String)this.formatHash.get(type);
      if (result != null) {
         for(int i = 0; i < args.length; ++i) {
            if (args[i] == null) {
               args[i] = "";
            }

            result = result.replace("{" + i + "}", args[i].toString());
         }

         return result;
      } else {
         result = (String)this.formatHash.get(type);
         if (result != null) {
            for(int i = 0; i < args.length; ++i) {
               if (args[i] == null) {
                  args[i] = "";
               }

               result = result.replace("{" + i + "}", args[i].toString());
            }

            return result;
         } else {
            return "";
         }
      }
   }

   public String getPn() {
      return this.pn;
   }

   public ServerManager getServerManager() {
      return this.serverManager;
   }

   public RedStone getRedStone() {
      return this.redStone;
   }

   public Crop getCrop() {
      return this.crop;
   }

   public String getMainPath() {
      return this.mainPath;
   }

   public Liquid getLiquid() {
      return this.liquid;
   }

   public String getPluginPath() {
      return this.pluginPath;
   }

   public Time getTime() {
      return this.time;
   }

   public Tps getTps() {
      return this.tps;
   }
}
