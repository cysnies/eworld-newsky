package org.hibernate.hql.internal.ast.tree;

import antlr.SemanticException;
import org.hibernate.hql.internal.ast.util.ColumnHelper;
import org.hibernate.type.Type;

public class UnaryArithmeticNode extends AbstractSelectExpression implements UnaryOperatorNode {
   public Type getDataType() {
      return ((SqlNode)this.getOperand()).getDataType();
   }

   public void setScalarColumnText(int i) throws SemanticException {
      ColumnHelper.generateSingleScalarColumn(this, i);
   }

   public void initialize() {
   }

   public Node getOperand() {
      return (Node)this.getFirstChild();
   }
}
