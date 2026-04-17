package org.hibernate.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.dialect.Dialect;

public class SimpleSelect {
   private String tableName;
   private String orderBy;
   private Dialect dialect;
   private LockOptions lockOptions;
   private String comment;
   private List columns;
   private Map aliases;
   private List whereTokens;

   public SimpleSelect(Dialect dialect) {
      this.lockOptions = new LockOptions(LockMode.READ);
      this.columns = new ArrayList();
      this.aliases = new HashMap();
      this.whereTokens = new ArrayList();
      this.dialect = dialect;
   }

   public SimpleSelect addColumns(String[] columnNames, String[] columnAliases) {
      for(int i = 0; i < columnNames.length; ++i) {
         if (columnNames[i] != null) {
            this.addColumn(columnNames[i], columnAliases[i]);
         }
      }

      return this;
   }

   public SimpleSelect addColumns(String[] columns, String[] aliases, boolean[] ignore) {
      for(int i = 0; i < ignore.length; ++i) {
         if (!ignore[i] && columns[i] != null) {
            this.addColumn(columns[i], aliases[i]);
         }
      }

      return this;
   }

   public SimpleSelect addColumns(String[] columnNames) {
      for(int i = 0; i < columnNames.length; ++i) {
         if (columnNames[i] != null) {
            this.addColumn(columnNames[i]);
         }
      }

      return this;
   }

   public SimpleSelect addColumn(String columnName) {
      this.columns.add(columnName);
      return this;
   }

   public SimpleSelect addColumn(String columnName, String alias) {
      this.columns.add(columnName);
      this.aliases.put(columnName, alias);
      return this;
   }

   public SimpleSelect setTableName(String tableName) {
      this.tableName = tableName;
      return this;
   }

   public SimpleSelect setLockOptions(LockOptions lockOptions) {
      LockOptions.copy(lockOptions, this.lockOptions);
      return this;
   }

   public SimpleSelect setLockMode(LockMode lockMode) {
      this.lockOptions.setLockMode(lockMode);
      return this;
   }

   public SimpleSelect addWhereToken(String token) {
      this.whereTokens.add(token);
      return this;
   }

   private void and() {
      if (this.whereTokens.size() > 0) {
         this.whereTokens.add("and");
      }

   }

   public SimpleSelect addCondition(String lhs, String op, String rhs) {
      this.and();
      this.whereTokens.add(lhs + ' ' + op + ' ' + rhs);
      return this;
   }

   public SimpleSelect addCondition(String lhs, String condition) {
      this.and();
      this.whereTokens.add(lhs + ' ' + condition);
      return this;
   }

   public SimpleSelect addCondition(String[] lhs, String op, String[] rhs) {
      for(int i = 0; i < lhs.length; ++i) {
         this.addCondition(lhs[i], op, rhs[i]);
      }

      return this;
   }

   public SimpleSelect addCondition(String[] lhs, String condition) {
      for(int i = 0; i < lhs.length; ++i) {
         if (lhs[i] != null) {
            this.addCondition(lhs[i], condition);
         }
      }

      return this;
   }

   public String toStatementString() {
      StringBuilder buf = new StringBuilder(this.columns.size() * 10 + this.tableName.length() + this.whereTokens.size() * 10 + 10);
      if (this.comment != null) {
         buf.append("/* ").append(this.comment).append(" */ ");
      }

      buf.append("select ");
      Set uniqueColumns = new HashSet();
      Iterator iter = this.columns.iterator();
      boolean appendComma = false;

      while(iter.hasNext()) {
         String col = (String)iter.next();
         String alias = (String)this.aliases.get(col);
         if (uniqueColumns.add(alias == null ? col : alias)) {
            if (appendComma) {
               buf.append(", ");
            }

            buf.append(col);
            if (alias != null && !alias.equals(col)) {
               buf.append(" as ").append(alias);
            }

            appendComma = true;
         }
      }

      buf.append(" from ").append(this.dialect.appendLockHint(this.lockOptions, this.tableName));
      if (this.whereTokens.size() > 0) {
         buf.append(" where ").append(this.toWhereClause());
      }

      if (this.orderBy != null) {
         buf.append(this.orderBy);
      }

      if (this.lockOptions != null) {
         buf.append(this.dialect.getForUpdateString(this.lockOptions));
      }

      return this.dialect.transformSelectString(buf.toString());
   }

   public String toWhereClause() {
      StringBuilder buf = new StringBuilder(this.whereTokens.size() * 5);
      Iterator iter = this.whereTokens.iterator();

      while(iter.hasNext()) {
         buf.append(iter.next());
         if (iter.hasNext()) {
            buf.append(' ');
         }
      }

      return buf.toString();
   }

   public SimpleSelect setOrderBy(String orderBy) {
      this.orderBy = orderBy;
      return this;
   }

   public SimpleSelect setComment(String comment) {
      this.comment = comment;
      return this;
   }
}
