package org.hibernate.hql.internal.ast.tree;

import antlr.collections.AST;
import org.hibernate.hql.internal.ast.util.ASTUtil;
import org.hibernate.internal.CoreMessageLogger;

public abstract class AbstractRestrictableStatement extends AbstractStatement implements RestrictableStatement {
   private FromClause fromClause;
   private AST whereClause;

   protected abstract int getWhereClauseParentTokenType();

   protected abstract CoreMessageLogger getLog();

   public final FromClause getFromClause() {
      if (this.fromClause == null) {
         this.fromClause = (FromClause)ASTUtil.findTypeInChildren(this, 22);
      }

      return this.fromClause;
   }

   public final boolean hasWhereClause() {
      AST whereClause = this.locateWhereClause();
      return whereClause != null && whereClause.getNumberOfChildren() > 0;
   }

   public final AST getWhereClause() {
      if (this.whereClause == null) {
         this.whereClause = this.locateWhereClause();
         if (this.whereClause == null) {
            this.getLog().debug("getWhereClause() : Creating a new WHERE clause...");
            this.whereClause = ASTUtil.create(this.getWalker().getASTFactory(), 53, "WHERE");
            AST parent = ASTUtil.findTypeInChildren(this, this.getWhereClauseParentTokenType());
            this.whereClause.setNextSibling(parent.getNextSibling());
            parent.setNextSibling(this.whereClause);
         }
      }

      return this.whereClause;
   }

   protected AST locateWhereClause() {
      return ASTUtil.findTypeInChildren(this, 53);
   }
}
