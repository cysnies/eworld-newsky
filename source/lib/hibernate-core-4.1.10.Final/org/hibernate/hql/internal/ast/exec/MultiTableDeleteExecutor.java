package org.hibernate.hql.internal.ast.exec;

import org.hibernate.HibernateException;
import org.hibernate.action.internal.BulkOperationCleanupAction;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.hql.internal.ast.HqlSqlWalker;
import org.hibernate.hql.spi.MultiTableBulkIdStrategy;
import org.hibernate.persister.entity.Queryable;

public class MultiTableDeleteExecutor implements StatementExecutor {
   private final MultiTableBulkIdStrategy.DeleteHandler deleteHandler;

   public MultiTableDeleteExecutor(HqlSqlWalker walker) {
      MultiTableBulkIdStrategy strategy = walker.getSessionFactoryHelper().getFactory().getSettings().getMultiTableBulkIdStrategy();
      this.deleteHandler = strategy.buildDeleteHandler(walker.getSessionFactoryHelper().getFactory(), walker);
   }

   public String[] getSqlStatements() {
      return this.deleteHandler.getSqlStatements();
   }

   public int execute(QueryParameters parameters, SessionImplementor session) throws HibernateException {
      BulkOperationCleanupAction action = new BulkOperationCleanupAction(session, new Queryable[]{this.deleteHandler.getTargetedQueryable()});
      if (session.isEventSource()) {
         ((EventSource)session).getActionQueue().addAction(action);
      } else {
         action.getAfterTransactionCompletionProcess().doAfterTransactionCompletion(true, session);
      }

      return this.deleteHandler.execute(session, parameters);
   }
}
