package org.maxgamer.QuickShop.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class Database {
   private DatabaseCore core;

   public Database(DatabaseCore core) throws ConnectionException {
      try {
         try {
            if (!core.getConnection().isValid(10)) {
               throw new ConnectionException("Database doesn not appear to be valid!");
            }
         } catch (AbstractMethodError var3) {
         }
      } catch (SQLException e) {
         throw new ConnectionException(e.getMessage());
      }

      this.core = core;
   }

   public DatabaseCore getCore() {
      return this.core;
   }

   public Connection getConnection() {
      return this.core.getConnection();
   }

   public void execute(String query, Object... objs) {
      BufferStatement bs = new BufferStatement(query, objs);
      this.core.queue(bs);
   }

   public boolean hasTable(String table) throws SQLException {
      ResultSet rs = this.getConnection().getMetaData().getTables((String)null, (String)null, "%", (String[])null);

      while(rs.next()) {
         if (table.equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
            rs.close();
            return true;
         }
      }

      rs.close();
      return false;
   }

   public void close() {
      this.core.close();
   }

   public boolean hasColumn(String table, String column) throws SQLException {
      if (!this.hasTable(table)) {
         return false;
      } else {
         String query = "SELECT * FROM " + table + " LIMIT 0,1";

         try {
            PreparedStatement ps = this.getConnection().prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
               rs.getString(column);
               return true;
            } else {
               return false;
            }
         } catch (SQLException var6) {
            return false;
         }
      }
   }

   public void copyTo(Database db) throws SQLException {
      ResultSet rs = this.getConnection().getMetaData().getTables((String)null, (String)null, "%", (String[])null);
      List<String> tables = new LinkedList();

      while(rs.next()) {
         tables.add(rs.getString("TABLE_NAME"));
      }

      rs.close();
      this.core.flush();

      for(String table : tables) {
         if (!table.toLowerCase().startsWith("sqlite_autoindex_")) {
            System.out.println("Copying " + table);
            db.getConnection().prepareStatement("DELETE FROM " + table).execute();
            rs = this.getConnection().prepareStatement("SELECT * FROM " + table).executeQuery();
            int n = 0;
            String query = "INSERT INTO " + table + " VALUES (";
            query = query + "?";

            for(int i = 2; i <= rs.getMetaData().getColumnCount(); ++i) {
               query = query + ", ?";
            }

            query = query + ")";
            PreparedStatement ps = db.getConnection().prepareStatement(query);

            while(rs.next()) {
               ++n;

               for(int i = 1; i <= rs.getMetaData().getColumnCount(); ++i) {
                  ps.setObject(i, rs.getObject(i));
               }

               ps.addBatch();
               if (n % 100 == 0) {
                  ps.executeBatch();
                  System.out.println(n + " records copied...");
               }
            }

            ps.executeBatch();
            rs.close();
         }
      }

      db.getConnection().close();
      this.getConnection().close();
   }

   public static class ConnectionException extends Exception {
      private static final long serialVersionUID = 8348749992936357317L;

      public ConnectionException(String msg) {
         super(msg);
      }
   }
}
