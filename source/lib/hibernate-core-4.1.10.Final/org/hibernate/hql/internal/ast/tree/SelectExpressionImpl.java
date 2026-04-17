package org.hibernate.hql.internal.ast.tree;

import antlr.SemanticException;
import antlr.collections.AST;

public class SelectExpressionImpl extends FromReferenceNode implements SelectExpression {
   public void resolveIndex(AST parent) throws SemanticException {
      throw new UnsupportedOperationException();
   }

   public void setScalarColumnText(int i) throws SemanticException {
      String text = this.getFromElement().renderScalarIdentifierSelect(i);
      this.setText(text);
   }

   public void resolve(boolean generateJoin, boolean implicitJoin, String classAlias, AST parent) throws SemanticException {
   }
}
