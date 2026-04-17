package org.hibernate.internal;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.MultiTenancyStrategy;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollableResults;
import org.hibernate.SessionException;
import org.hibernate.SharedSessionContract;
import org.hibernate.cache.spi.CacheKey;
import org.hibernate.engine.jdbc.LobCreationContext;
import org.hibernate.engine.jdbc.spi.JdbcConnectionAccess;
import org.hibernate.engine.query.spi.HQLQueryPlan;
import org.hibernate.engine.query.spi.NativeSQLQueryPlan;
import org.hibernate.engine.query.spi.ParameterMetadata;
import org.hibernate.engine.query.spi.sql.NativeSQLQuerySpecification;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.NamedQueryDefinition;
import org.hibernate.engine.spi.NamedSQLQueryDefinition;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.transaction.spi.TransactionContext;
import org.hibernate.engine.transaction.spi.TransactionEnvironment;
import org.hibernate.id.uuid.StandardRandomStrategy;
import org.hibernate.jdbc.WorkExecutor;
import org.hibernate.jdbc.WorkExecutorVisitable;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.type.Type;

public abstract class AbstractSessionImpl implements Serializable, SharedSessionContract, SessionImplementor, TransactionContext {
   protected transient SessionFactoryImpl factory;
   private final String tenantIdentifier;
   private boolean closed = false;
   private transient JdbcConnectionAccess jdbcConnectionAccess;
   private UUID sessionIdentifier;

   protected AbstractSessionImpl(SessionFactoryImpl factory, String tenantIdentifier) {
      this.factory = factory;
      this.tenantIdentifier = tenantIdentifier;
      if (MultiTenancyStrategy.NONE == factory.getSettings().getMultiTenancyStrategy()) {
         if (tenantIdentifier != null) {
            throw new HibernateException("SessionFactory was not configured for multi-tenancy");
         }
      } else if (tenantIdentifier == null) {
         throw new HibernateException("SessionFactory configured for multi-tenancy, but no tenant identifier specified");
      }

   }

   public SessionFactoryImplementor getFactory() {
      return this.factory;
   }

   public TransactionEnvironment getTransactionEnvironment() {
      return this.factory.getTransactionEnvironment();
   }

   public Object execute(final LobCreationContext.Callback callback) {
      return this.getTransactionCoordinator().getJdbcCoordinator().coordinateWork(new WorkExecutorVisitable() {
         public Object accept(WorkExecutor workExecutor, Connection connection) throws SQLException {
            try {
               return callback.executeOnConnection(connection);
            } catch (SQLException e) {
               throw AbstractSessionImpl.this.getFactory().getSQLExceptionHelper().convert(e, "Error creating contextual LOB : " + e.getMessage());
            }
         }
      });
   }

   public boolean isClosed() {
      return this.closed;
   }

   protected void setClosed() {
      this.closed = true;
   }

   protected void errorIfClosed() {
      if (this.closed) {
         throw new SessionException("Session is closed!");
      }
   }

   public Query getNamedQuery(String queryName) throws MappingException {
      this.errorIfClosed();
      NamedQueryDefinition nqd = this.factory.getNamedQuery(queryName);
      Query query;
      if (nqd != null) {
         String queryString = nqd.getQueryString();
         query = new QueryImpl(queryString, nqd.getFlushMode(), this, this.getHQLQueryPlan(queryString, false).getParameterMetadata());
         query.setComment("named HQL query " + queryName);
         if (nqd.getLockTimeout() != null) {
            ((QueryImpl)query).getLockOptions().setTimeOut(nqd.getLockTimeout());
         }
      } else {
         NamedSQLQueryDefinition nsqlqd = this.factory.getNamedSQLQuery(queryName);
         if (nsqlqd == null) {
            throw new MappingException("Named query not known: " + queryName);
         }

         ParameterMetadata parameterMetadata = this.factory.getQueryPlanCache().getSQLParameterMetadata(nsqlqd.getQueryString());
         query = new SQLQueryImpl(nsqlqd, this, parameterMetadata);
         query.setComment("named native SQL query " + queryName);
         nqd = nsqlqd;
      }

      this.initQuery(query, nqd);
      return query;
   }

   public Query getNamedSQLQuery(String queryName) throws MappingException {
      this.errorIfClosed();
      NamedSQLQueryDefinition nsqlqd = this.factory.getNamedSQLQuery(queryName);
      if (nsqlqd == null) {
         throw new MappingException("Named SQL query not known: " + queryName);
      } else {
         Query query = new SQLQueryImpl(nsqlqd, this, this.factory.getQueryPlanCache().getSQLParameterMetadata(nsqlqd.getQueryString()));
         query.setComment("named native SQL query " + queryName);
         this.initQuery(query, nsqlqd);
         return query;
      }
   }

   private void initQuery(Query query, NamedQueryDefinition nqd) {
      query.setCacheable(nqd.isCacheable());
      query.setCacheRegion(nqd.getCacheRegion());
      if (nqd.getTimeout() != null) {
         query.setTimeout(nqd.getTimeout());
      }

      if (nqd.getFetchSize() != null) {
         query.setFetchSize(nqd.getFetchSize());
      }

      if (nqd.getCacheMode() != null) {
         query.setCacheMode(nqd.getCacheMode());
      }

      query.setReadOnly(nqd.isReadOnly());
      if (nqd.getComment() != null) {
         query.setComment(nqd.getComment());
      }

   }

   public Query createQuery(String queryString) {
      this.errorIfClosed();
      QueryImpl query = new QueryImpl(queryString, this, this.getHQLQueryPlan(queryString, false).getParameterMetadata());
      query.setComment(queryString);
      return query;
   }

   public SQLQuery createSQLQuery(String sql) {
      this.errorIfClosed();
      SQLQueryImpl query = new SQLQueryImpl(sql, this, this.factory.getQueryPlanCache().getSQLParameterMetadata(sql));
      query.setComment("dynamic native SQL query");
      return query;
   }

   protected HQLQueryPlan getHQLQueryPlan(String query, boolean shallow) throws HibernateException {
      return this.factory.getQueryPlanCache().getHQLQueryPlan(query, shallow, this.getEnabledFilters());
   }

   protected NativeSQLQueryPlan getNativeSQLQueryPlan(NativeSQLQuerySpecification spec) throws HibernateException {
      return this.factory.getQueryPlanCache().getNativeSQLQueryPlan(spec);
   }

   public List list(NativeSQLQuerySpecification spec, QueryParameters queryParameters) throws HibernateException {
      return this.listCustomQuery(this.getNativeSQLQueryPlan(spec).getCustomQuery(), queryParameters);
   }

   public ScrollableResults scroll(NativeSQLQuerySpecification spec, QueryParameters queryParameters) throws HibernateException {
      return this.scrollCustomQuery(this.getNativeSQLQueryPlan(spec).getCustomQuery(), queryParameters);
   }

   public String getTenantIdentifier() {
      return this.tenantIdentifier;
   }

   public EntityKey generateEntityKey(Serializable id, EntityPersister persister) {
      return new EntityKey(id, persister, this.getTenantIdentifier());
   }

   public CacheKey generateCacheKey(Serializable id, Type type, String entityOrRoleName) {
      return new CacheKey(id, type, entityOrRoleName, this.getTenantIdentifier(), this.getFactory());
   }

   public JdbcConnectionAccess getJdbcConnectionAccess() {
      if (this.jdbcConnectionAccess == null) {
         if (MultiTenancyStrategy.NONE == this.factory.getSettings().getMultiTenancyStrategy()) {
            this.jdbcConnectionAccess = new NonContextualJdbcConnectionAccess((ConnectionProvider)this.factory.getServiceRegistry().getService(ConnectionProvider.class));
         } else {
            this.jdbcConnectionAccess = new ContextualJdbcConnectionAccess((MultiTenantConnectionProvider)this.factory.getServiceRegistry().getService(MultiTenantConnectionProvider.class));
         }
      }

      return this.jdbcConnectionAccess;
   }

   public UUID getSessionIdentifier() {
      if (this.sessionIdentifier == null) {
         this.sessionIdentifier = StandardRandomStrategy.INSTANCE.generateUUID(this);
      }

      return this.sessionIdentifier;
   }

   private static class NonContextualJdbcConnectionAccess implements JdbcConnectionAccess, Serializable {
      private final ConnectionProvider connectionProvider;

      private NonContextualJdbcConnectionAccess(ConnectionProvider connectionProvider) {
         this.connectionProvider = connectionProvider;
      }

      public Connection obtainConnection() throws SQLException {
         return this.connectionProvider.getConnection();
      }

      public void releaseConnection(Connection connection) throws SQLException {
         this.connectionProvider.closeConnection(connection);
      }

      public boolean supportsAggressiveRelease() {
         return this.connectionProvider.supportsAggressiveRelease();
      }
   }

   private class ContextualJdbcConnectionAccess implements JdbcConnectionAccess, Serializable {
      private final MultiTenantConnectionProvider connectionProvider;

      private ContextualJdbcConnectionAccess(MultiTenantConnectionProvider connectionProvider) {
         this.connectionProvider = connectionProvider;
      }

      public Connection obtainConnection() throws SQLException {
         if (AbstractSessionImpl.this.tenantIdentifier == null) {
            throw new HibernateException("Tenant identifier required!");
         } else {
            return this.connectionProvider.getConnection(AbstractSessionImpl.this.tenantIdentifier);
         }
      }

      public void releaseConnection(Connection connection) throws SQLException {
         if (AbstractSessionImpl.this.tenantIdentifier == null) {
            throw new HibernateException("Tenant identifier required!");
         } else {
            this.connectionProvider.releaseConnection(AbstractSessionImpl.this.tenantIdentifier, connection);
         }
      }

      public boolean supportsAggressiveRelease() {
         return this.connectionProvider.supportsAggressiveRelease();
      }
   }
}
