package org.hibernate.sql;

import java.util.Iterator;
import java.util.Map;

public class DecodeCaseFragment extends CaseFragment {
   public String toFragmentString() {
      StringBuilder buf = (new StringBuilder(this.cases.size() * 15 + 10)).append("decode(");
      Iterator iter = this.cases.entrySet().iterator();

      while(iter.hasNext()) {
         Map.Entry me = (Map.Entry)iter.next();
         if (iter.hasNext()) {
            buf.append(", ").append(me.getKey()).append(", ").append(me.getValue());
         } else {
            buf.insert(7, me.getKey()).append(", ").append(me.getValue());
         }
      }

      buf.append(')');
      if (this.returnColumnName != null) {
         buf.append(" as ").append(this.returnColumnName);
      }

      return buf.toString();
   }
}
