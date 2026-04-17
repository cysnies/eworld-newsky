package org.hibernate.internal;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.hibernate.CacheMode;
import org.hibernate.ConnectionReleaseMode;
import org.hibernate.Criteria;
import org.hibernate.EmptyInterceptor;
import org.hibernate.EntityMode;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.LockMode;
import org.hibernate.MappingException;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.SessionException;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.UnresolvableObjectException;
import org.hibernate.cache.spi.CacheKey;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.internal.StatefulPersistenceContext;
import org.hibernate.engine.internal.Versioning;
import org.hibernate.engine.query.spi.HQLQueryPlan;
import org.hibernate.engine.query.spi.NativeSQLQueryPlan;
import org.hibernate.engine.query.spi.sql.NativeSQLQuerySpecification;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.NonFlushedChanges;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.transaction.internal.TransactionCoordinatorImpl;
import org.hibernate.engine.transaction.spi.TransactionCoordinator;
import org.hibernate.engine.transaction.spi.TransactionEnvironment;
import org.hibernate.engine.transaction.spi.TransactionImplementor;
import org.hibernate.id.IdentifierGeneratorHelper;
import org.hibernate.loader.criteria.CriteriaLoader;
import org.hibernate.loader.custom.CustomLoader;
import org.hibernate.loader.custom.CustomQuery;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class StatelessSessionImpl extends AbstractSessionImpl implements StatelessSession {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, StatelessSessionImpl.class.getName());
   private TransactionCoordinator transactionCoordinator;
   private PersistenceContext temporaryPersistenceContext = new StatefulPersistenceContext(this);

   StatelessSessionImpl(Connection connection, String tenantIdentifier, SessionFactoryImpl factory) {
      super(factory, tenantIdentifier);
      this.transactionCoordinator = new TransactionCoordinatorImpl(connection, this);
   }

   public TransactionCoordinator getTransactionCoordinator() {
      return this.transactionCoordinator;
   }

   public TransactionEnvironment getTransactionEnvironment() {
      return this.factory.getTransactionEnvironment();
   }

   public Serializable insert(Object entity) {
      this.errorIfClosed();
      return this.insert((String)null, entity);
   }

   public Serializable insert(String entityName, Object entity) {
      this.errorIfClosed();
      EntityPersister persister = this.getEntityPersister(entityName, entity);
      Serializable id = persister.getIdentifierGenerator().generate(this, entity);
      Object[] state = persister.getPropertyValues(entity);
      if (persister.isVersioned()) {
         boolean substitute = Versioning.seedVersion(state, persister.getVersionProperty(), persister.getVersionType(), this);
         if (substitute) {
            persister.setPropertyValues(entity, state);
         }
      }

      if (id == IdentifierGeneratorHelper.POST_INSERT_INDICATOR) {
         id = persister.insert(state, entity, this);
      } else {
         persister.insert(id, state, entity, this);
      }

      persister.setIdentifier(entity, id, this);
      return id;
   }

   public void delete(Object entity) {
      this.errorIfClosed();
      this.delete((String)null, entity);
   }

   public void delete(String entityName, Object entity) {
      this.errorIfClosed();
      EntityPersister persister = this.getEntityPersister(entityName, entity);
      Serializable id = persister.getIdentifier(entity, this);
      Object version = persister.getVersion(entity);
      persister.delete(id, version, entity, this);
   }

   public void update(Object entity) {
      this.errorIfClosed();
      this.update((String)null, entity);
   }

   public void update(String entityName, Object entity) {
      this.errorIfClosed();
      EntityPersister persister = this.getEntityPersister(entityName, entity);
      Serializable id = persister.getIdentifier(entity, this);
      Object[] state = persister.getPropertyValues(entity);
      Object oldVersion;
      if (persister.isVersioned()) {
         oldVersion = persister.getVersion(entity);
         Object newVersion = Versioning.increment(oldVersion, persister.getVersionType(), this);
         Versioning.setVersion(state, newVersion, persister);
         persister.setPropertyValues(entity, state);
      } else {
         oldVersion = null;
      }

      persister.update(id, state, (int[])null, false, (Object[])null, oldVersion, entity, (Object)null, this);
   }

   public Object get(Class entityClass, Serializable id) {
      return this.get(entityClass.getName(), id);
   }

   public Object get(Class entityClass, Serializable id, LockMode lockMode) {
      return this.get(entityClass.getName(), id, lockMode);
   }

   public Object get(String entityName, Serializable id) {
      return this.get(entityName, id, LockMode.NONE);
   }

   public Object get(String entityName, Serializable id, LockMode lockMode) {
      this.errorIfClosed();
      Object result = this.getFactory().getEntityPersister(entityName).load(id, (Object)null, (LockMode)lockMode, this);
      if (this.temporaryPersistenceContext.isLoadFinished()) {
         this.temporaryPersistenceContext.clear();
      }

      return result;
   }

   public void refresh(Object entity) {
      this.refresh(this.bestGuessEntityName(entity), entity, LockMode.NONE);
   }

   public void refresh(String entityName, Object entity) {
      this.refresh(entityName, entity, LockMode.NONE);
   }

   public void refresh(Object entity, LockMode lockMode) {
      this.refresh(this.bestGuessEntityName(entity), entity, lockMode);
   }

   public void refresh(String entityName, Object entity, LockMode lockMode) {
      EntityPersister persister = this.getEntityPersister(entityName, entity);
      Serializable id = persister.getIdentifier(entity, this);
      if (LOG.isTraceEnabled()) {
         LOG.tracev("Refreshing transient {0}", MessageHelper.infoString((EntityPersister)persister, (Object)id, (SessionFactoryImplementor)this.getFactory()));
      }

      if (persister.hasCache()) {
         CacheKey ck = this.generateCacheKey(id, persister.getIdentifierType(), persister.getRootEntityName());
         persister.getCacheAccessStrategy().evict(ck);
      }

      String previousFetchProfile = this.getFetchProfile();
      Object result = null;

      try {
         this.setFetchProfile("refresh");
         result = persister.load(id, entity, (LockMode)lockMode, this);
      } finally {
         this.setFetchProfile(previousFetchProfile);
      }

      UnresolvableObjectException.throwIfNull(result, id, persister.getEntityName());
   }

   public Object immediateLoad(String entityName, Serializable id) throws HibernateException {
      throw new SessionException("proxies cannot be fetched by a stateless session");
   }

   public void initializeCollection(PersistentCollection collection, boolean writing) throws HibernateException {
      throw new SessionException("collections cannot be fetched by a stateless session");
   }

   public Object instantiate(String entityName, Serializable id) throws HibernateException {
      this.errorIfClosed();
      return this.getFactory().getEntityPersister(entityName).instantiate(id, this);
   }

   public Object internalLoad(String entityName, Serializable id, boolean eager, boolean nullable) throws HibernateException {
      this.errorIfClosed();
      EntityPersister persister = this.getFactory().getEntityPersister(entityName);
      Object loaded = this.temporaryPersistenceContext.getEntity(this.generateEntityKey(id, persister));
      if (loaded != null) {
         return loaded;
      } else {
         return !eager && persister.hasProxy() ? persister.createProxy(id, this) : this.get(entityName, id);
      }
   }

   public Iterator iterate(String query, QueryParameters queryParameters) throws HibernateException {
      throw new UnsupportedOperationException();
   }

   public Iterator iterateFilter(Object collection, String filter, QueryParameters queryParameters) throws HibernateException {
      throw new UnsupportedOperationException();
   }

   public List listFilter(Object collection, String filter, QueryParameters queryParameters) throws HibernateException {
      throw new UnsupportedOperationException();
   }

   public boolean isOpen() {
      return !this.isClosed();
   }

   public void close() {
      this.managedClose();
   }

   public ConnectionReleaseMode getConnectionReleaseMode() {
      return this.factory.getSettings().getConnectionReleaseMode();
   }

   public boolean shouldAutoJoinTransaction() {
      return true;
   }

   public boolean isAutoCloseSessionEnabled() {
      return this.factory.getSettings().isAutoCloseSessionEnabled();
   }

   public boolean isFlushBeforeCompletionEnabled() {
      return true;
   }

   public boolean isFlushModeNever() {
      return false;
   }

   public void managedClose() {
      if (this.isClosed()) {
         throw new SessionException("Session was already closed!");
      } else {
         this.transactionCoordinator.close();
         this.setClosed();
      }
   }

   public void managedFlush() {
      this.errorIfClosed();
      this.getTransactionCoordinator().getJdbcCoordinator().executeBatch();
   }

   public boolean shouldAutoClose() {
      return this.isAutoCloseSessionEnabled() && !this.isClosed();
   }

   public void afterTransactionBegin(TransactionImplementor hibernateTransaction) {
   }

   public void beforeTransactionCompletion(TransactionImplementor hibernateTransaction) {
   }

   public void afterTransactionCompletion(TransactionImplementor hibernateTransaction, boolean successful) {
   }

   public String onPrepareStatement(String sql) {
      return sql;
   }

   public String bestGuessEntityName(Object object) {
      if (object instanceof HibernateProxy) {
         object = ((HibernateProxy)object).getHibernateLazyInitializer().getImplementation();
      }

      return this.guessEntityName(object);
   }

   public Connection connection() {
      this.errorIfClosed();
      return this.transactionCoordinator.getJdbcCoordinator().getLogicalConnection().getDistinctConnectionProxy();
   }

   public int executeUpdate(String query, QueryParameters queryParameters) throws HibernateException {
      this.errorIfClosed();
      queryParameters.validateParameters();
      HQLQueryPlan plan = this.getHQLQueryPlan(query, false);
      boolean success = false;
      int result = 0;

      try {
         result = plan.performExecuteUpdate(queryParameters, this);
         success = true;
      } finally {
         this.afterOperation(success);
      }

      this.temporaryPersistenceContext.clear();
      return result;
   }

   public CacheMode getCacheMode() {
      return CacheMode.IGNORE;
   }

   public int getDontFlushFromFind() {
      return 0;
   }

   public Map getEnabledFilters() {
      return Collections.EMPTY_MAP;
   }

   public Serializable getContextEntityIdentifier(Object object) {
      this.errorIfClosed();
      return null;
   }

   public EntityMode getEntityMode() {
      return EntityMode.POJO;
   }

   public EntityPersister getEntityPersister(String entityName, Object object) throws HibernateException {
      this.errorIfClosed();
      return entityName == null ? this.factory.getEntityPersister(this.guessEntityName(object)) : this.factory.getEntityPersister(entityName).getSubclassEntityPersister(object, this.getFactory());
   }

   public Object getEntityUsingInterceptor(EntityKey key) throws HibernateException {
      this.errorIfClosed();
      return null;
   }

   public Type getFilterParameterType(String filterParameterName) {
      throw new UnsupportedOperationException();
   }

   public Object getFilterParameterValue(String filterParameterName) {
      throw new UnsupportedOperationException();
   }

   public FlushMode getFlushMode() {
      return FlushMode.COMMIT;
   }

   public Interceptor getInterceptor() {
      return EmptyInterceptor.INSTANCE;
   }

   public PersistenceContext getPersistenceContext() {
      return this.temporaryPersistenceContext;
   }

   public long getTimestamp() {
      throw new UnsupportedOperationException();
   }

   public String guessEntityName(Object entity) throws HibernateException {
      this.errorIfClosed();
      return entity.getClass().getName();
   }

   public boolean isConnected() {
      return this.transactionCoordinator.getJdbcCoordinator().getLogicalConnection().isPhysicallyConnected();
   }

   public boolean isTransactionInProgress() {
      return this.transactionCoordinator.isTransactionInProgress();
   }

   public void setAutoClear(boolean enabled) {
      throw new UnsupportedOperationException();
   }

   public void disableTransactionAutoJoin() {
      throw new UnsupportedOperationException();
   }

   public void setCacheMode(CacheMode cm) {
      throw new UnsupportedOperationException();
   }

   public void setFlushMode(FlushMode fm) {
      throw new UnsupportedOperationException();
   }

   public Transaction getTransaction() throws HibernateException {
      this.errorIfClosed();
      return this.transactionCoordinator.getTransaction();
   }

   public Transaction beginTransaction() throws HibernateException {
      this.errorIfClosed();
      Transaction result = this.getTransaction();
      result.begin();
      return result;
   }

   public boolean isEventSource() {
      return false;
   }

   public boolean isDefaultReadOnly() {
      return false;
   }

   public void setDefaultReadOnly(boolean readOnly) throws HibernateException {
      if (readOnly) {
         throw new UnsupportedOperationException();
      }
   }

   public List list(String query, QueryParameters queryParameters) throws HibernateException {
      this.errorIfClosed();
      queryParameters.validateParameters();
      HQLQueryPlan plan = this.getHQLQueryPlan(query, false);
      boolean success = false;
      List results = Collections.EMPTY_LIST;

      try {
         results = plan.performList(queryParameters, this);
         success = true;
      } finally {
         this.afterOperation(success);
      }

      this.temporaryPersistenceContext.clear();
      return results;
   }

   public void afterOperation(boolean success) {
      if (!this.transactionCoordinator.isTransactionInProgress()) {
         this.transactionCoordinator.afterNonTransactionalQuery(success);
      }

   }

   public Criteria createCriteria(Class persistentClass, String alias) {
      this.errorIfClosed();
      return new CriteriaImpl(persistentClass.getName(), alias, this);
   }

   public Criteria createCriteria(String entityName, String alias) {
      this.errorIfClosed();
      return new CriteriaImpl(entityName, alias, this);
   }

   public Criteria createCriteria(Class persistentClass) {
      this.errorIfClosed();
      return new CriteriaImpl(persistentClass.getName(), this);
   }

   public Criteria createCriteria(String entityName) {
      this.errorIfClosed();
      return new CriteriaImpl(entityName, this);
   }

   public ScrollableResults scroll(CriteriaImpl criteria, ScrollMode scrollMode) {
      this.errorIfClosed();
      String entityName = criteria.getEntityOrClassName();
      CriteriaLoader loader = new CriteriaLoader(this.getOuterJoinLoadable(entityName), this.factory, criteria, entityName, this.getLoadQueryInfluencers());
      return loader.scroll(this, scrollMode);
   }

   public List list(CriteriaImpl criteria) throws HibernateException {
      this.errorIfClosed();
      String[] implementors = this.factory.getImplementors(criteria.getEntityOrClassName());
      int size = implementors.length;
      CriteriaLoader[] loaders = new CriteriaLoader[size];

      for(int i = 0; i < size; ++i) {
         loaders[i] = new CriteriaLoader(this.getOuterJoinLoadable(implementors[i]), this.factory, criteria, implementors[i], this.getLoadQueryInfluencers());
      }

      List results = Collections.EMPTY_LIST;
      boolean success = false;

      try {
         for(int i = 0; i < size; ++i) {
            List currentResults = loaders[i].list(this);
            currentResults.addAll(results);
            results = currentResults;
         }

         success = true;
      } finally {
         this.afterOperation(success);
      }

      this.temporaryPersistenceContext.clear();
      return results;
   }

   private OuterJoinLoadable getOuterJoinLoadable(String entityName) throws MappingException {
      EntityPersister persister = this.factory.getEntityPersister(entityName);
      if (!(persister instanceof OuterJoinLoadable)) {
         throw new MappingException("class persister is not OuterJoinLoadable: " + entityName);
      } else {
         return (OuterJoinLoadable)persister;
      }
   }

   public List listCustomQuery(CustomQuery customQuery, QueryParameters queryParameters) throws HibernateException {
      this.errorIfClosed();
      CustomLoader loader = new CustomLoader(customQuery, this.getFactory());
      boolean success = false;

      List results;
      try {
         results = loader.list(this, queryParameters);
         success = true;
      } finally {
         this.afterOperation(success);
      }

      this.temporaryPersistenceContext.clear();
      return results;
   }

   public ScrollableResults scrollCustomQuery(CustomQuery customQuery, QueryParameters queryParameters) throws HibernateException {
      this.errorIfClosed();
      CustomLoader loader = new CustomLoader(customQuery, this.getFactory());
      return loader.scroll(queryParameters, this);
   }

   public ScrollableResults scroll(String query, QueryParameters queryParameters) throws HibernateException {
      this.errorIfClosed();
      HQLQueryPlan plan = this.getHQLQueryPlan(query, false);
      return plan.performScroll(queryParameters, this);
   }

   public void afterScrollOperation() {
      this.temporaryPersistenceContext.clear();
   }

   public void flush() {
   }

   public NonFlushedChanges getNonFlushedChanges() {
      throw new UnsupportedOperationException();
   }

   public void applyNonFlushedChanges(NonFlushedChanges nonFlushedChanges) {
      throw new UnsupportedOperationException();
   }

   public String getFetchProfile() {
      return null;
   }

   public LoadQueryInfluencers getLoadQueryInfluencers() {
      return LoadQueryInfluencers.NONE;
   }

   public void setFetchProfile(String name) {
   }

   public int executeNativeUpdate(NativeSQLQuerySpecification nativeSQLQuerySpecification, QueryParameters queryParameters) throws HibernateException {
      this.errorIfClosed();
      queryParameters.validateParameters();
      NativeSQLQueryPlan plan = this.getNativeSQLQueryPlan(nativeSQLQuerySpecification);
      boolean success = false;
      int result = 0;

      try {
         result = plan.performExecuteUpdate(queryParameters, this);
         success = true;
      } finally {
         this.afterOperation(success);
      }

      this.temporaryPersistenceContext.clear();
      return result;
   }
}
