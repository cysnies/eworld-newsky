package org.hibernate.sql;

import java.util.Map;

public class ANSICaseFragment extends CaseFragment {
   public String toFragmentString() {
      StringBuilder buf = (new StringBuilder(this.cases.size() * 15 + 10)).append("case");

      for(Map.Entry me : this.cases.entrySet()) {
         buf.append(" when ").append(me.getKey()).append(" is not null then ").append(me.getValue());
      }

      buf.append(" end");
      if (this.returnColumnName != null) {
         buf.append(" as ").append(this.returnColumnName);
      }

      return buf.toString();
   }
}
