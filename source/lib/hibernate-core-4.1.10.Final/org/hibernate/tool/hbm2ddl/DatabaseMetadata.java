package org.hibernate.tool.hbm2ddl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.exception.spi.SQLExceptionConverter;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.Table;
import org.jboss.logging.Logger;

public class DatabaseMetadata {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, DatabaseMetaData.class.getName());
   private final Map tables;
   private final Set sequences;
   private final boolean extras;
   private DatabaseMetaData meta;
   private SQLExceptionConverter sqlExceptionConverter;
   private static final String[] TYPES = new String[]{"TABLE", "VIEW"};

   public DatabaseMetadata(Connection connection, Dialect dialect) throws SQLException {
      this(connection, dialect, true);
   }

   public DatabaseMetadata(Connection connection, Dialect dialect, boolean extras) throws SQLException {
      this.tables = new HashMap();
      this.sequences = new HashSet();
      this.sqlExceptionConverter = dialect.buildSQLExceptionConverter();
      this.meta = connection.getMetaData();
      this.extras = extras;
      this.initSequences(connection, dialect);
   }

   public TableMetadata getTableMetadata(String name, String schema, String catalog, boolean isQuoted) throws HibernateException {
      Object identifier = this.identifier(catalog, schema, name);
      TableMetadata table = (TableMetadata)this.tables.get(identifier);
      if (table != null) {
         return table;
      } else {
         try {
            ResultSet rs = null;

            TableMetadata var9;
            try {
               if (isQuoted && this.meta.storesMixedCaseQuotedIdentifiers()) {
                  rs = this.meta.getTables(catalog, schema, name, TYPES);
               } else if ((!isQuoted || !this.meta.storesUpperCaseQuotedIdentifiers()) && (isQuoted || !this.meta.storesUpperCaseIdentifiers())) {
                  if ((!isQuoted || !this.meta.storesLowerCaseQuotedIdentifiers()) && (isQuoted || !this.meta.storesLowerCaseIdentifiers())) {
                     rs = this.meta.getTables(catalog, schema, name, TYPES);
                  } else {
                     rs = this.meta.getTables(StringHelper.toLowerCase(catalog), StringHelper.toLowerCase(schema), StringHelper.toLowerCase(name), TYPES);
                  }
               } else {
                  rs = this.meta.getTables(StringHelper.toUpperCase(catalog), StringHelper.toUpperCase(schema), StringHelper.toUpperCase(name), TYPES);
               }

               String tableName;
               do {
                  if (!rs.next()) {
                     LOG.tableNotFound(name);
                     Object var16 = null;
                     return (TableMetadata)var16;
                  }

                  tableName = rs.getString("TABLE_NAME");
               } while(!name.equalsIgnoreCase(tableName));

               table = new TableMetadata(rs, this.meta, this.extras);
               this.tables.put(identifier, table);
               var9 = table;
            } finally {
               if (rs != null) {
                  rs.close();
               }

            }

            return var9;
         } catch (SQLException sqlException) {
            throw (new SqlExceptionHelper(this.sqlExceptionConverter)).convert(sqlException, "could not get table metadata: " + name);
         }
      }
   }

   private Object identifier(String catalog, String schema, String name) {
      return Table.qualify(catalog, schema, name);
   }

   private void initSequences(Connection connection, Dialect dialect) throws SQLException {
      if (dialect.supportsSequences()) {
         String sql = dialect.getQuerySequencesString();
         if (sql != null) {
            Statement statement = null;
            ResultSet rs = null;

            try {
               statement = connection.createStatement();
               rs = statement.executeQuery(sql);

               while(rs.next()) {
                  this.sequences.add(rs.getString(1).toLowerCase().trim());
               }
            } finally {
               if (rs != null) {
                  rs.close();
               }

               if (statement != null) {
                  statement.close();
               }

            }
         }
      }

   }

   public boolean isSequence(Object key) {
      if (key instanceof String) {
         String[] strings = StringHelper.split(".", (String)key);
         return this.sequences.contains(strings[strings.length - 1].toLowerCase());
      } else {
         return false;
      }
   }

   public boolean isTable(Object key) throws HibernateException {
      if (key instanceof String) {
         Table tbl = new Table((String)key);
         if (this.getTableMetadata(tbl.getName(), tbl.getSchema(), tbl.getCatalog(), tbl.isQuoted()) != null) {
            return true;
         }

         String[] strings = StringHelper.split(".", (String)key);
         if (strings.length == 3) {
            tbl = new Table(strings[2]);
            tbl.setCatalog(strings[0]);
            tbl.setSchema(strings[1]);
            return this.getTableMetadata(tbl.getName(), tbl.getSchema(), tbl.getCatalog(), tbl.isQuoted()) != null;
         }

         if (strings.length == 2) {
            tbl = new Table(strings[1]);
            tbl.setSchema(strings[0]);
            return this.getTableMetadata(tbl.getName(), tbl.getSchema(), tbl.getCatalog(), tbl.isQuoted()) != null;
         }
      }

      return false;
   }

   public String toString() {
      return "DatabaseMetadata" + this.tables.keySet().toString() + this.sequences.toString();
   }
}
