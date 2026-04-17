package org.hibernate.hql.internal.ast.tree;

import antlr.SemanticException;
import org.hibernate.hql.internal.ast.util.ColumnHelper;
import org.hibernate.type.Type;

public class CaseNode extends AbstractSelectExpression implements SelectExpression {
   public Type getDataType() {
      return this.getFirstThenNode().getDataType();
   }

   private SelectExpression getFirstThenNode() {
      return (SelectExpression)this.getFirstChild().getFirstChild().getNextSibling();
   }

   public void setScalarColumnText(int i) throws SemanticException {
      ColumnHelper.generateSingleScalarColumn(this, i);
   }
}
