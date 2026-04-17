package org.hibernate.sql;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.LiteralType;

public class Update {
   private String tableName;
   private String versionColumnName;
   private String where;
   private String assignments;
   private String comment;
   private Map primaryKeyColumns = new LinkedHashMap();
   private Map columns = new LinkedHashMap();
   private Map whereColumns = new LinkedHashMap();
   private Dialect dialect;

   public Update(Dialect dialect) {
      this.dialect = dialect;
   }

   public String getTableName() {
      return this.tableName;
   }

   public Update appendAssignmentFragment(String fragment) {
      if (this.assignments == null) {
         this.assignments = fragment;
      } else {
         this.assignments = this.assignments + ", " + fragment;
      }

      return this;
   }

   public Update setTableName(String tableName) {
      this.tableName = tableName;
      return this;
   }

   public Update setPrimaryKeyColumnNames(String[] columnNames) {
      this.primaryKeyColumns.clear();
      this.addPrimaryKeyColumns(columnNames);
      return this;
   }

   public Update addPrimaryKeyColumns(String[] columnNames) {
      for(int i = 0; i < columnNames.length; ++i) {
         this.addPrimaryKeyColumn(columnNames[i], "?");
      }

      return this;
   }

   public Update addPrimaryKeyColumns(String[] columnNames, boolean[] includeColumns, String[] valueExpressions) {
      for(int i = 0; i < columnNames.length; ++i) {
         if (includeColumns[i]) {
            this.addPrimaryKeyColumn(columnNames[i], valueExpressions[i]);
         }
      }

      return this;
   }

   public Update addPrimaryKeyColumns(String[] columnNames, String[] valueExpressions) {
      for(int i = 0; i < columnNames.length; ++i) {
         this.addPrimaryKeyColumn(columnNames[i], valueExpressions[i]);
      }

      return this;
   }

   public Update addPrimaryKeyColumn(String columnName, String valueExpression) {
      this.primaryKeyColumns.put(columnName, valueExpression);
      return this;
   }

   public Update setVersionColumnName(String versionColumnName) {
      this.versionColumnName = versionColumnName;
      return this;
   }

   public Update setComment(String comment) {
      this.comment = comment;
      return this;
   }

   public Update addColumns(String[] columnNames) {
      for(int i = 0; i < columnNames.length; ++i) {
         this.addColumn(columnNames[i]);
      }

      return this;
   }

   public Update addColumns(String[] columnNames, boolean[] updateable, String[] valueExpressions) {
      for(int i = 0; i < columnNames.length; ++i) {
         if (updateable[i]) {
            this.addColumn(columnNames[i], valueExpressions[i]);
         }
      }

      return this;
   }

   public Update addColumns(String[] columnNames, String valueExpression) {
      for(int i = 0; i < columnNames.length; ++i) {
         this.addColumn(columnNames[i], valueExpression);
      }

      return this;
   }

   public Update addColumn(String columnName) {
      return this.addColumn(columnName, "?");
   }

   public Update addColumn(String columnName, String valueExpression) {
      this.columns.put(columnName, valueExpression);
      return this;
   }

   public Update addColumn(String columnName, Object value, LiteralType type) throws Exception {
      return this.addColumn(columnName, type.objectToSQLString(value, this.dialect));
   }

   public Update addWhereColumns(String[] columnNames) {
      for(int i = 0; i < columnNames.length; ++i) {
         this.addWhereColumn(columnNames[i]);
      }

      return this;
   }

   public Update addWhereColumns(String[] columnNames, String valueExpression) {
      for(int i = 0; i < columnNames.length; ++i) {
         this.addWhereColumn(columnNames[i], valueExpression);
      }

      return this;
   }

   public Update addWhereColumn(String columnName) {
      return this.addWhereColumn(columnName, "=?");
   }

   public Update addWhereColumn(String columnName, String valueExpression) {
      this.whereColumns.put(columnName, valueExpression);
      return this;
   }

   public Update setWhere(String where) {
      this.where = where;
      return this;
   }

   public String toStatementString() {
      StringBuilder buf = new StringBuilder(this.columns.size() * 15 + this.tableName.length() + 10);
      if (this.comment != null) {
         buf.append("/* ").append(this.comment).append(" */ ");
      }

      buf.append("update ").append(this.tableName).append(" set ");
      boolean assignmentsAppended = false;

      for(Iterator iter = this.columns.entrySet().iterator(); iter.hasNext(); assignmentsAppended = true) {
         Map.Entry e = (Map.Entry)iter.next();
         buf.append(e.getKey()).append('=').append(e.getValue());
         if (iter.hasNext()) {
            buf.append(", ");
         }
      }

      if (this.assignments != null) {
         if (assignmentsAppended) {
            buf.append(", ");
         }

         buf.append(this.assignments);
      }

      boolean conditionsAppended = false;
      if (!this.primaryKeyColumns.isEmpty() || this.where != null || !this.whereColumns.isEmpty() || this.versionColumnName != null) {
         buf.append(" where ");
      }

      for(Iterator var6 = this.primaryKeyColumns.entrySet().iterator(); var6.hasNext(); conditionsAppended = true) {
         Map.Entry e = (Map.Entry)var6.next();
         buf.append(e.getKey()).append('=').append(e.getValue());
         if (var6.hasNext()) {
            buf.append(" and ");
         }
      }

      if (this.where != null) {
         if (conditionsAppended) {
            buf.append(" and ");
         }

         buf.append(this.where);
         conditionsAppended = true;
      }

      for(Map.Entry e : this.whereColumns.entrySet()) {
         if (conditionsAppended) {
            buf.append(" and ");
         }

         buf.append(e.getKey()).append(e.getValue());
         conditionsAppended = true;
      }

      if (this.versionColumnName != null) {
         if (conditionsAppended) {
            buf.append(" and ");
         }

         buf.append(this.versionColumnName).append("=?");
      }

      return buf.toString();
   }
}
