package trade;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Util {
   private static ProtocolManager pm = ProtocolLibrary.getProtocolManager();

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

   public static int getEmptySlots(Inventory inv) {
      int sum = 0;

      for(int i = 0; i < inv.getSize(); ++i) {
         if (inv.getItem(i) == null || inv.getItem(i).getTypeId() == 0) {
            ++sum;
         }
      }

      return sum;
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

   public static String saveItem(ItemStack is) {
      if (is == null) {
         return null;
      } else {
         YamlConfiguration config = new YamlConfiguration();
         config.createSection("item", is.serialize());
         Attributes a = new Attributes(is);

         for(int index = 0; index < a.size(); ++index) {
            Attributes.Attribute at = a.get(index);
            config.set("item.attributes.attribute" + index + ".amount", at.getAmount());
            config.set("item.attributes.attribute" + index + ".type", at.getAttributeType().getMinecraftId());
            config.set("item.attributes.attribute" + index + ".name", at.getName());
            config.set("item.attributes.attribute" + index + ".operation", at.getOperation().getId());
            config.set("item.attributes.attribute" + index + ".uuid", at.getUUID().toString());
         }

         return config.saveToString();
      }
   }

   public static ItemStack loadItem(String s) {
      try {
         YamlConfiguration config = new YamlConfiguration();
         config.loadFromString(s);
         ItemStack is = ItemStack.deserialize(((MemorySection)config.get("item")).getValues(true));
         if (is != null) {
            String attrPath = "item.attributes";
            if (config.contains(attrPath)) {
               Attributes a = new Attributes(is);
               MemorySection ms = (MemorySection)config.get(attrPath);

               for(String key : ms.getValues(false).keySet()) {
                  Attributes.Attribute.Builder b = Attributes.Attribute.newBuilder();
                  if (ms.contains(key + ".uuid")) {
                     b.uuid(UUID.fromString(ms.getString(key + ".uuid")));
                  }

                  if (ms.contains(key + ".amount")) {
                     b.amount((double)ms.getInt(key + ".amount"));
                  }

                  if (ms.contains(key + ".name")) {
                     b.name(ms.getString(key + ".name"));
                  }

                  if (ms.contains(key + ".operation")) {
                     b.operation(Attributes.Operation.fromId(ms.getInt(key + ".operation")));
                  }

                  if (ms.contains(key + ".type")) {
                     b.type(Attributes.AttributeType.fromId(ms.getString(key + ".type")));
                  }

                  a.add(b.build());
               }

               is = a.getStack();
            }
         }

         return is;
      } catch (Exception var9) {
         return null;
      }
   }

   public static void sendMsg(Player p, String msg) {
      try {
         PacketContainer pc = new PacketContainer(3);
         pc.getStrings().write(0, "{\"text\":\"" + msg + "\"}");
         pm.sendServerPacket(p, pc, false);
      } catch (InvocationTargetException var3) {
      }

   }
}
