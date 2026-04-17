package org.hibernate.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityNotFoundException;
import org.hibernate.AssertionFailure;
import org.hibernate.CacheMode;
import org.hibernate.ConnectionReleaseMode;
import org.hibernate.Criteria;
import org.hibernate.EmptyInterceptor;
import org.hibernate.EntityNameResolver;
import org.hibernate.Filter;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.IdentifierLoadAccess;
import org.hibernate.Interceptor;
import org.hibernate.LobHelper;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.MappingException;
import org.hibernate.NaturalIdLoadAccess;
import org.hibernate.ObjectDeletedException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.hibernate.QueryException;
import org.hibernate.ReplicationMode;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionBuilder;
import org.hibernate.SessionException;
import org.hibernate.SharedSessionBuilder;
import org.hibernate.SimpleNaturalIdLoadAccess;
import org.hibernate.Transaction;
import org.hibernate.TransientObjectException;
import org.hibernate.TypeHelper;
import org.hibernate.UnknownProfileException;
import org.hibernate.UnresolvableObjectException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.criterion.NaturalIdentifier;
import org.hibernate.engine.internal.StatefulPersistenceContext;
import org.hibernate.engine.jdbc.LobCreator;
import org.hibernate.engine.query.spi.FilterQueryPlan;
import org.hibernate.engine.query.spi.HQLQueryPlan;
import org.hibernate.engine.query.spi.NativeSQLQueryPlan;
import org.hibernate.engine.query.spi.sql.NativeSQLQuerySpecification;
import org.hibernate.engine.spi.ActionQueue;
import org.hibernate.engine.spi.CollectionEntry;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.NonFlushedChanges;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionOwner;
import org.hibernate.engine.spi.Status;
import org.hibernate.engine.transaction.internal.TransactionCoordinatorImpl;
import org.hibernate.engine.transaction.spi.TransactionCoordinator;
import org.hibernate.engine.transaction.spi.TransactionImplementor;
import org.hibernate.engine.transaction.spi.TransactionObserver;
import org.hibernate.event.service.spi.EventListenerGroup;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.AutoFlushEvent;
import org.hibernate.event.spi.AutoFlushEventListener;
import org.hibernate.event.spi.DeleteEvent;
import org.hibernate.event.spi.DeleteEventListener;
import org.hibernate.event.spi.DirtyCheckEvent;
import org.hibernate.event.spi.DirtyCheckEventListener;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.EvictEvent;
import org.hibernate.event.spi.EvictEventListener;
import org.hibernate.event.spi.FlushEvent;
import org.hibernate.event.spi.FlushEventListener;
import org.hibernate.event.spi.InitializeCollectionEvent;
import org.hibernate.event.spi.InitializeCollectionEventListener;
import org.hibernate.event.spi.LoadEvent;
import org.hibernate.event.spi.LoadEventListener;
import org.hibernate.event.spi.LockEvent;
import org.hibernate.event.spi.LockEventListener;
import org.hibernate.event.spi.MergeEvent;
import org.hibernate.event.spi.MergeEventListener;
import org.hibernate.event.spi.PersistEvent;
import org.hibernate.event.spi.PersistEventListener;
import org.hibernate.event.spi.RefreshEvent;
import org.hibernate.event.spi.RefreshEventListener;
import org.hibernate.event.spi.ReplicateEvent;
import org.hibernate.event.spi.ReplicateEventListener;
import org.hibernate.event.spi.ResolveNaturalIdEvent;
import org.hibernate.event.spi.ResolveNaturalIdEventListener;
import org.hibernate.event.spi.SaveOrUpdateEvent;
import org.hibernate.event.spi.SaveOrUpdateEventListener;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.jdbc.Work;
import org.hibernate.jdbc.WorkExecutor;
import org.hibernate.jdbc.WorkExecutorVisitable;
import org.hibernate.loader.criteria.CriteriaLoader;
import org.hibernate.loader.custom.CustomLoader;
import org.hibernate.loader.custom.CustomQuery;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.hibernate.stat.SessionStatistics;
import org.hibernate.stat.internal.SessionStatisticsImpl;
import org.hibernate.type.SerializationException;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public final class SessionImpl extends AbstractSessionImpl implements EventSource {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, SessionImpl.class.getName());
   private static final boolean tracing;
   private transient long timestamp;
   private transient SessionOwner sessionOwner;
   private transient ActionQueue actionQueue;
   private transient StatefulPersistenceContext persistenceContext;
   private transient TransactionCoordinatorImpl transactionCoordinator;
   private transient Interceptor interceptor;
   private transient EntityNameResolver entityNameResolver = new CoordinatingEntityNameResolver();
   private transient ConnectionReleaseMode connectionReleaseMode;
   private transient FlushMode flushMode;
   private transient CacheMode cacheMode;
   private transient boolean autoClear;
   private transient boolean autoJoinTransactions;
   private transient boolean flushBeforeCompletionEnabled;
   private transient boolean autoCloseSessionEnabled;
   private transient int dontFlushFromFind;
   private transient LoadQueryInfluencers loadQueryInfluencers;
   private final transient boolean isTransactionCoordinatorShared;
   private transient TransactionObserver transactionObserver;
   private transient LobHelperImpl lobHelper;

   SessionImpl(Connection connection, SessionFactoryImpl factory, SessionOwner sessionOwner, final TransactionCoordinatorImpl transactionCoordinator, boolean autoJoinTransactions, long timestamp, Interceptor interceptor, final boolean flushBeforeCompletionEnabled, final boolean autoCloseSessionEnabled, ConnectionReleaseMode connectionReleaseMode, String tenantIdentifier) {
      super(factory, tenantIdentifier);
      this.flushMode = FlushMode.AUTO;
      this.cacheMode = CacheMode.NORMAL;
      this.autoJoinTransactions = true;
      this.dontFlushFromFind = 0;
      this.timestamp = timestamp;
      this.sessionOwner = sessionOwner;
      this.interceptor = interceptor == null ? EmptyInterceptor.INSTANCE : interceptor;
      this.actionQueue = new ActionQueue(this);
      this.persistenceContext = new StatefulPersistenceContext(this);
      this.autoCloseSessionEnabled = autoCloseSessionEnabled;
      this.flushBeforeCompletionEnabled = flushBeforeCompletionEnabled;
      if (transactionCoordinator == null) {
         this.isTransactionCoordinatorShared = false;
         this.connectionReleaseMode = connectionReleaseMode;
         this.autoJoinTransactions = autoJoinTransactions;
         this.transactionCoordinator = new TransactionCoordinatorImpl(connection, this);
         this.transactionCoordinator.getJdbcCoordinator().getLogicalConnection().addObserver(new ConnectionObserverStatsBridge(factory));
      } else {
         if (connection != null) {
            throw new SessionException("Cannot simultaneously share transaction context and specify connection");
         }

         this.transactionCoordinator = transactionCoordinator;
         this.isTransactionCoordinatorShared = true;
         this.autoJoinTransactions = false;
         if (autoJoinTransactions) {
            LOG.debug("Session creation specified 'autoJoinTransactions', which is invalid in conjunction with sharing JDBC connection between sessions; ignoring");
         }

         if (connectionReleaseMode != transactionCoordinator.getJdbcCoordinator().getLogicalConnection().getConnectionReleaseMode()) {
            LOG.debug("Session creation specified 'connectionReleaseMode', which is invalid in conjunction with sharing JDBC connection between sessions; ignoring");
         }

         this.connectionReleaseMode = transactionCoordinator.getJdbcCoordinator().getLogicalConnection().getConnectionReleaseMode();
         this.transactionObserver = new TransactionObserver() {
            public void afterBegin(TransactionImplementor transaction) {
            }

            public void beforeCompletion(TransactionImplementor transaction) {
               if (SessionImpl.this.isOpen() && flushBeforeCompletionEnabled) {
                  SessionImpl.this.managedFlush();
               }

               SessionImpl.this.beforeTransactionCompletion(transaction);
            }

            public void afterCompletion(boolean successful, TransactionImplementor transaction) {
               SessionImpl.this.afterTransactionCompletion(transaction, successful);
               if (SessionImpl.this.isOpen() && autoCloseSessionEnabled) {
                  SessionImpl.this.managedClose();
               }

               transactionCoordinator.removeObserver(this);
            }
         };
         transactionCoordinator.addObserver(this.transactionObserver);
      }

      this.loadQueryInfluencers = new LoadQueryInfluencers(factory);
      if (factory.getStatistics().isStatisticsEnabled()) {
         factory.getStatisticsImplementor().openSession();
      }

      if (tracing) {
         LOG.tracef("Opened session at timestamp: %s", timestamp);
      }

   }

   public SharedSessionBuilder sessionWithOptions() {
      return new SharedSessionBuilderImpl(this);
   }

   public void clear() {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      this.internalClear();
   }

   private void internalClear() {
      this.persistenceContext.clear();
      this.actionQueue.clear();
   }

   public long getTimestamp() {
      this.checkTransactionSynchStatus();
      return this.timestamp;
   }

   public Connection close() throws HibernateException {
      LOG.trace("Closing session");
      if (this.isClosed()) {
         throw new SessionException("Session was already closed");
      } else {
         if (this.factory.getStatistics().isStatisticsEnabled()) {
            this.factory.getStatisticsImplementor().closeSession();
         }

         Connection var1;
         try {
            if (this.isTransactionCoordinatorShared) {
               if (this.getActionQueue().hasAfterTransactionActions()) {
                  LOG.warn("On close, shared Session had after transaction actions that have not yet been processed");
               } else {
                  this.transactionCoordinator.removeObserver(this.transactionObserver);
               }

               var1 = null;
               return var1;
            }

            var1 = this.transactionCoordinator.close();
         } finally {
            this.setClosed();
            this.cleanup();
         }

         return var1;
      }
   }

   public ConnectionReleaseMode getConnectionReleaseMode() {
      return this.connectionReleaseMode;
   }

   public boolean shouldAutoJoinTransaction() {
      return this.autoJoinTransactions;
   }

   public boolean isAutoCloseSessionEnabled() {
      return this.autoCloseSessionEnabled;
   }

   public boolean isOpen() {
      this.checkTransactionSynchStatus();
      return !this.isClosed();
   }

   public boolean isFlushModeNever() {
      return FlushMode.isManualFlushMode(this.getFlushMode());
   }

   public boolean isFlushBeforeCompletionEnabled() {
      return this.flushBeforeCompletionEnabled;
   }

   public void managedFlush() {
      if (this.isClosed()) {
         LOG.trace("Skipping auto-flush due to session closed");
      } else {
         LOG.trace("Automatically flushing session");
         this.flush();
      }
   }

   public NonFlushedChanges getNonFlushedChanges() throws HibernateException {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      return new NonFlushedChangesImpl(this);
   }

   public void applyNonFlushedChanges(NonFlushedChanges nonFlushedChanges) throws HibernateException {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      this.replacePersistenceContext(((NonFlushedChangesImpl)nonFlushedChanges).getPersistenceContext());
      this.replaceActionQueue(((NonFlushedChangesImpl)nonFlushedChanges).getActionQueue());
   }

   private void replacePersistenceContext(StatefulPersistenceContext persistenceContextNew) {
      if (persistenceContextNew.getSession() != null) {
         throw new IllegalStateException("new persistence context is already connected to a session ");
      } else {
         this.persistenceContext.clear();
         ObjectInputStream ois = null;

         try {
            ois = new ObjectInputStream(new ByteArrayInputStream(serializePersistenceContext(persistenceContextNew)));
            this.persistenceContext = StatefulPersistenceContext.deserialize(ois, this);
         } catch (IOException ex) {
            throw new SerializationException("could not deserialize the persistence context", ex);
         } catch (ClassNotFoundException ex) {
            throw new SerializationException("could not deserialize the persistence context", ex);
         } finally {
            try {
               if (ois != null) {
                  ois.close();
               }
            } catch (IOException var11) {
            }

         }

      }
   }

   private static byte[] serializePersistenceContext(StatefulPersistenceContext pc) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
      ObjectOutputStream oos = null;

      try {
         oos = new ObjectOutputStream(baos);
         pc.serialize(oos);
      } catch (IOException ex) {
         throw new SerializationException("could not serialize persistence context", ex);
      } finally {
         if (oos != null) {
            try {
               oos.close();
            } catch (IOException var10) {
            }
         }

      }

      return baos.toByteArray();
   }

   private void replaceActionQueue(ActionQueue actionQueueNew) {
      if (this.actionQueue.hasAnyQueuedActions()) {
         throw new IllegalStateException("cannot replace an ActionQueue with queued actions ");
      } else {
         this.actionQueue.clear();
         ObjectInputStream ois = null;

         try {
            ois = new ObjectInputStream(new ByteArrayInputStream(serializeActionQueue(actionQueueNew)));
            this.actionQueue = ActionQueue.deserialize(ois, this);
         } catch (IOException ex) {
            throw new SerializationException("could not deserialize the action queue", ex);
         } catch (ClassNotFoundException ex) {
            throw new SerializationException("could not deserialize the action queue", ex);
         } finally {
            try {
               if (ois != null) {
                  ois.close();
               }
            } catch (IOException var11) {
            }

         }

      }
   }

   private static byte[] serializeActionQueue(ActionQueue actionQueue) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
      ObjectOutputStream oos = null;

      try {
         oos = new ObjectOutputStream(baos);
         actionQueue.serialize(oos);
      } catch (IOException ex) {
         throw new SerializationException("could not serialize action queue", ex);
      } finally {
         if (oos != null) {
            try {
               oos.close();
            } catch (IOException var10) {
            }
         }

      }

      return baos.toByteArray();
   }

   public boolean shouldAutoClose() {
      if (this.isClosed()) {
         return false;
      } else {
         return this.sessionOwner != null ? this.sessionOwner.shouldAutoCloseSession() : this.isAutoCloseSessionEnabled();
      }
   }

   public void managedClose() {
      LOG.trace("Automatically closing session");
      this.close();
   }

   public Connection connection() throws HibernateException {
      this.errorIfClosed();
      return this.transactionCoordinator.getJdbcCoordinator().getLogicalConnection().getDistinctConnectionProxy();
   }

   public boolean isConnected() {
      this.checkTransactionSynchStatus();
      return !this.isClosed() && this.transactionCoordinator.getJdbcCoordinator().getLogicalConnection().isOpen();
   }

   public boolean isTransactionInProgress() {
      this.checkTransactionSynchStatus();
      return !this.isClosed() && this.transactionCoordinator.isTransactionInProgress();
   }

   public Connection disconnect() throws HibernateException {
      this.errorIfClosed();
      LOG.debug("Disconnecting session");
      return this.transactionCoordinator.getJdbcCoordinator().getLogicalConnection().manualDisconnect();
   }

   public void reconnect(Connection conn) throws HibernateException {
      this.errorIfClosed();
      LOG.debug("Reconnecting session");
      this.checkTransactionSynchStatus();
      this.transactionCoordinator.getJdbcCoordinator().getLogicalConnection().manualReconnect(conn);
   }

   public void setAutoClear(boolean enabled) {
      this.errorIfClosed();
      this.autoClear = enabled;
   }

   public void disableTransactionAutoJoin() {
      this.errorIfClosed();
      this.autoJoinTransactions = false;
   }

   public void afterOperation(boolean success) {
      if (!this.transactionCoordinator.isTransactionInProgress()) {
         this.transactionCoordinator.afterNonTransactionalQuery(success);
      }

   }

   public void afterTransactionBegin(TransactionImplementor hibernateTransaction) {
      this.errorIfClosed();
      this.interceptor.afterTransactionBegin(hibernateTransaction);
   }

   public void beforeTransactionCompletion(TransactionImplementor hibernateTransaction) {
      LOG.trace("before transaction completion");
      this.actionQueue.beforeTransactionCompletion();

      try {
         this.interceptor.beforeTransactionCompletion(hibernateTransaction);
      } catch (Throwable t) {
         LOG.exceptionInBeforeTransactionCompletionInterceptor(t);
      }

   }

   public void afterTransactionCompletion(TransactionImplementor hibernateTransaction, boolean successful) {
      LOG.trace("after transaction completion");
      this.persistenceContext.afterTransactionCompletion();
      this.actionQueue.afterTransactionCompletion(successful);
      if (hibernateTransaction != null) {
         try {
            this.interceptor.afterTransactionCompletion(hibernateTransaction);
         } catch (Throwable t) {
            LOG.exceptionInAfterTransactionCompletionInterceptor(t);
         }
      }

      if (this.autoClear) {
         this.internalClear();
      }

   }

   public String onPrepareStatement(String sql) {
      this.errorIfClosed();
      sql = this.interceptor.onPrepareStatement(sql);
      if (sql != null && sql.length() != 0) {
         return sql;
      } else {
         throw new AssertionFailure("Interceptor.onPrepareStatement() returned null or empty string.");
      }
   }

   private void cleanup() {
      this.persistenceContext.clear();
   }

   public LockMode getCurrentLockMode(Object object) throws HibernateException {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      if (object == null) {
         throw new NullPointerException("null object passed to getCurrentLockMode()");
      } else {
         if (object instanceof HibernateProxy) {
            object = ((HibernateProxy)object).getHibernateLazyInitializer().getImplementation(this);
            if (object == null) {
               return LockMode.NONE;
            }
         }

         EntityEntry e = this.persistenceContext.getEntry(object);
         if (e == null) {
            throw new TransientObjectException("Given object not associated with the session");
         } else if (e.getStatus() != Status.MANAGED) {
            throw new ObjectDeletedException("The given object was deleted", e.getId(), e.getPersister().getEntityName());
         } else {
            return e.getLockMode();
         }
      }
   }

   public Object getEntityUsingInterceptor(EntityKey key) throws HibernateException {
      this.errorIfClosed();
      Object result = this.persistenceContext.getEntity(key);
      if (result == null) {
         Object newObject = this.interceptor.getEntity(key.getEntityName(), key.getIdentifier());
         if (newObject != null) {
            this.lock(newObject, LockMode.NONE);
         }

         return newObject;
      } else {
         return result;
      }
   }

   private void checkNoUnresolvedActionsBeforeOperation() {
      if (this.persistenceContext.getCascadeLevel() == 0 && this.actionQueue.hasUnresolvedEntityInsertActions()) {
         throw new IllegalStateException("There are delayed insert actions before operation as cascade level 0.");
      }
   }

   private void checkNoUnresolvedActionsAfterOperation() {
      if (this.persistenceContext.getCascadeLevel() == 0) {
         this.actionQueue.checkNoUnresolvedActionsAfterOperation();
      }

   }

   public void saveOrUpdate(Object object) throws HibernateException {
      this.saveOrUpdate((String)null, object);
   }

   public void saveOrUpdate(String entityName, Object obj) throws HibernateException {
      this.fireSaveOrUpdate(new SaveOrUpdateEvent(entityName, obj, this));
   }

   private void fireSaveOrUpdate(SaveOrUpdateEvent event) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      this.checkNoUnresolvedActionsBeforeOperation();

      for(SaveOrUpdateEventListener listener : this.listeners(EventType.SAVE_UPDATE)) {
         listener.onSaveOrUpdate(event);
      }

      this.checkNoUnresolvedActionsAfterOperation();
   }

   private Iterable listeners(EventType type) {
      return this.eventListenerGroup(type).listeners();
   }

   private EventListenerGroup eventListenerGroup(EventType type) {
      return ((EventListenerRegistry)this.factory.getServiceRegistry().getService(EventListenerRegistry.class)).getEventListenerGroup(type);
   }

   public Serializable save(Object obj) throws HibernateException {
      return this.save((String)null, obj);
   }

   public Serializable save(String entityName, Object object) throws HibernateException {
      return this.fireSave(new SaveOrUpdateEvent(entityName, object, this));
   }

   private Serializable fireSave(SaveOrUpdateEvent event) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      this.checkNoUnresolvedActionsBeforeOperation();

      for(SaveOrUpdateEventListener listener : this.listeners(EventType.SAVE)) {
         listener.onSaveOrUpdate(event);
      }

      this.checkNoUnresolvedActionsAfterOperation();
      return event.getResultId();
   }

   public void update(Object obj) throws HibernateException {
      this.update((String)null, obj);
   }

   public void update(String entityName, Object object) throws HibernateException {
      this.fireUpdate(new SaveOrUpdateEvent(entityName, object, this));
   }

   private void fireUpdate(SaveOrUpdateEvent event) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      this.checkNoUnresolvedActionsBeforeOperation();

      for(SaveOrUpdateEventListener listener : this.listeners(EventType.UPDATE)) {
         listener.onSaveOrUpdate(event);
      }

      this.checkNoUnresolvedActionsAfterOperation();
   }

   public void lock(String entityName, Object object, LockMode lockMode) throws HibernateException {
      this.fireLock(new LockEvent(entityName, object, lockMode, this));
   }

   public Session.LockRequest buildLockRequest(LockOptions lockOptions) {
      return new LockRequestImpl(lockOptions);
   }

   public void lock(Object object, LockMode lockMode) throws HibernateException {
      this.fireLock(new LockEvent(object, lockMode, this));
   }

   private void fireLock(String entityName, Object object, LockOptions options) {
      this.fireLock(new LockEvent(entityName, object, options, this));
   }

   private void fireLock(Object object, LockOptions options) {
      this.fireLock(new LockEvent(object, options, this));
   }

   private void fireLock(LockEvent event) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();

      for(LockEventListener listener : this.listeners(EventType.LOCK)) {
         listener.onLock(event);
      }

   }

   public void persist(String entityName, Object object) throws HibernateException {
      this.firePersist(new PersistEvent(entityName, object, this));
   }

   public void persist(Object object) throws HibernateException {
      this.persist((String)null, object);
   }

   public void persist(String entityName, Object object, Map copiedAlready) throws HibernateException {
      this.firePersist(copiedAlready, new PersistEvent(entityName, object, this));
   }

   private void firePersist(Map copiedAlready, PersistEvent event) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();

      for(PersistEventListener listener : this.listeners(EventType.PERSIST)) {
         listener.onPersist(event, copiedAlready);
      }

   }

   private void firePersist(PersistEvent event) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      this.checkNoUnresolvedActionsBeforeOperation();

      for(PersistEventListener listener : this.listeners(EventType.PERSIST)) {
         listener.onPersist(event);
      }

      this.checkNoUnresolvedActionsAfterOperation();
   }

   public void persistOnFlush(String entityName, Object object) throws HibernateException {
      this.firePersistOnFlush(new PersistEvent(entityName, object, this));
   }

   public void persistOnFlush(Object object) throws HibernateException {
      this.persist((String)null, object);
   }

   public void persistOnFlush(String entityName, Object object, Map copiedAlready) throws HibernateException {
      this.firePersistOnFlush(copiedAlready, new PersistEvent(entityName, object, this));
   }

   private void firePersistOnFlush(Map copiedAlready, PersistEvent event) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();

      for(PersistEventListener listener : this.listeners(EventType.PERSIST_ONFLUSH)) {
         listener.onPersist(event, copiedAlready);
      }

   }

   private void firePersistOnFlush(PersistEvent event) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      this.checkNoUnresolvedActionsBeforeOperation();

      for(PersistEventListener listener : this.listeners(EventType.PERSIST_ONFLUSH)) {
         listener.onPersist(event);
      }

      this.checkNoUnresolvedActionsAfterOperation();
   }

   public Object merge(String entityName, Object object) throws HibernateException {
      return this.fireMerge(new MergeEvent(entityName, object, this));
   }

   public Object merge(Object object) throws HibernateException {
      return this.merge((String)null, object);
   }

   public void merge(String entityName, Object object, Map copiedAlready) throws HibernateException {
      this.fireMerge(copiedAlready, new MergeEvent(entityName, object, this));
   }

   private Object fireMerge(MergeEvent event) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      this.checkNoUnresolvedActionsBeforeOperation();

      for(MergeEventListener listener : this.listeners(EventType.MERGE)) {
         listener.onMerge(event);
      }

      this.checkNoUnresolvedActionsAfterOperation();
      return event.getResult();
   }

   private void fireMerge(Map copiedAlready, MergeEvent event) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();

      for(MergeEventListener listener : this.listeners(EventType.MERGE)) {
         listener.onMerge(event, copiedAlready);
      }

   }

   public void delete(Object object) throws HibernateException {
      this.fireDelete(new DeleteEvent(object, this));
   }

   public void delete(String entityName, Object object) throws HibernateException {
      this.fireDelete(new DeleteEvent(entityName, object, this));
   }

   public void delete(String entityName, Object object, boolean isCascadeDeleteEnabled, Set transientEntities) throws HibernateException {
      this.fireDelete(new DeleteEvent(entityName, object, isCascadeDeleteEnabled, this), transientEntities);
   }

   private void fireDelete(DeleteEvent event) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();

      for(DeleteEventListener listener : this.listeners(EventType.DELETE)) {
         listener.onDelete(event);
      }

   }

   private void fireDelete(DeleteEvent event, Set transientEntities) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();

      for(DeleteEventListener listener : this.listeners(EventType.DELETE)) {
         listener.onDelete(event, transientEntities);
      }

   }

   public void load(Object object, Serializable id) throws HibernateException {
      LoadEvent event = new LoadEvent(id, object, this);
      this.fireLoad(event, LoadEventListener.RELOAD);
   }

   public Object load(Class entityClass, Serializable id) throws HibernateException {
      return this.byId(entityClass).getReference(id);
   }

   public Object load(String entityName, Serializable id) throws HibernateException {
      return this.byId(entityName).getReference(id);
   }

   public Object get(Class entityClass, Serializable id) throws HibernateException {
      return this.byId(entityClass).load(id);
   }

   public Object get(String entityName, Serializable id) throws HibernateException {
      return this.byId(entityName).load(id);
   }

   public Object immediateLoad(String entityName, Serializable id) throws HibernateException {
      if (LOG.isDebugEnabled()) {
         EntityPersister persister = this.getFactory().getEntityPersister(entityName);
         LOG.debugf("Initializing proxy: %s", MessageHelper.infoString((EntityPersister)persister, (Object)id, (SessionFactoryImplementor)this.getFactory()));
      }

      LoadEvent event = new LoadEvent(id, entityName, true, this);
      this.fireLoad(event, LoadEventListener.IMMEDIATE_LOAD);
      return event.getResult();
   }

   public Object internalLoad(String entityName, Serializable id, boolean eager, boolean nullable) throws HibernateException {
      LoadEventListener.LoadType type = nullable ? LoadEventListener.INTERNAL_LOAD_NULLABLE : (eager ? LoadEventListener.INTERNAL_LOAD_EAGER : LoadEventListener.INTERNAL_LOAD_LAZY);
      LoadEvent event = new LoadEvent(id, entityName, true, this);
      this.fireLoad(event, type);
      if (!nullable) {
         UnresolvableObjectException.throwIfNull(event.getResult(), id, entityName);
      }

      return event.getResult();
   }

   public Object load(Class entityClass, Serializable id, LockMode lockMode) throws HibernateException {
      return this.byId(entityClass).with(new LockOptions(lockMode)).getReference(id);
   }

   public Object load(Class entityClass, Serializable id, LockOptions lockOptions) throws HibernateException {
      return this.byId(entityClass).with(lockOptions).getReference(id);
   }

   public Object load(String entityName, Serializable id, LockMode lockMode) throws HibernateException {
      return this.byId(entityName).with(new LockOptions(lockMode)).getReference(id);
   }

   public Object load(String entityName, Serializable id, LockOptions lockOptions) throws HibernateException {
      return this.byId(entityName).with(lockOptions).getReference(id);
   }

   public Object get(Class entityClass, Serializable id, LockMode lockMode) throws HibernateException {
      return this.byId(entityClass).with(new LockOptions(lockMode)).load(id);
   }

   public Object get(Class entityClass, Serializable id, LockOptions lockOptions) throws HibernateException {
      return this.byId(entityClass).with(lockOptions).load(id);
   }

   public Object get(String entityName, Serializable id, LockMode lockMode) throws HibernateException {
      return this.byId(entityName).with(new LockOptions(lockMode)).load(id);
   }

   public Object get(String entityName, Serializable id, LockOptions lockOptions) throws HibernateException {
      return this.byId(entityName).with(lockOptions).load(id);
   }

   public IdentifierLoadAccessImpl byId(String entityName) {
      return new IdentifierLoadAccessImpl(entityName);
   }

   public IdentifierLoadAccessImpl byId(Class entityClass) {
      return new IdentifierLoadAccessImpl(entityClass);
   }

   public NaturalIdLoadAccess byNaturalId(String entityName) {
      return new NaturalIdLoadAccessImpl(entityName);
   }

   public NaturalIdLoadAccess byNaturalId(Class entityClass) {
      return new NaturalIdLoadAccessImpl(entityClass);
   }

   public SimpleNaturalIdLoadAccess bySimpleNaturalId(String entityName) {
      return new SimpleNaturalIdLoadAccessImpl(entityName);
   }

   public SimpleNaturalIdLoadAccess bySimpleNaturalId(Class entityClass) {
      return new SimpleNaturalIdLoadAccessImpl(entityClass);
   }

   private void fireLoad(LoadEvent event, LoadEventListener.LoadType loadType) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();

      for(LoadEventListener listener : this.listeners(EventType.LOAD)) {
         listener.onLoad(event, loadType);
      }

   }

   private void fireResolveNaturalId(ResolveNaturalIdEvent event) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();

      for(ResolveNaturalIdEventListener listener : this.listeners(EventType.RESOLVE_NATURAL_ID)) {
         listener.onResolveNaturalId(event);
      }

   }

   public void refresh(Object object) throws HibernateException {
      this.refresh((String)null, (Object)object);
   }

   public void refresh(String entityName, Object object) throws HibernateException {
      this.fireRefresh(new RefreshEvent(entityName, object, this));
   }

   public void refresh(Object object, LockMode lockMode) throws HibernateException {
      this.fireRefresh(new RefreshEvent(object, lockMode, this));
   }

   public void refresh(Object object, LockOptions lockOptions) throws HibernateException {
      this.refresh((String)null, object, lockOptions);
   }

   public void refresh(String entityName, Object object, LockOptions lockOptions) throws HibernateException {
      this.fireRefresh(new RefreshEvent(entityName, object, lockOptions, this));
   }

   public void refresh(Object object, Map refreshedAlready) throws HibernateException {
      this.fireRefresh(refreshedAlready, new RefreshEvent(object, this));
   }

   private void fireRefresh(RefreshEvent event) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();

      for(RefreshEventListener listener : this.listeners(EventType.REFRESH)) {
         listener.onRefresh(event);
      }

   }

   private void fireRefresh(Map refreshedAlready, RefreshEvent event) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();

      for(RefreshEventListener listener : this.listeners(EventType.REFRESH)) {
         listener.onRefresh(event, refreshedAlready);
      }

   }

   public void replicate(Object obj, ReplicationMode replicationMode) throws HibernateException {
      this.fireReplicate(new ReplicateEvent(obj, replicationMode, this));
   }

   public void replicate(String entityName, Object obj, ReplicationMode replicationMode) throws HibernateException {
      this.fireReplicate(new ReplicateEvent(entityName, obj, replicationMode, this));
   }

   private void fireReplicate(ReplicateEvent event) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();

      for(ReplicateEventListener listener : this.listeners(EventType.REPLICATE)) {
         listener.onReplicate(event);
      }

   }

   public void evict(Object object) throws HibernateException {
      this.fireEvict(new EvictEvent(object, this));
   }

   private void fireEvict(EvictEvent event) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();

      for(EvictEventListener listener : this.listeners(EventType.EVICT)) {
         listener.onEvict(event);
      }

   }

   protected boolean autoFlushIfRequired(Set querySpaces) throws HibernateException {
      this.errorIfClosed();
      if (!this.isTransactionInProgress()) {
         return false;
      } else {
         AutoFlushEvent event = new AutoFlushEvent(querySpaces, this);

         for(AutoFlushEventListener listener : this.listeners(EventType.AUTO_FLUSH)) {
            listener.onAutoFlush(event);
         }

         return event.isFlushRequired();
      }
   }

   public boolean isDirty() throws HibernateException {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      LOG.debug("Checking session dirtiness");
      if (this.actionQueue.areInsertionsOrDeletionsQueued()) {
         LOG.debug("Session dirty (scheduled updates and insertions)");
         return true;
      } else {
         DirtyCheckEvent event = new DirtyCheckEvent(this);

         for(DirtyCheckEventListener listener : this.listeners(EventType.DIRTY_CHECK)) {
            listener.onDirtyCheck(event);
         }

         return event.isDirty();
      }
   }

   public void flush() throws HibernateException {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      if (this.persistenceContext.getCascadeLevel() > 0) {
         throw new HibernateException("Flush during cascade is dangerous");
      } else {
         FlushEvent flushEvent = new FlushEvent(this);

         for(FlushEventListener listener : this.listeners(EventType.FLUSH)) {
            listener.onFlush(flushEvent);
         }

      }
   }

   public void forceFlush(EntityEntry entityEntry) throws HibernateException {
      this.errorIfClosed();
      if (LOG.isDebugEnabled()) {
         LOG.debugf("Flushing to force deletion of re-saved object: %s", MessageHelper.infoString((EntityPersister)entityEntry.getPersister(), (Object)entityEntry.getId(), (SessionFactoryImplementor)this.getFactory()));
      }

      if (this.persistenceContext.getCascadeLevel() > 0) {
         throw new ObjectDeletedException("deleted object would be re-saved by cascade (remove deleted object from associations)", entityEntry.getId(), entityEntry.getPersister().getEntityName());
      } else {
         this.flush();
      }
   }

   public List list(String query, QueryParameters queryParameters) throws HibernateException {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      queryParameters.validateParameters();
      HQLQueryPlan plan = this.getHQLQueryPlan(query, false);
      this.autoFlushIfRequired(plan.getQuerySpaces());
      List results = Collections.EMPTY_LIST;
      boolean success = false;
      ++this.dontFlushFromFind;

      try {
         results = plan.performList(queryParameters, this);
         success = true;
      } finally {
         --this.dontFlushFromFind;
         this.afterOperation(success);
      }

      return results;
   }

   public int executeUpdate(String query, QueryParameters queryParameters) throws HibernateException {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      queryParameters.validateParameters();
      HQLQueryPlan plan = this.getHQLQueryPlan(query, false);
      this.autoFlushIfRequired(plan.getQuerySpaces());
      boolean success = false;
      int result = 0;

      try {
         result = plan.performExecuteUpdate(queryParameters, this);
         success = true;
      } finally {
         this.afterOperation(success);
      }

      return result;
   }

   public int executeNativeUpdate(NativeSQLQuerySpecification nativeQuerySpecification, QueryParameters queryParameters) throws HibernateException {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      queryParameters.validateParameters();
      NativeSQLQueryPlan plan = this.getNativeSQLQueryPlan(nativeQuerySpecification);
      this.autoFlushIfRequired(plan.getCustomQuery().getQuerySpaces());
      boolean success = false;
      int result = 0;

      try {
         result = plan.performExecuteUpdate(queryParameters, this);
         success = true;
      } finally {
         this.afterOperation(success);
      }

      return result;
   }

   public Iterator iterate(String query, QueryParameters queryParameters) throws HibernateException {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      queryParameters.validateParameters();
      HQLQueryPlan plan = this.getHQLQueryPlan(query, true);
      this.autoFlushIfRequired(plan.getQuerySpaces());
      ++this.dontFlushFromFind;

      Iterator var4;
      try {
         var4 = plan.performIterate(queryParameters, this);
      } finally {
         --this.dontFlushFromFind;
      }

      return var4;
   }

   public ScrollableResults scroll(String query, QueryParameters queryParameters) throws HibernateException {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      HQLQueryPlan plan = this.getHQLQueryPlan(query, false);
      this.autoFlushIfRequired(plan.getQuerySpaces());
      ++this.dontFlushFromFind;

      ScrollableResults var4;
      try {
         var4 = plan.performScroll(queryParameters, this);
      } finally {
         --this.dontFlushFromFind;
      }

      return var4;
   }

   public Query createFilter(Object collection, String queryString) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      CollectionFilterImpl filter = new CollectionFilterImpl(queryString, collection, this, this.getFilterQueryPlan(collection, queryString, (QueryParameters)null, false).getParameterMetadata());
      filter.setComment(queryString);
      return filter;
   }

   public Query getNamedQuery(String queryName) throws MappingException {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      return super.getNamedQuery(queryName);
   }

   public Object instantiate(String entityName, Serializable id) throws HibernateException {
      return this.instantiate(this.factory.getEntityPersister(entityName), id);
   }

   public Object instantiate(EntityPersister persister, Serializable id) throws HibernateException {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      Object result = this.interceptor.instantiate(persister.getEntityName(), persister.getEntityMetamodel().getEntityMode(), id);
      if (result == null) {
         result = persister.instantiate(id, this);
      }

      return result;
   }

   public void setFlushMode(FlushMode flushMode) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      LOG.tracev("Setting flush mode to: {0}", flushMode);
      this.flushMode = flushMode;
   }

   public FlushMode getFlushMode() {
      this.checkTransactionSynchStatus();
      return this.flushMode;
   }

   public CacheMode getCacheMode() {
      this.checkTransactionSynchStatus();
      return this.cacheMode;
   }

   public void setCacheMode(CacheMode cacheMode) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      LOG.tracev("Setting cache mode to: {0}", cacheMode);
      this.cacheMode = cacheMode;
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

   public EntityPersister getEntityPersister(String entityName, Object object) {
      this.errorIfClosed();
      if (entityName == null) {
         return this.factory.getEntityPersister(this.guessEntityName(object));
      } else {
         try {
            return this.factory.getEntityPersister(entityName).getSubclassEntityPersister(object, this.getFactory());
         } catch (HibernateException e) {
            try {
               return this.getEntityPersister((String)null, object);
            } catch (HibernateException var5) {
               throw e;
            }
         }
      }
   }

   public Serializable getIdentifier(Object object) throws HibernateException {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      if (object instanceof HibernateProxy) {
         LazyInitializer li = ((HibernateProxy)object).getHibernateLazyInitializer();
         if (li.getSession() != this) {
            throw new TransientObjectException("The proxy was not associated with this session");
         } else {
            return li.getIdentifier();
         }
      } else {
         EntityEntry entry = this.persistenceContext.getEntry(object);
         if (entry == null) {
            throw new TransientObjectException("The instance was not associated with this session");
         } else {
            return entry.getId();
         }
      }
   }

   public Serializable getContextEntityIdentifier(Object object) {
      this.errorIfClosed();
      if (object instanceof HibernateProxy) {
         return this.getProxyIdentifier(object);
      } else {
         EntityEntry entry = this.persistenceContext.getEntry(object);
         return entry != null ? entry.getId() : null;
      }
   }

   private Serializable getProxyIdentifier(Object proxy) {
      return ((HibernateProxy)proxy).getHibernateLazyInitializer().getIdentifier();
   }

   private FilterQueryPlan getFilterQueryPlan(Object collection, String filter, QueryParameters parameters, boolean shallow) throws HibernateException {
      if (collection == null) {
         throw new NullPointerException("null collection passed to filter");
      } else {
         CollectionEntry entry = this.persistenceContext.getCollectionEntryOrNull(collection);
         CollectionPersister roleBeforeFlush = entry == null ? null : entry.getLoadedPersister();
         FilterQueryPlan plan = null;
         if (roleBeforeFlush == null) {
            this.flush();
            entry = this.persistenceContext.getCollectionEntryOrNull(collection);
            CollectionPersister roleAfterFlush = entry == null ? null : entry.getLoadedPersister();
            if (roleAfterFlush == null) {
               throw new QueryException("The collection was unreferenced");
            }

            plan = this.factory.getQueryPlanCache().getFilterQueryPlan(filter, roleAfterFlush.getRole(), shallow, this.getEnabledFilters());
         } else {
            plan = this.factory.getQueryPlanCache().getFilterQueryPlan(filter, roleBeforeFlush.getRole(), shallow, this.getEnabledFilters());
            if (this.autoFlushIfRequired(plan.getQuerySpaces())) {
               entry = this.persistenceContext.getCollectionEntryOrNull(collection);
               CollectionPersister roleAfterFlush = entry == null ? null : entry.getLoadedPersister();
               if (roleBeforeFlush != roleAfterFlush) {
                  if (roleAfterFlush == null) {
                     throw new QueryException("The collection was dereferenced");
                  }

                  plan = this.factory.getQueryPlanCache().getFilterQueryPlan(filter, roleAfterFlush.getRole(), shallow, this.getEnabledFilters());
               }
            }
         }

         if (parameters != null) {
            parameters.getPositionalParameterValues()[0] = entry.getLoadedKey();
            parameters.getPositionalParameterTypes()[0] = entry.getLoadedPersister().getKeyType();
         }

         return plan;
      }
   }

   public List listFilter(Object collection, String filter, QueryParameters queryParameters) throws HibernateException {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      FilterQueryPlan plan = this.getFilterQueryPlan(collection, filter, queryParameters, false);
      List results = Collections.EMPTY_LIST;
      boolean success = false;
      ++this.dontFlushFromFind;

      try {
         results = plan.performList(queryParameters, this);
         success = true;
      } finally {
         --this.dontFlushFromFind;
         this.afterOperation(success);
      }

      return results;
   }

   public Iterator iterateFilter(Object collection, String filter, QueryParameters queryParameters) throws HibernateException {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      FilterQueryPlan plan = this.getFilterQueryPlan(collection, filter, queryParameters, true);
      return plan.performIterate(queryParameters, this);
   }

   public Criteria createCriteria(Class persistentClass, String alias) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      return new CriteriaImpl(persistentClass.getName(), alias, this);
   }

   public Criteria createCriteria(String entityName, String alias) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      return new CriteriaImpl(entityName, alias, this);
   }

   public Criteria createCriteria(Class persistentClass) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      return new CriteriaImpl(persistentClass.getName(), this);
   }

   public Criteria createCriteria(String entityName) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      return new CriteriaImpl(entityName, this);
   }

   public ScrollableResults scroll(CriteriaImpl criteria, ScrollMode scrollMode) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      String entityName = criteria.getEntityOrClassName();
      CriteriaLoader loader = new CriteriaLoader(this.getOuterJoinLoadable(entityName), this.factory, criteria, entityName, this.getLoadQueryInfluencers());
      this.autoFlushIfRequired(loader.getQuerySpaces());
      ++this.dontFlushFromFind;

      ScrollableResults var5;
      try {
         var5 = loader.scroll(this, scrollMode);
      } finally {
         --this.dontFlushFromFind;
      }

      return var5;
   }

   public List list(CriteriaImpl criteria) throws HibernateException {
      NaturalIdLoadAccess naturalIdLoadAccess = this.tryNaturalIdLoadAccess(criteria);
      if (naturalIdLoadAccess != null) {
         return Arrays.asList(naturalIdLoadAccess.load());
      } else {
         this.errorIfClosed();
         this.checkTransactionSynchStatus();
         String[] implementors = this.factory.getImplementors(criteria.getEntityOrClassName());
         int size = implementors.length;
         CriteriaLoader[] loaders = new CriteriaLoader[size];
         Set spaces = new HashSet();

         for(int i = 0; i < size; ++i) {
            loaders[i] = new CriteriaLoader(this.getOuterJoinLoadable(implementors[i]), this.factory, criteria, implementors[i], this.getLoadQueryInfluencers());
            spaces.addAll(loaders[i].getQuerySpaces());
         }

         this.autoFlushIfRequired(spaces);
         List results = Collections.EMPTY_LIST;
         ++this.dontFlushFromFind;
         boolean success = false;

         try {
            for(int i = 0; i < size; ++i) {
               List currentResults = loaders[i].list(this);
               currentResults.addAll(results);
               results = currentResults;
            }

            success = true;
         } finally {
            --this.dontFlushFromFind;
            this.afterOperation(success);
         }

         return results;
      }
   }

   private NaturalIdLoadAccess tryNaturalIdLoadAccess(CriteriaImpl criteria) {
      if (!criteria.isLookupByNaturalKey()) {
         return null;
      } else {
         String entityName = criteria.getEntityOrClassName();
         EntityPersister entityPersister = this.factory.getEntityPersister(entityName);
         if (!entityPersister.hasNaturalIdentifier()) {
            return null;
         } else {
            CriteriaImpl.CriterionEntry criterionEntry = (CriteriaImpl.CriterionEntry)criteria.iterateExpressionEntries().next();
            NaturalIdentifier naturalIdentifier = (NaturalIdentifier)criterionEntry.getCriterion();
            Map<String, Object> naturalIdValues = naturalIdentifier.getNaturalIdValues();
            int[] naturalIdentifierProperties = entityPersister.getNaturalIdentifierProperties();
            if (naturalIdentifierProperties.length != naturalIdValues.size()) {
               return null;
            } else {
               String[] propertyNames = entityPersister.getPropertyNames();
               NaturalIdLoadAccess naturalIdLoader = this.byNaturalId(entityName);

               for(int i = 0; i < naturalIdentifierProperties.length; ++i) {
                  String naturalIdProperty = propertyNames[naturalIdentifierProperties[i]];
                  Object naturalIdValue = naturalIdValues.get(naturalIdProperty);
                  if (naturalIdValue == null) {
                     return null;
                  }

                  naturalIdLoader.using(naturalIdProperty, naturalIdValue);
               }

               LOG.warn("Session.byNaturalId(" + entityName + ") should be used for naturalId queries instead of Restrictions.naturalId() from a Criteria");
               return naturalIdLoader;
            }
         }
      }
   }

   private OuterJoinLoadable getOuterJoinLoadable(String entityName) throws MappingException {
      EntityPersister persister = this.factory.getEntityPersister(entityName);
      if (!(persister instanceof OuterJoinLoadable)) {
         throw new MappingException("class persister is not OuterJoinLoadable: " + entityName);
      } else {
         return (OuterJoinLoadable)persister;
      }
   }

   public boolean contains(Object object) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      if (object instanceof HibernateProxy) {
         LazyInitializer li = ((HibernateProxy)object).getHibernateLazyInitializer();
         if (li.isUninitialized()) {
            return li.getSession() == this;
         }

         object = li.getImplementation();
      }

      EntityEntry entry = this.persistenceContext.getEntry(object);
      return entry != null && entry.getStatus() != Status.DELETED && entry.getStatus() != Status.GONE;
   }

   public Query createQuery(String queryString) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      return super.createQuery(queryString);
   }

   public SQLQuery createSQLQuery(String sql) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      return super.createSQLQuery(sql);
   }

   public ScrollableResults scrollCustomQuery(CustomQuery customQuery, QueryParameters queryParameters) throws HibernateException {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      if (LOG.isTraceEnabled()) {
         LOG.tracev("Scroll SQL query: {0}", customQuery.getSQL());
      }

      CustomLoader loader = new CustomLoader(customQuery, this.getFactory());
      this.autoFlushIfRequired(loader.getQuerySpaces());
      ++this.dontFlushFromFind;

      ScrollableResults var4;
      try {
         var4 = loader.scroll(queryParameters, this);
      } finally {
         --this.dontFlushFromFind;
      }

      return var4;
   }

   public List listCustomQuery(CustomQuery customQuery, QueryParameters queryParameters) throws HibernateException {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      if (LOG.isTraceEnabled()) {
         LOG.tracev("SQL query: {0}", customQuery.getSQL());
      }

      CustomLoader loader = new CustomLoader(customQuery, this.getFactory());
      this.autoFlushIfRequired(loader.getQuerySpaces());
      ++this.dontFlushFromFind;
      boolean success = false;

      List var6;
      try {
         List results = loader.list(this, queryParameters);
         success = true;
         var6 = results;
      } finally {
         --this.dontFlushFromFind;
         this.afterOperation(success);
      }

      return var6;
   }

   public SessionFactoryImplementor getSessionFactory() {
      this.checkTransactionSynchStatus();
      return this.factory;
   }

   public void initializeCollection(PersistentCollection collection, boolean writing) throws HibernateException {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      InitializeCollectionEvent event = new InitializeCollectionEvent(collection, this);

      for(InitializeCollectionEventListener listener : this.listeners(EventType.INIT_COLLECTION)) {
         listener.onInitializeCollection(event);
      }

   }

   public String bestGuessEntityName(Object object) {
      if (object instanceof HibernateProxy) {
         LazyInitializer initializer = ((HibernateProxy)object).getHibernateLazyInitializer();
         if (initializer.isUninitialized()) {
            return initializer.getEntityName();
         }

         object = initializer.getImplementation();
      }

      EntityEntry entry = this.persistenceContext.getEntry(object);
      return entry == null ? this.guessEntityName(object) : entry.getPersister().getEntityName();
   }

   public String getEntityName(Object object) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      if (object instanceof HibernateProxy) {
         if (!this.persistenceContext.containsProxy(object)) {
            throw new TransientObjectException("proxy was not associated with the session");
         }

         object = ((HibernateProxy)object).getHibernateLazyInitializer().getImplementation();
      }

      EntityEntry entry = this.persistenceContext.getEntry(object);
      if (entry == null) {
         this.throwTransientObjectException(object);
      }

      return entry.getPersister().getEntityName();
   }

   private void throwTransientObjectException(Object object) throws HibernateException {
      throw new TransientObjectException("object references an unsaved transient instance - save the transient instance before flushing: " + this.guessEntityName(object));
   }

   public String guessEntityName(Object object) throws HibernateException {
      this.errorIfClosed();
      return this.entityNameResolver.resolveEntityName(object);
   }

   public void cancelQuery() throws HibernateException {
      this.errorIfClosed();
      this.getTransactionCoordinator().getJdbcCoordinator().cancelLastQuery();
   }

   public Interceptor getInterceptor() {
      this.checkTransactionSynchStatus();
      return this.interceptor;
   }

   public int getDontFlushFromFind() {
      return this.dontFlushFromFind;
   }

   public String toString() {
      StringBuilder buf = (new StringBuilder(500)).append("SessionImpl(");
      if (!this.isClosed()) {
         buf.append(this.persistenceContext).append(";").append(this.actionQueue);
      } else {
         buf.append("<closed>");
      }

      return buf.append(')').toString();
   }

   public ActionQueue getActionQueue() {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      return this.actionQueue;
   }

   public PersistenceContext getPersistenceContext() {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      return this.persistenceContext;
   }

   public SessionStatistics getStatistics() {
      this.checkTransactionSynchStatus();
      return new SessionStatisticsImpl(this);
   }

   public boolean isEventSource() {
      this.checkTransactionSynchStatus();
      return true;
   }

   public boolean isDefaultReadOnly() {
      return this.persistenceContext.isDefaultReadOnly();
   }

   public void setDefaultReadOnly(boolean defaultReadOnly) {
      this.persistenceContext.setDefaultReadOnly(defaultReadOnly);
   }

   public boolean isReadOnly(Object entityOrProxy) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      return this.persistenceContext.isReadOnly(entityOrProxy);
   }

   public void setReadOnly(Object entity, boolean readOnly) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      this.persistenceContext.setReadOnly(entity, readOnly);
   }

   public void doWork(final Work work) throws HibernateException {
      WorkExecutorVisitable<Void> realWork = new WorkExecutorVisitable() {
         public Void accept(WorkExecutor workExecutor, Connection connection) throws SQLException {
            workExecutor.executeWork(work, connection);
            return null;
         }
      };
      this.doWork(realWork);
   }

   public Object doReturningWork(final ReturningWork work) throws HibernateException {
      WorkExecutorVisitable<T> realWork = new WorkExecutorVisitable() {
         public Object accept(WorkExecutor workExecutor, Connection connection) throws SQLException {
            return workExecutor.executeReturningWork(work, connection);
         }
      };
      return this.doWork(realWork);
   }

   private Object doWork(WorkExecutorVisitable work) throws HibernateException {
      return this.transactionCoordinator.getJdbcCoordinator().coordinateWork(work);
   }

   public void afterScrollOperation() {
   }

   public TransactionCoordinator getTransactionCoordinator() {
      this.errorIfClosed();
      return this.transactionCoordinator;
   }

   public LoadQueryInfluencers getLoadQueryInfluencers() {
      return this.loadQueryInfluencers;
   }

   public Filter getEnabledFilter(String filterName) {
      this.checkTransactionSynchStatus();
      return this.loadQueryInfluencers.getEnabledFilter(filterName);
   }

   public Filter enableFilter(String filterName) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      return this.loadQueryInfluencers.enableFilter(filterName);
   }

   public void disableFilter(String filterName) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      this.loadQueryInfluencers.disableFilter(filterName);
   }

   public Object getFilterParameterValue(String filterParameterName) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      return this.loadQueryInfluencers.getFilterParameterValue(filterParameterName);
   }

   public Type getFilterParameterType(String filterParameterName) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      return this.loadQueryInfluencers.getFilterParameterType(filterParameterName);
   }

   public Map getEnabledFilters() {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      return this.loadQueryInfluencers.getEnabledFilters();
   }

   public String getFetchProfile() {
      this.checkTransactionSynchStatus();
      return this.loadQueryInfluencers.getInternalFetchProfile();
   }

   public void setFetchProfile(String fetchProfile) {
      this.errorIfClosed();
      this.checkTransactionSynchStatus();
      this.loadQueryInfluencers.setInternalFetchProfile(fetchProfile);
   }

   public boolean isFetchProfileEnabled(String name) throws UnknownProfileException {
      return this.loadQueryInfluencers.isFetchProfileEnabled(name);
   }

   public void enableFetchProfile(String name) throws UnknownProfileException {
      this.loadQueryInfluencers.enableFetchProfile(name);
   }

   public void disableFetchProfile(String name) throws UnknownProfileException {
      this.loadQueryInfluencers.disableFetchProfile(name);
   }

   private void checkTransactionSynchStatus() {
      if (!this.isClosed()) {
         this.transactionCoordinator.pulse();
      }

   }

   private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
      LOG.trace("Deserializing session");
      ois.defaultReadObject();
      this.entityNameResolver = new CoordinatingEntityNameResolver();
      this.connectionReleaseMode = ConnectionReleaseMode.parse((String)ois.readObject());
      this.autoClear = ois.readBoolean();
      this.autoJoinTransactions = ois.readBoolean();
      this.flushMode = FlushMode.valueOf((String)ois.readObject());
      this.cacheMode = CacheMode.valueOf((String)ois.readObject());
      this.flushBeforeCompletionEnabled = ois.readBoolean();
      this.autoCloseSessionEnabled = ois.readBoolean();
      this.interceptor = (Interceptor)ois.readObject();
      this.factory = SessionFactoryImpl.deserialize(ois);
      this.sessionOwner = (SessionOwner)ois.readObject();
      this.transactionCoordinator = TransactionCoordinatorImpl.deserialize(ois, this);
      this.persistenceContext = StatefulPersistenceContext.deserialize(ois, this);
      this.actionQueue = ActionQueue.deserialize(ois, this);
      this.loadQueryInfluencers = (LoadQueryInfluencers)ois.readObject();

      for(String filterName : this.loadQueryInfluencers.getEnabledFilterNames()) {
         ((FilterImpl)this.loadQueryInfluencers.getEnabledFilter(filterName)).afterDeserialize(this.factory);
      }

   }

   private void writeObject(ObjectOutputStream oos) throws IOException {
      if (!this.transactionCoordinator.getJdbcCoordinator().getLogicalConnection().isReadyForSerialization()) {
         throw new IllegalStateException("Cannot serialize a session while connected");
      } else {
         LOG.trace("Serializing session");
         oos.defaultWriteObject();
         oos.writeObject(this.connectionReleaseMode.toString());
         oos.writeBoolean(this.autoClear);
         oos.writeBoolean(this.autoJoinTransactions);
         oos.writeObject(this.flushMode.toString());
         oos.writeObject(this.cacheMode.name());
         oos.writeBoolean(this.flushBeforeCompletionEnabled);
         oos.writeBoolean(this.autoCloseSessionEnabled);
         oos.writeObject(this.interceptor);
         this.factory.serialize(oos);
         oos.writeObject(this.sessionOwner);
         this.transactionCoordinator.serialize(oos);
         this.persistenceContext.serialize(oos);
         this.actionQueue.serialize(oos);
         oos.writeObject(this.loadQueryInfluencers);
      }
   }

   public TypeHelper getTypeHelper() {
      return this.getSessionFactory().getTypeHelper();
   }

   public LobHelper getLobHelper() {
      if (this.lobHelper == null) {
         this.lobHelper = new LobHelperImpl(this);
      }

      return this.lobHelper;
   }

   private EntityPersister locateEntityPersister(String entityName) {
      EntityPersister entityPersister = this.factory.getEntityPersister(entityName);
      if (entityPersister == null) {
         throw new HibernateException("Unable to locate persister: " + entityName);
      } else {
         return entityPersister;
      }
   }

   static {
      tracing = LOG.isTraceEnabled();
   }

   private static class LobHelperImpl implements LobHelper {
      private final SessionImpl session;

      private LobHelperImpl(SessionImpl session) {
         this.session = session;
      }

      public Blob createBlob(byte[] bytes) {
         return this.lobCreator().createBlob(bytes);
      }

      private LobCreator lobCreator() {
         return this.session.getFactory().getJdbcServices().getLobCreator(this.session);
      }

      public Blob createBlob(InputStream stream, long length) {
         return this.lobCreator().createBlob(stream, length);
      }

      public Clob createClob(String string) {
         return this.lobCreator().createClob(string);
      }

      public Clob createClob(Reader reader, long length) {
         return this.lobCreator().createClob(reader, length);
      }

      public NClob createNClob(String string) {
         return this.lobCreator().createNClob(string);
      }

      public NClob createNClob(Reader reader, long length) {
         return this.lobCreator().createNClob(reader, length);
      }
   }

   private static class SharedSessionBuilderImpl extends SessionFactoryImpl.SessionBuilderImpl implements SharedSessionBuilder {
      private final SessionImpl session;
      private boolean shareTransactionContext;

      private SharedSessionBuilderImpl(SessionImpl session) {
         super(session.factory);
         this.session = session;
         super.owner(session.sessionOwner);
         super.tenantIdentifier(session.getTenantIdentifier());
      }

      public SessionBuilder tenantIdentifier(String tenantIdentifier) {
         throw new SessionException("Cannot redefine tenant identifier on child session");
      }

      protected TransactionCoordinatorImpl getTransactionCoordinator() {
         return this.shareTransactionContext ? this.session.transactionCoordinator : super.getTransactionCoordinator();
      }

      public SharedSessionBuilder interceptor() {
         return this.interceptor(this.session.interceptor);
      }

      public SharedSessionBuilder connection() {
         this.shareTransactionContext = true;
         return this;
      }

      public SharedSessionBuilder connectionReleaseMode() {
         return this.connectionReleaseMode(this.session.connectionReleaseMode);
      }

      public SharedSessionBuilder autoJoinTransactions() {
         return this.autoJoinTransactions(this.session.autoJoinTransactions);
      }

      public SharedSessionBuilder autoClose() {
         return this.autoClose(this.session.autoCloseSessionEnabled);
      }

      public SharedSessionBuilder flushBeforeCompletion() {
         return this.flushBeforeCompletion(this.session.flushBeforeCompletionEnabled);
      }

      /** @deprecated */
      @Deprecated
      public SharedSessionBuilder transactionContext() {
         return this.connection();
      }

      public SharedSessionBuilder interceptor(Interceptor interceptor) {
         return (SharedSessionBuilder)super.interceptor(interceptor);
      }

      public SharedSessionBuilder noInterceptor() {
         return (SharedSessionBuilder)super.noInterceptor();
      }

      public SharedSessionBuilder connection(Connection connection) {
         return (SharedSessionBuilder)super.connection(connection);
      }

      public SharedSessionBuilder connectionReleaseMode(ConnectionReleaseMode connectionReleaseMode) {
         return (SharedSessionBuilder)super.connectionReleaseMode(connectionReleaseMode);
      }

      public SharedSessionBuilder autoJoinTransactions(boolean autoJoinTransactions) {
         return (SharedSessionBuilder)super.autoJoinTransactions(autoJoinTransactions);
      }

      public SharedSessionBuilder autoClose(boolean autoClose) {
         return (SharedSessionBuilder)super.autoClose(autoClose);
      }

      public SharedSessionBuilder flushBeforeCompletion(boolean flushBeforeCompletion) {
         return (SharedSessionBuilder)super.flushBeforeCompletion(flushBeforeCompletion);
      }
   }

   private class CoordinatingEntityNameResolver implements EntityNameResolver {
      private CoordinatingEntityNameResolver() {
      }

      public String resolveEntityName(Object entity) {
         String entityName = SessionImpl.this.interceptor.getEntityName(entity);
         if (entityName != null) {
            return entityName;
         } else {
            for(EntityNameResolver resolver : SessionImpl.this.factory.iterateEntityNameResolvers()) {
               entityName = resolver.resolveEntityName(entity);
               if (entityName != null) {
                  break;
               }
            }

            return entityName != null ? entityName : entity.getClass().getName();
         }
      }
   }

   private class LockRequestImpl implements Session.LockRequest {
      private final LockOptions lockOptions;

      private LockRequestImpl(LockOptions lo) {
         this.lockOptions = new LockOptions();
         LockOptions.copy(lo, this.lockOptions);
      }

      public LockMode getLockMode() {
         return this.lockOptions.getLockMode();
      }

      public Session.LockRequest setLockMode(LockMode lockMode) {
         this.lockOptions.setLockMode(lockMode);
         return this;
      }

      public int getTimeOut() {
         return this.lockOptions.getTimeOut();
      }

      public Session.LockRequest setTimeOut(int timeout) {
         this.lockOptions.setTimeOut(timeout);
         return this;
      }

      public boolean getScope() {
         return this.lockOptions.getScope();
      }

      public Session.LockRequest setScope(boolean scope) {
         this.lockOptions.setScope(scope);
         return this;
      }

      public void lock(String entityName, Object object) throws HibernateException {
         SessionImpl.this.fireLock(entityName, object, this.lockOptions);
      }

      public void lock(Object object) throws HibernateException {
         SessionImpl.this.fireLock(object, this.lockOptions);
      }
   }

   private class IdentifierLoadAccessImpl implements IdentifierLoadAccess {
      private final EntityPersister entityPersister;
      private LockOptions lockOptions;

      private IdentifierLoadAccessImpl(EntityPersister entityPersister) {
         this.entityPersister = entityPersister;
      }

      private IdentifierLoadAccessImpl(String entityName) {
         this((EntityPersister)SessionImpl.this.locateEntityPersister(entityName));
      }

      private IdentifierLoadAccessImpl(Class entityClass) {
         this((String)entityClass.getName());
      }

      public final IdentifierLoadAccessImpl with(LockOptions lockOptions) {
         this.lockOptions = lockOptions;
         return this;
      }

      public final Object getReference(Serializable id) {
         if (this.lockOptions != null) {
            LoadEvent event = new LoadEvent(id, this.entityPersister.getEntityName(), this.lockOptions, SessionImpl.this);
            SessionImpl.this.fireLoad(event, LoadEventListener.LOAD);
            return event.getResult();
         } else {
            LoadEvent event = new LoadEvent(id, this.entityPersister.getEntityName(), false, SessionImpl.this);
            boolean success = false;

            Object var4;
            try {
               SessionImpl.this.fireLoad(event, LoadEventListener.LOAD);
               if (event.getResult() == null) {
                  SessionImpl.this.getFactory().getEntityNotFoundDelegate().handleEntityNotFound(this.entityPersister.getEntityName(), id);
               }

               success = true;
               var4 = event.getResult();
            } finally {
               SessionImpl.this.afterOperation(success);
            }

            return var4;
         }
      }

      public final Object load(Serializable id) {
         if (this.lockOptions != null) {
            LoadEvent event = new LoadEvent(id, this.entityPersister.getEntityName(), this.lockOptions, SessionImpl.this);
            SessionImpl.this.fireLoad(event, LoadEventListener.GET);
            return event.getResult();
         } else {
            LoadEvent event = new LoadEvent(id, this.entityPersister.getEntityName(), false, SessionImpl.this);
            boolean success = false;

            Object var4;
            try {
               SessionImpl.this.fireLoad(event, LoadEventListener.GET);
               success = true;
               var4 = event.getResult();
            } finally {
               SessionImpl.this.afterOperation(success);
            }

            return var4;
         }
      }
   }

   private abstract class BaseNaturalIdLoadAccessImpl {
      private final EntityPersister entityPersister;
      private LockOptions lockOptions;
      private boolean synchronizationEnabled;

      private BaseNaturalIdLoadAccessImpl(EntityPersister entityPersister) {
         this.synchronizationEnabled = true;
         this.entityPersister = entityPersister;
         if (!entityPersister.hasNaturalIdentifier()) {
            throw new HibernateException(String.format("Entity [%s] did not define a natural id", entityPersister.getEntityName()));
         }
      }

      private BaseNaturalIdLoadAccessImpl(String entityName) {
         this((EntityPersister)SessionImpl.this.locateEntityPersister(entityName));
      }

      private BaseNaturalIdLoadAccessImpl(Class entityClass) {
         this((String)entityClass.getName());
      }

      public BaseNaturalIdLoadAccessImpl with(LockOptions lockOptions) {
         this.lockOptions = lockOptions;
         return this;
      }

      protected void synchronizationEnabled(boolean synchronizationEnabled) {
         this.synchronizationEnabled = synchronizationEnabled;
      }

      protected final Serializable resolveNaturalId(Map naturalIdParameters) {
         this.performAnyNeededCrossReferenceSynchronizations();
         ResolveNaturalIdEvent event = new ResolveNaturalIdEvent(naturalIdParameters, this.entityPersister, SessionImpl.this);
         SessionImpl.this.fireResolveNaturalId(event);
         return event.getEntityId() == PersistenceContext.NaturalIdHelper.INVALID_NATURAL_ID_REFERENCE ? null : event.getEntityId();
      }

      protected void performAnyNeededCrossReferenceSynchronizations() {
         if (this.synchronizationEnabled) {
            if (!this.entityPersister.getEntityMetamodel().hasImmutableNaturalId()) {
               if (SessionImpl.this.isTransactionInProgress()) {
                  for(Serializable pk : SessionImpl.this.getPersistenceContext().getNaturalIdHelper().getCachedPkResolutions(this.entityPersister)) {
                     EntityKey entityKey = SessionImpl.this.generateEntityKey(pk, this.entityPersister);
                     Object entity = SessionImpl.this.getPersistenceContext().getEntity(entityKey);
                     EntityEntry entry = SessionImpl.this.getPersistenceContext().getEntry(entity);
                     if (entry == null) {
                        if (SessionImpl.LOG.isDebugEnabled()) {
                           SessionImpl.LOG.debug("Cached natural-id/pk resolution linked to null EntityEntry in persistence context : " + MessageHelper.infoString((EntityPersister)this.entityPersister, (Object)pk, (SessionFactoryImplementor)SessionImpl.this.getFactory()));
                        }
                     } else if (entry.requiresDirtyCheck(entity) && entry.getStatus() == Status.MANAGED) {
                        SessionImpl.this.getPersistenceContext().getNaturalIdHelper().handleSynchronization(this.entityPersister, pk, entity);
                     }
                  }

               }
            }
         }
      }

      protected final IdentifierLoadAccess getIdentifierLoadAccess() {
         IdentifierLoadAccessImpl identifierLoadAccess = SessionImpl.this.new IdentifierLoadAccessImpl(this.entityPersister);
         if (this.lockOptions != null) {
            identifierLoadAccess.with(this.lockOptions);
         }

         return identifierLoadAccess;
      }

      protected EntityPersister entityPersister() {
         return this.entityPersister;
      }
   }

   private class NaturalIdLoadAccessImpl extends BaseNaturalIdLoadAccessImpl implements NaturalIdLoadAccess {
      private final Map naturalIdParameters;

      private NaturalIdLoadAccessImpl(EntityPersister entityPersister) {
         super(entityPersister, null);
         this.naturalIdParameters = new LinkedHashMap();
      }

      private NaturalIdLoadAccessImpl(String entityName) {
         this((EntityPersister)SessionImpl.this.locateEntityPersister(entityName));
      }

      private NaturalIdLoadAccessImpl(Class entityClass) {
         this((String)entityClass.getName());
      }

      public NaturalIdLoadAccessImpl with(LockOptions lockOptions) {
         return (NaturalIdLoadAccessImpl)super.with(lockOptions);
      }

      public NaturalIdLoadAccess using(String attributeName, Object value) {
         this.naturalIdParameters.put(attributeName, value);
         return this;
      }

      public NaturalIdLoadAccessImpl setSynchronizationEnabled(boolean synchronizationEnabled) {
         super.synchronizationEnabled(synchronizationEnabled);
         return this;
      }

      public final Object getReference() {
         Serializable entityId = this.resolveNaturalId(this.naturalIdParameters);
         return entityId == null ? null : this.getIdentifierLoadAccess().getReference(entityId);
      }

      public final Object load() {
         Serializable entityId = this.resolveNaturalId(this.naturalIdParameters);
         if (entityId == null) {
            return null;
         } else {
            try {
               return this.getIdentifierLoadAccess().load(entityId);
            } catch (EntityNotFoundException var3) {
            } catch (ObjectNotFoundException var4) {
            }

            return null;
         }
      }
   }

   private class SimpleNaturalIdLoadAccessImpl extends BaseNaturalIdLoadAccessImpl implements SimpleNaturalIdLoadAccess {
      private final String naturalIdAttributeName;

      private SimpleNaturalIdLoadAccessImpl(EntityPersister entityPersister) {
         super(entityPersister, null);
         if (entityPersister.getNaturalIdentifierProperties().length != 1) {
            throw new HibernateException(String.format("Entity [%s] did not define a simple natural id", entityPersister.getEntityName()));
         } else {
            int naturalIdAttributePosition = entityPersister.getNaturalIdentifierProperties()[0];
            this.naturalIdAttributeName = entityPersister.getPropertyNames()[naturalIdAttributePosition];
         }
      }

      private SimpleNaturalIdLoadAccessImpl(String entityName) {
         this((EntityPersister)SessionImpl.this.locateEntityPersister(entityName));
      }

      private SimpleNaturalIdLoadAccessImpl(Class entityClass) {
         this((String)entityClass.getName());
      }

      public final SimpleNaturalIdLoadAccessImpl with(LockOptions lockOptions) {
         return (SimpleNaturalIdLoadAccessImpl)super.with(lockOptions);
      }

      private Map getNaturalIdParameters(Object naturalIdValue) {
         return Collections.singletonMap(this.naturalIdAttributeName, naturalIdValue);
      }

      public SimpleNaturalIdLoadAccessImpl setSynchronizationEnabled(boolean synchronizationEnabled) {
         super.synchronizationEnabled(synchronizationEnabled);
         return this;
      }

      public Object getReference(Object naturalIdValue) {
         Serializable entityId = this.resolveNaturalId(this.getNaturalIdParameters(naturalIdValue));
         return entityId == null ? null : this.getIdentifierLoadAccess().getReference(entityId);
      }

      public Object load(Object naturalIdValue) {
         Serializable entityId = this.resolveNaturalId(this.getNaturalIdParameters(naturalIdValue));
         if (entityId == null) {
            return null;
         } else {
            try {
               return this.getIdentifierLoadAccess().load(entityId);
            } catch (EntityNotFoundException var4) {
            } catch (ObjectNotFoundException var5) {
            }

            return null;
         }
      }
   }
}
