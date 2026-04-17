package org.hibernate.action.internal;

import java.io.Serializable;
import org.hibernate.action.spi.AfterTransactionCompletionProcess;
import org.hibernate.action.spi.BeforeTransactionCompletionProcess;
import org.hibernate.action.spi.Executable;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.CacheKey;
import org.hibernate.cache.spi.access.SoftLock;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.service.spi.EventListenerGroup;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.pretty.MessageHelper;

public abstract class CollectionAction implements Executable, Serializable, Comparable {
   private transient CollectionPersister persister;
   private transient SessionImplementor session;
   private final PersistentCollection collection;
   private final Serializable key;
   private final String collectionRole;
   private AfterTransactionCompletionProcess afterTransactionProcess;

   public CollectionAction(CollectionPersister persister, PersistentCollection collection, Serializable key, SessionImplementor session) {
      this.persister = persister;
      this.session = session;
      this.key = key;
      this.collectionRole = persister.getRole();
      this.collection = collection;
   }

   protected PersistentCollection getCollection() {
      return this.collection;
   }

   public void afterDeserialize(SessionImplementor session) {
      if (this.session == null && this.persister == null) {
         if (session != null) {
            this.session = session;
            this.persister = session.getFactory().getCollectionPersister(this.collectionRole);
         }

      } else {
         throw new IllegalStateException("already attached to a session.");
      }
   }

   public final void beforeExecutions() throws CacheException {
      if (this.persister.hasCache()) {
         CacheKey ck = this.session.generateCacheKey(this.key, this.persister.getKeyType(), this.persister.getRole());
         SoftLock lock = this.persister.getCacheAccessStrategy().lockItem(ck, (Object)null);
         this.afterTransactionProcess = new CacheCleanupProcess(this.key, this.persister, lock);
      }

   }

   public BeforeTransactionCompletionProcess getBeforeTransactionCompletionProcess() {
      return null;
   }

   public AfterTransactionCompletionProcess getAfterTransactionCompletionProcess() {
      return this.afterTransactionProcess;
   }

   public Serializable[] getPropertySpaces() {
      return this.persister.getCollectionSpaces();
   }

   protected final CollectionPersister getPersister() {
      return this.persister;
   }

   protected final Serializable getKey() {
      Serializable finalKey = this.key;
      if (this.key instanceof DelayedPostInsertIdentifier) {
         finalKey = this.session.getPersistenceContext().getEntry(this.collection.getOwner()).getId();
         if (finalKey == this.key) {
         }
      }

      return finalKey;
   }

   protected final SessionImplementor getSession() {
      return this.session;
   }

   protected final void evict() throws CacheException {
      if (this.persister.hasCache()) {
         CacheKey ck = this.session.generateCacheKey(this.key, this.persister.getKeyType(), this.persister.getRole());
         this.persister.getCacheAccessStrategy().remove(ck);
      }

   }

   public String toString() {
      return StringHelper.unqualify(this.getClass().getName()) + MessageHelper.infoString(this.collectionRole, this.key);
   }

   public int compareTo(Object other) {
      CollectionAction action = (CollectionAction)other;
      int roleComparison = this.collectionRole.compareTo(action.collectionRole);
      return roleComparison != 0 ? roleComparison : this.persister.getKeyType().compare(this.key, action.key);
   }

   protected EventListenerGroup listenerGroup(EventType eventType) {
      return ((EventListenerRegistry)this.getSession().getFactory().getServiceRegistry().getService(EventListenerRegistry.class)).getEventListenerGroup(eventType);
   }

   protected EventSource eventSource() {
      return (EventSource)this.getSession();
   }

   private static class CacheCleanupProcess implements AfterTransactionCompletionProcess {
      private final Serializable key;
      private final CollectionPersister persister;
      private final SoftLock lock;

      private CacheCleanupProcess(Serializable key, CollectionPersister persister, SoftLock lock) {
         this.key = key;
         this.persister = persister;
         this.lock = lock;
      }

      public void doAfterTransactionCompletion(boolean success, SessionImplementor session) {
         CacheKey ck = session.generateCacheKey(this.key, this.persister.getKeyType(), this.persister.getRole());
         this.persister.getCacheAccessStrategy().unlockItem(ck, this.lock);
      }
   }
}
