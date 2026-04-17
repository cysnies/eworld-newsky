package org.hibernate.hql.internal.ast.tree;

import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class DeleteStatement extends AbstractRestrictableStatement {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, DeleteStatement.class.getName());

   public int getStatementType() {
      return 13;
   }

   public boolean needsExecutor() {
      return true;
   }

   protected int getWhereClauseParentTokenType() {
      return 22;
   }

   protected CoreMessageLogger getLog() {
      return LOG;
   }
}
