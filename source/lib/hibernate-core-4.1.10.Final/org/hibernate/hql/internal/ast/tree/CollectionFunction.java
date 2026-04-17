package org.hibernate.hql.internal.ast.tree;

import antlr.SemanticException;
import antlr.collections.AST;

public class CollectionFunction extends MethodNode implements DisplayableNode {
   public void resolve(boolean inSelect) throws SemanticException {
      this.initializeMethodNode(this, inSelect);
      if (!this.isCollectionPropertyMethod()) {
         throw new SemanticException(this.getText() + " is not a collection property name!");
      } else {
         AST expr = this.getFirstChild();
         if (expr == null) {
            throw new SemanticException(this.getText() + " requires a path!");
         } else {
            this.resolveCollectionProperty(expr);
         }
      }
   }

   protected void prepareSelectColumns(String[] selectColumns) {
      String subselect = selectColumns[0].trim();
      if (subselect.startsWith("(") && subselect.endsWith(")")) {
         subselect = subselect.substring(1, subselect.length() - 1);
      }

      selectColumns[0] = subselect;
   }
}
