package com.earth2me.essentials;

import com.earth2me.essentials.craftbukkit.FakeWorld;
import com.earth2me.essentials.settings.Spawns;
import com.earth2me.essentials.storage.YamlStorageWriter;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.inventory.ItemStack;

public class EssentialsUpgrade {
   private static final Logger LOGGER = Logger.getLogger("Minecraft");
   private final transient IEssentials ess;
   private final transient EssentialsConf doneFile;

   EssentialsUpgrade(IEssentials essentials) {
      this.ess = essentials;
      if (!this.ess.getDataFolder().exists()) {
         this.ess.getDataFolder().mkdirs();
      }

      this.doneFile = new EssentialsConf(new File(this.ess.getDataFolder(), "upgrades-done.yml"));
      this.doneFile.load();
   }

   private void moveWorthValuesToWorthYml() {
      if (!this.doneFile.getBoolean("moveWorthValuesToWorthYml", false)) {
         try {
            File configFile = new File(this.ess.getDataFolder(), "config.yml");
            if (!configFile.exists()) {
               return;
            }

            EssentialsConf conf = new EssentialsConf(configFile);
            conf.load();
            Worth worth = new Worth(this.ess.getDataFolder());
            boolean found = false;

            for(Material mat : Material.values()) {
               int id = mat.getId();
               double value = conf.getDouble("worth-" + id, Double.NaN);
               if (!Double.isNaN(value)) {
                  found = true;
                  worth.setPrice(new ItemStack(mat, 1, (short)0, (byte)0), value);
               }
            }

            if (found) {
               this.removeLinesFromConfig(configFile, "\\s*#?\\s*worth-[0-9]+.*", "# Worth values have been moved to worth.yml");
            }

            this.doneFile.setProperty("moveWorthValuesToWorthYml", (Object)true);
            this.doneFile.save();
         } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, I18n._("upgradingFilesError"), e);
         }

      }
   }

   private void moveMotdRulesToFile(String name) {
      if (!this.doneFile.getBoolean("move" + name + "ToFile", false)) {
         try {
            File file = new File(this.ess.getDataFolder(), name + ".txt");
            if (file.exists()) {
               return;
            }

            File configFile = new File(this.ess.getDataFolder(), "config.yml");
            if (!configFile.exists()) {
               return;
            }

            EssentialsConf conf = new EssentialsConf(configFile);
            conf.load();
            List<String> lines = conf.getStringList(name);
            if (lines != null && !lines.isEmpty()) {
               if (!file.createNewFile()) {
                  throw new IOException("Failed to create file " + file);
               }

               PrintWriter writer = new PrintWriter(file);

               for(String line : lines) {
                  writer.println(line);
               }

               writer.close();
            }

            this.doneFile.setProperty("move" + name + "ToFile", (Object)true);
            this.doneFile.save();
         } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, I18n._("upgradingFilesError"), e);
         }

      }
   }

   private void removeLinesFromConfig(File file, String regex, String info) throws Exception {
      boolean needUpdate = false;
      BufferedReader bReader = new BufferedReader(new FileReader(file));
      File tempFile = File.createTempFile("essentialsupgrade", ".tmp.yml", this.ess.getDataFolder());
      BufferedWriter bWriter = new BufferedWriter(new FileWriter(tempFile));

      while(true) {
         String line = bReader.readLine();
         if (line == null) {
            bReader.close();
            bWriter.close();
            if (needUpdate) {
               if (!file.renameTo(new File(file.getParentFile(), file.getName().concat("." + System.currentTimeMillis() + ".upgradebackup")))) {
                  throw new Exception(I18n._("configFileMoveError"));
               }

               if (!tempFile.renameTo(file)) {
                  throw new Exception(I18n._("configFileRenameError"));
               }
            } else {
               tempFile.delete();
            }

            return;
         }

         if (line.matches(regex)) {
            if (!needUpdate && info != null) {
               bWriter.write(info, 0, info.length());
               bWriter.newLine();
            }

            needUpdate = true;
         } else {
            if (line.endsWith("\r\n")) {
               bWriter.write(line, 0, line.length() - 2);
            } else if (!line.endsWith("\r") && !line.endsWith("\n")) {
               bWriter.write(line, 0, line.length());
            } else {
               bWriter.write(line, 0, line.length() - 1);
            }

            bWriter.newLine();
         }
      }
   }

   private void updateUsersToNewDefaultHome() {
      if (!this.doneFile.getBoolean("updateUsersToNewDefaultHome", false)) {
         File userdataFolder = new File(this.ess.getDataFolder(), "userdata");
         if (userdataFolder.exists() && userdataFolder.isDirectory()) {
            File[] userFiles = userdataFolder.listFiles();

            for(File file : userFiles) {
               if (file.isFile() && file.getName().endsWith(".yml")) {
                  EssentialsConf config = new EssentialsConf(file);

                  try {
                     config.load();
                     if (config.hasProperty("home") && !config.hasProperty("home.default")) {
                        List<Object> vals = (List)config.getProperty("home");
                        if (vals != null) {
                           World world = (World)this.ess.getServer().getWorlds().get(0);
                           if (vals.size() > 5) {
                              world = this.ess.getServer().getWorld((String)vals.get(5));
                           }

                           if (world != null) {
                              Location loc = new Location(world, ((Number)vals.get(0)).doubleValue(), ((Number)vals.get(1)).doubleValue(), ((Number)vals.get(2)).doubleValue(), ((Number)vals.get(3)).floatValue(), ((Number)vals.get(4)).floatValue());
                              String worldName = world.getName().toLowerCase(Locale.ENGLISH);
                              if (worldName != null && !worldName.isEmpty()) {
                                 config.removeProperty("home");
                                 config.setProperty("home.default", (Object)worldName);
                                 config.setProperty("home.worlds." + worldName, loc);
                                 config.save();
                              }
                           }
                        }
                     }
                  } catch (RuntimeException ex) {
                     LOGGER.log(Level.INFO, "File: " + file.toString());
                     throw ex;
                  }
               }
            }

            this.doneFile.setProperty("updateUsersToNewDefaultHome", (Object)true);
            this.doneFile.save();
         }
      }
   }

   private void updateUsersPowerToolsFormat() {
      if (!this.doneFile.getBoolean("updateUsersPowerToolsFormat", false)) {
         File userdataFolder = new File(this.ess.getDataFolder(), "userdata");
         if (userdataFolder.exists() && userdataFolder.isDirectory()) {
            File[] userFiles = userdataFolder.listFiles();

            for(File file : userFiles) {
               if (file.isFile() && file.getName().endsWith(".yml")) {
                  EssentialsConf config = new EssentialsConf(file);

                  try {
                     config.load();
                     if (config.hasProperty("powertools")) {
                        Map<String, Object> powertools = config.getConfigurationSection("powertools").getValues(false);
                        if (powertools != null) {
                           for(Map.Entry entry : powertools.entrySet()) {
                              if (entry.getValue() instanceof String) {
                                 List<String> temp = new ArrayList();
                                 temp.add((String)entry.getValue());
                                 powertools.put(entry.getKey(), temp);
                              }
                           }

                           config.save();
                        }
                     }
                  } catch (RuntimeException ex) {
                     LOGGER.log(Level.INFO, "File: " + file.toString());
                     throw ex;
                  }
               }
            }

            this.doneFile.setProperty("updateUsersPowerToolsFormat", (Object)true);
            this.doneFile.save();
         }
      }
   }

   private void updateUsersHomesFormat() {
      if (!this.doneFile.getBoolean("updateUsersHomesFormat", false)) {
         File userdataFolder = new File(this.ess.getDataFolder(), "userdata");
         if (userdataFolder.exists() && userdataFolder.isDirectory()) {
            File[] userFiles = userdataFolder.listFiles();

            for(File file : userFiles) {
               if (file.isFile() && file.getName().endsWith(".yml")) {
                  EssentialsConf config = new EssentialsConf(file);

                  try {
                     config.load();
                     if (config.hasProperty("home") && config.hasProperty("home.default")) {
                        String defworld = (String)config.getProperty("home.default");
                        Location defloc = this.getFakeLocation(config, "home.worlds." + defworld);
                        if (defloc != null) {
                           config.setProperty("homes.home", defloc);
                        }

                        Set<String> worlds = config.getConfigurationSection("home.worlds").getKeys(false);
                        if (worlds != null) {
                           for(String world : worlds) {
                              if (!defworld.equalsIgnoreCase(world)) {
                                 Location loc = this.getFakeLocation(config, "home.worlds." + world);
                                 if (loc != null) {
                                    String worldName = loc.getWorld().getName().toLowerCase(Locale.ENGLISH);
                                    if (worldName != null && !worldName.isEmpty()) {
                                       config.setProperty("homes." + worldName, loc);
                                    }
                                 }
                              }
                           }

                           config.removeProperty("home");
                           config.save();
                        }
                     }
                  } catch (RuntimeException ex) {
                     LOGGER.log(Level.INFO, "File: " + file.toString());
                     throw ex;
                  }
               }
            }

            this.doneFile.setProperty("updateUsersHomesFormat", (Object)true);
            this.doneFile.save();
         }
      }
   }

   private void moveUsersDataToUserdataFolder() {
      File usersFile = new File(this.ess.getDataFolder(), "users.yml");
      if (usersFile.exists()) {
         EssentialsConf usersConfig = new EssentialsConf(usersFile);
         usersConfig.load();

         for(String username : usersConfig.getKeys(false)) {
            User user = new User(new OfflinePlayer(username, this.ess), this.ess);
            String nickname = usersConfig.getString(username + ".nickname");
            if (nickname != null && !nickname.isEmpty() && !nickname.equals(username)) {
               user.setNickname(nickname);
            }

            List<String> mails = usersConfig.getStringList(username + ".mail");
            if (mails != null && !mails.isEmpty()) {
               user.setMails(mails);
            }

            if (!user.hasHome()) {
               List<Object> vals = (List)usersConfig.getProperty(username + ".home");
               if (vals != null) {
                  World world = (World)this.ess.getServer().getWorlds().get(0);
                  if (vals.size() > 5) {
                     world = this.getFakeWorld((String)vals.get(5));
                  }

                  if (world != null) {
                     user.setHome("home", new Location(world, ((Number)vals.get(0)).doubleValue(), ((Number)vals.get(1)).doubleValue(), ((Number)vals.get(2)).doubleValue(), ((Number)vals.get(3)).floatValue(), ((Number)vals.get(4)).floatValue()));
                  }
               }
            }
         }

         usersFile.renameTo(new File(usersFile.getAbsolutePath() + ".old"));
      }
   }

   private void convertWarps() {
      File warpsFolder = new File(this.ess.getDataFolder(), "warps");
      if (!warpsFolder.exists()) {
         warpsFolder.mkdirs();
      }

      File[] listOfFiles = warpsFolder.listFiles();
      if (listOfFiles.length >= 1) {
         for(int i = 0; i < listOfFiles.length; ++i) {
            String filename = listOfFiles[i].getName();
            if (listOfFiles[i].isFile() && filename.endsWith(".dat")) {
               try {
                  BufferedReader rx = new BufferedReader(new FileReader(listOfFiles[i]));

                  double x;
                  double y;
                  double z;
                  float yaw;
                  float pitch;
                  String worldName;
                  try {
                     if (!rx.ready()) {
                        continue;
                     }

                     x = Double.parseDouble(rx.readLine().trim());
                     if (!rx.ready()) {
                        continue;
                     }

                     y = Double.parseDouble(rx.readLine().trim());
                     if (!rx.ready()) {
                        continue;
                     }

                     z = Double.parseDouble(rx.readLine().trim());
                     if (!rx.ready()) {
                        continue;
                     }

                     yaw = Float.parseFloat(rx.readLine().trim());
                     if (!rx.ready()) {
                        continue;
                     }

                     pitch = Float.parseFloat(rx.readLine().trim());
                     worldName = rx.readLine();
                  } finally {
                     rx.close();
                  }

                  World w = null;

                  for(World world : this.ess.getServer().getWorlds()) {
                     if (world.getEnvironment() != Environment.NETHER) {
                        w = world;
                        break;
                     }
                  }

                  if (worldName != null) {
                     worldName = worldName.trim();
                     World w1 = null;
                     w1 = this.getFakeWorld(worldName);
                     if (w1 != null) {
                        w = w1;
                     }
                  }

                  Location loc = new Location(w, x, y, z, yaw, pitch);
                  this.ess.getWarps().setWarp(filename.substring(0, filename.length() - 4), loc);
                  if (!listOfFiles[i].renameTo(new File(warpsFolder, filename + ".old"))) {
                     throw new Exception(I18n._("fileRenameError", filename));
                  }
               } catch (Exception ex) {
                  LOGGER.log(Level.SEVERE, (String)null, ex);
               }
            }
         }
      }

      File warpFile = new File(this.ess.getDataFolder(), "warps.txt");
      if (warpFile.exists()) {
         try {
            BufferedReader rx = new BufferedReader(new FileReader(warpFile));

            try {
               for(String[] parts = new String[0]; rx.ready(); parts = rx.readLine().split(":")) {
                  if (parts.length >= 6) {
                     String name = parts[0];
                     double x = Double.parseDouble(parts[1].trim());
                     double y = Double.parseDouble(parts[2].trim());
                     double z = Double.parseDouble(parts[3].trim());
                     float yaw = Float.parseFloat(parts[4].trim());
                     float pitch = Float.parseFloat(parts[5].trim());
                     if (!name.isEmpty()) {
                        World w = null;

                        for(World world : this.ess.getServer().getWorlds()) {
                           if (world.getEnvironment() != Environment.NETHER) {
                              w = world;
                              break;
                           }
                        }

                        Location loc = new Location(w, x, y, z, yaw, pitch);
                        this.ess.getWarps().setWarp(name, loc);
                        if (!warpFile.renameTo(new File(this.ess.getDataFolder(), "warps.txt.old"))) {
                           throw new Exception(I18n._("fileRenameError", "warps.txt"));
                        }
                     }
                  }
               }
            } finally {
               rx.close();
            }
         } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, (String)null, ex);
         }
      }

   }

   private void sanitizeAllUserFilenames() {
      if (!this.doneFile.getBoolean("sanitizeAllUserFilenames", false)) {
         File usersFolder = new File(this.ess.getDataFolder(), "userdata");
         if (usersFolder.exists()) {
            File[] listOfFiles = usersFolder.listFiles();

            for(int i = 0; i < listOfFiles.length; ++i) {
               String filename = listOfFiles[i].getName();
               if (listOfFiles[i].isFile() && filename.endsWith(".yml")) {
                  String sanitizedFilename = Util.sanitizeFileName(filename.substring(0, filename.length() - 4)) + ".yml";
                  if (!sanitizedFilename.equals(filename)) {
                     File tmpFile = new File(listOfFiles[i].getParentFile(), sanitizedFilename + ".tmp");
                     File newFile = new File(listOfFiles[i].getParentFile(), sanitizedFilename);
                     if (!listOfFiles[i].renameTo(tmpFile)) {
                        LOGGER.log(Level.WARNING, I18n._("userdataMoveError", filename, sanitizedFilename));
                     } else if (newFile.exists()) {
                        LOGGER.log(Level.WARNING, I18n._("duplicatedUserdata", filename, sanitizedFilename));
                     } else if (!tmpFile.renameTo(newFile)) {
                        LOGGER.log(Level.WARNING, I18n._("userdataMoveBackError", sanitizedFilename, sanitizedFilename));
                     }
                  }
               }
            }

            this.doneFile.setProperty("sanitizeAllUserFilenames", (Object)true);
            this.doneFile.save();
         }
      }
   }

   private World getFakeWorld(String name) {
      File bukkitDirectory = this.ess.getDataFolder().getParentFile().getParentFile();
      File worldDirectory = new File(bukkitDirectory, name);
      return worldDirectory.exists() && worldDirectory.isDirectory() ? new FakeWorld(worldDirectory.getName(), Environment.NORMAL) : null;
   }

   public Location getFakeLocation(EssentialsConf config, String path) {
      String worldName = config.getString((path != null ? path + "." : "") + "world");
      if (worldName != null && !worldName.isEmpty()) {
         World world = this.getFakeWorld(worldName);
         return world == null ? null : new Location(world, config.getDouble((path != null ? path + "." : "") + "x", (double)0.0F), config.getDouble((path != null ? path + "." : "") + "y", (double)0.0F), config.getDouble((path != null ? path + "." : "") + "z", (double)0.0F), (float)config.getDouble((path != null ? path + "." : "") + "yaw", (double)0.0F), (float)config.getDouble((path != null ? path + "." : "") + "pitch", (double)0.0F));
      } else {
         return null;
      }
   }

   private void deleteOldItemsCsv() {
      if (!this.doneFile.getBoolean("deleteOldItemsCsv", false)) {
         File file = new File(this.ess.getDataFolder(), "items.csv");
         if (file.exists()) {
            try {
               Set<BigInteger> oldconfigs = new HashSet();
               oldconfigs.add(new BigInteger("66ec40b09ac167079f558d1099e39f10", 16));
               oldconfigs.add(new BigInteger("34284de1ead43b0bee2aae85e75c041d", 16));
               oldconfigs.add(new BigInteger("c33bc9b8ee003861611bbc2f48eb6f4f", 16));
               oldconfigs.add(new BigInteger("6ff17925430735129fc2a02f830c1daa", 16));
               MessageDigest digest = ManagedFile.getDigest();
               BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
               DigestInputStream dis = new DigestInputStream(bis, digest);
               byte[] buffer = new byte[1024];

               try {
                  while(dis.read(buffer) != -1) {
                  }
               } finally {
                  dis.close();
               }

               BigInteger hash = new BigInteger(1, digest.digest());
               if (oldconfigs.contains(hash) && !file.delete()) {
                  throw new IOException("Could not delete file " + file.toString());
               }

               this.doneFile.setProperty("deleteOldItemsCsv", (Object)true);
               this.doneFile.save();
            } catch (IOException ex) {
               Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
            }
         }

      }
   }

   private void updateSpawnsToNewSpawnsConfig() {
      if (!this.doneFile.getBoolean("updateSpawnsToNewSpawnsConfig", false)) {
         File configFile = new File(this.ess.getDataFolder(), "spawn.yml");
         if (configFile.exists()) {
            EssentialsConf config = new EssentialsConf(configFile);

            try {
               config.load();
               if (!config.hasProperty("spawns")) {
                  Spawns spawns = new Spawns();

                  for(String group : config.getKeys(false)) {
                     Location loc = this.getFakeLocation(config, group);
                     spawns.getSpawns().put(group.toLowerCase(Locale.ENGLISH), loc);
                  }

                  if (!configFile.renameTo(new File(this.ess.getDataFolder(), "spawn.yml.old"))) {
                     throw new Exception(I18n._("fileRenameError", "spawn.yml"));
                  }

                  PrintWriter writer = new PrintWriter(configFile);

                  try {
                     (new YamlStorageWriter(writer)).save(spawns);
                  } finally {
                     writer.close();
                  }
               }
            } catch (Exception ex) {
               Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
            }
         }

         this.doneFile.setProperty("updateSpawnsToNewSpawnsConfig", (Object)true);
         this.doneFile.save();
      }
   }

   private void updateJailsToNewJailsConfig() {
      if (!this.doneFile.getBoolean("updateJailsToNewJailsConfig", false)) {
         File configFile = new File(this.ess.getDataFolder(), "jail.yml");
         if (configFile.exists()) {
            EssentialsConf config = new EssentialsConf(configFile);

            try {
               config.load();
               if (!config.hasProperty("jails")) {
                  com.earth2me.essentials.settings.Jails jails = new com.earth2me.essentials.settings.Jails();

                  for(String jailName : config.getKeys(false)) {
                     Location loc = this.getFakeLocation(config, jailName);
                     jails.getJails().put(jailName.toLowerCase(Locale.ENGLISH), loc);
                  }

                  if (!configFile.renameTo(new File(this.ess.getDataFolder(), "jail.yml.old"))) {
                     throw new Exception(I18n._("fileRenameError", "jail.yml"));
                  }

                  PrintWriter writer = new PrintWriter(configFile);

                  try {
                     (new YamlStorageWriter(writer)).save(jails);
                  } finally {
                     writer.close();
                  }
               }
            } catch (Exception ex) {
               Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
            }
         }

         this.doneFile.setProperty("updateJailsToNewJailsConfig", (Object)true);
         this.doneFile.save();
      }
   }

   private void warnMetrics() {
      if (!this.doneFile.getBoolean("warnMetrics", false)) {
         this.ess.getSettings().setMetricsEnabled(false);
         this.doneFile.setProperty("warnMetrics", (Object)true);
         this.doneFile.save();
      }
   }

   public void beforeSettings() {
      if (!this.ess.getDataFolder().exists()) {
         this.ess.getDataFolder().mkdirs();
      }

      this.moveWorthValuesToWorthYml();
      this.moveMotdRulesToFile("motd");
      this.moveMotdRulesToFile("rules");
   }

   public void afterSettings() {
      this.sanitizeAllUserFilenames();
      this.updateUsersToNewDefaultHome();
      this.moveUsersDataToUserdataFolder();
      this.convertWarps();
      this.updateUsersPowerToolsFormat();
      this.updateUsersHomesFormat();
      this.deleteOldItemsCsv();
      this.updateSpawnsToNewSpawnsConfig();
      this.updateJailsToNewJailsConfig();
      this.warnMetrics();
   }
}
