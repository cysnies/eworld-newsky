package org.maxgamer.QuickShop.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

public class MySQLCore implements DatabaseCore {
   private String url;
   private Properties info = new Properties();
   private static final int MAX_CONNECTIONS = 8;
   private static ArrayList pool = new ArrayList();

   public MySQLCore(String host, String user, String pass, String database, String port) {
      this.info.put("autoReconnect", "true");
      this.info.put("user", user);
      this.info.put("password", pass);
      this.info.put("useUnicode", "true");
      this.info.put("characterEncoding", "utf8");
      this.url = "jdbc:mysql://" + host + ":" + port + "/" + database;

      for(int i = 0; i < 8; ++i) {
         pool.add((Object)null);
      }

   }

   public Connection getConnection() {
      for(int i = 0; i < 8; ++i) {
         Connection connection = (Connection)pool.get(i);

         try {
            if (connection != null && !connection.isClosed() && connection.isValid(10)) {
               return connection;
            }

            connection = DriverManager.getConnection(this.url, this.info);
            pool.set(i, connection);
            return connection;
         } catch (SQLException e) {
            e.printStackTrace();
         }
      }

      return null;
   }

   public void queue(BufferStatement bs) {
      try {
         Connection con;
         for(con = this.getConnection(); con == null; this.getConnection()) {
            try {
               Thread.sleep(15L);
            } catch (InterruptedException var4) {
            }
         }

         PreparedStatement ps = bs.prepareStatement(con);
         ps.execute();
         ps.close();
      } catch (SQLException e) {
         e.printStackTrace();
      }
   }

   public void close() {
   }

   public void flush() {
   }
}
