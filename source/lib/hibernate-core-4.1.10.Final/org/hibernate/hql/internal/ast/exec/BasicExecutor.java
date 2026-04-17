package org.hibernate.hql.internal.ast.exec;

import antlr.RecognitionException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import org.hibernate.HibernateException;
import org.hibernate.action.internal.BulkOperationCleanupAction;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.RowSelection;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.hql.internal.ast.HqlSqlWalker;
import org.hibernate.hql.internal.ast.QuerySyntaxException;
import org.hibernate.hql.internal.ast.SqlGenerator;
import org.hibernate.param.ParameterSpecification;
import org.hibernate.persister.entity.Queryable;

public class BasicExecutor implements StatementExecutor {
   private final SessionFactoryImplementor factory;
   private final Queryable persister;
   private final String sql;
   private final List parameterSpecifications;

   public BasicExecutor(HqlSqlWalker walker, Queryable persister) {
      this.factory = walker.getSessionFactoryHelper().getFactory();
      this.persister = persister;

      try {
         SqlGenerator gen = new SqlGenerator(this.factory);
         gen.statement(walker.getAST());
         this.sql = gen.getSQL();
         gen.getParseErrorHandler().throwQueryException();
         this.parameterSpecifications = gen.getCollectedParameters();
      } catch (RecognitionException e) {
         throw QuerySyntaxException.convert(e);
      }
   }

   public String[] getSqlStatements() {
      return new String[]{this.sql};
   }

   public int execute(QueryParameters parameters, SessionImplementor session) throws HibernateException {
      BulkOperationCleanupAction action = new BulkOperationCleanupAction(session, new Queryable[]{this.persister});
      if (session.isEventSource()) {
         ((EventSource)session).getActionQueue().addAction(action);
      } else {
         action.getAfterTransactionCompletionProcess().doAfterTransactionCompletion(true, session);
      }

      PreparedStatement st = null;
      RowSelection selection = parameters.getRowSelection();

      try {
         int var14;
         try {
            st = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(this.sql, false);
            Iterator parameterSpecifications = this.parameterSpecifications.iterator();

            for(int pos = 1; parameterSpecifications.hasNext(); pos += paramSpec.bind(st, parameters, session, pos)) {
               paramSpec = (ParameterSpecification)parameterSpecifications.next();
            }

            if (selection != null && selection.getTimeout() != null) {
               st.setQueryTimeout(selection.getTimeout());
            }

            var14 = st.executeUpdate();
         } finally {
            if (st != null) {
               st.close();
            }

         }

         return var14;
      } catch (SQLException sqle) {
         throw this.factory.getSQLExceptionHelper().convert(sqle, "could not execute update query", this.sql);
      }
   }
}
