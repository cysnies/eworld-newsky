package org.hibernate.sql;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.LiteralType;

public class Insert {
   private Dialect dialect;
   private String tableName;
   private String comment;
   private Map columns = new LinkedHashMap();

   public Insert(Dialect dialect) {
      this.dialect = dialect;
   }

   protected Dialect getDialect() {
      return this.dialect;
   }

   public Insert setComment(String comment) {
      this.comment = comment;
      return this;
   }

   public Insert addColumn(String columnName) {
      return this.addColumn(columnName, "?");
   }

   public Insert addColumns(String[] columnNames) {
      for(int i = 0; i < columnNames.length; ++i) {
         this.addColumn(columnNames[i]);
      }

      return this;
   }

   public Insert addColumns(String[] columnNames, boolean[] insertable) {
      for(int i = 0; i < columnNames.length; ++i) {
         if (insertable[i]) {
            this.addColumn(columnNames[i]);
         }
      }

      return this;
   }

   public Insert addColumns(String[] columnNames, boolean[] insertable, String[] valueExpressions) {
      for(int i = 0; i < columnNames.length; ++i) {
         if (insertable[i]) {
            this.addColumn(columnNames[i], valueExpressions[i]);
         }
      }

      return this;
   }

   public Insert addColumn(String columnName, String valueExpression) {
      this.columns.put(columnName, valueExpression);
      return this;
   }

   public Insert addColumn(String columnName, Object value, LiteralType type) throws Exception {
      return this.addColumn(columnName, type.objectToSQLString(value, this.dialect));
   }

   public Insert addIdentityColumn(String columnName) {
      String value = this.dialect.getIdentityInsertString();
      if (value != null) {
         this.addColumn(columnName, value);
      }

      return this;
   }

   public Insert setTableName(String tableName) {
      this.tableName = tableName;
      return this;
   }

   public String toStatementString() {
      StringBuilder buf = new StringBuilder(this.columns.size() * 15 + this.tableName.length() + 10);
      if (this.comment != null) {
         buf.append("/* ").append(this.comment).append(" */ ");
      }

      buf.append("insert into ").append(this.tableName);
      if (this.columns.size() == 0) {
         buf.append(' ').append(this.dialect.getNoColumnsInsertString());
      } else {
         buf.append(" (");
         Iterator iter = this.columns.keySet().iterator();

         while(iter.hasNext()) {
            buf.append(iter.next());
            if (iter.hasNext()) {
               buf.append(", ");
            }
         }

         buf.append(") values (");
         iter = this.columns.values().iterator();

         while(iter.hasNext()) {
            buf.append(iter.next());
            if (iter.hasNext()) {
               buf.append(", ");
            }
         }

         buf.append(')');
      }

      return buf.toString();
   }
}
