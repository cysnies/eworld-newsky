package org.hibernate.engine.query.spi;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.QueryException;
import org.hibernate.action.internal.BulkOperationCleanupAction;
import org.hibernate.engine.query.spi.sql.NativeSQLQuerySpecification;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.event.spi.EventSource;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.loader.custom.sql.SQLCustomQuery;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class NativeSQLQueryPlan implements Serializable {
   private final String sourceQuery;
   private final SQLCustomQuery customQuery;
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, NativeSQLQueryPlan.class.getName());

   public NativeSQLQueryPlan(NativeSQLQuerySpecification specification, SessionFactoryImplementor factory) {
      this.sourceQuery = specification.getQueryString();
      this.customQuery = new SQLCustomQuery(specification.getQueryString(), specification.getQueryReturns(), specification.getQuerySpaces(), factory);
   }

   public String getSourceQuery() {
      return this.sourceQuery;
   }

   public SQLCustomQuery getCustomQuery() {
      return this.customQuery;
   }

   private int[] getNamedParameterLocs(String name) throws QueryException {
      Object loc = this.customQuery.getNamedParameterBindPoints().get(name);
      if (loc == null) {
         throw new QueryException("Named parameter does not appear in Query: " + name, this.customQuery.getSQL());
      } else {
         return loc instanceof Integer ? new int[]{(Integer)loc} : ArrayHelper.toIntArray((List)loc);
      }
   }

   private int bindPositionalParameters(PreparedStatement st, QueryParameters queryParameters, int start, SessionImplementor session) throws SQLException {
      Object[] values = queryParameters.getFilteredPositionalParameterValues();
      Type[] types = queryParameters.getFilteredPositionalParameterTypes();
      int span = 0;

      for(int i = 0; i < values.length; ++i) {
         types[i].nullSafeSet(st, values[i], start + span, session);
         span += types[i].getColumnSpan(session.getFactory());
      }

      return span;
   }

   private int bindNamedParameters(PreparedStatement ps, Map namedParams, int start, SessionImplementor session) throws SQLException {
      if (namedParams == null) {
         return 0;
      } else {
         Iterator iter = namedParams.entrySet().iterator();

         int result;
         int[] locs;
         for(result = 0; iter.hasNext(); result += locs.length) {
            Map.Entry e = (Map.Entry)iter.next();
            String name = (String)e.getKey();
            TypedValue typedval = (TypedValue)e.getValue();
            locs = this.getNamedParameterLocs(name);

            for(int i = 0; i < locs.length; ++i) {
               LOG.debugf("bindNamedParameters() %s -> %s [%s]", typedval.getValue(), name, locs[i] + start);
               typedval.getType().nullSafeSet(ps, typedval.getValue(), locs[i] + start, session);
            }
         }

         return result;
      }
   }

   protected void coordinateSharedCacheCleanup(SessionImplementor session) {
      BulkOperationCleanupAction action = new BulkOperationCleanupAction(session, this.getCustomQuery().getQuerySpaces());
      if (session.isEventSource()) {
         ((EventSource)session).getActionQueue().addAction(action);
      } else {
         action.getAfterTransactionCompletionProcess().doAfterTransactionCompletion(true, session);
      }

   }

   public int performExecuteUpdate(QueryParameters queryParameters, SessionImplementor session) throws HibernateException {
      this.coordinateSharedCacheCleanup(session);
      if (queryParameters.isCallable()) {
         throw new IllegalArgumentException("callable not yet supported for native queries");
      } else {
         int result = 0;

         try {
            queryParameters.processFilters(this.customQuery.getSQL(), session);
            String sql = queryParameters.getFilteredSQL();
            PreparedStatement ps = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(sql, false);

            try {
               int col = 1;
               col += this.bindPositionalParameters(ps, queryParameters, col, session);
               int var10000 = col + this.bindNamedParameters(ps, queryParameters.getNamedParameters(), col, session);
               result = ps.executeUpdate();
            } finally {
               if (ps != null) {
                  ps.close();
               }

            }

            return result;
         } catch (SQLException sqle) {
            throw session.getFactory().getSQLExceptionHelper().convert(sqle, "could not execute native bulk manipulation query", this.sourceQuery);
         }
      }
   }
}
