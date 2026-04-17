package com.earth2me.essentials.storage;

import com.earth2me.essentials.IEssentials;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.logging.Level;
import org.bukkit.Bukkit;

public abstract class AbstractDelayedYamlFileWriter implements Runnable {
   private final transient File file;

   public AbstractDelayedYamlFileWriter(IEssentials ess, File file) {
      this.file = file;
      ess.runTaskAsynchronously(this);
   }

   public abstract StorageObject getObject();

   public void run() {
      PrintWriter pw = null;

      try {
         StorageObject object = this.getObject();
         File folder = this.file.getParentFile();
         if (!folder.exists()) {
            folder.mkdirs();
         }

         pw = new PrintWriter(this.file);
         (new YamlStorageWriter(pw)).save(object);
      } catch (FileNotFoundException ex) {
         Bukkit.getLogger().log(Level.SEVERE, this.file.toString(), ex);
      } finally {
         this.onFinish();
         if (pw != null) {
            pw.close();
         }

      }

   }

   public abstract void onFinish();
}
