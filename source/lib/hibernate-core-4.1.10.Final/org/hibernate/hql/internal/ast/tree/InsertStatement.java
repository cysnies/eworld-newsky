package org.hibernate.hql.internal.ast.tree;

import org.hibernate.QueryException;

public class InsertStatement extends AbstractStatement {
   public int getStatementType() {
      return 29;
   }

   public boolean needsExecutor() {
      return true;
   }

   public void validate() throws QueryException {
      this.getIntoClause().validateTypes(this.getSelectClause());
   }

   public IntoClause getIntoClause() {
      return (IntoClause)this.getFirstChild();
   }

   public SelectClause getSelectClause() {
      return ((QueryNode)this.getIntoClause().getNextSibling()).getSelectClause();
   }
}
