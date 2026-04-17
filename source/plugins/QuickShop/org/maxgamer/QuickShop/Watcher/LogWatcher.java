package org.maxgamer.QuickShop.Watcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import org.bukkit.scheduler.BukkitTask;
import org.maxgamer.QuickShop.QuickShop;

public class LogWatcher implements Runnable {
   private PrintStream ps;
   private ArrayList logs = new ArrayList(5);
   public BukkitTask task;

   public LogWatcher(QuickShop plugin, File log) {
      try {
         if (!log.exists()) {
            log.createNewFile();
         }

         FileOutputStream fos = new FileOutputStream(log, true);
         this.ps = new PrintStream(fos);
      } catch (FileNotFoundException e) {
         e.printStackTrace();
         plugin.getLogger().severe("Log file not found!");
      } catch (IOException e) {
         e.printStackTrace();
         plugin.getLogger().severe("Could not create log file!");
      }

   }

   public void run() {
      synchronized(this.logs) {
         for(String s : this.logs) {
            this.ps.println(s);
         }

         this.logs.clear();
      }
   }

   public void add(String s) {
      synchronized(this.logs) {
         this.logs.add(s);
      }
   }

   public void close() {
      this.ps.close();
   }
}
