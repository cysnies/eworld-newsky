package org.hibernate.hql.internal.ast.tree;

import antlr.collections.AST;
import java.util.ArrayList;
import org.hibernate.hql.internal.antlr.SqlTokenTypes;
import org.hibernate.hql.internal.ast.util.ASTPrinter;

public abstract class SelectExpressionList extends HqlSqlWalkerNode {
   public SelectExpression[] collectSelectExpressions() {
      AST firstChild = this.getFirstSelectExpression();
      ArrayList list = new ArrayList(this.getNumberOfChildren());

      for(AST n = firstChild; n != null; n = n.getNextSibling()) {
         if (!(n instanceof SelectExpression)) {
            throw new IllegalStateException("Unexpected AST: " + n.getClass().getName() + " " + (new ASTPrinter(SqlTokenTypes.class)).showAsString(n, ""));
         }

         list.add(n);
      }

      return (SelectExpression[])list.toArray(new SelectExpression[list.size()]);
   }

   protected abstract AST getFirstSelectExpression();
}
