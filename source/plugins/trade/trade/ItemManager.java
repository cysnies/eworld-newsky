package trade;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Properties;
import org.bukkit.inventory.ItemStack;

public class ItemManager {
   private String savePath;
   private HashMap itemHash = new HashMap();

   public ItemManager(Main main) {
      this.savePath = main.getDataFolder().getAbsolutePath() + File.separator + "save";
      (new File(this.savePath)).mkdirs();
      this.loadData();
   }

   public ItemStack getItem(String name) {
      return (ItemStack)this.itemHash.get(name);
   }

   public void save(String name, ItemStack is) {
      this.itemHash.put(name, is);
      String data = Util.saveItem(is);
      if (data != null) {
         Properties pro = new Properties();
         pro.setProperty("data", data);
         File file = new File(this.savePath + File.separator + name + ".properties");
         FileOutputStream fos = null;
         OutputStreamWriter osw = null;

         try {
            file.createNewFile();
            fos = new FileOutputStream(file);
            osw = new OutputStreamWriter(fos, Charset.forName("utf-8"));
            pro.store(osw, "item");
         } catch (Exception var11) {
         }

         try {
            fos.close();
         } catch (Exception var10) {
         }

         try {
            osw.close();
         } catch (Exception var9) {
         }
      }

   }

   public void delete(String name) {
      if (this.itemHash.remove(name) != null) {
         (new File(this.savePath + File.separator + name + ".properties")).delete();
      }

   }

   private void loadData() {
      File dir = new File(this.savePath);
      if (dir.exists() && dir.isDirectory()) {
         File[] var5;
         for(File file : var5 = dir.listFiles()) {
            try {
               String name = file.getName().split("\\.")[0];
               if (name != null && !name.trim().isEmpty()) {
                  this.load(name);
               }
            } catch (Exception var7) {
            }
         }
      }

   }

   private void load(String name) {
      Properties pro = new Properties();
      File file = new File(this.savePath + File.separator + name + ".properties");
      FileInputStream fis = null;
      InputStreamReader isr = null;

      try {
         fis = new FileInputStream(file);
         isr = new InputStreamReader(fis, Charset.forName("utf-8"));
         pro.load(isr);
         ItemStack is = Util.loadItem(pro.getProperty("data"));
         if (is != null) {
            this.itemHash.put(name, is);
         }
      } catch (Exception var9) {
      }

      try {
         fis.close();
      } catch (Exception var8) {
      }

      try {
         isr.close();
      } catch (Exception var7) {
      }

   }
}
