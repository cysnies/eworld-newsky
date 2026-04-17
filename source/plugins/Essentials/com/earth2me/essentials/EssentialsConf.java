package com.earth2me.essentials;

import com.google.common.io.Files;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class EssentialsConf extends YamlConfiguration {
   private static final Logger LOGGER = Logger.getLogger("Minecraft");
   private transient File configFile;
   private transient String templateName = null;
   private transient Class resourceClass = EssentialsConf.class;
   private static final Charset UTF8 = Charset.forName("UTF-8");
   private final byte[] bytebuffer = new byte[1024];

   public EssentialsConf(File configFile) {
      this.configFile = configFile;
   }

   public synchronized void load() {
      this.configFile = this.configFile.getAbsoluteFile();
      if (!this.configFile.getParentFile().exists() && !this.configFile.getParentFile().mkdirs()) {
         LOGGER.log(Level.SEVERE, I18n._("failedToCreateConfig", this.configFile.toString()));
      }

      if (this.configFile.exists() && this.configFile.length() != 0L) {
         try {
            InputStream input = new FileInputStream(this.configFile);

            try {
               if (input.read() == 0) {
                  input.close();
                  this.configFile.delete();
               }
            } catch (IOException ex) {
               LOGGER.log(Level.SEVERE, (String)null, ex);
            } finally {
               try {
                  input.close();
               } catch (IOException ex) {
                  LOGGER.log(Level.SEVERE, (String)null, ex);
               }

            }
         } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, (String)null, ex);
         }
      }

      if (!this.configFile.exists()) {
         if (this.templateName == null) {
            return;
         }

         LOGGER.log(Level.INFO, I18n._("creatingConfigFromTemplate", this.configFile.toString()));
         this.createFromTemplate();
      }

      try {
         FileInputStream inputStream = new FileInputStream(this.configFile);

         try {
            ByteBuffer buffer = ByteBuffer.allocate((int)this.configFile.length());

            int length;
            while((length = inputStream.read(this.bytebuffer)) != -1) {
               buffer.put(this.bytebuffer, 0, length);
            }

            buffer.rewind();
            CharBuffer data = CharBuffer.allocate((int)this.configFile.length());
            CharsetDecoder decoder = UTF8.newDecoder();
            CoderResult result = decoder.decode(buffer, data, true);
            if (result.isError()) {
               buffer.rewind();
               data.clear();
               LOGGER.log(Level.INFO, "File " + this.configFile.getAbsolutePath().toString() + " is not utf-8 encoded, trying " + Charset.defaultCharset().displayName());
               decoder = Charset.defaultCharset().newDecoder();
               result = decoder.decode(buffer, data, true);
               if (result.isError()) {
                  throw new InvalidConfigurationException("Invalid Characters in file " + this.configFile.getAbsolutePath().toString());
               }

               decoder.flush(data);
            } else {
               decoder.flush(data);
            }

            int end = data.position();
            data.rewind();
            super.loadFromString(data.subSequence(0, end).toString());
         } finally {
            inputStream.close();
         }
      } catch (IOException ex) {
         LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
      } catch (InvalidConfigurationException ex) {
         File broken = new File(this.configFile.getAbsolutePath() + ".broken." + System.currentTimeMillis());
         this.configFile.renameTo(broken);
         LOGGER.log(Level.SEVERE, "The file " + this.configFile.toString() + " is broken, it has been renamed to " + broken.toString(), ex.getCause());
      }

   }

   private void createFromTemplate() {
      InputStream istr = null;
      OutputStream ostr = null;

      try {
         istr = this.resourceClass.getResourceAsStream(this.templateName);
         if (istr != null) {
            ostr = new FileOutputStream(this.configFile);
            byte[] buffer = new byte[1024];
            int length = 0;

            for(int var21 = istr.read(buffer); var21 > 0; var21 = istr.read(buffer)) {
               ostr.write(buffer, 0, var21);
            }

            return;
         }

         LOGGER.log(Level.SEVERE, I18n._("couldNotFindTemplate", this.templateName));
      } catch (IOException ex) {
         LOGGER.log(Level.SEVERE, I18n._("failedToWriteConfig", this.configFile.toString()), ex);
         return;
      } finally {
         try {
            if (istr != null) {
               istr.close();
            }
         } catch (IOException ex) {
            Logger.getLogger(EssentialsConf.class.getName()).log(Level.SEVERE, (String)null, ex);
         }

         try {
            if (ostr != null) {
               ostr.close();
            }
         } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, I18n._("failedToCloseConfig", this.configFile.toString()), ex);
         }

      }

   }

   public void setTemplateName(String templateName) {
      this.templateName = templateName;
   }

   public File getFile() {
      return this.configFile;
   }

   public void setTemplateName(String templateName, Class resClass) {
      this.templateName = templateName;
      this.resourceClass = resClass;
   }

   public void save() {
      try {
         this.save(this.configFile);
      } catch (IOException ex) {
         LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
      }

   }

   public void saveWithError() throws IOException {
      this.save(this.configFile);
   }

   public synchronized void save(File file) throws IOException {
      if (file == null) {
         throw new IllegalArgumentException("File cannot be null");
      } else {
         Files.createParentDirs(file);
         String data = this.saveToString();
         if (data.length() != 0) {
            if (!this.configFile.exists()) {
               try {
                  LOGGER.log(Level.INFO, I18n._("creatingEmptyConfig", this.configFile.toString()));
                  if (!this.configFile.createNewFile()) {
                     LOGGER.log(Level.SEVERE, I18n._("failedToCreateConfig", this.configFile.toString()));
                  }
               } catch (IOException ex) {
                  LOGGER.log(Level.SEVERE, I18n._("failedToCreateConfig", this.configFile.toString()), ex);
               }
            }

            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), UTF8);

            try {
               writer.write(data);
            } finally {
               writer.close();
            }

         }
      }
   }

   public boolean hasProperty(String path) {
      return this.isSet(path);
   }

   public Location getLocation(String path, Server server) throws Exception {
      String worldName = this.getString((path == null ? "" : path + ".") + "world");
      if (worldName != null && !worldName.isEmpty()) {
         World world = server.getWorld(worldName);
         if (world == null) {
            throw new Exception(I18n._("invalidWorld"));
         } else {
            return new Location(world, this.getDouble((path == null ? "" : path + ".") + "x", (double)0.0F), this.getDouble((path == null ? "" : path + ".") + "y", (double)0.0F), this.getDouble((path == null ? "" : path + ".") + "z", (double)0.0F), (float)this.getDouble((path == null ? "" : path + ".") + "yaw", (double)0.0F), (float)this.getDouble((path == null ? "" : path + ".") + "pitch", (double)0.0F));
         }
      } else {
         return null;
      }
   }

   public void setProperty(String path, Location loc) {
      this.set((path == null ? "" : path + ".") + "world", loc.getWorld().getName());
      this.set((path == null ? "" : path + ".") + "x", loc.getX());
      this.set((path == null ? "" : path + ".") + "y", loc.getY());
      this.set((path == null ? "" : path + ".") + "z", loc.getZ());
      this.set((path == null ? "" : path + ".") + "yaw", loc.getYaw());
      this.set((path == null ? "" : path + ".") + "pitch", loc.getPitch());
   }

   public ItemStack getItemStack(String path) {
      ItemStack stack = new ItemStack(Material.valueOf(this.getString(path + ".type", "AIR")), this.getInt(path + ".amount", 1), (short)this.getInt(path + ".damage", 0));
      ConfigurationSection enchants = this.getConfigurationSection(path + ".enchant");
      if (enchants != null) {
         for(String enchant : enchants.getKeys(false)) {
            Enchantment enchantment = Enchantment.getByName(enchant.toUpperCase(Locale.ENGLISH));
            if (enchantment != null) {
               int level = this.getInt(path + ".enchant." + enchant, enchantment.getStartLevel());
               stack.addUnsafeEnchantment(enchantment, level);
            }
         }
      }

      return stack;
   }

   public void setProperty(String path, ItemStack stack) {
      Map<String, Object> map = new HashMap();
      map.put("type", stack.getType().toString());
      map.put("amount", stack.getAmount());
      map.put("damage", stack.getDurability());
      Map<Enchantment, Integer> enchantments = stack.getEnchantments();
      if (!enchantments.isEmpty()) {
         Map<String, Integer> enchant = new HashMap();

         for(Map.Entry entry : enchantments.entrySet()) {
            enchant.put(((Enchantment)entry.getKey()).getName().toLowerCase(Locale.ENGLISH), entry.getValue());
         }

         map.put("enchant", enchant);
      }

      this.set(path, map);
   }

   public void setProperty(String path, List object) {
      this.set(path, new ArrayList(object));
   }

   public void setProperty(String path, Map object) {
      this.set(path, new LinkedHashMap(object));
   }

   public Object getProperty(String path) {
      return this.get(path);
   }

   public void setProperty(String path, Object object) {
      this.set(path, object);
   }

   public void removeProperty(String path) {
      this.set(path, (Object)null);
   }

   public synchronized Object get(String path) {
      return super.get(path);
   }

   public synchronized Object get(String path, Object def) {
      return super.get(path, def);
   }

   public synchronized boolean getBoolean(String path) {
      return super.getBoolean(path);
   }

   public synchronized boolean getBoolean(String path, boolean def) {
      return super.getBoolean(path, def);
   }

   public synchronized List getBooleanList(String path) {
      return super.getBooleanList(path);
   }

   public synchronized List getByteList(String path) {
      return super.getByteList(path);
   }

   public synchronized List getCharacterList(String path) {
      return super.getCharacterList(path);
   }

   public synchronized ConfigurationSection getConfigurationSection(String path) {
      return super.getConfigurationSection(path);
   }

   public synchronized double getDouble(String path) {
      return super.getDouble(path);
   }

   public synchronized double getDouble(String path, double def) {
      return super.getDouble(path, def);
   }

   public synchronized List getDoubleList(String path) {
      return super.getDoubleList(path);
   }

   public synchronized List getFloatList(String path) {
      return super.getFloatList(path);
   }

   public synchronized int getInt(String path) {
      return super.getInt(path);
   }

   public synchronized int getInt(String path, int def) {
      return super.getInt(path, def);
   }

   public synchronized List getIntegerList(String path) {
      return super.getIntegerList(path);
   }

   public synchronized ItemStack getItemStack(String path, ItemStack def) {
      return super.getItemStack(path, def);
   }

   public synchronized Set getKeys(boolean deep) {
      return super.getKeys(deep);
   }

   public synchronized List getList(String path) {
      return super.getList(path);
   }

   public synchronized List getList(String path, List def) {
      return super.getList(path, def);
   }

   public synchronized long getLong(String path) {
      return super.getLong(path);
   }

   public synchronized long getLong(String path, long def) {
      return super.getLong(path, def);
   }

   public synchronized List getLongList(String path) {
      return super.getLongList(path);
   }

   public synchronized Map getMap() {
      return this.map;
   }

   public synchronized List getMapList(String path) {
      return super.getMapList(path);
   }

   public synchronized org.bukkit.OfflinePlayer getOfflinePlayer(String path) {
      return super.getOfflinePlayer(path);
   }

   public synchronized org.bukkit.OfflinePlayer getOfflinePlayer(String path, org.bukkit.OfflinePlayer def) {
      return super.getOfflinePlayer(path, def);
   }

   public synchronized List getShortList(String path) {
      return super.getShortList(path);
   }

   public synchronized String getString(String path) {
      return super.getString(path);
   }

   public synchronized String getString(String path, String def) {
      return super.getString(path, def);
   }

   public synchronized List getStringList(String path) {
      return super.getStringList(path);
   }

   public synchronized Map getValues(boolean deep) {
      return super.getValues(deep);
   }

   public synchronized Vector getVector(String path) {
      return super.getVector(path);
   }

   public synchronized Vector getVector(String path, Vector def) {
      return super.getVector(path, def);
   }

   public synchronized boolean isBoolean(String path) {
      return super.isBoolean(path);
   }

   public synchronized boolean isConfigurationSection(String path) {
      return super.isConfigurationSection(path);
   }

   public synchronized boolean isDouble(String path) {
      return super.isDouble(path);
   }

   public synchronized boolean isInt(String path) {
      return super.isInt(path);
   }

   public synchronized boolean isItemStack(String path) {
      return super.isItemStack(path);
   }

   public synchronized boolean isList(String path) {
      return super.isList(path);
   }

   public synchronized boolean isLong(String path) {
      return super.isLong(path);
   }

   public synchronized boolean isOfflinePlayer(String path) {
      return super.isOfflinePlayer(path);
   }

   public synchronized boolean isSet(String path) {
      return super.isSet(path);
   }

   public synchronized boolean isString(String path) {
      return super.isString(path);
   }

   public synchronized boolean isVector(String path) {
      return super.isVector(path);
   }

   public synchronized void set(String path, Object value) {
      super.set(path, value);
   }
}
