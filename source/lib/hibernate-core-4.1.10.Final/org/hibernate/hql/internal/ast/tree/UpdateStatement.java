package org.hibernate.hql.internal.ast.tree;

import antlr.collections.AST;
import org.hibernate.hql.internal.ast.util.ASTUtil;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class UpdateStatement extends AbstractRestrictableStatement {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, UpdateStatement.class.getName());

   public int getStatementType() {
      return 51;
   }

   public boolean needsExecutor() {
      return true;
   }

   protected int getWhereClauseParentTokenType() {
      return 46;
   }

   protected CoreMessageLogger getLog() {
      return LOG;
   }

   public AST getSetClause() {
      return ASTUtil.findTypeInChildren(this, 46);
   }
}
