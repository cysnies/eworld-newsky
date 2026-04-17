package org.maxgamer.QuickShop.Database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;

public class SQLiteCore implements DatabaseCore {
   private Connection connection;
   private File dbFile;
   private volatile Thread watcher;
   private volatile LinkedList queue = new LinkedList();

   public SQLiteCore(File dbFile) {
      this.dbFile = dbFile;
   }

   public Connection getConnection() {
      try {
         if (this.connection != null && !this.connection.isClosed()) {
            return this.connection;
         }
      } catch (SQLException e) {
         e.printStackTrace();
      }

      if (this.dbFile.exists()) {
         try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.dbFile);
            return this.connection;
         } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
         } catch (SQLException e) {
            e.printStackTrace();
            return null;
         }
      } else {
         try {
            this.dbFile.createNewFile();
            return this.getConnection();
         } catch (IOException e) {
            e.printStackTrace();
            return null;
         }
      }
   }

   public void queue(BufferStatement bs) {
      synchronized(this.queue) {
         this.queue.add(bs);
      }

      if (this.watcher == null || !this.watcher.isAlive()) {
         this.startWatcher();
      }

   }

   public void flush() {
      while(!this.queue.isEmpty()) {
         BufferStatement bs;
         synchronized(this.queue) {
            bs = (BufferStatement)this.queue.removeFirst();
         }

         synchronized(this.dbFile) {
            try {
               PreparedStatement ps = bs.prepareStatement(this.getConnection());
               ps.execute();
               ps.close();
            } catch (SQLException e) {
               e.printStackTrace();
            }
         }
      }

   }

   public void close() {
      this.flush();
   }

   private void startWatcher() {
      this.watcher = new Thread() {
         public void run() {
            try {
               Thread.sleep(30000L);
            } catch (InterruptedException var2) {
            }

            SQLiteCore.this.flush();
         }
      };
      this.watcher.start();
   }
}
