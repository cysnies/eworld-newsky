package org.hibernate.sql;

import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.internal.util.StringHelper;

public abstract class CaseFragment {
   protected String returnColumnName;
   protected Map cases = new LinkedHashMap();

   public abstract String toFragmentString();

   public CaseFragment setReturnColumnName(String returnColumnName) {
      this.returnColumnName = returnColumnName;
      return this;
   }

   public CaseFragment setReturnColumnName(String returnColumnName, String suffix) {
      return this.setReturnColumnName((new Alias(suffix)).toAliasString(returnColumnName));
   }

   public CaseFragment addWhenColumnNotNull(String alias, String columnName, String value) {
      this.cases.put(StringHelper.qualify(alias, columnName), value);
      return this;
   }
}
