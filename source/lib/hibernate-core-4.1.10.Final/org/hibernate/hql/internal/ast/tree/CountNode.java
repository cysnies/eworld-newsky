package org.hibernate.hql.internal.ast.tree;

import antlr.SemanticException;
import antlr.collections.AST;
import org.hibernate.hql.internal.ast.util.ColumnHelper;
import org.hibernate.type.Type;

public class CountNode extends AbstractSelectExpression implements SelectExpression {
   public Type getDataType() {
      return this.getSessionFactoryHelper().findFunctionReturnType(this.getText(), (AST)null);
   }

   public void setScalarColumnText(int i) throws SemanticException {
      ColumnHelper.generateSingleScalarColumn(this, i);
   }
}
