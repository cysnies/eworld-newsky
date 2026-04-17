package com.earth2me.essentials.storage;

import com.earth2me.essentials.IEssentials;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.Bukkit;

public abstract class AbstractDelayedYamlFileReader implements Runnable {
   private final transient File file;
   private final transient Class clazz;
   protected final transient IEssentials plugin;

   public AbstractDelayedYamlFileReader(IEssentials ess, File file, Class clazz) {
      this.file = file;
      this.clazz = clazz;
      this.plugin = ess;
      ess.runTaskAsynchronously(this);
   }

   public abstract void onStart();

   public void run() {
      this.onStart();

      try {
         FileReader reader = new FileReader(this.file);

         try {
            T object = (T)(new YamlStorageReader(reader, this.plugin)).load(this.clazz);
            this.onSuccess(object);
         } finally {
            try {
               reader.close();
            } catch (IOException ex) {
               Bukkit.getLogger().log(Level.SEVERE, "File can't be closed: " + this.file.toString(), ex);
            }

         }
      } catch (FileNotFoundException var12) {
         this.onException();
         if (this.plugin.getSettings() == null || this.plugin.getSettings().isDebug()) {
            Bukkit.getLogger().log(Level.INFO, "File not found: " + this.file.toString());
         }
      } catch (ObjectLoadException ex) {
         this.onException();
         Bukkit.getLogger().log(Level.SEVERE, "File broken: " + this.file.toString(), ex.getCause());
      }

   }

   public abstract void onSuccess(StorageObject var1);

   public abstract void onException();
}
