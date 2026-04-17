package lib.util;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lib.ItemMessage;
import lib.Lib;
import lib.Tps;
import lib.hashList.HashList;
import lib.time.Day;
import lib.types.InvalidTypeException;
import net.minecraft.server.v1_6_R2.EntityLightning;
import net.minecraft.server.v1_6_R2.Packet62NamedSoundEffect;
import net.minecraft.server.v1_6_R2.Packet71Weather;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.jline.internal.InputStreamReader;
import org.bukkit.craftbukkit.v1_6_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

public class Util {
   private static Random Random = new Random();
   private static final String VERSION_PATTERN = "\\(MC: [0-9.]{5}\\)";
   private static Lib LIB;
   private static Server Server;
   private static Essentials Essentials;
   private static String Pn;
   private static Day Day;
   private static ItemMessage Im;

   public static void init(Lib lib) {
      LIB = lib;
      Server = lib.getServer();
      Pn = lib.getPn();
      Day = lib.getDay();
      Im = lib.getIm();
   }

   public static YamlConfiguration loadConfigByUTF8(File file) {
      YamlConfiguration config = new YamlConfiguration();

      try {
         InputStreamReader reader = new InputStreamReader(new FileInputStream(file), Charset.forName("utf-8"));
         StringBuilder builder = new StringBuilder();
         BufferedReader input = new BufferedReader(reader);

         String line;
         try {
            while((line = input.readLine()) != null) {
               builder.append(line);
               builder.append('\n');
            }
         } finally {
            input.close();
         }

         config.loadFromString(builder.toString());
         return config;
      } catch (FileNotFoundException var12) {
         return null;
      } catch (IOException var13) {
         return null;
      } catch (InvalidConfigurationException var14) {
         return null;
      }
   }

   public static String strToU(String str) {
      if (str != null && !str.isEmpty()) {
         StringBuffer sb = new StringBuffer();

         char[] var5;
         for(char c : var5 = str.toCharArray()) {
            sb.append(" " + Integer.toHexString(c));
         }

         return sb.toString();
      } else {
         return str;
      }
   }

   public static String uToStr(String str) {
      if (str != null && !str.isEmpty()) {
         StringBuffer sb = new StringBuffer();

         try {
            String[] var5;
            for(String s : var5 = str.split(" ")) {
               if (!s.isEmpty()) {
                  int i = Integer.parseInt(s, 16);
                  sb.append((char)i);
               }
            }
         } catch (Exception var7) {
         }

         return sb.toString();
      } else {
         return str;
      }
   }

   public static boolean generateFiles(File sourceJarFile, String destPath, HashList filter) {
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

   public static String getMcVersion(Server server) {
      try {
         Pattern p = Pattern.compile("\\(MC: [0-9.]{5}\\)");
         Matcher m = p.matcher(server.getBukkitVersion());
         if (m.find()) {
            String result = m.group();
            if (result != null && !result.trim().isEmpty()) {
               return result.substring(result.indexOf(" ") + 1, result.indexOf(")"));
            }
         }
      } catch (Exception var4) {
      }

      return null;
   }

   public static int getPort(Server server) {
      return server.getPort();
   }

   public static byte[] intToByteArray(int i) {
      byte[] result = new byte[4];
      result[0] = (byte)(i >> 24 & 255);
      result[1] = (byte)(i >> 16 & 255);
      result[2] = (byte)(i >> 8 & 255);
      result[3] = (byte)(i & 255);
      return result;
   }

   public static int byteArrayToInt(byte[] byteArray) {
      int result = 0;
      int b0 = byteArray[0];
      int b1 = byteArray[1];
      int b2 = byteArray[2];
      int b3 = byteArray[3];
      result |= b0 << 24;
      result |= b1 << 16 & 16711680;
      result |= b2 << 8 & '\uff00';
      result |= b3 & 255;
      return result;
   }

   public static String charsToStr(char[] target) {
      StringBuffer buf = new StringBuffer();

      for(int i = 0; i < target.length; ++i) {
         buf.append(target[i]);
      }

      return buf.toString();
   }

   public static char[] StrToChars(String str) {
      char[] buf = new char[str.length()];

      for(int i = 0; i < str.length(); ++i) {
         buf[i] = str.charAt(i);
      }

      return buf;
   }

   public static double getTps() {
      return Tps.getTps();
   }

   public static String getLine(ItemStack itemStack, String message) {
      ItemMeta itemMeta = itemStack.getItemMeta();
      if (itemMeta == null) {
         return null;
      } else {
         List<String> lore = itemMeta.getLore();
         if (lore == null) {
            return null;
         } else {
            for(String s : lore) {
               if (s.indexOf(message) != -1) {
                  return s;
               }
            }

            return null;
         }
      }
   }

   public static String getDateTime(Date start, int addDay, int addHour, int addMinute) {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(start);
      calendar.add(5, addDay);
      calendar.add(11, addHour);
      calendar.add(12, addMinute);
      return sdf.format(calendar.getTime());
   }

   public static String getDateTime2(Date start, int addDay, int addHour, int addMinute) {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(start);
      calendar.add(5, addDay);
      calendar.add(11, addHour);
      calendar.add(12, addMinute);
      return sdf.format(calendar.getTime());
   }

   public static String getDate() {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
      return sdf.format(new Date());
   }

   public static void sendConsoleMessage(String msg) {
      try {
         if (Server.getConsoleSender() != null) {
            Server.getConsoleSender().sendMessage(msg);
         } else {
            Bukkit.getLogger().info(msg);
         }
      } catch (Exception var2) {
         System.out.println(msg);
      }

   }

   public static int getTotalExperience(Player p) {
      int exp = Math.round((float)getExpAtLevel(p.getLevel()) * p.getExp());

      for(int currentLevel = p.getLevel(); currentLevel > 0; exp += getExpAtLevel(currentLevel)) {
         --currentLevel;
      }

      if (exp < 0) {
         exp = Integer.MAX_VALUE;
      }

      return exp;
   }

   public static void setTotalExperience(Player p, int exp) {
      p.setExp(0.0F);
      p.setLevel(0);
      p.setTotalExperience(0);

      while(exp > 0) {
         int expToLevel = getExpAtLevel(p.getLevel());
         exp -= expToLevel;
         if (exp >= 0) {
            p.giveExp(expToLevel);
         } else {
            exp += expToLevel;
            p.giveExp(exp);
            exp = 0;
         }
      }

   }

   public static int getExpAtLevel(int level) {
      if (level > 29) {
         return 62 + (level - 30) * 7;
      } else {
         return level > 15 ? 17 + (level - 15) * 3 : 17;
      }
   }

   public static String combine(String[] args, String seperator, int start, int end) {
      String result = "";

      for(int i = 0; i < args.length; ++i) {
         if (i >= start) {
            if (i > end) {
               break;
            }

            if (i > start) {
               result = result + seperator;
            }

            result = result + args[i];
         }
      }

      return result;
   }

   public static boolean tp(Player p, Location l, boolean safe, boolean enableBack) {
      if (Essentials == null) {
         Essentials = (Essentials)Server.getPluginManager().getPlugin("Essentials");
      }

      if (!safe) {
         if (enableBack) {
            User user = Essentials.getUserMap().getUser(p.getName());

            try {
               user.getTeleport().now(l, false, TeleportCause.PLUGIN);
               return true;
            } catch (Exception var8) {
               return false;
            }
         } else {
            return p.teleport(l);
         }
      } else {
         try {
            if (UtilTypes.checkItem(Pn, "safeBlocks", String.valueOf(l.getBlock().getTypeId()))) {
               Location check = l.clone();

               for(int y = l.getBlockY(); y >= 0; --y) {
                  check.setY((double)y);
                  if (!UtilTypes.checkItem(Pn, "safeBlocks", String.valueOf(check.getBlock().getTypeId()))) {
                     check.setY((double)(y + 1));
                     break;
                  }
               }

               if (UtilTypes.checkItem(Pn, "safeBlocks", String.valueOf(check.getBlock().getRelative(BlockFace.UP).getTypeId()))) {
                  if (enableBack) {
                     User user = Essentials.getUserMap().getUser(p.getName());

                     try {
                        user.getTeleport().now(check, false, TeleportCause.PLUGIN);
                        return true;
                     } catch (Exception var9) {
                        return false;
                     }
                  }

                  return p.teleport(check);
               }
            }

            Location check = l.clone();

            for(int y = l.getBlockY() + 2; y <= 255; ++y) {
               check.setY((double)y);
               if (UtilTypes.checkItem(Pn, "safeBlocks", String.valueOf(check.getBlock().getTypeId())) && UtilTypes.checkItem(Pn, "safeBlocks", String.valueOf(check.getBlock().getRelative(BlockFace.DOWN).getTypeId()))) {
                  if (enableBack) {
                     User user = Essentials.getUserMap().getUser(p.getName());

                     try {
                        user.getTeleport().now(check, false, TeleportCause.PLUGIN);
                        return true;
                     } catch (Exception var10) {
                        return false;
                     }
                  }

                  return p.teleport(check);
               }
            }

            return false;
         } catch (InvalidTypeException e) {
            e.printStackTrace();
            return false;
         } catch (Exception e) {
            e.printStackTrace();
            return false;
         }
      }
   }

   public static boolean checkOnline(CommandSender sender, String name) {
      if (Server.getPlayer(name) == null) {
         if (sender != null) {
            sender.sendMessage(UtilFormat.format(Pn, "notOnline", name));
         }

         return false;
      } else {
         return true;
      }
   }

   public static String getRealName(CommandSender sender, String name) {
      return LIB.getRealName().getRealName(sender, name);
   }

   public static boolean isDay(World w) {
      return Day.isDay(w);
   }

   public static EntityType getEntityType(String s) {
      EntityType entityType = null;

      try {
         entityType = EntityType.fromId(Integer.parseInt(s));
      } catch (NumberFormatException var5) {
         try {
            entityType = (EntityType)EntityType.valueOf(EntityType.class, s);
         } catch (Exception var4) {
         }
      }

      return entityType;
   }

   public static Material getMaterial(String s) {
      try {
         int id = Integer.parseInt(s);
         return Material.getMaterial(id);
      } catch (NumberFormatException var2) {
         return Material.getMaterial(s);
      }
   }

   public static void eject(Entity entity, Location from, Location to) {
      Vector v = to.subtract(from).toVector();
      double length = v.length();
      v.setX(v.getX() / length);
      v.setY(v.getY() / length);
      v.setZ(v.getZ() / length);
      entity.setVelocity(v);
   }

   public static void eject(Entity entity, Location from, Location to, double multiply) {
      Vector v = to.subtract(from).toVector();
      double length = v.length() / multiply;
      v.setX(v.getX() / length);
      v.setY((double)1.5F);
      v.setZ(v.getZ() / length);
      entity.setVelocity(v);
   }

   public static void ejectRandom(Entity entity) {
      Vector v = new Vector((double)1.0F, 0.3, (double)1.0F);
      double d = (double)(Random.nextInt(11) - 5);
      if (d == (double)0.0F) {
         v.setX(0);
      } else {
         v.setX(v.getX() / d);
      }

      d = (double)(Random.nextInt(11) - 5);
      if (d == (double)0.0F) {
         v.setZ(0);
      } else {
         v.setZ(v.getZ() / d);
      }

      double length = v.length();
      v.setX(v.getX() / length);
      v.setZ(v.getZ() / length);
      entity.setVelocity(v);
   }

   public static boolean isValid(String s) {
      return s.matches("^[\\da-zA-Z_]*$");
   }

   public static void sendItemMessage(Player p, String msg, int dur) {
      Im.sendItemMessage(p, msg, dur);
   }

   public static List seperateLines(String s, int maxLength) {
      List<String> result = new LinkedList();

      for(int index = 0; index < s.length(); index += maxLength) {
         result.add(s.substring(index, Math.min(s.length(), index + maxLength)));
      }

      return result;
   }

   public static void seperateLines(List lore, int maxLength) {
      int index = 0;

      while(index < lore.size()) {
         List<String> lore1 = seperateLines((String)lore.get(index), maxLength);
         if (lore1.size() > 1) {
            lore.set(index, (String)lore1.get(0));

            for(int i = 1; i < lore1.size(); ++i) {
               lore.add(index + i, (String)lore1.get(i));
            }

            index += lore1.size();
         } else {
            ++index;
         }
      }

   }

   public static void strikeLightning(Location loc, int range) {
      CraftWorld cw = (CraftWorld)loc.getWorld();
      net.minecraft.server.v1_6_R2.World w = cw.getHandle();
      EntityLightning lightning = new EntityLightning(w, loc.getX(), loc.getY(), loc.getZ());
      Packet71Weather pc = new Packet71Weather(lightning);
      Packet62NamedSoundEffect pc1 = new Packet62NamedSoundEffect("random.explode", loc.getX(), loc.getY(), loc.getZ(), 2.0F, 0.0F);
      Packet62NamedSoundEffect pc2 = new Packet62NamedSoundEffect("ambient.weather.thunder", loc.getX(), loc.getY(), loc.getZ(), 2.0F, 0.0F);

      for(Player p : loc.getWorld().getPlayers()) {
         if (p.getLocation().distance(loc) < (double)range) {
            ((CraftPlayer)p).getHandle().playerConnection.sendPacket(pc);
            ((CraftPlayer)p).getHandle().playerConnection.sendPacket(pc1);
            ((CraftPlayer)p).getHandle().playerConnection.sendPacket(pc2);
         }
      }

   }

   public static void strikeLightningEffect(Location loc, int range) {
      CraftWorld cw = (CraftWorld)loc.getWorld();
      net.minecraft.server.v1_6_R2.World w = cw.getHandle();
      EntityLightning lightning = new EntityLightning(w, loc.getX(), loc.getY(), loc.getZ());
      Packet71Weather pc = new Packet71Weather(lightning);

      for(Player p : loc.getWorld().getPlayers()) {
         if (p.getLocation().distance(loc) < (double)range) {
            ((CraftPlayer)p).getHandle().playerConnection.sendPacket(pc);
         }
      }

   }

   public static void sendMsg(String name, String msg) {
      Player p = Bukkit.getServer().getPlayerExact(name);
      if (p != null && p.isOnline()) {
         p.sendMessage(msg);
      }

   }

   public static void sendMsg(Location l, double range, boolean nearest, String msg) {
      Player p = null;
      double d = (double)0.0F;

      for(Player tar : l.getWorld().getPlayers()) {
         double temp;
         if (tar.isOnline() && (temp = tar.getLocation().distance(l)) <= range) {
            if (!nearest) {
               tar.sendMessage(msg);
            } else if (p == null) {
               p = tar;
               d = temp;
            } else if (temp < d) {
               p = tar;
               d = temp;
            }
         }
      }

      if (p != null) {
         p.sendMessage(msg);
      }

   }

   public static int getDebt(String name) {
      return LIB.getDebt().getDebt(name);
   }

   public static boolean addDebt(String name, int debt, String reason) {
      return LIB.getDebt().addDebt(name, debt, reason);
   }
}
