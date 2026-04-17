package com.earth2me.essentials;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

public class Backup implements Runnable {
   private static final Logger LOGGER = Logger.getLogger("Minecraft");
   private final transient Server server;
   private final transient IEssentials ess;
   private transient boolean running = false;
   private transient int taskId = -1;
   private transient boolean active = false;

   public Backup(IEssentials ess) {
      this.ess = ess;
      this.server = ess.getServer();
      if (this.server.getOnlinePlayers().length > 0) {
         this.startTask();
      }

   }

   void onPlayerJoin() {
      this.startTask();
   }

   private void startTask() {
      if (!this.running) {
         long interval = this.ess.getSettings().getBackupInterval() * 1200L;
         if (interval < 1200L) {
            return;
         }

         this.taskId = this.ess.scheduleSyncRepeatingTask(this, interval, interval);
         this.running = true;
      }

   }

   public void run() {
      if (!this.active) {
         this.active = true;
         final String command = this.ess.getSettings().getBackupCommand();
         if (command != null && !"".equals(command)) {
            if ("save-all".equalsIgnoreCase(command)) {
               CommandSender cs = this.server.getConsoleSender();
               this.server.dispatchCommand(cs, "save-all");
               this.active = false;
            } else {
               LOGGER.log(Level.INFO, I18n._("backupStarted"));
               final CommandSender cs = this.server.getConsoleSender();
               this.server.dispatchCommand(cs, "save-all");
               this.server.dispatchCommand(cs, "save-off");
               this.ess.runTaskAsynchronously(new Runnable() {
                  public void run() {
                     try {
                        ProcessBuilder childBuilder = new ProcessBuilder(new String[]{command});
                        childBuilder.redirectErrorStream(true);
                        childBuilder.directory(Backup.this.ess.getDataFolder().getParentFile().getParentFile());
                        Process child = childBuilder.start();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(child.getInputStream()));

                        try {
                           child.waitFor();

                           String line;
                           do {
                              line = reader.readLine();
                              if (line != null) {
                                 Backup.LOGGER.log(Level.INFO, line);
                              }
                           } while(line != null);
                        } finally {
                           reader.close();
                        }
                     } catch (InterruptedException ex) {
                        Backup.LOGGER.log(Level.SEVERE, (String)null, ex);
                     } catch (IOException ex) {
                        Backup.LOGGER.log(Level.SEVERE, (String)null, ex);
                     } finally {
                        Backup.this.ess.scheduleSyncDelayedTask(new Runnable() {
                           public void run() {
                              Backup.this.server.dispatchCommand(cs, "save-on");
                              if (Backup.this.server.getOnlinePlayers().length == 0) {
                                 Backup.this.running = false;
                                 if (Backup.this.taskId != -1) {
                                    Backup.this.server.getScheduler().cancelTask(Backup.this.taskId);
                                 }
                              }

                              Backup.this.active = false;
                              Backup.LOGGER.log(Level.INFO, I18n._("backupFinished"));
                           }
                        });
                     }

                  }
               });
            }
         }
      }
   }
}
