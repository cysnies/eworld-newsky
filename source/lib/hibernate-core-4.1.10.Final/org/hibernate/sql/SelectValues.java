package org.hibernate.sql;

import java.util.ArrayList;
import java.util.HashSet;
import org.hibernate.dialect.Dialect;
import org.jboss.logging.Logger;

public class SelectValues {
   private static final Logger log = Logger.getLogger(SelectValues.class);
   private final Dialect dialect;
   private final ArrayList selectValueList = new ArrayList();

   public SelectValues(Dialect dialect) {
      this.dialect = dialect;
   }

   public SelectValues addColumns(String qualifier, String[] columnNames, String[] columnAliases) {
      for(int i = 0; i < columnNames.length; ++i) {
         if (columnNames[i] != null) {
            this.addColumn(qualifier, columnNames[i], columnAliases[i]);
         }
      }

      return this;
   }

   public SelectValues addColumn(String qualifier, String columnName, String columnAlias) {
      this.selectValueList.add(new SelectValue(qualifier, columnName, columnAlias));
      return this;
   }

   public SelectValues addParameter(int jdbcTypeCode, int length) {
      String selectExpression = this.dialect.requiresCastingOfParametersInSelectClause() ? this.dialect.cast("?", jdbcTypeCode, length) : "?";
      this.selectValueList.add(new SelectValue((String)null, selectExpression, (String)null));
      return this;
   }

   public SelectValues addParameter(int jdbcTypeCode, int precision, int scale) {
      String selectExpression = this.dialect.requiresCastingOfParametersInSelectClause() ? this.dialect.cast("?", jdbcTypeCode, precision, scale) : "?";
      this.selectValueList.add(new SelectValue((String)null, selectExpression, (String)null));
      return this;
   }

   public String render() {
      StringBuilder buf = new StringBuilder(this.selectValueList.size() * 10);
      HashSet<String> uniqueAliases = new HashSet();
      boolean firstExpression = true;

      for(SelectValue selectValue : this.selectValueList) {
         if (selectValue.alias != null && !uniqueAliases.add(selectValue.alias)) {
            log.debug("Skipping select-value with non-unique alias");
         } else {
            if (firstExpression) {
               firstExpression = false;
            } else {
               buf.append(", ");
            }

            if (selectValue.qualifier != null) {
               buf.append(selectValue.qualifier).append('.');
            }

            buf.append(selectValue.value);
            if (selectValue.alias != null) {
               buf.append(" as ").append(selectValue.alias);
            }
         }
      }

      return buf.toString();
   }

   private static class SelectValue {
      private final String qualifier;
      private final String value;
      private final String alias;

      private SelectValue(String qualifier, String value, String alias) {
         this.qualifier = qualifier;
         this.value = value;
         this.alias = alias;
      }
   }
}
