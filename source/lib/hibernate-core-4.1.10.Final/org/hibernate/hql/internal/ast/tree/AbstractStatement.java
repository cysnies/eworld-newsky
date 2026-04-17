package org.hibernate.hql.internal.ast.tree;

import java.util.Iterator;

public abstract class AbstractStatement extends HqlSqlWalkerNode implements DisplayableNode, Statement {
   public String getDisplayText() {
      StringBuilder buf = new StringBuilder();
      if (this.getWalker().getQuerySpaces().size() > 0) {
         buf.append(" querySpaces (");
         Iterator iterator = this.getWalker().getQuerySpaces().iterator();

         while(iterator.hasNext()) {
            buf.append(iterator.next());
            if (iterator.hasNext()) {
               buf.append(",");
            }
         }

         buf.append(")");
      }

      return buf.toString();
   }
}
