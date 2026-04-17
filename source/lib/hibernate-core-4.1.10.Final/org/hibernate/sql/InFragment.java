package org.hibernate.sql;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.internal.util.StringHelper;

public class InFragment {
   public static final String NULL = "null";
   public static final String NOT_NULL = "not null";
   private String columnName;
   private List values = new ArrayList();

   public InFragment addValue(Object value) {
      this.values.add(value);
      return this;
   }

   public InFragment setColumn(String columnName) {
      this.columnName = columnName;
      return this;
   }

   public InFragment setColumn(String alias, String columnName) {
      this.columnName = StringHelper.qualify(alias, columnName);
      return this.setColumn(this.columnName);
   }

   public InFragment setFormula(String alias, String formulaTemplate) {
      this.columnName = StringHelper.replace(formulaTemplate, "$PlaceHolder$", alias);
      return this.setColumn(this.columnName);
   }

   public String toFragmentString() {
      if (this.values.size() == 0) {
         return "1=2";
      } else {
         StringBuilder buf = new StringBuilder(this.values.size() * 5);
         if (this.values.size() == 1) {
            Object value = this.values.get(0);
            buf.append(this.columnName);
            if ("null".equals(value)) {
               buf.append(" is null");
            } else if ("not null".equals(value)) {
               buf.append(" is not null");
            } else {
               buf.append('=').append(value);
            }

            return buf.toString();
         } else {
            boolean allowNull = false;

            for(Object value : this.values) {
               if ("null".equals(value)) {
                  allowNull = true;
               } else if ("not null".equals(value)) {
                  throw new IllegalArgumentException("not null makes no sense for in expression");
               }
            }

            if (allowNull) {
               buf.append('(').append(this.columnName).append(" is null or ").append(this.columnName).append(" in (");
            } else {
               buf.append(this.columnName).append(" in (");
            }

            for(Object value : this.values) {
               if (!"null".equals(value)) {
                  buf.append(value);
                  buf.append(", ");
               }
            }

            buf.setLength(buf.length() - 2);
            if (allowNull) {
               buf.append("))");
            } else {
               buf.append(')');
            }

            return buf.toString();
         }
      }
   }
}
